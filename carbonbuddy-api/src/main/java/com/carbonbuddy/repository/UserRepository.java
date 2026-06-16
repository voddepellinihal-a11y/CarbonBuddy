package com.carbonbuddy.repository;

import com.carbonbuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC")
    List<User> findByOrderByTotalPointsDesc(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.totalPoints > :points")
    int countUsersWithMorePoints(@Param("points") long points);

    @Query("SELECT COUNT(u) FROM User u")
    int countTotalUsers();

    @Query(value = "SELECT COUNT(*) FROM users", nativeQuery = true)
    long countTotalUsersNative();
}
