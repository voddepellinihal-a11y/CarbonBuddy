package com.carbonbuddy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a utility bill.
 * Validates total kWh, utility type, billing dates, and allocation count.
 */
public class UtilityBillRequest {

    private static final int DEFAULT_ALLOCATION_COUNT = 1;

    @Positive(message = "Total kWh must be positive")
    private Double totalKwh;

    @NotBlank(message = "Utility type is required")
    private String utilityType;

    @NotBlank(message = "Billing start date is required")
    private String billingStart;

    @NotBlank(message = "Billing end date is required")
    private String billingEnd;

    @Positive(message = "Allocation count must be positive")
    private Integer allocationCount = DEFAULT_ALLOCATION_COUNT;

    public Double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(Double totalKwh) { this.totalKwh = totalKwh; }
    public String getUtilityType() { return utilityType; }
    public void setUtilityType(String utilityType) { this.utilityType = utilityType; }
    public String getBillingStart() { return billingStart; }
    public void setBillingStart(String billingStart) { this.billingStart = billingStart; }
    public String getBillingEnd() { return billingEnd; }
    public void setBillingEnd(String billingEnd) { this.billingEnd = billingEnd; }
    public Integer getAllocationCount() { return allocationCount; }
    public void setAllocationCount(Integer allocationCount) { this.allocationCount = allocationCount; }
}
