package com.carbonbuddy.repository;

import com.carbonbuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Repository for {@link User} entity persistence.
 * Provides custom queries for email lookup, leaderboard ranking, and user counts.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with that email exists
     */
    boolean existsByEmail(String email);

    /**
     * Returns users ordered by total points descending with pagination.
     *
     * @param pageable pagination parameters
     * @return a list of users ordered by points
     */
    List<User> findByOrderByTotalPointsDesc(Pageable pageable);

    /**
     * Counts users with more points than the given threshold.
     *
     * @param points the point threshold
     * @return the number of users with more points
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.totalPoints > :points")
    int countUsersWithMorePoints(@Param("points") long points);

    /**
     * Counts the total number of registered users.
     *
     * @return the total user count
     */
    @Query("SELECT COUNT(u) FROM User u")
    int countTotalUsers();
}
