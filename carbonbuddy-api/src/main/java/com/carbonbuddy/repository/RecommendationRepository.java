package com.carbonbuddy.repository;

import com.carbonbuddy.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Recommendation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
