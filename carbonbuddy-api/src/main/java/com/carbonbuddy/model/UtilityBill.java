package com.carbonbuddy.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utility_bills")
public class UtilityBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Double totalKwh;

    @Column(length = 50)
    private String utilityType;

    private LocalDate billingStart;

    private LocalDate billingEnd;

    private Integer allocationCount = 1;

    @Column(length = 20)
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
