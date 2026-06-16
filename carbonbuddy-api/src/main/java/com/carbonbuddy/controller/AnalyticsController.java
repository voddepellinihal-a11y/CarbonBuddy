package com.carbonbuddy.controller;

import com.carbonbuddy.dto.response.DashboardResponse;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analytics and dashboard data.
 * Provides aggregated carbon emission summaries and user statistics.
 */
@RestController
@RequestMapping("/api/analytics")
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Constructs AnalyticsController with the analytics service.
     *
     * @param analyticsService the service handling analytics logic
     */
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns the dashboard data for the authenticated user.
     *
     * @param auth the authentication principal
     * @return 200 OK with the dashboard response
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        DashboardResponse dashboard = analyticsService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
