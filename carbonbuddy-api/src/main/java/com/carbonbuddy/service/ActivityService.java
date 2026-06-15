package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CarbonComputationEngine carbonEngine;
    private final RewardService rewardService;

    public ActivityService(ActivityRepository activityRepository,
                           CarbonComputationEngine carbonEngine,
                           RewardService rewardService) {
        this.activityRepository = activityRepository;
        this.carbonEngine = carbonEngine;
        this.rewardService = rewardService;
    }

    @Transactional
    public Activity createActivity(Long userId, ActivityRequest request) {
        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setTransitMode(request.getTransitMode());
        activity.setDistanceKm(request.getDistanceKm());
        activity.setDurationMinutes(request.getDurationMinutes());
        activity.setRoutePolyline(request.getRoutePolyline());
        activity.setActivityStart(request.getActivityStart());
        activity.setActivityEnd(request.getActivityEnd());
        activity.setIsManual(request.getIsManual());

        activity = activityRepository.save(activity);

        LocalDate date = request.getActivityStart().toLocalDate();
        CarbonRecord record = carbonEngine.computeTransportCarbon(
                userId, request.getTransitMode(), request.getDistanceKm(), date);
        record.setSourceId(activity.getId());
        record.setSourceType("ACTIVITY_" + request.getTransitMode());

        rewardService.updateStreak(userId);
        rewardService.awardPoints(userId, "ACTIVITY", activity.getId(), record.getCarbonKg());

        return activity;
    }
}
