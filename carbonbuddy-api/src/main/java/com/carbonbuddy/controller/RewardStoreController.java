package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rewards")
@Validated
@Tag(name = "Rewards", description = "Reward store and redemption")
public class RewardStoreController {

    private final RewardService rewardService;

    public RewardStoreController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Operation(summary = "Get store", description = "Returns all store items with affordability status and balance")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Store retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/store")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStore(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        List<StoreItem> items = rewardService.getStoreItems(userId);
        int balance = rewardService.getStoreBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("items", items, "balance", balance)));
    }

    @Operation(summary = "Redeem item", description = "Redeems a store item for the authenticated user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item redeemed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid item or insufficient balance"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/redeem/{itemId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> redeem(
            Authentication auth, @PathVariable int itemId) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Map<String, Object> result = rewardService.redeemItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item redeemed", result));
    }

    @Operation(summary = "Get redemption history", description = "Returns paginated redemption history for the user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        List<Map<String, Object>> all = rewardService.getRedemptionHistory(userId);
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        Page<Map<String, Object>> result = new PageImpl<>(
                all.subList(Math.min(start, all.size()), end), pageable, all.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
