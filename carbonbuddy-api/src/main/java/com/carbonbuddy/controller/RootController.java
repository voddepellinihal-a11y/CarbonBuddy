package com.carbonbuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "service", "CarbonBuddy API",
            "version", "1.0.0",
            "status", "running",
            "endpoints", Map.of(
                "auth", Map.of("register", "POST /api/auth/register", "login", "POST /api/auth/login"),
                "activities", "POST /api/activities",
                "utility-bills", "POST /api/utility-bills",
                "analytics", "GET /api/analytics/dashboard",
                "recommendations", Map.of("list", "GET /api/recommendations", "generate", "POST /api/recommendations/generate", "complete", "POST /api/recommendations/{id}/complete")
            )
        ));
    }
}
