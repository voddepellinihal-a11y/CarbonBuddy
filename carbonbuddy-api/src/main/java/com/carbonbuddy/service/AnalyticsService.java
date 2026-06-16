package com.carbonbuddy.service;

import com.carbonbuddy.dto.response.DashboardResponse;
import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private static final int DEFAULT_PERCENTILE = 1;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;
    private static final int STREAK_DANGER_HOURS = 24;
    private static final String DEFAULT_TOP_CATEGORY = "TRANSPORT";
    private static final String CATEGORY_TRANSPORT = "TRANSPORT";
    private static final String STATUS_PENDING = "PENDING";

    private final CarbonRecordRepository carbonRecordRepository;
    private final RecommendationRepository recommendationRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    public AnalyticsService(CarbonRecordRepository carbonRecordRepository,
                            RecommendationRepository recommendationRepository,
                            RewardRepository rewardRepository,
                            UserRepository userRepository) {
        this.carbonRecordRepository = carbonRecordRepository;
        this.recommendationRepository = recommendationRepository;
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "dashboard", key = "#userId")
    public DashboardResponse getDashboard(Long userId) {
        log.debug("Building dashboard for user {}", userId);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        DashboardResponse response = new DashboardResponse();

        DashboardResponse.PeriodSummary daily = new DashboardResponse.PeriodSummary();
        daily.setTotalCarbonKg(carbonRecordRepository.sumCarbonByUserIdAndPeriod(userId, today, today));
        daily.setTotalDistanceKm(0);

        DashboardResponse.PeriodSummary weekly = new DashboardResponse.PeriodSummary();
        weekly.setTotalCarbonKg(carbonRecordRepository.sumCarbonByUserIdAndPeriod(userId, weekStart, today));
        weekly.setTotalDistanceKm(0);

        DashboardResponse.PeriodSummary monthly = new DashboardResponse.PeriodSummary();
        monthly.setTotalCarbonKg(carbonRecordRepository.sumCarbonByUserIdAndPeriod(userId, monthStart, today));
        monthly.setTotalDistanceKm(0);

        double prevWeekCarbon = carbonRecordRepository.sumCarbonByUserIdAndPeriod(
                userId, weekStart.minusDays(7), weekStart.minusDays(1));
        weekly.setChangePercent(prevWeekCarbon > 0
                ? ((weekly.getTotalCarbonKg() - prevWeekCarbon) / prevWeekCarbon) * PERCENTAGE_MULTIPLIER : 0);

        response.setDaily(daily);
        response.setWeekly(weekly);
        response.setMonthly(monthly);

        List<Object[]> categoryData = carbonRecordRepository.sumCarbonByCategory(userId, monthStart, today);
        double totalCarbon = categoryData.stream()
                .mapToDouble(row -> ((Number) row[1]).doubleValue()).sum();

        List<DashboardResponse.CategoryBreakdown> breakdown = categoryData.stream().map(row -> {
            DashboardResponse.CategoryBreakdown cb = new DashboardResponse.CategoryBreakdown();
            cb.setCategory((String) row[0]);
            cb.setCarbonKg(((Number) row[1]).doubleValue());
            cb.setPercentage(totalCarbon > 0 ? (cb.getCarbonKg() / totalCarbon) * PERCENTAGE_MULTIPLIER : 0);
            return cb;
        }).collect(Collectors.toList());
        response.setBreakdown(breakdown);

        double avgCarbon = carbonRecordRepository.avgCarbonByDate(today);
        List<DashboardResponse.BenchmarkComparison> benchmarks = new ArrayList<>();
        DashboardResponse.BenchmarkComparison bench = new DashboardResponse.BenchmarkComparison();
        bench.setLabel("Daily vs National Average");
        bench.setUserValue(daily.getTotalCarbonKg());
        bench.setAverageValue(avgCarbon);
        benchmarks.add(bench);
        response.setBenchmarks(benchmarks);

        response.setTotalCredits(rewardRepository.getBalanceByUserId(userId));

        List<Recommendation> recs = recommendationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, STATUS_PENDING);
        List<DashboardResponse.RecommendationItem> items = recs.stream().map(r -> {
            DashboardResponse.RecommendationItem item = new DashboardResponse.RecommendationItem();
            item.setId(r.getId());
            item.setTitle(r.getTitle());
            item.setDescription(r.getDescription());
            item.setEstimatedSavingsKg(r.getEstimatedSavingsKg());
            item.setCategory(r.getCategory());
            item.setStatus(r.getStatus());
            item.setCompletionCount(rewardRepository.countUsersCompletedRecommendation(r.getId()));
            return item;
        }).collect(Collectors.toList());
        response.setRecommendations(items);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            enrichStreakInfo(response, user);
            enrichLevelInfo(response, user, totalUsers());
            enrichFomoStats(response, user, breakdown);
        }

        return response;
    }

    private void enrichStreakInfo(DashboardResponse response, User user) {
        DashboardResponse.StreakInfo streakInfo = new DashboardResponse.StreakInfo();
        streakInfo.setCurrentStreak(user.getCurrentStreak());
        streakInfo.setLongestStreak(user.getLongestStreak());
        streakInfo.setPointsMultiplier(RewardService.getStreakMultiplier(user.getCurrentStreak()));
        streakInfo.setLabel(RewardService.getStreakLabel(user.getCurrentStreak()));
        response.setStreak(streakInfo);
    }

    private void enrichLevelInfo(DashboardResponse response, User user, int totalUsers) {
        DashboardResponse.LevelInfo levelInfo = new DashboardResponse.LevelInfo();
        int level = RewardService.getLevelForPoints(user.getTotalPoints());
        levelInfo.setLevel(level);
        levelInfo.setTitle(RewardService.getLevelTitle(level));
        levelInfo.setIcon(RewardService.getLevelIcon(level));
        levelInfo.setPoints(user.getTotalPoints());
        levelInfo.setPointsToNext(RewardService.getPointsToNextLevel(user.getTotalPoints()));

        int usersAhead = userRepository.countUsersWithMorePoints(user.getTotalPoints());
        int percentile = Math.max(DEFAULT_PERCENTILE,
                (int) Math.ceil(((double) (totalUsers - usersAhead) / totalUsers) * PERCENTAGE_MULTIPLIER));
        levelInfo.setPercentile(percentile);
        response.setLevel(levelInfo);
    }

    private void enrichFomoStats(DashboardResponse response, User user,
                                 List<DashboardResponse.CategoryBreakdown> breakdown) {
        DashboardResponse.FomoStats fomo = new DashboardResponse.FomoStats();
        int totalUsers = totalUsers();
        fomo.setTotalUsers(totalUsers);
        int activeToday = rewardRepository.countActiveUsersSince(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        fomo.setUsersActiveToday(activeToday);
        String topCat = breakdown.stream()
                .max(Comparator.comparingDouble(DashboardResponse.CategoryBreakdown::getCarbonKg))
                .map(DashboardResponse.CategoryBreakdown::getCategory)
                .orElse(DEFAULT_TOP_CATEGORY);
        fomo.setTopCategory(topCat);

        if (user.getCurrentStreak() > 0 && user.getLastActivityDate() != null) {
            long hoursSince = java.time.Duration.between(
                    user.getLastActivityDate().atStartOfDay(), LocalDateTime.now()).toHours();
            if (hoursSince >= STREAK_DANGER_HOURS) {
                fomo.setStreakDanger("Your streak will reset! Log an activity now.");
            }
        }
        response.setFomo(fomo);
    }

    private int totalUsers() {
        return Math.max(userRepository.countTotalUsers(), 1);
    }
}
