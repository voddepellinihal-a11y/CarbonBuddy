package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_points", columnList = "totalPoints")
})
public class User {

    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_MUNICIPALITY_LENGTH = 100;
    private static final int MAX_TRANSIT_MODE_LENGTH = 50;
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;
    private static final int DEFAULT_LEVEL = 1;
    private static final long DEFAULT_POINTS = 0;
    private static final int DEFAULT_STREAK = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Size(max = MAX_EMAIL_LENGTH)
    @Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank
    @Size(max = MAX_NAME_LENGTH)
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Min(value = MIN_AGE, message = "Age must be at least 1")
    @Max(value = MAX_AGE, message = "Age must not exceed 150")
    private Integer age;

    @Size(max = MAX_MUNICIPALITY_LENGTH)
    @Column(length = MAX_MUNICIPALITY_LENGTH)
    private String municipality;

    @Size(max = MAX_TRANSIT_MODE_LENGTH)
    @Column(length = MAX_TRANSIT_MODE_LENGTH)
    private String defaultTransitMode;

    private long totalPoints = DEFAULT_POINTS;

    private int currentStreak = DEFAULT_STREAK;

    private int longestStreak = DEFAULT_STREAK;

    private int level = DEFAULT_LEVEL;

    private LocalDate lastActivityDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getDefaultTransitMode() { return defaultTransitMode; }
    public void setDefaultTransitMode(String defaultTransitMode) { this.defaultTransitMode = defaultTransitMode; }
    public long getTotalPoints() { return totalPoints; }
    public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public LocalDate getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
