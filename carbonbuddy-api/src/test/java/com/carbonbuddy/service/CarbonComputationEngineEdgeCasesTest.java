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
class CarbonComputationEngineEdgeCasesTest {

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    private CarbonComputationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CarbonComputationEngine(carbonRecordRepository);
    }

    @Test
    void should_returnZeroCarbon_when_distanceIsZero() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "METRO", 0.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_returnZeroCarbon_when_distanceIsZeroForBus() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 0.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_handleLargeDistance_when_provided() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "CAR_PETROL", 10000.0, LocalDate.now());

        assertTrue(record.getCarbonKg() > 0);
        assertEquals(10000.0, record.getDistanceKm());
    }

    @Test
    void should_handleFractionalDistance_when_provided() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 0.001, LocalDate.now());

        assertTrue(record.getCarbonKg() > 0);
    }

    @Test
    void should_throwNPE_when_transitModeIsNull() {
        assertThrows(NullPointerException.class,
                () -> engine.computeTransportCarbon(1L, null, 10.0, LocalDate.now()));
    }

    @Test
    void should_handleCaseInsensitiveMode_when_lowercaseProvided() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "metro", 10.0, LocalDate.now());

        assertEquals(0.35, record.getCarbonKg(), 0.0001);
    }

    @Test
    void should_useDefaultFactor_when_unknownMode() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "HOVERBOARD", 10.0, LocalDate.now());

        assertEquals(1.0, record.getCarbonKg());
    }

    @Test
    void should_returnZeroCarbon_when_walkMode() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "WALK", 5.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_returnZeroCarbon_when_bikeMode() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BIKE", 8.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_calculateMetroEmission_when_modeIsMetro() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "METRO", 10.0, LocalDate.now());

        assertEquals(0.35, record.getCarbonKg(), 0.0001);
    }

    @Test
    void should_calculateBusEmission_when_modeIsBus() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 10.0, LocalDate.now());

        assertEquals(0.89, record.getCarbonKg(), 0.0001);
    }

    @Test
    void should_calculateCarPetrolEmission_when_modeIsCarPetrol() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "CAR_PETROL", 10.0, LocalDate.now());

        assertEquals(1.92, record.getCarbonKg(), 0.0001);
    }

    @Test
    void should_setUserId_when_computingTransport() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(42L, "METRO", 10.0, LocalDate.now());

        assertEquals(42L, record.getUserId());
    }

    @Test
    void should_setCategoryAsTransport_when_computingTransport() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 10.0, LocalDate.now());

        assertEquals("TRANSPORT", record.getCategory());
    }

    @Test
    void should_setRecordDate_when_computingTransport() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeTransportCarbon(1L, "METRO", 10.0, date);

        assertEquals(date, record.getRecordDate());
    }

    @Test
    void should_calculateSinglePersonUtility_when_allocationCountIsOne() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(82.0, record.getCarbonKg());
    }

    @Test
    void should_splitUtilityCarbon_when_allocationCountIsMultiple() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 4,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(20.5, record.getCarbonKg());
    }

    @Test
    void should_handleLargeAllocationCount_when_provided() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 100,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertTrue(record.getCarbonKg() > 0);
        assertTrue(record.getCarbonKg() < 1.0);
    }

    @Test
    void should_setCategoryAsUtility_when_computingUtility() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 2,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals("UTILITY", record.getCategory());
    }

    @Test
    void should_setZeroDistance_when_computingUtility() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(0.0, record.getDistanceKm());
    }

    @Test
    void should_computeFoodCarbon_when_chickenMeal() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeFoodCarbon(1L, "chicken curry", LocalDate.now());

        assertEquals(2.07, record.getCarbonKg());
        assertEquals("FOOD", record.getCategory());
    }

    @Test
    void should_computeFoodCarbon_when_salad() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeFoodCarbon(1L, "fresh salad", LocalDate.now());

        assertEquals(0.12, record.getCarbonKg());
    }

    @Test
    void should_useDefaultFactor_when_unknownFoodItem() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeFoodCarbon(1L, "pasta", LocalDate.now());

        assertEquals(0.6, record.getCarbonKg());
    }

    @Test
    void should_throwNPE_when_foodItemIsNull() {
        assertThrows(NullPointerException.class,
                () -> engine.computeFoodCarbon(1L, null, LocalDate.now()));
    }

    @Test
    void should_handleEmptyFoodItem_when_provided() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeFoodCarbon(1L, "", LocalDate.now());

        assertEquals(0.6, record.getCarbonKg());
    }

    @Test
    void should_setUserId_when_computingUtility() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(42L, 100.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(42L, record.getUserId());
    }

    @Test
    void should_setEndDate_when_computingUtilityRecordDate() {
        LocalDate end = LocalDate.of(2025, 6, 30);
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1,
                LocalDate.of(2025, 6, 1), end);

        assertEquals(end, record.getRecordDate());
    }

    @Test
    void should_handleVerySmallKwh_when_computingUtility() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 0.001, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertTrue(record.getCarbonKg() > 0);
    }

    @Test
    void should_handleZeroKwh_when_computingUtility() {
        when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CarbonRecord record = engine.computeUtilityCarbon(1L, 0.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }
}
