package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.Activity;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@Validated
@Tag(name = "Activities", description = "Transport activity tracking")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Operation(summary = "Create activity", description = "Log a transport activity with carbon tracking")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Activity created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Activity>> createActivity(
            Authentication auth,
            @Valid @RequestBody ActivityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Activity activity = activityService.createActivity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Activity created", activity));
    }
}
