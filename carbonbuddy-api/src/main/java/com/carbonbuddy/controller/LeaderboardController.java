package com.carbonbuddy.controller;

import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeaderboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User currentUser = userRepository.findById(userId).orElseThrow();

        List<User> topUsers = userRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()))
                .limit(20)
                .collect(Collectors.toList());

        List<Map<String, Object>> entries = new ArrayList<>();
        int rank = 0;
        for (User u : topUsers) {
            rank++;
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank);
            entry.put("name", u.getName());
            entry.put("points", u.getTotalPoints());
            entry.put("streak", u.getCurrentStreak());
            entry.put("level", com.carbonbuddy.service.RewardService.getLevelTitle(
                    com.carbonbuddy.service.RewardService.getLevelForPoints(u.getTotalPoints())));
            entry.put("levelIcon", com.carbonbuddy.service.RewardService.getLevelIcon(
                    com.carbonbuddy.service.RewardService.getLevelForPoints(u.getTotalPoints())));
            entry.put("isMe", u.getId().equals(userId));
            entries.add(entry);
        }

        int userRank = (int) entries.stream().filter(e -> (boolean) e.get("isMe")).count() > 0
                ? (int) entries.stream().filter(e -> (boolean) e.get("isMe")).findFirst().map(e -> e.get("rank")).orElse(99)
                : userRepository.countUsersWithMorePoints(currentUser.getTotalPoints()) + 1;

        return ResponseEntity.ok(Map.of(
                "leaderboard", entries,
                "myRank", userRank,
                "totalUsers", userRepository.countTotalUsers(),
                "myPoints", currentUser.getTotalPoints()
        ));
    }
}
