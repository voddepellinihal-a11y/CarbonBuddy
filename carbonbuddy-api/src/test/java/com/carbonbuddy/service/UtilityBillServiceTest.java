package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.repository.UtilityBillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilityBillServiceTest {

    @Mock
    private UtilityBillRepository utilityBillRepository;

    @Mock
    private CarbonComputationEngine carbonEngine;

    private UtilityBillService utilityBillService;

    @BeforeEach
    void setUp() {
        utilityBillService = new UtilityBillService(utilityBillRepository, carbonEngine);
    }

    @Test
    void should_createBill_when_validRequest() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(150.0);
        request.setUtilityType("electricity");
        request.setBillingStart("2025-01-01");
        request.setBillingEnd("2025-01-31");
        request.setAllocationCount(3);

        when(utilityBillRepository.save(any())).thenAnswer(i -> {
            UtilityBill b = i.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());

        UtilityBill result = utilityBillService.createUtilityBill(1L, request);

        assertNotNull(result);
        assertEquals(150.0, result.getTotalKwh());
        assertEquals("electricity", result.getUtilityType());
        assertEquals("PROCESSED", result.getStatus());
        assertEquals(3, result.getAllocationCount());
        assertEquals(LocalDate.of(2025, 1, 1), result.getBillingStart());
        assertEquals(LocalDate.of(2025, 1, 31), result.getBillingEnd());
    }

    @Test
    void should_computeCarbon_when_billIsCreated() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(200.0);
        request.setUtilityType("electricity");
        request.setBillingStart("2025-06-01");
        request.setBillingEnd("2025-06-30");
        request.setAllocationCount(4);

        when(utilityBillRepository.save(any())).thenAnswer(i -> {
            UtilityBill b = i.getArgument(0);
            b.setId(2L);
            return b;
        });
        CarbonRecord mockRecord = new CarbonRecord();
        when(carbonEngine.computeUtilityCarbon(eq(1L), eq(200.0), eq(4),
                eq(LocalDate.of(2025, 6, 1)), eq(LocalDate.of(2025, 6, 30))))
                .thenReturn(mockRecord);

        utilityBillService.createUtilityBill(1L, request);

        verify(carbonEngine).computeUtilityCarbon(1L, 200.0, 4,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));
    }

    @Test
    void should_setCorrectAllocationCount_when_requestProvided() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(100.0);
        request.setUtilityType("gas");
        request.setBillingStart("2025-03-01");
        request.setBillingEnd("2025-03-31");
        request.setAllocationCount(5);

        when(utilityBillRepository.save(any())).thenAnswer(i -> {
            UtilityBill b = i.getArgument(0);
            b.setId(3L);
            return b;
        });
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());

        UtilityBill result = utilityBillService.createUtilityBill(1L, request);

        assertEquals(5, result.getAllocationCount());
    }

    @Test
    void should_setUserId_when_creatingBill() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(50.0);
        request.setUtilityType("electricity");
        request.setBillingStart("2025-04-01");
        request.setBillingEnd("2025-04-30");
        request.setAllocationCount(1);

        when(utilityBillRepository.save(any())).thenAnswer(i -> {
            UtilityBill b = i.getArgument(0);
            b.setId(4L);
            return b;
        });
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());

        UtilityBill result = utilityBillService.createUtilityBill(42L, request);

        assertEquals(42L, result.getUserId());
    }

    @Test
    void should_setStatusAsProcessed_when_billCreated() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(300.0);
        request.setUtilityType("electricity");
        request.setBillingStart("2025-02-01");
        request.setBillingEnd("2025-02-28");
        request.setAllocationCount(2);

        when(utilityBillRepository.save(any())).thenAnswer(i -> {
            UtilityBill b = i.getArgument(0);
            b.setId(5L);
            return b;
        });
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());

        UtilityBill result = utilityBillService.createUtilityBill(1L, request);

        assertEquals("PROCESSED", result.getStatus());
    }

    @Test
    void should_returnSavedBill_when_repositoryReturnsIt() {
        UtilityBillRequest request = new UtilityBillRequest();
        request.setTotalKwh(120.0);
        request.setUtilityType("electricity");
        request.setBillingStart("2025-05-01");
        request.setBillingEnd("2025-05-31");
        request.setAllocationCount(1);

        UtilityBill savedBill = new UtilityBill();
        savedBill.setId(10L);
        savedBill.setUserId(1L);
        savedBill.setTotalKwh(120.0);
        when(utilityBillRepository.save(any())).thenReturn(savedBill);
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());

        UtilityBill result = utilityBillService.createUtilityBill(1L, request);

        assertEquals(10L, result.getId());
    }
}
