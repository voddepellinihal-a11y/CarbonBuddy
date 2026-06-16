package com.carbonbuddy.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new activity.
 * Validates distance, duration, transit mode, and activity timestamps.
 */
public class ActivityRequest {

    private static final int MAX_TRANSIT_MODE_LENGTH = 50;
    private static final int MAX_ROUTE_LENGTH = 10000;

    @Positive(message = "Distance must be positive")
    @NotNull
    private Double distanceKm;

    @Positive(message = "Duration must be positive")
    private Double durationMinutes;

    @NotNull
    private String transitMode;

    private String routePolyline;

    @NotNull
    private LocalDateTime activityStart;

    private LocalDateTime activityEnd;

    private boolean isManual = true;

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Double durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getTransitMode() { return transitMode; }
    public void setTransitMode(String transitMode) { this.transitMode = transitMode; }
    public String getRoutePolyline() { return routePolyline; }
    public void setRoutePolyline(String routePolyline) { this.routePolyline = routePolyline; }
    public LocalDateTime getActivityStart() { return activityStart; }
    public void setActivityStart(LocalDateTime activityStart) { this.activityStart = activityStart; }
    public LocalDateTime getActivityEnd() { return activityEnd; }
    public void setActivityEnd(LocalDateTime activityEnd) { this.activityEnd = activityEnd; }
    public boolean isManual() { return isManual; }
    public void setManual(boolean isManual) { this.isManual = isManual; }
}
