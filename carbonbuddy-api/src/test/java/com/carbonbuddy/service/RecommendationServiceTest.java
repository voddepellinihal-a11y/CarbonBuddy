package com.carbonbuddy.service;

import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    @Mock
    private RewardRepository rewardRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                recommendationRepository, carbonRecordRepository, rewardRepository);
    }

    @Test
    void should_returnRecommendations_when_userHasAny() {
        Recommendation rec = new Recommendation();
        rec.setId(1L);
        rec.setUserId(1L);
        rec.setTitle("Switch to Metro");
        rec.setStatus("PENDING");

        when(recommendationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(rec));

        List<Recommendation> result = recommendationService.getUserRecommendations(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Switch to Metro", result.get(0).getTitle());
    }

    @Test
    void should_returnEmptyList_when_userHasNoRecommendations() {
        when(recommendationRepository.findByUserIdOrderByCreatedAtDesc(99L))
                .thenReturn(Collections.emptyList());

        List<Recommendation> result = recommendationService.getUserRecommendations(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void should_generateRecommendations_when_transportDataExists() {
        Object[][] data = {{"TRANSPORT", 150.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(any(Recommendation.class));
    }

    @Test
    void should_generateRecommendations_when_foodDataIsTop() {
        Object[][] data = {{"FOOD", 80.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "Try a plant-based meal today".equals(r.getTitle())));
    }

    @Test
    void should_generateRecommendations_when_utilityDataIsTop() {
        Object[][] data = {{"UTILITY", 120.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "Reduce AC usage by 1 hour daily".equals(r.getTitle())));
    }

    @Test
    void should_defaultToTransport_when_noDataExists() {
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "TRANSPORT".equals(r.getCategory())));
    }

    @Test
    void should_useMetroRecommendation_when_transportCarbonExceeds100() {
        Object[][] data = {{"TRANSPORT", 150.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r -> {
            if ("TRANSPORT".equals(r.getCategory()) && r.getEstimatedSavingsKg() > 50) {
                return "Switch to Metro for your daily commute".equals(r.getTitle());
            }
            return false;
        }));
    }

    @Test
    void should_useCarpoolRecommendation_when_transportCarbonBelow100() {
        Object[][] data = {{"TRANSPORT", 50.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "Try carpooling this week".equals(r.getTitle())));
    }

    @Test
    void should_completeRecommendation_when_ownedByUser() {
        Recommendation rec = new Recommendation();
        rec.setId(1L);
        rec.setUserId(1L);
        rec.setStatus("PENDING");
        rec.setEstimatedSavingsKg(30.0);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(recommendationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(rewardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Recommendation result = recommendationService.completeRecommendation(1L, 1L);

        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void should_throwException_when_recommendationNotFound() {
        when(recommendationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> recommendationService.completeRecommendation(1L, 99L));
    }

    @Test
    void should_throwException_when_recommendationNotOwnedByUser() {
        Recommendation rec = new Recommendation();
        rec.setId(1L);
        rec.setUserId(2L);
        rec.setStatus("PENDING");

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(rec));

        assertThrows(IllegalArgumentException.class,
                () -> recommendationService.completeRecommendation(1L, 1L));
    }

    @Test
    void should_setCorrectSavings_when_completingTransportRecommendation() {
        Recommendation rec = new Recommendation();
        rec.setId(1L);
        rec.setUserId(1L);
        rec.setStatus("PENDING");
        rec.setCategory("TRANSPORT");
        rec.setEstimatedSavingsKg(20.0);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(recommendationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(rewardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        recommendationService.completeRecommendation(1L, 1L);

        verify(rewardRepository).save(argThat(reward ->
                reward.getCreditsEarned() == 300 &&
                "RECOMMENDATION".equals(reward.getSource()) &&
                "CREDIT".equals(reward.getTransactionType())));
    }

    @Test
    void should_saveWithCorrectStatus_when_generatingRecommendations() {
        Object[][] data = {{"TRANSPORT", 60.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "PENDING".equals(r.getStatus())));
    }

    @Test
    void should_handleDefaultCategory_when_unknownCategoryIsTop() {
        Object[][] data = {{"OTHER", 200.0}};
        List<Object[]> categoryData = List.of(data);
        when(carbonRecordRepository.sumCarbonByCategory(eq(1L), any(), any()))
                .thenReturn(categoryData);
        when(recommendationRepository.save(any())).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        recommendationService.generateRecommendations(1L);

        verify(recommendationRepository).save(argThat(r ->
                "Walk or bike for short trips".equals(r.getTitle())));
    }
}
