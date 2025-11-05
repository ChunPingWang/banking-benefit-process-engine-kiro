package com.bank.promotion.application.query.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 可用優惠視圖
 * 用於查詢端的讀取模型
 */
public final class AvailablePromotionView {
    
    private final String promotionId;
    private final String promotionName;
    private final String promotionType;
    private final String description;
    private final BigDecimal maxDiscountAmount;
    private final BigDecimal maxDiscountPercentage;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private final String eligibilityCriteria;
    private final String status;
    private final Map<String, Object> terms;
    
    public AvailablePromotionView(String promotionId, String promotionName, String promotionType,
                                 String description, BigDecimal maxDiscountAmount, 
                                 BigDecimal maxDiscountPercentage, LocalDateTime validFrom,
                                 LocalDateTime validUntil, String eligibilityCriteria,
                                 String status, Map<String, Object> terms) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.promotionType = promotionType;
        this.description = description;
        this.maxDiscountAmount = maxDiscountAmount;
        this.maxDiscountPercentage = maxDiscountPercentage;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.eligibilityCriteria = eligibilityCriteria;
        this.status = status;
        this.terms = terms != null ? Map.copyOf(terms) : Map.of();
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
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }
    
    public BigDecimal getMaxDiscountPercentage() {
        return maxDiscountPercentage;
    }
    
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    
    public LocalDateTime getValidUntil() {
        return validUntil;
    }
    
    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Map<String, Object> getTerms() {
        return terms;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailablePromotionView that = (AvailablePromotionView) o;
        return Objects.equals(promotionId, that.promotionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(promotionId);
    }
    
    @Override
    public String toString() {
        return "AvailablePromotionView{" +
               "promotionId='" + promotionId + '\'' +
               ", promotionName='" + promotionName + '\'' +
               ", promotionType='" + promotionType + '\'' +
               ", description='" + description + '\'' +
               ", maxDiscountAmount=" + maxDiscountAmount +
               ", maxDiscountPercentage=" + maxDiscountPercentage +
               ", validFrom=" + validFrom +
               ", validUntil=" + validUntil +
               ", eligibilityCriteria='" + eligibilityCriteria + '\'' +
               ", status='" + status + '\'' +
               ", terms=" + terms +
               '}';
    }
}