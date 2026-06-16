package com.carbonbuddy.dto.response;

import java.util.List;

/**
 * Response DTO for the user dashboard.
 * Aggregates daily/weekly/monthly summaries, category breakdown,
 * benchmarks, streak info, level info, recommendations, and engagement stats.
 */
public class DashboardResponse {

    private PeriodSummary daily;
    private PeriodSummary weekly;
    private PeriodSummary monthly;
    private List<CategoryBreakdown> breakdown;
    private List<BenchmarkComparison> benchmarks;
    private int totalCredits;
    private List<RecommendationItem> recommendations;
    private StreakInfo streak;
    private LevelInfo level;
    private FomoStats fomo;

    public PeriodSummary getDaily() { return daily; }
    public void setDaily(PeriodSummary daily) { this.daily = daily; }
    public PeriodSummary getWeekly() { return weekly; }
    public void setWeekly(PeriodSummary weekly) { this.weekly = weekly; }
    public PeriodSummary getMonthly() { return monthly; }
    public void setMonthly(PeriodSummary monthly) { this.monthly = monthly; }
    public List<CategoryBreakdown> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CategoryBreakdown> breakdown) { this.breakdown = breakdown; }
    public List<BenchmarkComparison> getBenchmarks() { return benchmarks; }
    public void setBenchmarks(List<BenchmarkComparison> benchmarks) { this.benchmarks = benchmarks; }
    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }
    public List<RecommendationItem> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationItem> recommendations) { this.recommendations = recommendations; }
    public StreakInfo getStreak() { return streak; }
    public void setStreak(StreakInfo streak) { this.streak = streak; }
    public LevelInfo getLevel() { return level; }
    public void setLevel(LevelInfo level) { this.level = level; }
    public FomoStats getFomo() { return fomo; }
    public void setFomo(FomoStats fomo) { this.fomo = fomo; }

    /**
     * Summary for a time period (daily, weekly, monthly).
     */
    public static class PeriodSummary {
        private double totalCarbonKg;
        private double totalDistanceKm;
        private double changePercent;
        public double getTotalCarbonKg() { return totalCarbonKg; }
        public void setTotalCarbonKg(double totalCarbonKg) { this.totalCarbonKg = totalCarbonKg; }
        public double getTotalDistanceKm() { return totalDistanceKm; }
        public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
        public double getChangePercent() { return changePercent; }
        public void setChangePercent(double changePercent) { this.changePercent = changePercent; }
    }

    /**
     * Breakdown of emissions by category.
     */
    public static class CategoryBreakdown {
        private String category;
        private double carbonKg;
        private double percentage;
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getCarbonKg() { return carbonKg; }
        public void setCarbonKg(double carbonKg) { this.carbonKg = carbonKg; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    /**
     * Benchmark comparison data.
     */
    public static class BenchmarkComparison {
        private String label;
        private double userValue;
        private double averageValue;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getUserValue() { return userValue; }
        public void setUserValue(double userValue) { this.userValue = userValue; }
        public double getAverageValue() { return averageValue; }
        public void setAverageValue(double averageValue) { this.averageValue = averageValue; }
    }

    /**
     * Recommendation item for the dashboard.
     */
    public static class RecommendationItem {
        private Long id;
        private String title;
        private String description;
        private double estimatedSavingsKg;
        private String category;
        private String status;
        private int completionCount;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getEstimatedSavingsKg() { return estimatedSavingsKg; }
        public void setEstimatedSavingsKg(double estimatedSavingsKg) { this.estimatedSavingsKg = estimatedSavingsKg; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCompletionCount() { return completionCount; }
        public void setCompletionCount(int completionCount) { this.completionCount = completionCount; }
    }

    /**
     * User streak information.
     */
    public static class StreakInfo {
        private int currentStreak;
        private int longestStreak;
        private double pointsMultiplier;
        private String label;
        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
        public double getPointsMultiplier() { return pointsMultiplier; }
        public void setPointsMultiplier(double pointsMultiplier) { this.pointsMultiplier = pointsMultiplier; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    /**
     * User level information.
     */
    public static class LevelInfo {
        private int level;
        private String title;
        private String icon;
        private long points;
        private long pointsToNext;
        private int percentile;
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public long getPoints() { return points; }
        public void setPoints(long points) { this.points = points; }
        public long getPointsToNext() { return pointsToNext; }
        public void setPointsToNext(long pointsToNext) { this.pointsToNext = pointsToNext; }
        public int getPercentile() { return percentile; }
        public void setPercentile(int percentile) { this.percentile = percentile; }
    }

    /**
     * Engagement and FOMO statistics.
     */
    public static class FomoStats {
        private int totalUsers;
        private int usersActiveToday;
        private String topCategory;
        private String streakDanger;
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        public int getUsersActiveToday() { return usersActiveToday; }
        public void setUsersActiveToday(int usersActiveToday) { this.usersActiveToday = usersActiveToday; }
        public String getTopCategory() { return topCategory; }
        public void setTopCategory(String topCategory) { this.topCategory = topCategory; }
        public String getStreakDanger() { return streakDanger; }
        public void setStreakDanger(String streakDanger) { this.streakDanger = streakDanger; }
    }
}
