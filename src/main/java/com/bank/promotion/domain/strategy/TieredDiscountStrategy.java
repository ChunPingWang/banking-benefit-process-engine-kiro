package com.bank.promotion.domain.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 階層式折扣策略
 * 根據不同的金額階層提供不同的折扣率
 */
public class TieredDiscountStrategy implements CalculationStrategy {
    
    private static final String STRATEGY_TYPE = "TIERED_DISCOUNT";
    private static final String TIERS_KEY = "tiers";
    private static final String BASE_AMOUNT_KEY = "baseAmount";
    private static final String PROMOTION_NAME_KEY = "promotionName";
    private static final String PROMOTION_ID_KEY = "promotionId";
    private static final String VALID_DAYS_KEY = "validDays";
    
    @Override
    public BigDecimal calculate(CustomerProfile customer, Map<String, Object> parameters) {
        if (!validateParameters(parameters)) {
            throw new IllegalArgumentException("Invalid parameters for tiered discount calculation");
        }
        
        BigDecimal baseAmount = getBaseAmount(parameters, customer);
        List<DiscountTier> tiers = parseTiers(parameters);
        
        return calculateTieredDiscount(baseAmount, tiers);
    }
    
    @Override
    public PromotionResult createPromotionResult(CustomerProfile customer, BigDecimal calculatedAmount, Map<String, Object> parameters) {
        String promotionId = getParameterAsString(parameters, PROMOTION_ID_KEY, "TIERED_DISCOUNT_" + System.currentTimeMillis());
        String promotionName = getParameterAsString(parameters, PROMOTION_NAME_KEY, "階層式折扣優惠");
        Integer validDays = getParameterAsInteger(parameters, VALID_DAYS_KEY, 30);
        
        LocalDateTime validUntil = LocalDateTime.now().plusDays(validDays);
        BigDecimal baseAmount = getBaseAmount(parameters, customer);
        List<DiscountTier> tiers = parseTiers(parameters);
        
        // 計算適用的階層和折扣率
        DiscountTier appliedTier = findApplicableTier(baseAmount, tiers);
        BigDecimal effectiveDiscountRate = appliedTier != null ? appliedTier.getDiscountPercentage() : BigDecimal.ZERO;
        
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("strategyType", STRATEGY_TYPE);
        additionalDetails.put("baseAmount", baseAmount);
        additionalDetails.put("appliedTier", appliedTier != null ? appliedTier.toString() : "無適用階層");
        additionalDetails.put("effectiveDiscountRate", effectiveDiscountRate);
        additionalDetails.put("customerId", customer.getCustomerId());
        additionalDetails.put("calculationTimestamp", LocalDateTime.now());
        additionalDetails.put("allTiers", tiers.toString());
        
        String description = String.format("階層式折扣優惠，基準金額 %s 元，適用折扣率 %.1f%%，折扣金額 %s 元", 
                                         baseAmount.toString(),
                                         effectiveDiscountRate.doubleValue(), 
                                         calculatedAmount.toString());
        
        return new PromotionResult(
            promotionId,
            promotionName,
            STRATEGY_TYPE,
            calculatedAmount,
            effectiveDiscountRate,
            description,
            validUntil,
            additionalDetails,
            true
        );
    }
    
    @Override
    public String getStrategyType() {
        return STRATEGY_TYPE;
    }
    
