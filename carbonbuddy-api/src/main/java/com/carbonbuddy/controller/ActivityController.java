package com.carbonbuddy.controller;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user activities.
 * Provides endpoint to log transport activities with carbon tracking.
 */
@RestController
@RequestMapping("/api/activities")
@Validated
public class ActivityController {

    private final ActivityService activityService;

    /**
     * Constructs ActivityController with the activity service.
     *
     * @param activityService the service handling activity logic
     */
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Creates a new activity for the authenticated user.
     *
     * @param auth    the authentication principal
     * @param request the validated activity request
     * @return 201 Created with the persisted activity
     */
    @PostMapping
    public ResponseEntity<Activity> createActivity(
            Authentication auth,
            @Valid @RequestBody ActivityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Activity activity = activityService.createActivity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }
}
