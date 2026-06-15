package com.carbonbuddy.repository;

import com.carbonbuddy.model.CarbonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CarbonRecordRepository extends JpaRepository<CarbonRecord, Long> {
    List<CarbonRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(c.carbonKg), 0) FROM CarbonRecord c WHERE c.userId = :userId " +
           "AND c.recordDate BETWEEN :start AND :end")
    double sumCarbonByUserIdAndPeriod(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    @Query("SELECT c.category, SUM(c.carbonKg) FROM CarbonRecord c WHERE c.userId = :userId " +
           "AND c.recordDate BETWEEN :start AND :end GROUP BY c.category")
    List<Object[]> sumCarbonByCategory(@Param("userId") Long userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("SELECT COALESCE(AVG(c.carbonKg), 0) FROM CarbonRecord c WHERE c.recordDate = :date")
    double avgCarbonByDate(@Param("date") LocalDate date);
}
