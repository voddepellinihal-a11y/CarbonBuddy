package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.ActivityRepository;
import com.carbonbuddy.security.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private static final String SOURCE_TYPE_PREFIX = "ACTIVITY_";

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
    @Caching(evict = {
        @org.springframework.cache.annotation.CacheEvict(value = "dashboard", key = "#userId"),
        @org.springframework.cache.annotation.CacheEvict(value = "leaderboard", allEntries = true)
    })
    public Activity createActivity(Long userId, ActivityRequest request) {
        log.debug("Creating activity for user {} with mode {}", userId, request.getTransitMode());

        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setTransitMode(InputSanitizer.sanitize(request.getTransitMode()));
        activity.setDistanceKm(request.getDistanceKm());
        activity.setDurationMinutes(request.getDurationMinutes());
        activity.setRoutePolyline(InputSanitizer.sanitizeWithLength(request.getRoutePolyline(), 1000));
        activity.setActivityStart(request.getActivityStart());
        activity.setActivityEnd(request.getActivityEnd());
        activity.setIsManual(request.isManual());

        activity = activityRepository.save(activity);

        LocalDate date = request.getActivityStart().toLocalDate();
        CarbonRecord record = carbonEngine.computeTransportCarbon(
                userId, request.getTransitMode(), request.getDistanceKm(), date);
        record.setSourceId(activity.getId());
        record.setSourceType(SOURCE_TYPE_PREFIX + request.getTransitMode());

        rewardService.updateStreak(userId);
        rewardService.awardPoints(userId, "ACTIVITY", activity.getId(), record.getCarbonKg());

        return activity;
    }
}
