package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for creating and processing user activities.
 * Computes carbon emissions and awards streak points upon activity creation.
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private static final String SOURCE_TYPE_PREFIX = "ACTIVITY_";

    private final ActivityRepository activityRepository;
    private final CarbonComputationEngine carbonEngine;
    private final RewardService rewardService;

    /**
     * Constructs ActivityService with required dependencies.
     *
     * @param activityRepository the activity persistence repository
     * @param carbonEngine       the engine for computing carbon emissions
     * @param rewardService      the service for managing user rewards and streaks
     */
    public ActivityService(ActivityRepository activityRepository,
                           CarbonComputationEngine carbonEngine,
                           RewardService rewardService) {
        this.activityRepository = activityRepository;
        this.carbonEngine = carbonEngine;
        this.rewardService = rewardService;
    }

    /**
     * Creates a new activity, computes its carbon footprint, and updates user rewards.
     *
     * @param userId  the ID of the user performing the activity
     * @param request the activity request containing transit details
     * @return the persisted {@link Activity} entity
     */
    @Transactional
    public Activity createActivity(Long userId, ActivityRequest request) {
        log.debug("Creating activity for user {} with mode {}", userId, request.getTransitMode());

        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setTransitMode(sanitizeInput(request.getTransitMode()));
        activity.setDistanceKm(request.getDistanceKm());
        activity.setDurationMinutes(request.getDurationMinutes());
        activity.setRoutePolyline(request.getRoutePolyline());
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

    /**
     * Sanitizes a string input by trimming and limiting length.
     *
     * @param input the raw string
     * @return the sanitized string, or null if input is null
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().substring(0, Math.min(input.trim().length(), 255));
    }
}
