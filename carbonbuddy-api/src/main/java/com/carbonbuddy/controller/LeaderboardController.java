package com.carbonbuddy.controller;

import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private static final int LEADERBOARD_LIMIT = 20;

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeaderboard(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        List<User> topUsers = userRepository.findByOrderByTotalPointsDesc(PageRequest.of(0, LEADERBOARD_LIMIT));

        List<Map<String, Object>> entries = new ArrayList<>();
        int rank = 0;
        for (User u : topUsers) {
            rank++;
            int level = RewardService.getLevelForPoints(u.getTotalPoints());
            entries.add(Map.of(
                    "rank", rank,
                    "name", u.getName(),
                    "points", u.getTotalPoints(),
                    "streak", u.getCurrentStreak(),
                    "level", RewardService.getLevelTitle(level),
                    "levelIcon", RewardService.getLevelIcon(level),
                    "isMe", u.getId().equals(userId)
            ));
        }

        boolean foundMe = entries.stream().anyMatch(e -> Boolean.TRUE.equals(e.get("isMe")));
        int userRank = foundMe
                ? (int) entries.stream().filter(e -> Boolean.TRUE.equals(e.get("isMe")))
                        .findFirst().map(e -> e.get("rank")).orElse(LEADERBOARD_LIMIT)
                : userRepository.countUsersWithMorePoints(currentUser.getTotalPoints()) + 1;

        return ResponseEntity.ok(Map.of(
                "leaderboard", entries,
                "myRank", userRank,
                "totalUsers", userRepository.countTotalUsers(),
                "myPoints", currentUser.getTotalPoints()
        ));
    }
}
