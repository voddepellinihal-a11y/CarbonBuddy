package com.carbonbuddy.repository;

import com.carbonbuddy.model.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    List<UtilityBill> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT u FROM UtilityBill u WHERE u.userId = :userId ORDER BY u.createdAt DESC")
    List<UtilityBill> findPagedByUserId(@Param("userId") Long userId,
                                         org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM utility_bills ub WHERE ub.user_id = :userId",
           countQuery = "SELECT COUNT(*) FROM utility_bills ub WHERE ub.user_id = :userId",
           nativeQuery = true)
    long countByUserId(@Param("userId") Long userId);
}
