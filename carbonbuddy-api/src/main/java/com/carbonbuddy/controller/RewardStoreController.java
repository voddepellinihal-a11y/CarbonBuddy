package com.carbonbuddy.controller;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the reward store, item redemption, and redemption history.
 */
@RestController
@RequestMapping("/api/rewards")
@Validated
public class RewardStoreController {

    private final RewardService rewardService;

    /**
     * Constructs RewardStoreController with the reward service.
     *
     * @param rewardService the service handling reward logic
     */
    public RewardStoreController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /**
     * Returns all store items with affordability status and the user's balance.
     *
     * @param auth the authentication principal
     * @return 200 OK with items and balance
     */
    @GetMapping("/store")
    public ResponseEntity<Map<String, Object>> getStore(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        List<StoreItem> items = rewardService.getStoreItems(userId);
        int balance = rewardService.getStoreBalance(userId);
        return ResponseEntity.ok(Map.of("items", items, "balance", balance));
    }

    /**
     * Redeems a store item for the authenticated user.
     *
     * @param auth   the authentication principal
     * @param itemId the store item ID
     * @return 200 OK with redemption details
     */
    @PostMapping("/redeem/{itemId}")
    public ResponseEntity<Map<String, Object>> redeem(
            Authentication auth, @PathVariable int itemId) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Map<String, Object> result = rewardService.redeemItem(userId, itemId);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns the redemption history for the authenticated user.
     *
     * @param auth the authentication principal
     * @return 200 OK with an unmodifiable list of redemption entries
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        return ResponseEntity.ok(Collections.unmodifiableList(rewardService.getRedemptionHistory(userId)));
    }
}
