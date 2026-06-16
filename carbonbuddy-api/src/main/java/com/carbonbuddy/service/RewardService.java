package com.carbonbuddy.service;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private static final long BASE_POINTS_MULTIPLIER = 10;
    private static final long MIN_POINTS = 1;
    private static final int STREAK_THRESHOLD_30 = 30;
    private static final int STREAK_THRESHOLD_14 = 14;
    private static final int STREAK_THRESHOLD_7 = 7;
    private static final int STREAK_THRESHOLD_3 = 3;
    private static final int STREAK_THRESHOLD_2 = 2;
    private static final double MULTIPLIER_5X = 5.0;
    private static final double MULTIPLIER_3X = 3.0;
    private static final double MULTIPLIER_2_5X = 2.5;
    private static final double MULTIPLIER_2X = 2.0;
    private static final double MULTIPLIER_1_5X = 1.5;
    private static final double MULTIPLIER_1X = 1.0;

    private static final long LEVEL_5_THRESHOLD = 10000;
    private static final long LEVEL_4_THRESHOLD = 2000;
    private static final long LEVEL_3_THRESHOLD = 500;
    private static final long LEVEL_2_THRESHOLD = 100;
    private static final int LEVEL_5 = 5;
    private static final int LEVEL_4 = 4;
    private static final int LEVEL_3 = 3;
    private static final int LEVEL_2 = 2;
    private static final int LEVEL_1 = 1;

    private static final String SOURCE_STORE_REDEEM = "STORE_REDEEM";
    private static final String TRANSACTION_CREDIT = "CREDIT";
    private static final String TRANSACTION_DEBIT = "DEBIT";
    private static final String COUPON_PREFIX = "CB-";
    private static final int COUPON_CODE_LENGTH = 8;
    private static final int MODULUS_FACTOR = 10000;
    private static final String COUPON_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    private static final List<StoreItem> STORE_ITEMS = List.of(
        new StoreItem(1, "Eco Tote Bag", "Reusable organic cotton tote for guilt-free shopping", 100, "bag"),
        new StoreItem(2, "Bamboo Toothbrush Set", "Pack of 4 biodegradable bamboo toothbrushes", 50, "toothbrush"),
        new StoreItem(3, "Free Bike Share Pass", "30-minute city bike share ride - zero emissions!", 200, "bike"),
        new StoreItem(4, "Reusable Coffee Cup", "Keep your coffee hot and planet cool - 200+ uses", 150, "coffee"),
        new StoreItem(5, "Plant a Tree Certificate", "We'll plant a native tree in your name", 300, "tree"),
        new StoreItem(6, "Metro Pass Discount", "50 off your next monthly metro pass recharge", 500, "metro"),
        new StoreItem(7, "Carbon Offset Hero Badge", "Exclusive profile badge + 5x streak boost for 1 week", 1000, "badge")
    );

    public RewardService(RewardRepository rewardRepository, UserRepository userRepository) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }

    public static long getPointsForCarbon(double carbonKg, int streak) {
        long base = (long) (carbonKg * BASE_POINTS_MULTIPLIER);
        if (base < MIN_POINTS) {
            base = MIN_POINTS;
        }
        double multiplier = getStreakMultiplier(streak);
        return Math.round(base * multiplier);
    }

    public static double getStreakMultiplier(int streak) {
        if (streak >= STREAK_THRESHOLD_30) return MULTIPLIER_5X;
        if (streak >= STREAK_THRESHOLD_14) return MULTIPLIER_3X;
        if (streak >= STREAK_THRESHOLD_7) return MULTIPLIER_2_5X;
        if (streak >= STREAK_THRESHOLD_3) return MULTIPLIER_2X;
        if (streak >= STREAK_THRESHOLD_2) return MULTIPLIER_1_5X;
        return MULTIPLIER_1X;
    }

    public static String getStreakLabel(int streak) {
        if (streak >= STREAK_THRESHOLD_30) return "Legendary";
        if (streak >= STREAK_THRESHOLD_14) return "On Fire";
        if (streak >= STREAK_THRESHOLD_7) return "Unstoppable";
        if (streak >= STREAK_THRESHOLD_3) return "Getting Started";
        if (streak >= 1) return "Just Begun";
        return "Start Today!";
    }

    public static int getLevelForPoints(long points) {
        if (points >= LEVEL_5_THRESHOLD) return LEVEL_5;
        if (points >= LEVEL_4_THRESHOLD) return LEVEL_4;
        if (points >= LEVEL_3_THRESHOLD) return LEVEL_3;
        if (points >= LEVEL_2_THRESHOLD) return LEVEL_2;
        return LEVEL_1;
    }

    public static String getLevelTitle(int level) {
        return switch (level) {
            case LEVEL_5 -> "Climate Champion";
            case LEVEL_4 -> "Forest Guardian";
            case LEVEL_3 -> "Growing Tree";
            case LEVEL_2 -> "Green Sprout";
            default -> "Eco Seedling";
        };
    }

    public static String getLevelIcon(int level) {
        return switch (level) {
            case LEVEL_5 -> "champion";
            case LEVEL_4 -> "forest";
            case LEVEL_3 -> "tree";
            case LEVEL_2 -> "sprout";
            default -> "seed";
        };
    }

    public static long getPointsToNextLevel(long points) {
        if (points >= LEVEL_5_THRESHOLD) return 0;
        if (points >= LEVEL_4_THRESHOLD) return LEVEL_5_THRESHOLD - points;
        if (points >= LEVEL_3_THRESHOLD) return LEVEL_4_THRESHOLD - points;
        if (points >= LEVEL_2_THRESHOLD) return LEVEL_3_THRESHOLD - points;
        return LEVEL_2_THRESHOLD - points;
    }

    @Transactional
    @CacheEvict(value = "dashboard", key = "#userId")
    public void awardPoints(Long userId, String source, Long sourceId, double carbonKg) {
        log.debug("Awarding points to user {} from source {}", userId, source);

        User user = userRepository.findById(userId).orElseThrow();
        long points = getPointsForCarbon(carbonKg, user.getCurrentStreak());

        user.setTotalPoints(user.getTotalPoints() + points);
        int newLevel = getLevelForPoints(user.getTotalPoints());
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
        }
        userRepository.save(user);

        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsEarned((int) points);
        reward.setSource(source);
        reward.setSourceId(sourceId);
        reward.setTransactionType(TRANSACTION_CREDIT);
        rewardRepository.save(reward);
    }

    @Transactional
    @CacheEvict(value = "dashboard", key = "#userId")
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

    @Cacheable(value = "storeItems", key = "#userId")
    public List<StoreItem> getStoreItems(Long userId) {
        int balance = rewardRepository.getBalanceByUserId(userId);
        return Collections.unmodifiableList(STORE_ITEMS.stream().map(item -> {
            StoreItem copy = new StoreItem(item.getId(), item.getName(), item.getDescription(), item.getCost(), item.getIcon());
            copy.setAffordable(item.getCost() <= balance);
            return copy;
        }).collect(Collectors.toList()));
    }

    public int getStoreBalance(Long userId) {
        return rewardRepository.getBalanceByUserId(userId);
    }

    @Transactional
    @Caching(evict = {
        @org.springframework.cache.annotation.CacheEvict(value = "storeItems", key = "#userId"),
        @org.springframework.cache.annotation.CacheEvict(value = "dashboard", key = "#userId")
    })
    public Map<String, Object> redeemItem(Long userId, int itemId) {
        log.debug("Redeeming item {} for user {}", itemId, userId);

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
        reward.setSource(SOURCE_STORE_REDEEM);
        reward.setSourceId((long) itemId);
        reward.setTransactionType(TRANSACTION_DEBIT);
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
        return Collections.unmodifiableMap(result);
    }

    public List<Map<String, Object>> getRedemptionHistory(Long userId) {
        List<Reward> redemptions = rewardRepository.findByUserIdAndTransactionType(userId, TRANSACTION_DEBIT);
        return Collections.unmodifiableList(redemptions.stream().map(r -> {
            String itemName = STORE_ITEMS.stream()
                    .filter(i -> i.getId() == (r.getSourceId() != null ? r.getSourceId().intValue() : 0))
                    .findFirst().map(i -> i.getName() + " " + i.getIcon()).orElse("Unknown item");
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", r.getId());
            entry.put("points", r.getCreditsSpent());
            entry.put("item", itemName);
            entry.put("date", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            return entry;
        }).collect(Collectors.toList()));
    }

    private String generateCode(int itemId, Long userId) {
        Random rnd = new Random();
        StringBuilder code = new StringBuilder(COUPON_PREFIX);
        for (int i = 0; i < COUPON_CODE_LENGTH; i++) {
            code.append(COUPON_CHARS.charAt(rnd.nextInt(COUPON_CHARS.length())));
        }
        code.append("-").append(String.format("%04d", userId % MODULUS_FACTOR));
        return code.toString();
    }
}
