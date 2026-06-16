package com.carbonbuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root controller providing API metadata and available endpoint listing.
 */
@RestController
public class RootController {

    private static final String API_VERSION = "1.0.0";
    private static final String SERVICE_NAME = "CarbonBuddy API";
    private static final String STATUS_RUNNING = "running";

    /**
     * Returns API metadata including service name, version, and available endpoints.
     *
     * @return 200 OK with API metadata
     */
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "service", SERVICE_NAME,
            "version", API_VERSION,
            "status", STATUS_RUNNING,
            "endpoints", Map.of(
                "auth", Map.of("register", "POST /api/auth/register", "login", "POST /api/auth/login"),
                "activities", "POST /api/activities",
                "utility-bills", "POST /api/utility-bills",
                "analytics", "GET /api/analytics/dashboard",
                "recommendations", Map.of(
                    "list", "GET /api/recommendations",
                    "generate", "POST /api/recommendations/generate",
                    "complete", "POST /api/recommendations/{id}/complete")
            )
        ));
    }
}
