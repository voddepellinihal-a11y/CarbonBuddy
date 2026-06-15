package com.carbonbuddy.service;

import com.carbonbuddy.model.*;
import com.carbonbuddy.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SeedDataService implements CommandLineRunner {

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
        if (userRepository.count() > 1) return;

        User rohan = createUser("rohan@hyderabad.college", "Rohan123!", "Rohan Sharma", 21, "Hyderabad", "METRO", 450, 5, 7);
        User priya = createUser("priya@techcorp.com", "Priya123!", "Priya Singh", 24, "Hyderabad", "CAR_PETROL", 1280, 3, 5);
        User arjun = createUser("arjun@green.org", "Arjun123!", "Arjun Patel", 22, "Bengaluru", "BUS", 2100, 12, 12);
        User neha = createUser("neha@eco.in", "Neha123!", "Neha Gupta", 26, "Mumbai", "WALK", 5600, 8, 10);

        seedActivities(rohan.getId(), "METRO", new double[][]{{12.5, 0.035}, {8.0, 0.035}, {15.0, 0.035}, {10.0, 0.035}, {6.5, 0.035}}, 5);
        seedActivities(priya.getId(), "CAR_PETROL", new double[][]{{18.0, 0.192}, {15.0, 0.192}, {20.0, 0.192}}, 3);
        seedActivities(arjun.getId(), "BUS", new double[][]{{22.0, 0.089}, {18.0, 0.089}, {25.0, 0.089}, {15.0, 0.089}, {20.0, 0.089}}, 5);
        seedActivities(neha.getId(), "WALK", new double[][]{{3.0, 0.0}, {4.5, 0.0}, {2.0, 0.0}, {5.0, 0.0}}, 4);

        seedUtilityBills(rohan.getId(), 450, 3);
        seedUtilityBills(priya.getId(), 680, 2);
        seedUtilityBills(arjun.getId(), 320, 4);
        seedUtilityBills(neha.getId(), 520, 2);

        CarbonRecord lastRecord = carbonRecordRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(rohan.getId(), LocalDate.now(), LocalDate.now())
                .stream().findFirst().orElse(null);

        recommendationService.generateRecommendations(rohan.getId());
        recommendationService.generateRecommendations(priya.getId());
        recommendationService.generateRecommendations(arjun.getId());
        recommendationService.generateRecommendations(neha.getId());

        System.out.println("Seed data loaded: " + userRepository.count() + " users, "
                + activityRepository.count() + " activities, "
                + carbonRecordRepository.count() + " carbon records");
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
        return userRepository.save(user);
    }

    private void seedActivities(Long userId, String mode, double[][] trips, int daysBack) {
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
            a = activityRepository.save(a);

            CarbonRecord cr = carbonEngine.computeTransportCarbon(userId, mode, dist, day);
            cr.setSourceId(a.getId());
            cr.setSourceType("ACTIVITY_" + mode);
            carbonRecordRepository.save(cr);
        }
    }

    private void seedUtilityBills(Long userId, double kwh, int allocation) {
        UtilityBill bill = new UtilityBill();
        bill.setUserId(userId);
        bill.setTotalKwh(kwh);
        bill.setUtilityType("electricity");
        bill.setBillingStart(LocalDate.now().minusMonths(1).withDayOfMonth(1));
        bill.setBillingEnd(LocalDate.now().minusMonths(1).withDayOfMonth(30));
        bill.setAllocationCount(allocation);
        bill.setStatus("PROCESSED");
        utilityBillRepository.save(bill);

        carbonEngine.computeUtilityCarbon(userId, kwh, allocation,
                bill.getBillingStart(), bill.getBillingEnd());
    }
}
