package com.carbonbuddy.service;

import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final CarbonRecordRepository carbonRecordRepository;
    private final RewardRepository rewardRepository;

    public RecommendationService(RecommendationRepository recommendationRepository,
                                 CarbonRecordRepository carbonRecordRepository,
                                 RewardRepository rewardRepository) {
        this.recommendationRepository = recommendationRepository;
        this.carbonRecordRepository = carbonRecordRepository;
        this.rewardRepository = rewardRepository;
    }

    public List<Recommendation> getUserRecommendations(Long userId) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void generateRecommendations(Long userId) {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        List<Object[]> categoryData = carbonRecordRepository.sumCarbonByCategory(userId, monthStart, today);
        String topCategory = categoryData.stream()
                .max(Comparator.comparingDouble(r -> ((Number) r[1]).doubleValue()))
                .map(r -> (String) r[0])
                .orElse("TRANSPORT");

        double topCarbon = categoryData.stream()
                .filter(r -> ((String) r[0]).equals(topCategory))
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .findFirst().orElse(0);

        saveRecommendationForCategory(userId, topCategory, topCarbon);
    }

    public void saveRecommendationForCategory(Long userId, String category, double carbonKg) {
        Recommendation rec = new Recommendation();
        rec.setUserId(userId);
        rec.setCategory(category);
        rec.setStatus("PENDING");

        switch (category) {
            case "TRANSPORT":
                if (carbonKg > 100) {
                    rec.setTitle("Switch to Metro for your daily commute");
                    rec.setDescription("Your transport emissions are high. Hyderabad Metro reduces your carbon impact by 80% compared to driving. 🚇");
                    rec.setEstimatedSavingsKg(Math.round(carbonKg * 0.6 * 100.0) / 100.0);
                    rec.setEstimatedSavingsPercent(60.0);
                } else {
                    rec.setTitle("Try carpooling this week");
                    rec.setDescription("Sharing rides with colleagues cuts commute emissions by 75%. Find a carpool buddy!");
                    rec.setEstimatedSavingsKg(Math.round(carbonKg * 0.5 * 100.0) / 100.0);
                    rec.setEstimatedSavingsPercent(50.0);
                }
                break;
            case "FOOD":
                rec.setTitle("Try a plant-based meal today");
                rec.setDescription("Swapping one meat meal for a plant-based option saves ~2.5kg CO₂. Your food footprint can drop 30%!");
                rec.setEstimatedSavingsKg(Math.round(carbonKg * 0.3 * 100.0) / 100.0);
                rec.setEstimatedSavingsPercent(30.0);
                break;
            case "UTILITY":
                rec.setTitle("Reduce AC usage by 1 hour daily");
                rec.setDescription("Running your AC one hour less each day cuts 30% from your electricity carbon footprint. Set to 24°C for max savings!");
                rec.setEstimatedSavingsKg(Math.round(carbonKg * 0.3 * 100.0) / 100.0);
                rec.setEstimatedSavingsPercent(30.0);
                break;
            default:
                rec.setTitle("Walk or bike for short trips");
                rec.setDescription("Replace car trips under 2km with walking or cycling — zero emissions and great for health! 🚶‍♂️");
                rec.setEstimatedSavingsKg(12.0);
                rec.setEstimatedSavingsPercent(8.0);
        }

        recommendationRepository.save(rec);
    }

    @Transactional
    public Recommendation completeRecommendation(Long userId, Long recommendationId) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));

        if (!rec.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Recommendation does not belong to user");
        }

        rec.setStatus("COMPLETED");
        rec.setCompletedAt(LocalDateTime.now());
        rec = recommendationRepository.save(rec);

        int bonusPoints = (int) Math.round(rec.getEstimatedSavingsKg() * 15);
        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsEarned(bonusPoints);
        reward.setSource("RECOMMENDATION");
        reward.setSourceId(rec.getId());
        reward.setTransactionType("CREDIT");
        rewardRepository.save(reward);

        return rec;
    }
}
