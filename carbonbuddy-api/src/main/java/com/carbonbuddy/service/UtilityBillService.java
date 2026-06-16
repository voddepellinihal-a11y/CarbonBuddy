package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.repository.UtilityBillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for creating and processing utility bills.
 * Computes carbon emissions from electricity consumption.
 */
@Service
public class UtilityBillService {

    private static final Logger log = LoggerFactory.getLogger(UtilityBillService.class);

    private static final String STATUS_PROCESSED = "PROCESSED";
    private static final int MAX_INPUT_LENGTH = 255;

    private final UtilityBillRepository utilityBillRepository;
    private final CarbonComputationEngine carbonEngine;

    /**
     * Constructs UtilityBillService with required dependencies.
     *
     * @param utilityBillRepository the utility bill repository
     * @param carbonEngine          the carbon computation engine
     */
    public UtilityBillService(UtilityBillRepository utilityBillRepository,
                              CarbonComputationEngine carbonEngine) {
        this.utilityBillRepository = utilityBillRepository;
        this.carbonEngine = carbonEngine;
    }

    /**
     * Creates a new utility bill and computes its carbon emissions.
     *
     * @param userId  the user ID
     * @param request the utility bill request
     * @return the persisted {@link UtilityBill}
     */
    @Transactional
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

    /**
     * Sanitizes a string input by trimming and limiting length.
     *
     * @param input the raw string
     * @return the sanitized string, or null if input is null
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().substring(0, Math.min(input.trim().length(), MAX_INPUT_LENGTH));
    }
}
