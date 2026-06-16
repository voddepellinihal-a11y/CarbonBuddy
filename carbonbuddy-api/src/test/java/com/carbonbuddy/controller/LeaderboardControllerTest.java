package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaderboardController leaderboardController;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(1L, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnLeaderboard_when_usersExist() {
        User user = buildUser(1L, "Alice", 500);
        User currentUser = buildUser(1L, "Alice", 500);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(user));
        when(userRepository.countTotalUsers()).thenReturn(1);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().getData().get("leaderboard"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnSortedUsers_when_multipleUsersExist() {
        User high = buildUser(2L, "Bob", 1000);
        User low = buildUser(3L, "Charlie", 200);
        User currentUser = buildUser(1L, "Alice", 500);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(high, low));
        when(userRepository.countTotalUsers()).thenReturn(3);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        List<Map<String, Object>> leaderboard =
                (List<Map<String, Object>>) response.getBody().getData().get("leaderboard");
        assertEquals(2, leaderboard.size());
        assertEquals("Bob", leaderboard.get(0).get("name"));
        assertEquals("Charlie", leaderboard.get(1).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnCorrectCallerRank_when_userIsInTopList() {
        User user1 = buildUser(1L, "Alice", 500);
        User user2 = buildUser(2L, "Bob", 300);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(user1, user2));
        when(userRepository.countTotalUsers()).thenReturn(2);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        assertEquals(1, response.getBody().getData().get("myRank"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnCallerRank_when_userNotInTopList() {
        User topUser = buildUser(2L, "Bob", 1000);
        User currentUser = buildUser(1L, "Alice", 100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(topUser));
        when(userRepository.countUsersWithMorePoints(100)).thenReturn(1);
        when(userRepository.countTotalUsers()).thenReturn(2);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        assertEquals(2, response.getBody().getData().get("myRank"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnEmptyLeaderboard_when_noUsersExist() {
        User currentUser = buildUser(1L, "Alice", 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(userRepository.countTotalUsers()).thenReturn(1);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        List<Map<String, Object>> leaderboard =
                (List<Map<String, Object>>) response.getBody().getData().get("leaderboard");
        assertTrue(leaderboard.isEmpty());
        assertEquals(1, response.getBody().getData().get("myRank"));
    }

    @Test
    void should_returnCorrectTotalUsers() {
        User currentUser = buildUser(1L, "Alice", 500);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(currentUser));
        when(userRepository.countTotalUsers()).thenReturn(42);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        assertEquals(42, response.getBody().getData().get("totalUsers"));
    }

    @Test
    void should_returnCorrectMyPoints() {
        User currentUser = buildUser(1L, "Alice", 750);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(currentUser));
        when(userRepository.countTotalUsers()).thenReturn(1);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        assertEquals(750L, ((Number) response.getBody().getData().get("myPoints")).longValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_markCurrentUser_when_userInLeaderboard() {
        User user1 = buildUser(1L, "Alice", 500);
        User user2 = buildUser(2L, "Bob", 300);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(user1, user2));
        when(userRepository.countTotalUsers()).thenReturn(2);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        List<Map<String, Object>> leaderboard =
                (List<Map<String, Object>>) response.getBody().getData().get("leaderboard");
        assertTrue(Boolean.TRUE.equals(leaderboard.get(0).get("isMe")));
        assertFalse(Boolean.TRUE.equals(leaderboard.get(1).get("isMe")));
    }

    @Test
    void should_throwException_when_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> leaderboardController.getLeaderboard(auth, 0, 20));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_includeLevelInfo_when_returningEntries() {
        User user = buildUser(1L, "Alice", 500);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByOrderByTotalPointsDesc(any(PageRequest.class)))
                .thenReturn(List.of(user));
        when(userRepository.countTotalUsers()).thenReturn(1);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                leaderboardController.getLeaderboard(auth, 0, 20);

        List<Map<String, Object>> leaderboard =
                (List<Map<String, Object>>) response.getBody().getData().get("leaderboard");
        assertNotNull(leaderboard.get(0).get("level"));
        assertNotNull(leaderboard.get(0).get("levelIcon"));
    }

    private User buildUser(Long id, String name, long points) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setTotalPoints(points);
        user.setCurrentStreak(0);
        user.setLevel(1);
        return user;
    }
}
