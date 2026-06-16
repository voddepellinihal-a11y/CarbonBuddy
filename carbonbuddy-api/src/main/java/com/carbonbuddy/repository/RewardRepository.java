package com.carbonbuddy.repository;

import com.carbonbuddy.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link Reward} entity persistence.
 * Provides aggregate queries for balances, earned points, and active user counts.
 */
@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

    /**
     * Calculates the net balance (earned - spent) for a user.
     *
     * @param userId the user ID
     * @return the net balance in CarbonCoins
     */
    @Query("SELECT COALESCE(SUM(r.creditsEarned), 0) - COALESCE(SUM(r.creditsSpent), 0) " +
           "FROM Reward r WHERE r.userId = :userId")
    int getBalanceByUserId(@Param("userId") Long userId);

    /**
     * Sums credits earned by a user since a given timestamp.
     *
     * @param userId the user ID
     * @param since  the cutoff timestamp
     * @return the total credits earned
     */
    @Query("SELECT COALESCE(SUM(r.creditsEarned), 0) FROM Reward r WHERE r.userId = :userId " +
           "AND r.createdAt >= :since")
    long sumEarnedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Counts distinct users who have had activity since a given timestamp.
     *
     * @param since the cutoff timestamp
     * @return the number of active users
     */
    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r WHERE r.createdAt >= :since")
    int countActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Counts distinct users who have any reward record.
     *
     * @return the count of users with activity
     */
    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r")
    int countTotalUsersWithActivity();

    /**
     * Counts distinct users who completed a specific recommendation.
     *
     * @param recId the recommendation ID
     * @return the number of users who completed it
     */
    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r WHERE r.source = 'RECOMMENDATION' " +
           "AND r.sourceId = :recId")
    int countUsersCompletedRecommendation(@Param("recId") Long recId);

    /**
     * Returns rewards for a user filtered by transaction type.
     *
     * @param userId          the user ID
     * @param transactionType the type (CREDIT or DEBIT)
     * @return a list of matching rewards
     */
    List<Reward> findByUserIdAndTransactionType(Long userId, String transactionType);
}
