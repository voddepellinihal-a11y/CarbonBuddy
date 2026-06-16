package com.carbonbuddy.service;

import com.carbonbuddy.model.Recommendation;
import com.carbonbuddy.model.Reward;
import com.carbonbuddy.repository.CarbonRecordRepository;
import com.carbonbuddy.repository.RecommendationRepository;
import com.carbonbuddy.repository.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service for generating, managing, and completing carbon-reduction recommendations.
 * Analyzes user's top emission category and provides targeted suggestions.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final String DEFAULT_CATEGORY = "TRANSPORT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final double HIGH_EMISSION_THRESHOLD = 100.0;
    private static final double TRANSPORT_HIGH_SAVINGS_PERCENT = 0.6;
    private static final double TRANSPORT_LOW_SAVINGS_PERCENT = 0.5;
    private static final double FOOD_SAVINGS_PERCENT = 0.3;
    private static final double UTILITY_SAVINGS_PERCENT = 0.3;
    private static final double WALK_DEFAULT_SAVINGS_KG = 12.0;
    private static final double WALK_DEFAULT_SAVINGS_PERCENT = 8.0;
    private static final double BONUS_POINTS_MULTIPLIER = 15.0;
    private static final double ROUNDING_FACTOR = 100.0;
    private static final String SOURCE_RECOMMENDATION = "RECOMMENDATION";
    private static final String TRANSACTION_CREDIT = "CREDIT";

    private final RecommendationRepository recommendationRepository;
    private final CarbonRecordRepository carbonRecordRepository;
    private final RewardRepository rewardRepository;

    /**
     * Constructs RecommendationService with required repositories.
     *
     * @param recommendationRepository repository for recommendations
     * @param carbonRecordRepository   repository for carbon records
     * @param rewardRepository         repository for rewards
     */
    public RecommendationService(RecommendationRepository recommendationRepository,
                                 CarbonRecordRepository carbonRecordRepository,
                                 RewardRepository rewardRepository) {
        this.recommendationRepository = recommendationRepository;
        this.carbonRecordRepository = carbonRecordRepository;
        this.rewardRepository = rewardRepository;
    }

    /**
     * Retrieves all recommendations for a user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return an unmodifiable list of recommendations
     */
    public List<Recommendation> getUserRecommendations(Long userId) {
        return Collections.unmodifiableList(
                recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /**
     * Analyzes the user's top emission category for the current month
     * and generates a targeted recommendation.
     *
     * @param userId the user ID
     */
    public void generateRecommendations(Long userId) {
        log.debug("Generating recommendations for user {}", userId);

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        List<Object[]> categoryData = carbonRecordRepository.sumCarbonByCategory(userId, monthStart, today);
        String topCategory = categoryData.stream()
                .max(Comparator.comparingDouble(r -> ((Number) r[1]).doubleValue()))
                .map(r -> (String) r[0])
                .orElse(DEFAULT_CATEGORY);

        double topCarbon = categoryData.stream()
                .filter(r -> ((String) r[0]).equals(topCategory))
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .findFirst().orElse(0);

        saveRecommendationForCategory(userId, topCategory, topCarbon);
    }

    /**
     * Saves a category-specific recommendation for a user.
     *
     * @param userId   the user ID
     * @param category the emission category
     * @param carbonKg the total carbon kg for the category
     */
    public void saveRecommendationForCategory(Long userId, String category, double carbonKg) {
        Recommendation rec = new Recommendation();
        rec.setUserId(userId);
        rec.setCategory(category);
        rec.setStatus(STATUS_PENDING);

        switch (category) {
            case "TRANSPORT":
                if (carbonKg > HIGH_EMISSION_THRESHOLD) {
                    rec.setTitle("Switch to Metro for your daily commute");
                    rec.setDescription("Your transport emissions are high. Hyderabad Metro reduces your carbon impact by 80% compared to driving.");
                    rec.setEstimatedSavingsKg(Math.round(carbonKg * TRANSPORT_HIGH_SAVINGS_PERCENT * ROUNDING_FACTOR) / ROUNDING_FACTOR);
                    rec.setEstimatedSavingsPercent(60.0);
                } else {
                    rec.setTitle("Try carpooling this week");
                    rec.setDescription("Sharing rides with colleagues cuts commute emissions by 75%. Find a carpool buddy!");
                    rec.setEstimatedSavingsKg(Math.round(carbonKg * TRANSPORT_LOW_SAVINGS_PERCENT * ROUNDING_FACTOR) / ROUNDING_FACTOR);
                    rec.setEstimatedSavingsPercent(50.0);
                }
                break;
            case "FOOD":
                rec.setTitle("Try a plant-based meal today");
                rec.setDescription("Swapping one meat meal for a plant-based option saves ~2.5kg CO2. Your food footprint can drop 30%!");
                rec.setEstimatedSavingsKg(Math.round(carbonKg * FOOD_SAVINGS_PERCENT * ROUNDING_FACTOR) / ROUNDING_FACTOR);
                rec.setEstimatedSavingsPercent(30.0);
                break;
            case "UTILITY":
                rec.setTitle("Reduce AC usage by 1 hour daily");
                rec.setDescription("Running your AC one hour less each day cuts 30% from your electricity carbon footprint. Set to 24C for max savings!");
                rec.setEstimatedSavingsKg(Math.round(carbonKg * UTILITY_SAVINGS_PERCENT * ROUNDING_FACTOR) / ROUNDING_FACTOR);
                rec.setEstimatedSavingsPercent(30.0);
                break;
            default:
                rec.setTitle("Walk or bike for short trips");
                rec.setDescription("Replace car trips under 2km with walking or cycling - zero emissions and great for health!");
                rec.setEstimatedSavingsKg(WALK_DEFAULT_SAVINGS_KG);
                rec.setEstimatedSavingsPercent(WALK_DEFAULT_SAVINGS_PERCENT);
        }

        recommendationRepository.save(rec);
    }

    /**
     * Marks a recommendation as completed and awards bonus points to the user.
     *
     * @param userId           the user ID
     * @param recommendationId the recommendation ID
     * @return the completed {@link Recommendation}
     * @throws IllegalArgumentException if the recommendation is not found or does not belong to the user
     */
    @Transactional
    public Recommendation completeRecommendation(Long userId, Long recommendationId) {
        log.debug("Completing recommendation {} for user {}", recommendationId, userId);

        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));

        if (!rec.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Recommendation does not belong to user");
        }

        rec.setStatus(STATUS_COMPLETED);
        rec.setCompletedAt(LocalDateTime.now());
        rec = recommendationRepository.save(rec);

        int bonusPoints = (int) Math.round(rec.getEstimatedSavingsKg() * BONUS_POINTS_MULTIPLIER);
        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setCreditsEarned(bonusPoints);
        reward.setSource(SOURCE_RECOMMENDATION);
        reward.setSourceId(rec.getId());
        reward.setTransactionType(TRANSACTION_CREDIT);
        rewardRepository.save(reward);

        return rec;
    }
}
