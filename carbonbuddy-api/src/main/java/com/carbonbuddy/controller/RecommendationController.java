package com.carbonbuddy.controller;

import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for managing carbon-reduction recommendations.
 * Supports listing, generating, and completing recommendations.
 */
@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Constructs RecommendationController with the recommendation service.
     *
     * @param recommendationService the service handling recommendation logic
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Returns all recommendations for the authenticated user.
     *
     * @param auth the authentication principal
     * @return 200 OK with an unmodifiable list of recommendations
     */
    @GetMapping
    public ResponseEntity<List<Recommendation>> getRecommendations(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        return ResponseEntity.ok(Collections.unmodifiableList(
                recommendationService.getUserRecommendations(userId)));
    }

    /**
     * Generates new recommendations based on the user's emission data.
     *
     * @param auth the authentication principal
     * @return 201 Created with no body
     */
    @PostMapping("/generate")
    public ResponseEntity<Void> generateRecommendations(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        recommendationService.generateRecommendations(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Marks a recommendation as completed and awards bonus points.
     *
     * @param auth the authentication principal
     * @param id   the recommendation ID
     * @return 200 OK with the completed recommendation
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Recommendation> completeRecommendation(
            Authentication auth, @PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Recommendation rec = recommendationService.completeRecommendation(userId, id);
        return ResponseEntity.ok(rec);
    }
}
