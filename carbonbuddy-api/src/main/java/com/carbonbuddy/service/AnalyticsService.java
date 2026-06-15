package com.carbonbuddy.service;

import com.carbonbuddy.dto.response.DashboardResponse;
import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

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

    public DashboardResponse getDashboard(Long userId) {
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
                ? ((weekly.getTotalCarbonKg() - prevWeekCarbon) / prevWeekCarbon) * 100 : 0);

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
            cb.setPercentage(totalCarbon > 0 ? (cb.getCarbonKg() / totalCarbon) * 100 : 0);
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
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, "PENDING");
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
            DashboardResponse.StreakInfo streakInfo = new DashboardResponse.StreakInfo();
            streakInfo.setCurrentStreak(user.getCurrentStreak());
            streakInfo.setLongestStreak(user.getLongestStreak());
            streakInfo.setPointsMultiplier(RewardService.getStreakMultiplier(user.getCurrentStreak()));
            streakInfo.setLabel(RewardService.getStreakLabel(user.getCurrentStreak()));
            response.setStreak(streakInfo);

            DashboardResponse.LevelInfo levelInfo = new DashboardResponse.LevelInfo();
            int level = RewardService.getLevelForPoints(user.getTotalPoints());
            levelInfo.setLevel(level);
            levelInfo.setTitle(RewardService.getLevelTitle(level));
            levelInfo.setIcon(RewardService.getLevelIcon(level));
            levelInfo.setPoints(user.getTotalPoints());
            levelInfo.setPointsToNext(RewardService.getPointsToNextLevel(user.getTotalPoints()));

            int usersAhead = userRepository.countUsersWithMorePoints(user.getTotalPoints());
            int totalUsers = Math.max(userRepository.countTotalUsers(), 1);
            int percentile = Math.max(1, (int) Math.ceil(((double) (totalUsers - usersAhead) / totalUsers) * 100));
            levelInfo.setPercentile(percentile);
            response.setLevel(levelInfo);

            DashboardResponse.FomoStats fomo = new DashboardResponse.FomoStats();
            fomo.setTotalUsers(totalUsers);
            int activeToday = rewardRepository.countActiveUsersSince(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
            fomo.setUsersActiveToday(activeToday);
            String topCat = breakdown.stream()
                    .max(Comparator.comparingDouble(DashboardResponse.CategoryBreakdown::getCarbonKg))
                    .map(DashboardResponse.CategoryBreakdown::getCategory)
                    .orElse("TRANSPORT");
            fomo.setTopCategory(topCat);

            if (user.getCurrentStreak() > 0 && user.getLastActivityDate() != null) {
                long hoursSince = java.time.Duration.between(
                        user.getLastActivityDate().atStartOfDay(), LocalDateTime.now()).toHours();
                if (hoursSince >= 24) {
                    fomo.setStreakDanger("⚠️ Your streak will reset! Log an activity now.");
                }
            }
            response.setFomo(fomo);
        }

        return response;
    }
}
