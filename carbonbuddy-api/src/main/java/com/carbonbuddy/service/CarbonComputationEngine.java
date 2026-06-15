package com.carbonbuddy.service;

import com.carbonbuddy.model.CarbonRecord;
import com.carbonbuddy.repository.CarbonRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarbonComputationEngine {

    private static final Map<String, Double> EMISSION_FACTORS_PER_KM = new HashMap<>();
    private static final double GRID_EMISSION_FACTOR_KG_PER_KWH = 0.82;
    private static final Map<String, Double> FOOD_EMISSION_FACTORS = new HashMap<>();

    static {
        EMISSION_FACTORS_PER_KM.put("METRO", 0.035);
        EMISSION_FACTORS_PER_KM.put("BUS", 0.089);
        EMISSION_FACTORS_PER_KM.put("BIKE", 0.0);
        EMISSION_FACTORS_PER_KM.put("WALK", 0.0);
        EMISSION_FACTORS_PER_KM.put("CAR_PETROL", 0.192);
        EMISSION_FACTORS_PER_KM.put("CAR_DIESEL", 0.171);
        EMISSION_FACTORS_PER_KM.put("SCOOTER_PETROL", 0.052);
        EMISSION_FACTORS_PER_KM.put("RIDESHARE", 0.085);
        EMISSION_FACTORS_PER_KM.put("AUTO", 0.070);
        EMISSION_FACTORS_PER_KM.put("OTHER", 0.100);

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

    public CarbonComputationEngine(CarbonRecordRepository carbonRecordRepository) {
        this.carbonRecordRepository = carbonRecordRepository;
    }

    public CarbonRecord computeTransportCarbon(Long userId, String transitMode,
                                                double distanceKm, LocalDate date) {
        double factor = EMISSION_FACTORS_PER_KM.getOrDefault(transitMode.toUpperCase(), 0.100);
        double carbonKg = distanceKm * factor;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory("TRANSPORT");
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(distanceKm);
        record.setSourceType(transitMode);
        record.setRecordDate(date);

        return carbonRecordRepository.save(record);
    }

    public CarbonRecord computeUtilityCarbon(Long userId, double totalKwh,
                                              int allocationCount, LocalDate start, LocalDate end) {
        double perPersonKwh = totalKwh / allocationCount;
        double carbonKg = perPersonKwh * GRID_EMISSION_FACTOR_KG_PER_KWH;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory("UTILITY");
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(0.0);
        record.setSourceType("electricity");
        record.setRecordDate(end);

        return carbonRecordRepository.save(record);
    }

    public CarbonRecord computeFoodCarbon(Long userId, String itemMarker, LocalDate date) {
        double factor = FOOD_EMISSION_FACTORS.entrySet().stream()
                .filter(e -> itemMarker.toLowerCase().contains(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(2.0);
        double carbonKg = factor * 0.3;

        CarbonRecord record = new CarbonRecord();
        record.setUserId(userId);
        record.setCategory("FOOD");
        record.setCarbonKg(carbonKg);
        record.setDistanceKm(0.0);
        record.setSourceType(itemMarker);
        record.setRecordDate(date);

        return carbonRecordRepository.save(record);
    }
}
