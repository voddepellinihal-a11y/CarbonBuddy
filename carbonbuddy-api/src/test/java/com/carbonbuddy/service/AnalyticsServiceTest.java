package com.carbonbuddy.service;

import com.carbonbuddy.dto.response.DashboardResponse;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private UserRepository userRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                carbonRecordRepository, recommendationRepository, rewardRepository, userRepository);
    }

    @Test
    void should_returnZeros_when_dashboardWithNoData() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getDaily().getTotalCarbonKg());
        assertEquals(0.0, response.getWeekly().getTotalCarbonKg());
        assertEquals(0.0, response.getMonthly().getTotalCarbonKg());
        assertEquals(0, response.getTotalCredits());
    }

    @Test
    void should_returnCorrectTotals_when_dashboardWithData() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any()))
                .thenReturn(10.0, 50.0, 200.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(5.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(250);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertEquals(10.0, response.getDaily().getTotalCarbonKg());
        assertEquals(50.0, response.getWeekly().getTotalCarbonKg());
        assertEquals(200.0, response.getMonthly().getTotalCarbonKg());
        assertEquals(250, response.getTotalCredits());
    }

    @Test
    void should_returnCategoryBreakdown_when_categoryDataExists() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);

        List<Object[]> categoryData = new ArrayList<>();
        categoryData.add(new Object[]{"TRANSPORT", 80.0});
        categoryData.add(new Object[]{"FOOD", 20.0});

        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any())).thenReturn(categoryData);
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getBreakdown());
        assertEquals(2, response.getBreakdown().size());
    }

    @Test
    void should_returnCorrectPercentages_when_multipleCategoriesExist() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);

        List<Object[]> categoryData = new ArrayList<>();
        categoryData.add(new Object[]{"TRANSPORT", 75.0});
        categoryData.add(new Object[]{"FOOD", 25.0});

        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any())).thenReturn(categoryData);
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        DashboardResponse.CategoryBreakdown transport = response.getBreakdown().get(0);
        assertEquals(75.0, transport.getPercentage(), 0.01);
        DashboardResponse.CategoryBreakdown food = response.getBreakdown().get(1);
        assertEquals(25.0, food.getPercentage(), 0.01);
    }

    @Test
    void should_returnZeroPercent_when_totalCarbonIsZero() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);

        List<Object[]> categoryData = new ArrayList<>();
        categoryData.add(new Object[]{"TRANSPORT", 0.0});

        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any())).thenReturn(categoryData);
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertEquals(0.0, response.getBreakdown().get(0).getPercentage());
    }

    @Test
    void should_calculateChangePercent_when_previousWeekHasData() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any()))
                .thenReturn(10.0, 50.0, 200.0, 25.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertTrue(response.getWeekly().getChangePercent() != 0);
    }

    @Test
    void should_returnZeroChangePercent_when_previousWeekIsZero() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any()))
                .thenReturn(0.0, 50.0, 200.0, 0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertEquals(0.0, response.getWeekly().getChangePercent());
    }

    @Test
    void should_returnBenchmarks_when_dashboardBuilt() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(5.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getBenchmarks());
        assertFalse(response.getBenchmarks().isEmpty());
        assertEquals("Daily vs National Average", response.getBenchmarks().get(0).getLabel());
    }

    @Test
    void should_enrichStreakInfo_when_userExists() {
        User user = new User();
        user.setId(1L);
        user.setCurrentStreak(5);
        user.setLongestStreak(10);
        user.setTotalPoints(500);
        user.setLevel(3);

        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countUsersWithMorePoints(anyLong())).thenReturn(0);
        when(userRepository.countTotalUsers()).thenReturn(10);
        when(rewardRepository.countActiveUsersSince(any())).thenReturn(5);

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getStreak());
        assertEquals(5, response.getStreak().getCurrentStreak());
        assertEquals(10, response.getStreak().getLongestStreak());
    }

    @Test
    void should_enrichLevelInfo_when_userExists() {
        User user = new User();
        user.setId(1L);
        user.setTotalPoints(500);
        user.setLevel(3);

        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countUsersWithMorePoints(anyLong())).thenReturn(2);
        when(userRepository.countTotalUsers()).thenReturn(10);
        when(rewardRepository.countActiveUsersSince(any())).thenReturn(3);

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getLevel());
        assertEquals(3, response.getLevel().getLevel());
        assertEquals(500, response.getLevel().getPoints());
    }

    @Test
    void should_returnRecommendations_when_userHasPending() {
        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);

        com.carbonbuddy.model.Recommendation rec = new com.carbonbuddy.model.Recommendation();
        rec.setId(1L);
        rec.setTitle("Switch to Metro");
        rec.setDescription("Reduce emissions");
        rec.setCategory("TRANSPORT");
        rec.setStatus("PENDING");
        rec.setEstimatedSavingsKg(15.0);

        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(List.of(rec));
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(rewardRepository.countUsersCompletedRecommendation(anyLong())).thenReturn(3);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getRecommendations());
        assertEquals(1, response.getRecommendations().size());
        assertEquals("Switch to Metro", response.getRecommendations().get(0).getTitle());
    }

    @Test
    void should_handleMultipleUsers_when_calculatingPercentile() {
        User user = new User();
        user.setId(1L);
        user.setTotalPoints(1000);
        user.setLevel(4);

        when(carbonRecordRepository.sumCarbonByUserIdAndPeriod(anyLong(), any(), any())).thenReturn(0.0);
        when(carbonRecordRepository.sumCarbonByCategory(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(carbonRecordRepository.avgCarbonByDate(any())).thenReturn(0.0);
        when(recommendationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());
        when(rewardRepository.getBalanceByUserId(anyLong())).thenReturn(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countUsersWithMorePoints(1000)).thenReturn(5);
        when(userRepository.countTotalUsers()).thenReturn(100);
        when(rewardRepository.countActiveUsersSince(any())).thenReturn(10);

        DashboardResponse response = analyticsService.getDashboard(1L);

        assertNotNull(response.getLevel());
        assertTrue(response.getLevel().getPercentile() > 0);
    }
}
