package com.carbonbuddy.controller;

import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for the leaderboard endpoint.
 * Returns top users ranked by total points along with the caller's rank.
 */
@RestController
@RequestMapping("/api/leaderboard")
@Validated
public class LeaderboardController {

    private static final int LEADERBOARD_LIMIT = 20;
    private static final int DEFAULT_PAGE = 0;

    private final UserRepository userRepository;

    /**
     * Constructs LeaderboardController with the user repository.
     *
     * @param userRepository the user repository
     */
    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the top users leaderboard and the authenticated user's rank.
     *
     * @param auth the authentication principal
     * @return 200 OK with leaderboard, myRank, totalUsers, and myPoints
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeaderboard(Authentication auth) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        List<User> topUsers = userRepository.findByOrderByTotalPointsDesc(
                PageRequest.of(DEFAULT_PAGE, LEADERBOARD_LIMIT));

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
                "leaderboard", Collections.unmodifiableList(entries),
                "myRank", userRank,
                "totalUsers", userRepository.countTotalUsers(),
                "myPoints", currentUser.getTotalPoints()
        ));
    }
}
