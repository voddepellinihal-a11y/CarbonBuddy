package com.carbonbuddy.repository;

import com.carbonbuddy.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link Activity} entity persistence.
 * Provides queries for user activity history and distance aggregation.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Finds activities for a user within a date range, ordered by start time descending.
     *
     * @param userId the user ID
     * @param start  the start of the date range
     * @param end    the end of the date range
     * @return a list of matching activities
     */
    List<Activity> findByUserIdAndActivityStartBetweenOrderByActivityStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Sums the distance traveled by a user within a date range.
     *
     * @param userId the user ID
     * @param start  the start of the date range
     * @param end    the end of the date range
     * @return the total distance in km, or 0 if no records
     */
    @Query("SELECT COALESCE(SUM(a.distanceKm), 0) FROM Activity a WHERE a.userId = :userId " +
           "AND a.activityStart BETWEEN :start AND :end")
    double sumDistanceByUserIdAndPeriod(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
