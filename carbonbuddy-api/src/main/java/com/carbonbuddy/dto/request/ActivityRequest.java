package com.carbonbuddy.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ActivityRequest {

    @NotNull
    private Double distanceKm;

    private Double durationMinutes;

    @NotNull
    private String transitMode;

    private String routePolyline;

    @NotNull
    private LocalDateTime activityStart;

    private LocalDateTime activityEnd;

    private Boolean isManual = true;

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
    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }
}
