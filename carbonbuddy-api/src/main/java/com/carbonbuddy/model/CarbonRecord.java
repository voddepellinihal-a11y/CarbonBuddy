package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_records", indexes = {
    @Index(name = "idx_carbon_user_id", columnList = "userId"),
    @Index(name = "idx_carbon_record_date", columnList = "recordDate"),
    @Index(name = "idx_carbon_user_date", columnList = "userId, recordDate"),
    @Index(name = "idx_carbon_user_category", columnList = "userId, category")
})
public class CarbonRecord {

    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final int MAX_SOURCE_TYPE_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @Size(max = MAX_CATEGORY_LENGTH)
    @Column(length = MAX_CATEGORY_LENGTH)
    private String category;

    @PositiveOrZero(message = "Carbon kg must be non-negative")
    private Double carbonKg;

    private Double distanceKm;

    @Size(max = MAX_SOURCE_TYPE_LENGTH)
    @Column(length = MAX_SOURCE_TYPE_LENGTH)
    private String sourceType;

    private Long sourceId;

    @NotNull
    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getCarbonKg() { return carbonKg; }
    public void setCarbonKg(Double carbonKg) { this.carbonKg = carbonKg; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
