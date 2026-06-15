package com.carbonbuddy.controller;

import com.carbonbuddy.dto.response.DashboardResponse;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        DashboardResponse dashboard = analyticsService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
