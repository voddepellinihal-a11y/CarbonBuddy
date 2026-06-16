package com.carbonbuddy.repository;

import com.carbonbuddy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_findByEmail_when_userExists() {
        User user = new User();
        user.setEmail("found@example.com");
        user.setPasswordHash("hash");
        user.setName("Found User");
        user.setTotalPoints(100);
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("found@example.com");

        assertTrue(result.isPresent());
        assertEquals("Found User", result.get().getName());
    }

    @Test
    void should_returnEmpty_when_emailNotFound() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void should_returnTrue_when_emailExists() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPasswordHash("hash");
        user.setName("Exists User");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertTrue(exists);
    }

    @Test
    void should_returnFalse_when_emailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("missing@example.com");

        assertFalse(exists);
    }

    @Test
    void should_findByOrderByTotalPointsDesc_when_multipleUsers() {
        User high = new User();
        high.setEmail("high@example.com");
        high.setPasswordHash("hash");
        high.setName("High Score");
        high.setTotalPoints(500);
        userRepository.save(high);

        User low = new User();
        low.setEmail("low@example.com");
        low.setPasswordHash("hash");
        low.setName("Low Score");
        low.setTotalPoints(50);
        userRepository.save(low);

        var results = userRepository.findByOrderByTotalPointsDesc(PageRequest.of(0, 10));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("High Score", results.get(0).getName());
    }

    @Test
    void should_returnCorrectPage_when_usingPageable() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setPasswordHash("hash");
            user.setName("User " + i);
            user.setTotalPoints(i * 100L);
            userRepository.save(user);
        }

        var page1 = userRepository.findByOrderByTotalPointsDesc(PageRequest.of(0, 2));
        var page2 = userRepository.findByOrderByTotalPointsDesc(PageRequest.of(1, 2));

        assertEquals(2, page1.size());
        assertTrue(page2.size() <= 2);
        assertNotEquals(page1.get(0).getEmail(), page2.get(0).getEmail());
    }

    @Test
    void should_countUsersWithMorePoints_when_usersExist() {
        User high = new User();
        high.setEmail("highcount@example.com");
        high.setPasswordHash("hash");
        high.setName("High Count");
        high.setTotalPoints(1000);
        userRepository.save(high);

        User low = new User();
        low.setEmail("lowcount@example.com");
        low.setPasswordHash("hash");
        low.setName("Low Count");
        low.setTotalPoints(100);
        userRepository.save(low);

        int count = userRepository.countUsersWithMorePoints(500);

        assertTrue(count >= 1);
    }

    @Test
    void should_returnZeroCount_when_noUsersHaveMorePoints() {
        int count = userRepository.countUsersWithMorePoints(1_000_000);

        assertEquals(0, count);
    }

    @Test
    void should_countTotalUsers_when_usersExist() {
        User u1 = new User();
        u1.setEmail("total1@example.com");
        u1.setPasswordHash("hash");
        u1.setName("Total 1");
        userRepository.save(u1);

        User u2 = new User();
        u2.setEmail("total2@example.com");
        u2.setPasswordHash("hash");
        u2.setName("Total 2");
        userRepository.save(u2);

        int count = userRepository.countTotalUsers();

        assertTrue(count >= 2);
    }

    @Test
    void should_returnEmptyList_when_noUsersExist() {
        var results = userRepository.findByOrderByTotalPointsDesc(PageRequest.of(0, 10));

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
