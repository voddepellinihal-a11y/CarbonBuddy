package com.carbonbuddy.repository;

import com.carbonbuddy.model.CarbonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link CarbonRecord} entity persistence.
 * Provides aggregate queries for carbon summaries by period and category.
 */
@Repository
public interface CarbonRecordRepository extends JpaRepository<CarbonRecord, Long> {

    /**
     * Finds carbon records for a user within a date range.
     *
     * @param userId the user ID
     * @param start  the start date (inclusive)
     * @param end    the end date (inclusive)
     * @return a list of carbon records ordered by date descending
     */
    List<CarbonRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId, LocalDate start, LocalDate end);

    /**
     * Sums total carbon emissions for a user within a date range.
     *
     * @param userId the user ID
     * @param start  the start date
     * @param end    the end date
     * @return the total carbon in kg, or 0 if no records
     */
    @Query("SELECT COALESCE(SUM(c.carbonKg), 0) FROM CarbonRecord c WHERE c.userId = :userId " +
           "AND c.recordDate BETWEEN :start AND :end")
    double sumCarbonByUserIdAndPeriod(@Param("userId") Long userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    /**
     * Aggregates carbon emissions by category for a user within a date range.
     *
     * @param userId the user ID
     * @param start  the start date
     * @param end    the end date
     * @return a list of Object arrays where [0] is category and [1] is total carbon
     */
    @Query("SELECT c.category, SUM(c.carbonKg) FROM CarbonRecord c WHERE c.userId = :userId " +
           "AND c.recordDate BETWEEN :start AND :end GROUP BY c.category")
    List<Object[]> sumCarbonByCategory(@Param("userId") Long userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    /**
     * Calculates the average carbon emission across all users for a specific date.
     *
     * @param date the date to average
     * @return the average carbon in kg, or 0 if no records
     */
    @Query("SELECT COALESCE(AVG(c.carbonKg), 0) FROM CarbonRecord c WHERE c.recordDate = :date")
    double avgCarbonByDate(@Param("date") LocalDate date);
}
