package com.bank.promotion.application.query.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 優惠歷史視圖
 * 用於查詢端的讀取模型
 */
public final class PromotionHistoryView {
    
    private final String id;
    private final String customerId;
    private final String promotionId;
    private final String promotionName;
    private final String promotionType;
    private final BigDecimal discountAmount;
    private final BigDecimal discountPercentage;
    private final String status;
    private final LocalDateTime executedAt;
    private final Map<String, Object> additionalDetails;
    
    public PromotionHistoryView(String id, String customerId, String promotionId, 
                               String promotionName, String promotionType,
                               BigDecimal discountAmount, BigDecimal discountPercentage,
                               String status, LocalDateTime executedAt,
                               Map<String, Object> additionalDetails) {
        this.id = id;
        this.customerId = customerId;
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.promotionType = promotionType;
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
        this.status = status;
        this.executedAt = executedAt;
        this.additionalDetails = additionalDetails != null ? Map.copyOf(additionalDetails) : Map.of();
    }
    
    public String getId() {
        return id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getPromotionId() {
        return promotionId;
    }
    
    public String getPromotionName() {
        return promotionName;
    }
    
    public String getPromotionType() {
        return promotionType;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionHistoryView that = (PromotionHistoryView) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PromotionHistoryView{" +
               "id='" + id + '\'' +
               ", customerId='" + customerId + '\'' +
               ", promotionId='" + promotionId + '\'' +
               ", promotionName='" + promotionName + '\'' +
               ", promotionType='" + promotionType + '\'' +
               ", discountAmount=" + discountAmount +
               ", discountPercentage=" + discountPercentage +
               ", status='" + status + '\'' +
               ", executedAt=" + executedAt +
               ", additionalDetails=" + additionalDetails +
               '}';
    }
}