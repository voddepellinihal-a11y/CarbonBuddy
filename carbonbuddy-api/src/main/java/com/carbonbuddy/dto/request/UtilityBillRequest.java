package com.carbonbuddy.dto.request;

public class UtilityBillRequest {

    private Double totalKwh;

    private String utilityType;

    private String billingStart;

    private String billingEnd;

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
