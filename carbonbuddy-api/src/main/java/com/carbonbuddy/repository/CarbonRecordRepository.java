package com.carbonbuddy.repository;

import com.carbonbuddy.model.CarbonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
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

    @Query("SELECT c FROM CarbonRecord c WHERE c.userId = :userId ORDER BY c.recordDate DESC")
    List<CarbonRecord> findPagedByUserId(@Param("userId") Long userId,
                                          org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM carbon_records cr WHERE cr.user_id = :userId",
           countQuery = "SELECT COUNT(*) FROM carbon_records cr WHERE cr.user_id = :userId",
           nativeQuery = true)
    long countByUserId(@Param("userId") Long userId);
}
