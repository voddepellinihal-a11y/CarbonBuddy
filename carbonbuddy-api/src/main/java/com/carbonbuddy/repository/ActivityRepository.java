package com.carbonbuddy.repository;

import com.carbonbuddy.model.Activity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @EntityGraph(attributePaths = {})
    List<Activity> findByUserIdAndActivityStartBetweenOrderByActivityStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(a.distanceKm), 0) FROM Activity a WHERE a.userId = :userId " +
           "AND a.activityStart BETWEEN :start AND :end")
    double sumDistanceByUserIdAndPeriod(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query(value = "SELECT COUNT(*) FROM activities a WHERE a.user_id = :userId", nativeQuery = true)
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Activity a WHERE a.userId = :userId ORDER BY a.activityStart DESC")
    @EntityGraph(attributePaths = {})
    List<Activity> findRecentByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
