package com.carbonbuddy.repository;

import com.carbonbuddy.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Recommendation} entity persistence.
 * Provides queries for user-specific recommendation lookups.
 */
@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /**
     * Returns all recommendations for a user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return a list of recommendations
     */
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Returns recommendations for a user filtered by status, ordered by creation date descending.
     *
     * @param userId the user ID
     * @param status the recommendation status (e.g. PENDING, COMPLETED)
     * @return a list of matching recommendations
     */
    List<Recommendation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
