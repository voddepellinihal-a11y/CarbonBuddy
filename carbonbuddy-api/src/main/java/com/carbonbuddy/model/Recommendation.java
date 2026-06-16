package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations", indexes = {
    @Index(name = "idx_rec_user_id", columnList = "userId"),
    @Index(name = "idx_rec_status", columnList = "status"),
    @Index(name = "idx_rec_user_status", columnList = "userId, status")
})
public class Recommendation {

    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final int MAX_STATUS_LENGTH = 20;
    private static final String DEFAULT_STATUS = "PENDING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero(message = "Estimated savings must be non-negative")
    private Double estimatedSavingsKg;

    @PositiveOrZero(message = "Estimated savings percent must be non-negative")
    private Double estimatedSavingsPercent;

    @Size(max = MAX_CATEGORY_LENGTH)
    @Column(length = MAX_CATEGORY_LENGTH)
    private String category;

    @Size(max = MAX_STATUS_LENGTH)
    @Column(length = MAX_STATUS_LENGTH)
    private String status = DEFAULT_STATUS;

    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getEstimatedSavingsKg() { return estimatedSavingsKg; }
    public void setEstimatedSavingsKg(Double estimatedSavingsKg) { this.estimatedSavingsKg = estimatedSavingsKg; }
    public Double getEstimatedSavingsPercent() { return estimatedSavingsPercent; }
    public void setEstimatedSavingsPercent(Double estimatedSavingsPercent) { this.estimatedSavingsPercent = estimatedSavingsPercent; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
