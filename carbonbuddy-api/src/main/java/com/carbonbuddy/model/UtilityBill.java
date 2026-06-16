package com.carbonbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a utility bill (e.g. electricity).
 * Stores consumption data and allocation count for per-person carbon computation.
 */
@Entity
@Table(name = "utility_bills")
public class UtilityBill {

    private static final int MAX_UTILITY_TYPE_LENGTH = 50;
    private static final int MAX_STATUS_LENGTH = 20;
    private static final int DEFAULT_ALLOCATION_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @Positive(message = "Total kWh must be positive")
    private Double totalKwh;

    @Size(max = MAX_UTILITY_TYPE_LENGTH)
    @Column(length = MAX_UTILITY_TYPE_LENGTH)
    private String utilityType;

    private LocalDate billingStart;

    private LocalDate billingEnd;

    @Positive(message = "Allocation count must be positive")
    private Integer allocationCount = DEFAULT_ALLOCATION_COUNT;

    @Size(max = MAX_STATUS_LENGTH)
    @Column(length = MAX_STATUS_LENGTH)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String rawOcrText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(Double totalKwh) { this.totalKwh = totalKwh; }
    public String getUtilityType() { return utilityType; }
    public void setUtilityType(String utilityType) { this.utilityType = utilityType; }
    public LocalDate getBillingStart() { return billingStart; }
    public void setBillingStart(LocalDate billingStart) { this.billingStart = billingStart; }
    public LocalDate getBillingEnd() { return billingEnd; }
    public void setBillingEnd(LocalDate billingEnd) { this.billingEnd = billingEnd; }
    public Integer getAllocationCount() { return allocationCount; }
    public void setAllocationCount(Integer allocationCount) { this.allocationCount = allocationCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRawOcrText() { return rawOcrText; }
    public void setRawOcrText(String rawOcrText) { this.rawOcrText = rawOcrText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
