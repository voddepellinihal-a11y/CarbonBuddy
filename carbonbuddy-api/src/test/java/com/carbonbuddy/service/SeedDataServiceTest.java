package com.carbonbuddy.service;

import com.carbonbuddy.model.*;
import com.carbonbuddy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedDataServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    @Mock
    private UtilityBillRepository utilityBillRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CarbonComputationEngine carbonEngine;

    @Mock
    private RecommendationService recommendationService;

    private SeedDataService seedDataService;

    @BeforeEach
    void setUp() {
        seedDataService = new SeedDataService(
                userRepository, activityRepository, carbonRecordRepository,
                utilityBillRepository, recommendationRepository, rewardRepository,
                passwordEncoder, carbonEngine, recommendationService);
    }

    @Test
    void should_createFourUsers_when_dataNotYetSeeded() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(userRepository, times(4)).save(any(User.class));
    }

    @Test
    void should_skipSeeding_when_dataAlreadyExists() {
        when(userRepository.count()).thenReturn(5L);

        seedDataService.run();

        verify(userRepository, never()).save(any(User.class));
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void should_encodePasswords_when_creatingUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(passwordEncoder, atLeastOnce()).encode(any());
    }

    @Test
    void should_createActivities_when_seedingRohan() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(activityRepository, atLeast(4)).save(any(Activity.class));
    }

    @Test
    void should_computeCarbonRecords_when_seedingActivities() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(carbonEngine, atLeastOnce()).computeTransportCarbon(anyLong(), any(), anyDouble(), any());
    }

    @Test
    void should_createUtilityBills_when_seedingData() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(utilityBillRepository, atLeastOnce()).save(any(UtilityBill.class));
    }

    @Test
    void should_generateRecommendations_when_seedingData() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        verify(recommendationService, times(4)).generateRecommendations(anyLong());
    }

    @Test
    void should_setDefaultTransitMode_when_creatingUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        User[] capturedUser = new User[1];
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            capturedUser[0] = u;
            u.setId(1L);
            return u;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeTransportCarbon(anyLong(), any(), anyDouble(), any()))
                .thenReturn(new CarbonRecord());
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(utilityBillRepository.save(any(UtilityBill.class))).thenAnswer(i -> i.getArgument(0));
        when(carbonEngine.computeUtilityCarbon(anyLong(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(new CarbonRecord());
        when(userRepository.count()).thenReturn(0L, 4L);

        seedDataService.run();

        assertNotNull(capturedUser[0].getDefaultTransitMode());
    }
}
