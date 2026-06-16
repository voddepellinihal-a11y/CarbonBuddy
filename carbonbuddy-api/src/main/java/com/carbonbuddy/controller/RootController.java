package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    private static final String API_VERSION = "1.0.0";
    private static final String SERVICE_NAME = "CarbonBuddy API";
    private static final String STATUS_RUNNING = "running";

    @GetMapping("/api")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> data = Map.of(
            "service", SERVICE_NAME,
            "version", API_VERSION,
            "status", STATUS_RUNNING,
            "endpoints", Map.of(
                "auth", Map.of("register", "POST /api/v1/auth/register", "login", "POST /api/v1/auth/login"),
                "activities", "POST /api/v1/activities",
                "utility-bills", "POST /api/v1/utility-bills",
                "analytics", "GET /api/v1/analytics/dashboard",
                "recommendations", Map.of(
                    "list", "GET /api/v1/recommendations",
                    "generate", "POST /api/v1/recommendations/generate",
                    "complete", "POST /api/v1/recommendations/{id}/complete")
            )
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
