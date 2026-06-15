package com.carbonbuddy.repository;

import com.carbonbuddy.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    @Query("SELECT COALESCE(SUM(r.creditsEarned), 0) - COALESCE(SUM(r.creditsSpent), 0) " +
           "FROM Reward r WHERE r.userId = :userId")
    int getBalanceByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(r.creditsEarned), 0) FROM Reward r WHERE r.userId = :userId " +
           "AND r.createdAt >= :since")
    long sumEarnedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r WHERE r.createdAt >= :since")
    int countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r")
    int countTotalUsersWithActivity();

    @Query("SELECT COUNT(DISTINCT r.userId) FROM Reward r WHERE r.source = 'RECOMMENDATION' " +
           "AND r.sourceId = :recId")
    int countUsersCompletedRecommendation(@Param("recId") Long recId);

    List<Reward> findByUserIdAndTransactionType(Long userId, String transactionType);
}
