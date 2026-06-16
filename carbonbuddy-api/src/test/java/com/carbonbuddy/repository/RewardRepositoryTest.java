package com.carbonbuddy.repository;

import com.carbonbuddy.model.Reward;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RewardRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Test
    void should_findByUserIdAndTransactionType_when_exists() {
        Reward reward = new Reward();
        reward.setUserId(1L);
        reward.setCreditsEarned(100);
        reward.setSource("ACTIVITY");
        reward.setTransactionType("CREDIT");
        rewardRepository.save(reward);

        List<Reward> results = rewardRepository.findByUserIdAndTransactionType(1L, "CREDIT");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("CREDIT", results.get(0).getTransactionType());
    }

    @Test
    void should_returnEmptyList_when_noRewardsForUser() {
        List<Reward> results = rewardRepository.findByUserIdAndTransactionType(999L, "CREDIT");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void should_returnEmptyList_when_transactionTypeDoesNotMatch() {
        Reward reward = new Reward();
        reward.setUserId(1L);
        reward.setCreditsEarned(50);
        reward.setSource("STORE_REDEEM");
        reward.setTransactionType("CREDIT");
        rewardRepository.save(reward);

        List<Reward> results = rewardRepository.findByUserIdAndTransactionType(1L, "DEBIT");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void should_returnMultipleRewards_when_multipleExist() {
        for (int i = 0; i < 3; i++) {
            Reward reward = new Reward();
            reward.setUserId(1L);
            reward.setCreditsEarned(10);
            reward.setSource("ACTIVITY");
            reward.setTransactionType("CREDIT");
            rewardRepository.save(reward);
        }

        List<Reward> results = rewardRepository.findByUserIdAndTransactionType(1L, "CREDIT");

        assertEquals(3, results.size());
    }

    @Test
    void should_getBalanceByUserId_when_hasCreditsAndDebits() {
        Reward credit = new Reward();
        credit.setUserId(2L);
        credit.setCreditsEarned(200);
        credit.setTransactionType("CREDIT");
        rewardRepository.save(credit);

        Reward debit = new Reward();
        debit.setUserId(2L);
        debit.setCreditsSpent(50);
        debit.setTransactionType("DEBIT");
        rewardRepository.save(debit);

        int balance = rewardRepository.getBalanceByUserId(2L);

        assertEquals(150, balance);
    }

    @Test
    void should_returnZeroBalance_when_noRewardsExist() {
        int balance = rewardRepository.getBalanceByUserId(999L);

        assertEquals(0, balance);
    }

    @Test
    void should_sumEarnedSince_when_rewardsExistAfterDate() {
        Reward recent = new Reward();
        recent.setUserId(3L);
        recent.setCreditsEarned(100);
        recent.setTransactionType("CREDIT");
        rewardRepository.save(recent);

        long earned = rewardRepository.sumEarnedSince(3L, LocalDateTime.now().minusHours(1));

        assertEquals(100, earned);
    }

    @Test
    void should_returnZeroEarned_when_noRecentRewards() {
        long earned = rewardRepository.sumEarnedSince(999L, LocalDateTime.now());

        assertEquals(0, earned);
    }

    @Test
    void should_countActiveUsersSince_when_usersActive() {
        Reward r1 = new Reward();
        r1.setUserId(10L);
        r1.setCreditsEarned(10);
        r1.setTransactionType("CREDIT");
        rewardRepository.save(r1);

        Reward r2 = new Reward();
        r2.setUserId(11L);
        r2.setCreditsEarned(10);
        r2.setTransactionType("CREDIT");
        rewardRepository.save(r2);

        int count = rewardRepository.countActiveUsersSince(LocalDateTime.now().minusHours(2));

        assertTrue(count >= 2);
    }

    @Test
    void should_countTotalUsersWithActivity_when_rewardsExist() {
        Reward r = new Reward();
        r.setUserId(20L);
        r.setCreditsEarned(10);
        r.setTransactionType("CREDIT");
        rewardRepository.save(r);

        int count = rewardRepository.countTotalUsersWithActivity();

        assertTrue(count >= 1);
    }

    @Test
    void should_returnZeroActiveUsers_when_noneExist() {
        int count = rewardRepository.countActiveUsersSince(LocalDateTime.now().plusDays(1));

        assertEquals(0, count);
    }
}
