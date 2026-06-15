package com.carbonbuddy.repository;

import com.carbonbuddy.model.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {
    List<UtilityBill> findByUserIdOrderByCreatedAtDesc(Long userId);
}
