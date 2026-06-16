package com.carbonbuddy.service;

import com.carbonbuddy.model.*;
import com.carbonbuddy.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SeedDataService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    private static final int MIN_SEED_USER_COUNT = 1;
    private static final int DEFAULT_ALLOCATION_COUNT = 1;
    private static final double ZERO_EMISSION = 0.0;
    private static final String SOURCE_TYPE_PREFIX = "ACTIVITY_";
    private static final String UTILITY_TYPE_ELECTRICITY = "electricity";
    private static final String STATUS_PROCESSED = "PROCESSED";

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final CarbonRecordRepository carbonRecordRepository;
    private final UtilityBillRepository utilityBillRepository;
    private final RecommendationRepository recommendationRepository;
    private final RewardRepository rewardRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarbonComputationEngine carbonEngine;
    private final RecommendationService recommendationService;

    public SeedDataService(UserRepository userRepository,
                           ActivityRepository activityRepository,
                           CarbonRecordRepository carbonRecordRepository,
                           UtilityBillRepository utilityBillRepository,
                           RecommendationRepository recommendationRepository,
                           RewardRepository rewardRepository,
                           PasswordEncoder passwordEncoder,
                           CarbonComputationEngine carbonEngine,
                           RecommendationService recommendationService) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.carbonRecordRepository = carbonRecordRepository;
        this.utilityBillRepository = utilityBillRepository;
        this.recommendationRepository = recommendationRepository;
        this.rewardRepository = rewardRepository;
        this.passwordEncoder = passwordEncoder;
        this.carbonEngine = carbonEngine;
        this.recommendationService = recommendationService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > MIN_SEED_USER_COUNT) {
            return;
        }

        log.info("Seeding database with sample data");

        List<User> users = userRepository.saveAll(List.of(
                createUser("rohan@hyderabad.college", "Rohan123!", "Rohan Sharma", 21, "Hyderabad", "METRO", 450, 5, 7),
                createUser("priya@techcorp.com", "Priya123!", "Priya Singh", 24, "Hyderabad", "CAR_PETROL", 1280, 3, 5),
                createUser("arjun@green.org", "Arjun123!", "Arjun Patel", 22, "Bengaluru", "BUS", 2100, 12, 12),
                createUser("neha@eco.in", "Neha123!", "Neha Gupta", 26, "Mumbai", "WALK", 5600, 8, 10)
        ));

        User rohan = users.get(0);
        User priya = users.get(1);
        User arjun = users.get(2);
        User neha = users.get(3);

        seedActivitiesBatch(rohan.getId(), "METRO", new double[][]{{12.5, 0.035}, {8.0, 0.035}, {15.0, 0.035}, {10.0, 0.035}, {6.5, 0.035}}, 5);
        seedActivitiesBatch(priya.getId(), "CAR_PETROL", new double[][]{{18.0, 0.192}, {15.0, 0.192}, {20.0, 0.192}}, 3);
        seedActivitiesBatch(arjun.getId(), "BUS", new double[][]{{22.0, 0.089}, {18.0, 0.089}, {25.0, 0.089}, {15.0, 0.089}, {20.0, 0.089}}, 5);
        seedActivitiesBatch(neha.getId(), "WALK", new double[][]{{3.0, ZERO_EMISSION}, {4.5, ZERO_EMISSION}, {2.0, ZERO_EMISSION}, {5.0, ZERO_EMISSION}}, 4);

        seedUtilityBillsBatch(rohan.getId(), 450, 3);
        seedUtilityBillsBatch(priya.getId(), 680, 2);
        seedUtilityBillsBatch(arjun.getId(), 320, 4);
        seedUtilityBillsBatch(neha.getId(), 520, 2);

        recommendationService.generateRecommendations(rohan.getId());
        recommendationService.generateRecommendations(priya.getId());
        recommendationService.generateRecommendations(arjun.getId());
        recommendationService.generateRecommendations(neha.getId());

        log.info("Seed data loaded: {} users, {} activities, {} carbon records",
                userRepository.count(), activityRepository.count(), carbonRecordRepository.count());
    }

    private User createUser(String email, String password, String name, int age, String city, String transit,
                            long points, int streak, int longest) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        user.setAge(age);
        user.setMunicipality(city);
        user.setDefaultTransitMode(transit);
        user.setTotalPoints(points);
        user.setCurrentStreak(streak);
        user.setLongestStreak(longest);
        user.setLastActivityDate(LocalDate.now());
        user.setLevel(RewardService.getLevelForPoints(points));
        return user;
    }

    private void seedActivitiesBatch(Long userId, String mode, double[][] trips, int daysBack) {
        List<Activity> activities = new ArrayList<>();
        List<CarbonRecord> records = new ArrayList<>();

        for (int i = 0; i < trips.length; i++) {
            double dist = trips[i][0];
            LocalDate day = LocalDate.now().minusDays(daysBack - i);

            Activity a = new Activity();
            a.setUserId(userId);
            a.setTransitMode(mode);
            a.setDistanceKm(dist);
            a.setActivityStart(day.atTime(9, 0));
            a.setActivityEnd(day.atTime(9, 30));
            a.setIsManual(false);
            activities.add(a);
        }

        List<Activity> saved = activityRepository.saveAll(activities);

        for (int i = 0; i < saved.size(); i++) {
            double dist = trips[i][0];
            LocalDate day = LocalDate.now().minusDays(daysBack - i);

            CarbonRecord cr = carbonEngine.computeTransportCarbon(userId, mode, dist, day);
            cr.setSourceId(saved.get(i).getId());
            cr.setSourceType(SOURCE_TYPE_PREFIX + mode);
            records.add(cr);
        }

        carbonRecordRepository.saveAll(records);
    }

    private void seedUtilityBillsBatch(Long userId, double kwh, int allocation) {
        UtilityBill bill = new UtilityBill();
        bill.setUserId(userId);
        bill.setTotalKwh(kwh);
        bill.setUtilityType(UTILITY_TYPE_ELECTRICITY);
        bill.setBillingStart(LocalDate.now().minusMonths(1).withDayOfMonth(1));
        bill.setBillingEnd(LocalDate.now().minusMonths(1).withDayOfMonth(30));
        bill.setAllocationCount(allocation);
        bill.setStatus(STATUS_PROCESSED);
        utilityBillRepository.save(bill);

        carbonEngine.computeUtilityCarbon(userId, kwh, allocation,
                bill.getBillingStart(), bill.getBillingEnd());
    }
}
