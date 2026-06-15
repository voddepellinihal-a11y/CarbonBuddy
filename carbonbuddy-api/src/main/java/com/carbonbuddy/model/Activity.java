package com.carbonbuddy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 50)
    private String transitMode;

    private Double distanceKm;

    private Double durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String routePolyline;

    @Column(nullable = false)
    private LocalDateTime activityStart;

    private LocalDateTime activityEnd;

    @Column(nullable = false)
    private Boolean isManual = false;

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
