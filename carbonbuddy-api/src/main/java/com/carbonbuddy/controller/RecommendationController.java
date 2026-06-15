package com.carbonbuddy.controller;

import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<Recommendation>> getRecommendations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userId));
    }

    @PostMapping("/generate")
    public ResponseEntity<Void> generateRecommendations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        recommendationService.generateRecommendations(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Recommendation> completeRecommendation(
            Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        Recommendation rec = recommendationService.completeRecommendation(userId, id);
        return ResponseEntity.ok(rec);
    }
}
