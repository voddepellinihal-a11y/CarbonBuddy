package com.carbonbuddy.controller;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(
            Authentication auth,
            @Valid @RequestBody ActivityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Activity activity = activityService.createActivity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }
}
