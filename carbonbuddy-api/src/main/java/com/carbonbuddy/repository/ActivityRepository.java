package com.carbonbuddy.repository;

import com.carbonbuddy.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserIdAndActivityStartBetweenOrderByActivityStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(a.distanceKm), 0) FROM Activity a WHERE a.userId = :userId " +
           "AND a.activityStart BETWEEN :start AND :end")
    double sumDistanceByUserIdAndPeriod(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
