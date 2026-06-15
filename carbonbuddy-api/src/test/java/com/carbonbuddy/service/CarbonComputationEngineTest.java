package com.carbonbuddy.service;

import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.CarbonRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarbonComputationEngineTest {

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    private CarbonComputationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CarbonComputationEngine(carbonRecordRepository);
    }

    @Test
    void computeTransportCarbon_shouldReturnRecord() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 10.0, LocalDate.now());

        assertNotNull(record);
        assertEquals(1L, record.getUserId());
        assertEquals("TRANSPORT", record.getCategory());
        assertEquals(10.0, record.getDistanceKm());
        assertTrue(record.getCarbonKg() > 0);
    }

    @Test
    void computeTransportCarbon_shouldHandleZeroDistance() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 0.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void computeTransportCarbon_shouldHandleUnknownMode() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "UNKNOWN_MODE", 10.0, LocalDate.now());

        assertTrue(record.getCarbonKg() > 0);
    }

    @Test
    void computeUtilityCarbon_shouldReturnRecord() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1, LocalDate.now().minusDays(30), LocalDate.now());

        assertNotNull(record);
        assertEquals("UTILITY", record.getCategory());
        assertTrue(record.getCarbonKg() > 0);
    }

    @Test
    void computeUtilityCarbon_shouldSplitByAllocation() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord single = engine.computeUtilityCarbon(1L, 100.0, 1, LocalDate.now().minusDays(30), LocalDate.now());
        CarbonRecord shared = engine.computeUtilityCarbon(1L, 100.0, 4, LocalDate.now().minusDays(30), LocalDate.now());

        assertTrue(single.getCarbonKg() > shared.getCarbonKg());
    }
}
