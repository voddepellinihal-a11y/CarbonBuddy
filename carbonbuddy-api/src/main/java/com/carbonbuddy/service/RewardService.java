package com.carbonbuddy.service;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    public RewardService(RewardRepository rewardRepository, UserRepository userRepository) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }

    public static long getPointsForCarbon(double carbonKg, int streak) {
        long base = (long) (carbonKg * 10);
        if (base < 1) base = 1;
        double multiplier = getStreakMultiplier(streak);
        return Math.round(base * multiplier);
    }

    public static double getStreakMultiplier(int streak) {
        if (streak >= 30) return 5.0;
        if (streak >= 14) return 3.0;
        if (streak >= 7) return 2.5;
        if (streak >= 3) return 2.0;
        if (streak >= 2) return 1.5;
        return 1.0;
    }

    public static String getStreakLabel(int streak) {
        if (streak >= 30) return "🔥 Legendary";
        if (streak >= 14) return "⚡ On Fire";
        if (streak >= 7) return "🌟 Unstoppable";
        if (streak >= 3) return "💪 Getting Started";
        if (streak >= 1) return "🌱 Just Begun";
        return "Start Today!";
    }

    public static int getLevelForPoints(long points) {
        if (points >= 10000) return 5;
        if (points >= 2000) return 4;
        if (points >= 500) return 3;
        if (points >= 100) return 2;
        return 1;
    }

    public static String getLevelTitle(int level) {
        return switch (level) {
            case 5 -> "Climate Champion";
            case 4 -> "Forest Guardian";
            case 3 -> "Growing Tree";
            case 2 -> "Green Sprout";
            default -> "Eco Seedling";
        };
    }

    public static String getLevelIcon(int level) {
        return switch (level) {
            case 5 -> "\uD83C\uDFC6";
            case 4 -> "\uD83C\uDF32";
            case 3 -> "\uD83C\uDF33";
            case 2 -> "\uD83C\uDF3F";
            default -> "\uD83C\uDF31";
        };
    }

    public static long getPointsToNextLevel(long points) {
        if (points >= 10000) return 0;
        if (points >= 2000) return 10000 - points;
        if (points >= 500) return 2000 - points;
        if (points >= 100) return 500 - points;
        return 100 - points;
    }

    @Transactional
    public void awardPoints(Long userId, String source, Long sourceId, double carbonKg) {
        User user = userRepository.findById(userId).orElseThrow();
        long points = getPointsForCarbon(carbonKg, user.getCurrentStreak());

        user.setTotalPoints(user.getTotalPoints() + points);
        int newLevel = getLevelForPoints(user.getTotalPoints());
        if (newLevel > user.getLevel()) user.setLevel(newLevel);
        userRepository.save(user);

        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsEarned((int) points);
        reward.setSource(source);
        reward.setSourceId(sourceId);
        reward.setTransactionType("CREDIT");
        rewardRepository.save(reward);
    }

    @Transactional
    public int updateStreak(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        LocalDate today = LocalDate.now();

        if (user.getLastActivityDate() == null) {
            user.setCurrentStreak(1);
            user.setLastActivityDate(today);
        } else {
            long daysSince = ChronoUnit.DAYS.between(user.getLastActivityDate(), today);
            if (daysSince == 1) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (daysSince > 1) {
                user.setCurrentStreak(1);
            }
            user.setLastActivityDate(today);
        }

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        userRepository.save(user);
        return user.getCurrentStreak();
    }

    private static final List<StoreItem> STORE_ITEMS = List.of(
        new StoreItem(1, "Eco Tote Bag", "Reusable organic cotton tote for guilt-free shopping", 100, "🛍️"),
        new StoreItem(2, "Bamboo Toothbrush Set", "Pack of 4 biodegradable bamboo toothbrushes", 50, "🧴"),
        new StoreItem(3, "Free Bike Share Pass", "30-minute city bike share ride — zero emissions!", 200, "🚲"),
        new StoreItem(4, "Reusable Coffee Cup", "Keep your coffee hot and planet cool — 200+ uses", 150, "☕"),
        new StoreItem(5, "Plant a Tree Certificate", "We'll plant a native tree in your name 🌱", 300, "🌳"),
        new StoreItem(6, "Metro Pass Discount", "₹50 off your next monthly metro pass recharge", 500, "🎟️"),
        new StoreItem(7, "Carbon Offset Hero Badge", "Exclusive profile badge + 5x streak boost for 1 week", 1000, "♻️")
    );

    private static final String COUPON_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public List<StoreItem> getStoreItems(Long userId) {
        int balance = rewardRepository.getBalanceByUserId(userId);
        return STORE_ITEMS.stream().map(item -> {
            StoreItem copy = new StoreItem(item.getId(), item.getName(), item.getDescription(), item.getCost(), item.getIcon());
            copy.setAffordable(item.getCost() <= balance);
            return copy;
        }).collect(Collectors.toList());
    }

    public int getStoreBalance(Long userId) {
        return rewardRepository.getBalanceByUserId(userId);
    }

    @Transactional
    public Map<String, Object> redeemItem(Long userId, int itemId) {
        StoreItem item = STORE_ITEMS.stream()
                .filter(i -> i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid item: " + itemId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        int balance = rewardRepository.getBalanceByUserId(userId);
        if (balance < item.getCost()) {
            throw new IllegalArgumentException("Insufficient CarbonCoins");
        }

        user.setTotalPoints(user.getTotalPoints() - item.getCost());
        userRepository.save(user);

        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsSpent(item.getCost());
        reward.setSource("STORE_REDEEM");
        reward.setSourceId((long) itemId);
        reward.setTransactionType("DEBIT");
        rewardRepository.save(reward);

        int newBalance = rewardRepository.getBalanceByUserId(userId);
        String redemptionCode = generateCode(itemId, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("item", item.getName());
        result.put("icon", item.getIcon());
        result.put("cost", item.getCost());
        result.put("newBalance", newBalance);
        result.put("redemptionCode", redemptionCode);
        return result;
    }

    public List<Map<String, Object>> getRedemptionHistory(Long userId) {
        List<Reward> redemptions = rewardRepository.findByUserIdAndTransactionType(userId, "DEBIT");
        return redemptions.stream().map(r -> {
            String itemName = STORE_ITEMS.stream()
                    .filter(i -> i.getId() == (r.getSourceId() != null ? r.getSourceId().intValue() : 0))
                    .findFirst().map(i -> i.getName() + " " + i.getIcon()).orElse("Unknown item");
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", r.getId());
            entry.put("points", r.getCreditsSpent());
            entry.put("item", itemName);
            entry.put("date", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            return entry;
        }).collect(Collectors.toList());
    }

    private String generateCode(int itemId, Long userId) {
        Random rnd = new Random();
        StringBuilder code = new StringBuilder("CB-");
        for (int i = 0; i < 8; i++) {
            code.append(COUPON_CHARS.charAt(rnd.nextInt(COUPON_CHARS.length())));
        }
        code.append("-").append(String.format("%04d", userId % 10000));
        return code.toString();
    }
}
