package com.carbonbuddy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double estimatedSavingsKg;

    private Double estimatedSavingsPercent;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String status;

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
