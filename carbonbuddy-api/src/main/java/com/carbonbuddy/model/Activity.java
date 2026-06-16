package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing a user activity (e.g. transport trip).
 * Stores transit mode, distance, duration, and route data.
 */
@Entity
@Table(name = "activities")
public class Activity {

    private static final int MAX_TRANSIT_MODE_LENGTH = 50;
    private static final Boolean DEFAULT_IS_MANUAL = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @Size(max = MAX_TRANSIT_MODE_LENGTH)
    @Column(length = MAX_TRANSIT_MODE_LENGTH)
    private String transitMode;

    @Positive(message = "Distance must be positive")
    private Double distanceKm;

    @Positive(message = "Duration must be positive")
    private Double durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String routePolyline;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime activityStart;

    private LocalDateTime activityEnd;

    @NotNull
    @Column(nullable = false)
    private Boolean isManual = DEFAULT_IS_MANUAL;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTransitMode() { return transitMode; }
    public void setTransitMode(String transitMode) { this.transitMode = transitMode; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Double durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getRoutePolyline() { return routePolyline; }
    public void setRoutePolyline(String routePolyline) { this.routePolyline = routePolyline; }
    public LocalDateTime getActivityStart() { return activityStart; }
    public void setActivityStart(LocalDateTime activityStart) { this.activityStart = activityStart; }
    public LocalDateTime getActivityEnd() { return activityEnd; }
    public void setActivityEnd(LocalDateTime activityEnd) { this.activityEnd = activityEnd; }
    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
