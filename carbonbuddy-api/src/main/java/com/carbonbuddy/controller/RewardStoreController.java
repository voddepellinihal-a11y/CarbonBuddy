package com.carbonbuddy.controller;

import com.carbonbuddy.model.Reward;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rewards")
public class RewardStoreController {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;

    public RewardStoreController(UserRepository userRepository, RewardRepository rewardRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
    }

    private static final List<Map<String, Object>> STORE_ITEMS = List.of(
        item(1, "Eco Tote Bag", "Reusable organic cotton tote for guilt-free shopping", 100, "🛍️"),
        item(2, "Bamboo Toothbrush Set", "Pack of 4 biodegradable bamboo toothbrushes", 50, "🧴"),
        item(3, "Free Bike Share Pass", "30-minute city bike share ride — zero emissions!", 200, "🚲"),
        item(4, "Reusable Coffee Cup", "Keep your coffee hot and planet cool — 200+ uses", 150, "☕"),
        item(5, "Plant a Tree Certificate", "We'll plant a native tree in your name 🌱", 300, "🌳"),
        item(6, "Metro Pass Discount", "₹50 off your next monthly metro pass recharge", 500, "🎟️"),
        item(7, "Carbon Offset Hero Badge", "Exclusive profile badge + 5x streak boost for 1 week", 1000, "♻️")
    );

    private static Map<String, Object> item(int id, String name, String desc, int cost, String icon) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("description", desc);
        m.put("cost", cost); m.put("icon", icon);
        return m;
    }

    @GetMapping("/store")
    public ResponseEntity<Map<String, Object>> getStore(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow();
        int balance = rewardRepository.getBalanceByUserId(userId);

        List<Map<String, Object>> items = STORE_ITEMS.stream().map(item -> {
            Map<String, Object> copy = new LinkedHashMap<>(item);
            copy.put("affordable", (int) copy.get("cost") <= balance);
            return copy;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("items", items, "balance", balance));
    }

    @PostMapping("/redeem/{itemId}")
    public ResponseEntity<Map<String, Object>> redeem(
            Authentication auth, @PathVariable int itemId) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow();

        Map<String, Object> item = STORE_ITEMS.stream()
                .filter(i -> (int) i.get("id") == itemId)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid item"));

        int cost = (int) item.get("cost");
        int balance = rewardRepository.getBalanceByUserId(userId);

        if (balance < cost) {
            throw new IllegalArgumentException("Insufficient CarbonCoins");
        }

        user.setTotalPoints(user.getTotalPoints() - cost);
        userRepository.save(user);

        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsSpent(cost);
        reward.setSource("STORE_REDEEM");
        reward.setSourceId((long) itemId);
        reward.setTransactionType("DEBIT");
        rewardRepository.save(reward);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("item", item.get("name"));
        result.put("icon", item.get("icon"));
        result.put("cost", cost);
        result.put("newBalance", rewardRepository.getBalanceByUserId(userId));
        result.put("redemptionCode", generateCode(itemId, userId));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Reward> redemptions = rewardRepository.findByUserIdAndTransactionType(userId, "DEBIT");

        List<Map<String, Object>> history = redemptions.stream().map(r -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", r.getId());
            entry.put("points", r.getCreditsSpent());
            String itemName = STORE_ITEMS.stream()
                    .filter(i -> (int) i.get("id") == (r.getSourceId() != null ? r.getSourceId().intValue() : 0))
                    .findFirst().map(i -> i.get("name") + " " + i.get("icon")).orElse("Unknown item");
            entry.put("item", itemName);
            entry.put("date", r.getCreatedAt().toString());
            return entry;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    private String generateCode(int itemId, Long userId) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random rnd = new Random();
        StringBuilder code = new StringBuilder("CB-");
        for (int i = 0; i < 8; i++) code.append(chars.charAt(rnd.nextInt(chars.length())));
        code.append("-").append(String.format("%04d", userId % 10000));
        return code.toString();
    }
}
