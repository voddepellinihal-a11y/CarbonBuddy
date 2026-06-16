package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
@Validated
@Tag(name = "Recommendations", description = "Carbon-reduction recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Get recommendations", description = "Returns paginated recommendations for the user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Recommendation>>> getRecommendations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        List<Recommendation> all = recommendationService.getUserRecommendations(userId);
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        Page<Recommendation> result = new PageImpl<>(
                all.subList(Math.min(start, all.size()), end), pageable, all.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Generate recommendations", description = "Generates new recommendations based on emission data")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recommendations generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateRecommendations(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        recommendationService.generateRecommendations(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recommendations generated", Map.of("status", "generated")));
    }

    @Operation(summary = "Complete recommendation", description = "Marks a recommendation as completed and awards bonus points")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendation completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Recommendation not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Recommendation>> completeRecommendation(
            Authentication auth, @PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Recommendation rec = recommendationService.completeRecommendation(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Recommendation completed", rec));
    }
}
