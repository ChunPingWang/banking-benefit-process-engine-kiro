package com.bank.promotion.application.query;

import java.util.Objects;

/**
 * 查詢可用優惠查詢物件
 */
public final class GetAvailablePromotionsQuery {
    
    private final String customerId;
    private final String accountType;
    private final String region;
    private final boolean activeOnly;
    
    public GetAvailablePromotionsQuery(String customerId, String accountType, 
                                     String region, boolean activeOnly) {
        this.customerId = validateCustomerId(customerId);
        this.accountType = accountType;
        this.region = region;
        this.activeOnly = activeOnly;
    }
    
    private String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        return customerId.trim();
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public String getRegion() {
        return region;
    }
    
    public boolean isActiveOnly() {
        return activeOnly;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAvailablePromotionsQuery that = (GetAvailablePromotionsQuery) o;
        return activeOnly == that.activeOnly &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(accountType, that.accountType) &&
               Objects.equals(region, that.region);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, accountType, region, activeOnly);
    }
    
    @Override
    public String toString() {
        return "GetAvailablePromotionsQuery{" +
               "customerId='" + customerId + '\'' +
               ", accountType='" + accountType + '\'' +
               ", region='" + region + '\'' +
               ", activeOnly=" + activeOnly +
               '}';
    }
}