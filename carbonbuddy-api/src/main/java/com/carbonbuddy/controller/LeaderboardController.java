package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leaderboard")
@Validated
@Tag(name = "Leaderboard", description = "User rankings and leaderboard")
public class LeaderboardController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int DEFAULT_PAGE = 0;

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get leaderboard", description = "Returns top users ranked by total points along with caller's rank")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leaderboard retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeaderboard(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int size) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        List<User> topUsers = userRepository.findByOrderByTotalPointsDesc(
                PageRequest.of(page, size));

        List<Map<String, Object>> entries = new ArrayList<>();
        int rank = page * size;
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
                        .findFirst().map(e -> e.get("rank")).orElse(size)
                : userRepository.countUsersWithMorePoints(currentUser.getTotalPoints()) + 1;

        Map<String, Object> data = Map.of(
                "leaderboard", Collections.unmodifiableList(entries),
                "myRank", userRank,
                "totalUsers", userRepository.countTotalUsers(),
                "myPoints", currentUser.getTotalPoints(),
                "page", page,
                "size", size
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