    @Override
    public boolean validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }
        
        // 驗證階層配置
        Object tiersObj = parameters.get(TIERS_KEY);
        if (tiersObj == null) {
            return false;
        }
        
        try {
            List<DiscountTier> tiers = parseTiers(parameters);
            if (tiers.isEmpty()) {
                return false;
            }
            
            // 驗證階層配置的合理性
            for (DiscountTier tier : tiers) {
                if (tier.getMinAmount().compareTo(BigDecimal.ZERO) < 0 ||
                    tier.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0 ||
                    tier.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                    return false;
                }
            }
            
        } catch (Exception e) {
            return false;
        }
        
        // 驗證基準金額（可選）
        Object baseAmountObj = parameters.get(BASE_AMOUNT_KEY);
        if (baseAmountObj != null) {
            try {
                BigDecimal baseAmount = convertToBigDecimal(baseAmountObj);
                if (baseAmount.compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        return true;
    }
    
    private BigDecimal calculateTieredDiscount(BigDecimal baseAmount, List<DiscountTier> tiers) {
        // 找到適用的階層
        DiscountTier applicableTier = findApplicableTier(baseAmount, tiers);
        
        if (applicableTier == null) {
            return BigDecimal.ZERO;
        }
        
        // 計算折扣金額
        return baseAmount.multiply(applicableTier.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    private DiscountTier findApplicableTier(BigDecimal baseAmount, List<DiscountTier> tiers) {
        // 按最小金額降序排列，找到第一個符合條件的階層
        return tiers.stream()
                   .sorted((t1, t2) -> t2.getMinAmount().compareTo(t1.getMinAmount()))
                   .filter(tier -> baseAmount.compareTo(tier.getMinAmount()) >= 0)
                   .findFirst()
                   .orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    private List<DiscountTier> parseTiers(Map<String, Object> parameters) {
        Object tiersObj = parameters.get(TIERS_KEY);
        List<DiscountTier> tiers = new ArrayList<>();
        
        if (tiersObj instanceof List) {
            List<Object> tiersList = (List<Object>) tiersObj;
            
            for (Object tierObj : tiersList) {
                if (tierObj instanceof Map) {
                    Map<String, Object> tierMap = (Map<String, Object>) tierObj;
                    DiscountTier tier = parseTierFromMap(tierMap);
                    if (tier != null) {
                        tiers.add(tier);
                    }
                }
            }
        }
        
        return tiers;
    }
    
    private DiscountTier parseTierFromMap(Map<String, Object> tierMap) {
        try {
            Object minAmountObj = tierMap.get("minAmount");
            Object discountPercentageObj = tierMap.get("discountPercentage");
            
            if (minAmountObj == null || discountPercentageObj == null) {
                return null;
            }
            
            BigDecimal minAmount = convertToBigDecimal(minAmountObj);
            BigDecimal discountPercentage = convertToBigDecimal(discountPercentageObj);
            String description = tierMap.get("description") != null ? tierMap.get("description").toString() : "";
            
            return new DiscountTier(minAmount, discountPercentage, description);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private BigDecimal getBaseAmount(Map<String, Object> parameters, CustomerProfile customer) {
        Object baseAmountObj = parameters.get(BASE_AMOUNT_KEY);
        if (baseAmountObj != null) {
            return convertToBigDecimal(baseAmountObj);
        }
        
        // 預設使用客戶年收入作為基準金額
        return customer.getBasicInfo().getAnnualIncome();
    }
    
    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else {
            throw new IllegalArgumentException("Cannot convert value to BigDecimal: " + value);
        }
    }
    
    private String getParameterAsString(Map<String, Object> parameters, String key, String defaultValue) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private Integer getParameterAsInteger(Map<String, Object> parameters, String key, Integer defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 折扣階層內部類別
     */
    public static class DiscountTier {
        private final BigDecimal minAmount;
        private final BigDecimal discountPercentage;
        private final String description;
        
        public DiscountTier(BigDecimal minAmount, BigDecimal discountPercentage, String description) {
            this.minAmount = minAmount;
            this.discountPercentage = discountPercentage;
            this.description = description;
        }
        
        public BigDecimal getMinAmount() {
            return minAmount;
        }
        
        public BigDecimal getDiscountPercentage() {
            return discountPercentage;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return String.format("DiscountTier{minAmount=%s, discountPercentage=%s%%, description='%s'}", 
                               minAmount, discountPercentage, description);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiscountTier that = (DiscountTier) o;
            return Objects.equals(minAmount, that.minAmount) &&
                   Objects.equals(discountPercentage, that.discountPercentage) &&
                   Objects.equals(description, that.description);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minAmount, discountPercentage, description);
        }
    }
}