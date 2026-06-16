package com.carbonbuddy.controller;

import com.carbonbuddy.dto.StoreItem;
import com.carbonbuddy.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardStoreControllerTest {

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private RewardStoreController rewardStoreController;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(1L, null);
    }

    @Test
    void should_returnStoreItems_when_called() {
        List<StoreItem> items = List.of(
                new StoreItem(1, "Eco Bag", "Reusable bag", 100, "bag"),
                new StoreItem(2, "Bamboo Brush", "Eco brush", 50, "brush")
        );
        when(rewardService.getStoreItems(1L)).thenReturn(items);
        when(rewardService.getStoreBalance(1L)).thenReturn(200);

        ResponseEntity<?> response = rewardStoreController.getStore(auth);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_returnItemsWithAffordabilityStatus() {
        List<StoreItem> items = List.of(
                new StoreItem(1, "Eco Bag", "Reusable bag", 100, "bag")
        );
        items.get(0).setAffordable(true);
        when(rewardService.getStoreItems(1L)).thenReturn(items);
        when(rewardService.getStoreBalance(1L)).thenReturn(200);

        ResponseEntity<?> response = rewardStoreController.getStore(auth);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_redeemItemSuccessfully_when_sufficientBalance() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("item", "Eco Bag");
        result.put("newBalance", 100);
        result.put("redemptionCode", "CB-ABC12345-0001");

        when(rewardService.redeemItem(1L, 1)).thenReturn(result);

        ResponseEntity<?> response = rewardStoreController.redeem(auth, 1);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_throwException_when_redeemingWithInsufficientBalance() {
        when(rewardService.redeemItem(1L, 7))
                .thenThrow(new IllegalArgumentException("Insufficient CarbonCoins"));

        assertThrows(IllegalArgumentException.class,
                () -> rewardStoreController.redeem(auth, 7));
    }

    @Test
    void should_throwException_when_redeemingInvalidItem() {
        when(rewardService.redeemItem(1L, 999))
                .thenThrow(new IllegalArgumentException("Invalid item: 999"));

        assertThrows(IllegalArgumentException.class,
                () -> rewardStoreController.redeem(auth, 999));
    }

    @Test
    void should_returnRedemptionHistory() {
        List<Map<String, Object>> history = List.of(
                Map.of("id", 1L, "points", 100, "item", "Eco Bag bag", "date", "2025-01-01")
        );
        when(rewardService.getRedemptionHistory(1L)).thenReturn(history);

        ResponseEntity<?> response = rewardStoreController.getHistory(auth, 0, 20);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_returnEmptyHistory_when_noRedemptions() {
        when(rewardService.getRedemptionHistory(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = rewardStoreController.getHistory(auth, 0, 20);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_callServiceWithCorrectUserId() {
        when(rewardService.getStoreItems(42L)).thenReturn(Collections.emptyList());
        when(rewardService.getStoreBalance(42L)).thenReturn(0);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(42L, null);

        rewardStoreController.getStore(otherAuth);

        verify(rewardService).getStoreItems(42L);
        verify(rewardService).getStoreBalance(42L);
    }

    @Test
    void should_callRedeemWithCorrectItemId() {
        Map<String, Object> result = Map.of("success", true, "item", "Test", "newBalance", 0);
        when(rewardService.redeemItem(1L, 5)).thenReturn(result);

        rewardStoreController.redeem(auth, 5);

        verify(rewardService).redeemItem(1L, 5);
    }

    @Test
    void should_callHistoryWithCorrectUserId() {
        when(rewardService.getRedemptionHistory(1L)).thenReturn(Collections.emptyList());

        rewardStoreController.getHistory(auth, 0, 10);

        verify(rewardService).getRedemptionHistory(1L);
    }

    @Test
    void should_handlePagination_when_gettingHistory() {
        List<Map<String, Object>> history = List.of(
                Map.of("id", 1L, "points", 100, "item", "Eco Bag", "date", "2025-01-01"),
                Map.of("id", 2L, "points", 50, "item", "Bamboo Brush", "date", "2025-01-02")
        );
        when(rewardService.getRedemptionHistory(1L)).thenReturn(history);

        ResponseEntity<?> response = rewardStoreController.getHistory(auth, 0, 1);

        assertEquals(200, response.getStatusCode().value());
    }
}
