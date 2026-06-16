package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CarbonComputationEngine carbonEngine;

    @Mock
    private RewardService rewardService;

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService(activityRepository, carbonEngine, rewardService);
    }

    @Test
    void should_createActivity_when_validRequest() {
        ActivityRequest request = buildRequest("BUS", 10.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(0.89));

        Activity result = activityService.createActivity(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("BUS", result.getTransitMode());
        assertEquals(10.0, result.getDistanceKm());
    }

    @Test
    void should_computeCorrectCarbon_when_activityCreated() {
        ActivityRequest request = buildRequest("METRO", 20.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        CarbonRecord record = buildCarbonRecord(0.70);
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(record);

        activityService.createActivity(1L, request);

        verify(carbonEngine).computeTransportCarbon(eq(1L), eq("METRO"), eq(20.0), any());
    }

    @Test
    void should_updateStreak_when_activityCreated() {
        ActivityRequest request = buildRequest("BUS", 5.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(0.445));

        activityService.createActivity(1L, request);

        verify(rewardService).updateStreak(1L);
    }

    @Test
    void should_awardPoints_when_activityCreated() {
        ActivityRequest request = buildRequest("BUS", 10.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        CarbonRecord record = buildCarbonRecord(0.89);
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(record);

        activityService.createActivity(1L, request);

        verify(rewardService).awardPoints(eq(1L), eq("ACTIVITY"), eq(1L), eq(0.89));
    }

    @Test
    void should_setSourceId_when_activityCreated() {
        ActivityRequest request = buildRequest("CAR_PETROL", 15.0);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        when(activityRepository.save(captor.capture())).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(42L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(2.88));

        Activity result = activityService.createActivity(1L, request);

        assertNotNull(result);
        assertEquals(42L, result.getId());
    }

    @Test
    void should_setSourceType_when_activityCreated() {
        ActivityRequest request = buildRequest("WALK", 2.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        CarbonRecord record = new CarbonRecord();
        record.setCarbonKg(0.0);
        record.setSourceType("WALK");
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(record);

        Activity result = activityService.createActivity(1L, request);

        assertNotNull(result);
        assertEquals("WALK", result.getTransitMode());
    }

    @Test
    void should_setUserId_when_activityCreated() {
        ActivityRequest request = buildRequest("BUS", 10.0);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        when(activityRepository.save(captor.capture())).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(0.89));

        activityService.createActivity(42L, request);

        assertEquals(42L, captor.getValue().getUserId());
    }

    @Test
    void should_handleZeroCarbon_when_zeroDistance() {
        ActivityRequest request = buildRequest("BIKE", 0.0);

        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(0.0));

        Activity result = activityService.createActivity(1L, request);

        assertNotNull(result);
        verify(rewardService).awardPoints(eq(1L), eq("ACTIVITY"), eq(1L), eq(0.0));
    }

    @Test
    void should_setActivityStart_when_activityCreated() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        ActivityRequest request = buildRequest("BUS", 10.0);
        request.setActivityStart(start);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        when(activityRepository.save(captor.capture())).thenAnswer(i -> {
            Activity a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(carbonEngine.computeTransportCarbon(anyLong(), anyString(), anyDouble(), any()))
                .thenReturn(buildCarbonRecord(0.89));

        activityService.createActivity(1L, request);

        assertEquals(start, captor.getValue().getActivityStart());
    }

    private ActivityRequest buildRequest(String transitMode, double distanceKm) {
        ActivityRequest request = new ActivityRequest();
        request.setTransitMode(transitMode);
        request.setDistanceKm(distanceKm);
        request.setDurationMinutes(30.0);
        request.setActivityStart(LocalDateTime.now());
        request.setManual(true);
        return request;
    }

    private CarbonRecord buildCarbonRecord(double carbonKg) {
        CarbonRecord record = new CarbonRecord();
        record.setCarbonKg(carbonKg);
        record.setSourceType("BUS");
        record.setCategory("TRANSPORT");
        return record;
    }
}
