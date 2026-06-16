package com.carbonbuddy.service;

import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.CarbonRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine for computing carbon emissions across transport, utility, and food categories.
 * Uses pre-defined emission factors per km and per kWh.
 */
@Service
public class CarbonComputationEngine {

    private static final Map<String, Double> EMISSION_FACTORS_PER_KM = new HashMap<>();
    private static final double GRID_EMISSION_FACTOR_KG_PER_KWH = 0.82;
    private static final double DEFAULT_EMISSION_FACTOR = 0.100;
    private static final double DEFAULT_FOOD_FACTOR = 2.0;
    private static final double FOOD_PORTION_MULTIPLIER = 0.3;
    private static final double ZERO_EMISSION = 0.0;
    private static final double DISTANCE_KM_ZERO = 0.0;

    private static final String CATEGORY_TRANSPORT = "TRANSPORT";
    private static final String CATEGORY_UTILITY = "UTILITY";
    private static final String CATEGORY_FOOD = "FOOD";
    private static final String SOURCE_TYPE_ELECTRICITY = "electricity";

    private static final Map<String, Double> FOOD_EMISSION_FACTORS = new HashMap<>();

    static {
        EMISSION_FACTORS_PER_KM.put("METRO", 0.035);
        EMISSION_FACTORS_PER_KM.put("BUS", 0.089);
        EMISSION_FACTORS_PER_KM.put("BIKE", ZERO_EMISSION);
        EMISSION_FACTORS_PER_KM.put("WALK", ZERO_EMISSION);
        EMISSION_FACTORS_PER_KM.put("CAR_PETROL", 0.192);
        EMISSION_FACTORS_PER_KM.put("CAR_DIESEL", 0.171);
        EMISSION_FACTORS_PER_KM.put("SCOOTER_PETROL", 0.052);
        EMISSION_FACTORS_PER_KM.put("RIDESHARE", 0.085);
        EMISSION_FACTORS_PER_KM.put("AUTO", 0.070);
        EMISSION_FACTORS_PER_KM.put("OTHER", DEFAULT_EMISSION_FACTOR);

        FOOD_EMISSION_FACTORS.put("chicken", 6.9);
        FOOD_EMISSION_FACTORS.put("paneer", 2.8);
        FOOD_EMISSION_FACTORS.put("salad", 0.4);
        FOOD_EMISSION_FACTORS.put("vegetable", 0.5);
        FOOD_EMISSION_FACTORS.put("rice", 1.2);
        FOOD_EMISSION_FACTORS.put("mutton", 12.5);
        FOOD_EMISSION_FACTORS.put("fish", 5.0);
        FOOD_EMISSION_FACTORS.put("egg", 2.1);
        FOOD_EMISSION_FACTORS.put("milk", 1.9);
    }

    private final CarbonRecordRepository carbonRecordRepository;

    /**
     * Constructs the computation engine with the carbon record repository.
     *
     * @param carbonRecordRepository repository for persisting computed carbon records
     */
    public CarbonComputationEngine(CarbonRecordRepository carbonRecordRepository) {
        this.carbonRecordRepository = carbonRecordRepository;
    }

    /**
     * Computes carbon emissions for a transport activity based on transit mode and distance.
     *
     * @param userId      the user ID
     * @param transitMode the mode of transport (e.g. METRO, BUS, CAR_PETROL)
     * @param distanceKm  the distance traveled in kilometers
     * @param date        the date of the activity
     * @return the persisted {@link CarbonRecord}
     */
    public CarbonRecord computeTransportCarbon(Long userId, String transitMode,
                                                double distanceKm, LocalDate date) {
        String normalizedMode = transitMode.toUpperCase();
        double factor = EMISSION_FACTORS_PER_KM.getOrDefault(normalizedMode, DEFAULT_EMISSION_FACTOR);
        double carbonKg = distanceKm * factor;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory(CATEGORY_TRANSPORT);
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(distanceKm);
        record.setSourceType(transitMode);
        record.setRecordDate(date);

        return carbonRecordRepository.save(record);
    }

    /**
     * Computes carbon emissions for utility usage (electricity).
     * Divides total kWh by the number of occupants for per-person allocation.
     *
     * @param userId          the user ID
     * @param totalKwh        total electricity consumption in kWh
     * @param allocationCount number of occupants sharing the bill
     * @param start           the billing period start date
     * @param end             the billing period end date
     * @return the persisted {@link CarbonRecord}
     */
    public CarbonRecord computeUtilityCarbon(Long userId, double totalKwh,
                                              int allocationCount, LocalDate start, LocalDate end) {
        double perPersonKwh = totalKwh / allocationCount;
        double carbonKg = perPersonKwh * GRID_EMISSION_FACTOR_KG_PER_KWH;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory(CATEGORY_UTILITY);
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(DISTANCE_KM_ZERO);
        record.setSourceType(SOURCE_TYPE_ELECTRICITY);
        record.setRecordDate(end);

        return carbonRecordRepository.save(record);
    }

    /**
     * Computes carbon emissions for a food item based on its ingredient markers.
     *
     * @param userId     the user ID
     * @param itemMarker description or name of the food item
     * @param date       the date of consumption
     * @return the persisted {@link CarbonRecord}
     */
    public CarbonRecord computeFoodCarbon(Long userId, String itemMarker, LocalDate date) {
        double factor = FOOD_EMISSION_FACTORS.entrySet().stream()
                .filter(e -> itemMarker.toLowerCase().contains(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(DEFAULT_FOOD_FACTOR);
        double carbonKg = factor * FOOD_PORTION_MULTIPLIER;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory(CATEGORY_FOOD);
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(DISTANCE_KM_ZERO);
        record.setSourceType(itemMarker);
        record.setRecordDate(date);

        return carbonRecordRepository.save(record);
    }
}
