package com.bank.promotion.application.query;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 查詢優惠歷史查詢物件
 */
public final class GetPromotionHistoryQuery {
    
    private final String customerId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final int page;
    private final int size;
    
    public GetPromotionHistoryQuery(String customerId, LocalDateTime startDate, 
                                   LocalDateTime endDate, int page, int size) {
        this.customerId = validateCustomerId(customerId);
        this.startDate = startDate;
        this.endDate = endDate;
        this.page = validatePage(page);
        this.size = validateSize(size);
    }
    
    private String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        return customerId.trim();
    }
    
    private int validatePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        return page;
    }
    
    private int validateSize(int size) {
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        return size;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPromotionHistoryQuery that = (GetPromotionHistoryQuery) o;
        return page == that.page &&
               size == that.size &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, startDate, endDate, page, size);
    }
    
    @Override
    public String toString() {
        return "GetPromotionHistoryQuery{" +
               "customerId='" + customerId + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", page=" + page +
               ", size=" + size +
               '}';
    }
}