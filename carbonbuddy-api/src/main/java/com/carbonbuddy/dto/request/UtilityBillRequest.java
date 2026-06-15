package com.carbonbuddy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UtilityBillRequest {

    @Positive(message = "Total kWh must be positive")
    private Double totalKwh;

    @NotBlank(message = "Utility type is required")
    private String utilityType;

    @NotBlank(message = "Billing start date is required")
    private String billingStart;

    @NotBlank(message = "Billing end date is required")
    private String billingEnd;

    @Positive(message = "Allocation count must be positive")
    private Integer allocationCount = 1;

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
