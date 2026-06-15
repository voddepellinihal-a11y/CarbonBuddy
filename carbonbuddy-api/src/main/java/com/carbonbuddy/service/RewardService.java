package com.carbonbuddy.service;

import com.carbonbuddy.model.Reward;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
}
