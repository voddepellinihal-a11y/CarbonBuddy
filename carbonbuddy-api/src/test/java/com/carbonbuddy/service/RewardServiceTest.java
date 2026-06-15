package com.carbonbuddy.service;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.RewardRepository;
import com.carbonbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private UserRepository userRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(rewardRepository, userRepository);
    }

    @Test
    void getPointsForCarbon_shouldCalculateBasePoints() {
        long points = RewardService.getPointsForCarbon(10.0, 0);
        assertEquals(100, points);
    }

    @Test
    void getPointsForCarbon_shouldApplyStreakMultiplier() {
        long points = RewardService.getPointsForCarbon(10.0, 30);
        assertEquals(500, points);
    }

    @Test
    void getPointsForCarbon_shouldFloorTo1() {
        long points = RewardService.getPointsForCarbon(0.01, 0);
        assertTrue(points >= 1);
    }

    @Test
    void getStreakMultiplier_shouldReturnCorrectMultiplier() {
        assertEquals(1.0, RewardService.getStreakMultiplier(0));
        assertEquals(1.0, RewardService.getStreakMultiplier(1));
        assertEquals(1.5, RewardService.getStreakMultiplier(2));
        assertEquals(2.0, RewardService.getStreakMultiplier(3));
        assertEquals(2.5, RewardService.getStreakMultiplier(7));
        assertEquals(3.0, RewardService.getStreakMultiplier(14));
        assertEquals(5.0, RewardService.getStreakMultiplier(30));
        assertEquals(5.0, RewardService.getStreakMultiplier(100));
    }

    @Test
    void getLevelForPoints_shouldReturnCorrectLevel() {
        assertEquals(1, RewardService.getLevelForPoints(0));
        assertEquals(1, RewardService.getLevelForPoints(99));
        assertEquals(2, RewardService.getLevelForPoints(100));
        assertEquals(2, RewardService.getLevelForPoints(499));
        assertEquals(3, RewardService.getLevelForPoints(500));
        assertEquals(3, RewardService.getLevelForPoints(1999));
        assertEquals(4, RewardService.getLevelForPoints(2000));
        assertEquals(4, RewardService.getLevelForPoints(9999));
        assertEquals(5, RewardService.getLevelForPoints(10000));
        assertEquals(5, RewardService.getLevelForPoints(100000));
    }

    @Test
    void getLevelTitle_shouldReturnCorrectTitle() {
        assertEquals("Eco Seedling", RewardService.getLevelTitle(1));
        assertEquals("Green Sprout", RewardService.getLevelTitle(2));
        assertEquals("Growing Tree", RewardService.getLevelTitle(3));
        assertEquals("Forest Guardian", RewardService.getLevelTitle(4));
        assertEquals("Climate Champion", RewardService.getLevelTitle(5));
    }

    @Test
    void getStoreItems_shouldReturnItemsWithAffordability() {
        when(rewardRepository.getBalanceByUserId(1L)).thenReturn(100);

        List<StoreItem> items = rewardService.getStoreItems(1L);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(StoreItem::isAffordable));
        assertTrue(items.stream().anyMatch(i -> !i.isAffordable()));
        verify(rewardRepository).getBalanceByUserId(1L);
    }

    @Test
    void redeemItem_shouldFailForInvalidItem() {
        assertThrows(IllegalArgumentException.class, () -> rewardService.redeemItem(1L, 999));
    }

    @Test
    void redeemItem_shouldFailForInsufficientBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(rewardRepository.getBalanceByUserId(1L)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> rewardService.redeemItem(1L, 1));
    }

    @Test
    void redeemItem_shouldSucceedWithSufficientBalance() {
        User user = new User();
        user.setTotalPoints(500);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rewardRepository.getBalanceByUserId(1L)).thenReturn(500, 400);
        when(rewardRepository.save(any(Reward.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = rewardService.redeemItem(1L, 1);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertNotNull(result.get("redemptionCode"));
        assertEquals(400, result.get("newBalance"));
        verify(userRepository).save(user);
        verify(rewardRepository, times(1)).save(any(Reward.class));
        verify(rewardRepository, times(2)).getBalanceByUserId(1L);
    }

    @Test
    void redeemItem_shouldThrowForMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> rewardService.redeemItem(1L, 1));
    }
}
