package com.carbonbuddy.repository;

import com.carbonbuddy.model.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link UtilityBill} entity persistence.
 * Provides user-specific utility bill lookups.
 */
@Repository
public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    /**
     * Returns all utility bills for a user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return a list of utility bills
     */
    List<UtilityBill> findByUserIdOrderByCreatedAtDesc(Long userId);
}
