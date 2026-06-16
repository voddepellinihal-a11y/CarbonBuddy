package com.carbonbuddy.repository;

import com.carbonbuddy.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Recommendation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Recommendation> findPagedByUserId(@Param("userId") Long userId,
                                            org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM recommendations r WHERE r.user_id = :userId AND r.status = :status",
           countQuery = "SELECT COUNT(*) FROM recommendations r WHERE r.user_id = :userId AND r.status = :status",
           nativeQuery = true)
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
