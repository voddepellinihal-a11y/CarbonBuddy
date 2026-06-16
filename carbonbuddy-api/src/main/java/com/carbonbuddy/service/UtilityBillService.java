package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.repository.UtilityBillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UtilityBillService {

    private static final Logger log = LoggerFactory.getLogger(UtilityBillService.class);

    private static final String STATUS_PROCESSED = "PROCESSED";
    private static final int MAX_INPUT_LENGTH = 255;

    private final UtilityBillRepository utilityBillRepository;
    private final CarbonComputationEngine carbonEngine;

    public UtilityBillService(UtilityBillRepository utilityBillRepository,
                              CarbonComputationEngine carbonEngine) {
        this.utilityBillRepository = utilityBillRepository;
        this.carbonEngine = carbonEngine;
    }

    @Transactional
    @CacheEvict(value = "dashboard", key = "#userId")
    public UtilityBill createUtilityBill(Long userId, UtilityBillRequest request) {
        log.debug("Creating utility bill for user {}", userId);

        UtilityBill bill = new UtilityBill();
        bill.setUserId(userId);
        bill.setTotalKwh(request.getTotalKwh());
        bill.setUtilityType(sanitizeInput(request.getUtilityType()));
        bill.setBillingStart(LocalDate.parse(request.getBillingStart()));
        bill.setBillingEnd(LocalDate.parse(request.getBillingEnd()));
        bill.setAllocationCount(request.getAllocationCount());
        bill.setStatus(STATUS_PROCESSED);
        bill = utilityBillRepository.save(bill);

        carbonEngine.computeUtilityCarbon(
                userId,
                request.getTotalKwh(),
                request.getAllocationCount(),
                bill.getBillingStart(),
                bill.getBillingEnd()
        );

        return bill;
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().substring(0, Math.min(input.trim().length(), MAX_INPUT_LENGTH));
    }
}
