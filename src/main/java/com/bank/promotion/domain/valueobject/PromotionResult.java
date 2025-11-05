package com.bank.promotion.domain.valueobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 優惠推薦結果值物件
 * 包含優惠詳情和計算結果
 */
public final class PromotionResult {
    
    private final String promotionId;
    private final String promotionName;
    private final String promotionType;
    private final BigDecimal discountAmount;
    private final BigDecimal discountPercentage;
    private final String description;
    private final LocalDateTime validUntil;
    private final Map<String, Object> additionalDetails;
    private final boolean isEligible;
    
    public PromotionResult(String promotionId, String promotionName, String promotionType,
                          BigDecimal discountAmount, BigDecimal discountPercentage,
                          String description, LocalDateTime validUntil,
                          Map<String, Object> additionalDetails, boolean isEligible) {
        this.promotionId = validatePromotionId(promotionId);
        this.promotionName = validatePromotionName(promotionName);
        this.promotionType = validatePromotionType(promotionType);
        this.discountAmount = validateDiscountAmount(discountAmount);
        this.discountPercentage = validateDiscountPercentage(discountPercentage);
        this.description = description;
        this.validUntil = validUntil;
        this.additionalDetails = additionalDetails != null ? Map.copyOf(additionalDetails) : Map.of();
        this.isEligible = isEligible;
    }
    
    private String validatePromotionId(String promotionId) {
        if (promotionId == null || promotionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion ID cannot be null or empty");
        }
        return promotionId.trim();
    }
    
    private String validatePromotionName(String promotionName) {
        if (promotionName == null || promotionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion name cannot be null or empty");
        }
        return promotionName.trim();
    }
    
    private String validatePromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion type cannot be null or empty");
        }
        return promotionType.trim();
    }
    
    private BigDecimal validateDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must be non-negative");
        }
        return discountAmount;
    }
    
    private BigDecimal validateDiscountPercentage(BigDecimal discountPercentage) {
        if (discountPercentage != null && 
            (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || 
             discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        return discountPercentage;
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
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getValidUntil() {
        return validUntil;
    }
    
    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }
    
    public boolean isEligible() {
        return isEligible;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionResult that = (PromotionResult) o;
        return isEligible == that.isEligible &&
               Objects.equals(promotionId, that.promotionId) &&
               Objects.equals(promotionName, that.promotionName) &&
               Objects.equals(promotionType, that.promotionType) &&
               Objects.equals(discountAmount, that.discountAmount) &&
               Objects.equals(discountPercentage, that.discountPercentage) &&
               Objects.equals(description, that.description) &&
               Objects.equals(validUntil, that.validUntil) &&
               Objects.equals(additionalDetails, that.additionalDetails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(promotionId, promotionName, promotionType, discountAmount,
                          discountPercentage, description, validUntil, additionalDetails, isEligible);
    }
    
    @Override
    public String toString() {
        return "PromotionResult{" +
               "promotionId='" + promotionId + '\'' +
               ", promotionName='" + promotionName + '\'' +
               ", promotionType='" + promotionType + '\'' +
               ", discountAmount=" + discountAmount +
               ", discountPercentage=" + discountPercentage +
               ", description='" + description + '\'' +
               ", validUntil=" + validUntil +
               ", additionalDetails=" + additionalDetails +
               ", isEligible=" + isEligible +
               '}';
    }
}