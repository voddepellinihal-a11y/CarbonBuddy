package com.carbonbuddy.service;

import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.CarbonRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarbonComputationEngineParamTest {

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    private CarbonComputationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CarbonComputationEngine(carbonRecordRepository);
        lenient().when(carbonRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @ParameterizedTest
    @CsvSource({
            "METRO, 10.0, 0.35",
            "BUS, 10.0, 0.89",
            "BIKE, 10.0, 0.0",
            "WALK, 10.0, 0.0",
            "CAR_PETROL, 10.0, 1.92",
            "CAR_DIESEL, 10.0, 1.71",
            "SCOOTER_PETROL, 10.0, 0.52",
            "RIDESHARE, 10.0, 0.85",
            "AUTO, 10.0, 0.70",
            "OTHER, 10.0, 1.0"
    })
    void should_computeCorrectCarbon_when_knownTransitMode(String mode, double distance, double expectedCarbon) {
        CarbonRecord record = engine.computeTransportCarbon(1L, mode, distance, LocalDate.now());

        assertEquals(expectedCarbon, record.getCarbonKg(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 5.0, 10.0, 50.0, 100.0, 500.0, 1000.0})
    void should_scaleLinearly_when_distanceChanges(double distance) {
        CarbonRecord record10 = engine.computeTransportCarbon(1L, "BUS", 10.0, LocalDate.now());
        CarbonRecord recordN = engine.computeTransportCarbon(1L, "BUS", distance, LocalDate.now());

        double ratio = recordN.getCarbonKg() / record10.getCarbonKg();
        assertEquals(distance / 10.0, ratio, 0.001);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.001, 0.01})
    void should_handleSmallDistances_when_provided(double distance) {
        CarbonRecord record = engine.computeTransportCarbon(1L, "METRO", distance, LocalDate.now());

        assertTrue(record.getCarbonKg() >= 0);
    }

    @Test
    void should_returnZeroCarbon_when_distanceIsZero() {
        CarbonRecord record = engine.computeTransportCarbon(1L, "BUS", 0.0, LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_throwNPE_when_transitModeIsNull() {
        assertThrows(NullPointerException.class,
                () -> engine.computeTransportCarbon(1L, null, 10.0, LocalDate.now()));
    }

    @Test
    void should_handleEmptyTransitMode() {
        CarbonRecord record = engine.computeTransportCarbon(1L, "", 10.0, LocalDate.now());

        assertEquals(1.0, record.getCarbonKg());
    }

    @Test
    void should_handleLargeDistance() {
        CarbonRecord record = engine.computeTransportCarbon(1L, "CAR_PETROL", 100000.0, LocalDate.now());

        assertEquals(19200.0, record.getCarbonKg(), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "100.0, 1, 82.0",
            "100.0, 2, 41.0",
            "100.0, 4, 20.5",
            "100.0, 5, 16.4",
            "200.0, 1, 164.0",
            "200.0, 4, 41.0"
    })
    void should_splitByAllocation_when_utilityComputed(double kwh, int count, double expectedCarbon) {
        CarbonRecord record = engine.computeUtilityCarbon(1L, kwh, count,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(expectedCarbon, record.getCarbonKg(), 0.001);
    }

    @Test
    void should_setZeroDistance_when_computingUtility() {
        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(0.0, record.getDistanceKm());
    }

    @Test
    void should_setCategoryUtility_when_computingUtility() {
        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 2,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals("UTILITY", record.getCategory());
    }

    @Test
    void should_setSourceTypeElectricity_when_computingUtility() {
        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals("electricity", record.getSourceType());
    }

    @Test
    void should_handleZeroKwh_when_computingUtility() {
        CarbonRecord record = engine.computeUtilityCarbon(1L, 0.0, 1,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(0.0, record.getCarbonKg());
    }

    @Test
    void should_returnInfinity_when_allocationCountIsZero() {
        CarbonRecord record = engine.computeUtilityCarbon(1L, 100.0, 0,
                LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(Double.POSITIVE_INFINITY, record.getCarbonKg());
    }

    @ParameterizedTest
    @CsvSource({
            "chicken curry, 2.07",
            "paneer tikka, 0.84",
            "fresh salad, 0.12",
            "vegetable biryani, 0.15",
            "rice bowl, 0.36",
            "mutton biryani, 3.75",
            "fish fry, 1.5",
            "egg curry, 0.63",
            "milk tea, 0.57",
            "pasta, 0.6"
    })
    void should_computeCorrectFoodCarbon_when_itemProvided(String item, double expectedCarbon) {
        CarbonRecord record = engine.computeFoodCarbon(1L, item, LocalDate.now());

        assertEquals(expectedCarbon, record.getCarbonKg(), 0.001);
    }

    @Test
    void should_setCategoryFood_when_computingFoodCarbon() {
        CarbonRecord record = engine.computeFoodCarbon(1L, "chicken", LocalDate.now());

        assertEquals("FOOD", record.getCategory());
    }

    @Test
    void should_handleEmptyFoodItem() {
        CarbonRecord record = engine.computeFoodCarbon(1L, "", LocalDate.now());

        assertEquals(0.6, record.getCarbonKg());
    }

    @Test
    void should_throwNPE_when_foodItemIsNull() {
        assertThrows(NullPointerException.class,
                () -> engine.computeFoodCarbon(1L, null, LocalDate.now()));
    }

    @Test
    void should_setUserId_when_computingFoodCarbon() {
        CarbonRecord record = engine.computeFoodCarbon(42L, "chicken", LocalDate.now());

        assertEquals(42L, record.getUserId());
    }

    @Test
    void should_setRecordDate_when_computingFoodCarbon() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        CarbonRecord record = engine.computeFoodCarbon(1L, "chicken", date);

        assertEquals(date, record.getRecordDate());
    }
}
