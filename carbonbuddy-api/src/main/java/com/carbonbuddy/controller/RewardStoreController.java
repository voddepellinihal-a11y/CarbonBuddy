package com.carbonbuddy.controller;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardStoreController {

    private final RewardService rewardService;

    public RewardStoreController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/store")
    public ResponseEntity<Map<String, Object>> getStore(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        List<StoreItem> items = rewardService.getStoreItems(userId);
        int balance = rewardService.getStoreBalance(userId);
        return ResponseEntity.ok(Map.of("items", items, "balance", balance));
    }

    @PostMapping("/redeem/{itemId}")
    public ResponseEntity<Map<String, Object>> redeem(
            Authentication auth, @PathVariable int itemId) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        Map<String, Object> result = rewardService.redeemItem(userId, itemId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        return ResponseEntity.ok(rewardService.getRedemptionHistory(userId));
    }
}
