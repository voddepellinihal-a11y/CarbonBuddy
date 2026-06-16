package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing a reward transaction (credit or debit).
 * Tracks points earned from activities or spent in the store.
 */
@Entity
@Table(name = "rewards")
public class Reward {

    private static final int MAX_SOURCE_LENGTH = 50;
    private static final int MAX_TRANSACTION_TYPE_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    private Integer creditsEarned;

    private Integer creditsSpent;

    @Size(max = MAX_SOURCE_LENGTH)
    @Column(length = MAX_SOURCE_LENGTH)
    private String source;

    private Long sourceId;

    @Size(max = MAX_TRANSACTION_TYPE_LENGTH)
    @Column(length = MAX_TRANSACTION_TYPE_LENGTH)
    private String transactionType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getCreditsEarned() { return creditsEarned; }
    public void setCreditsEarned(Integer creditsEarned) { this.creditsEarned = creditsEarned; }
    public Integer getCreditsSpent() { return creditsSpent; }
    public void setCreditsSpent(Integer creditsSpent) { this.creditsSpent = creditsSpent; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
