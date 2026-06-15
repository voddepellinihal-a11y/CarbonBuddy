package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.repository.UtilityBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UtilityBillService {

    private final UtilityBillRepository utilityBillRepository;
    private final CarbonComputationEngine carbonEngine;

    public UtilityBillService(UtilityBillRepository utilityBillRepository,
                              CarbonComputationEngine carbonEngine) {
        this.utilityBillRepository = utilityBillRepository;
        this.carbonEngine = carbonEngine;
    }

    @Transactional
    public UtilityBill createUtilityBill(Long userId, UtilityBillRequest request) {
        UtilityBill bill = new UtilityBill();
        bill.setUserId(userId);
        bill.setTotalKwh(request.getTotalKwh());
        bill.setUtilityType(request.getUtilityType());
        bill.setBillingStart(LocalDate.parse(request.getBillingStart()));
        bill.setBillingEnd(LocalDate.parse(request.getBillingEnd()));
        bill.setAllocationCount(request.getAllocationCount());
        bill.setStatus("PROCESSED");
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
}
