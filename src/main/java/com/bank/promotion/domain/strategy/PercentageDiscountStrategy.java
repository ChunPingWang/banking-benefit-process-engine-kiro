package com.bank.promotion.domain.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 百分比折扣策略
 * 根據指定的百分比計算折扣金額
 */
public class PercentageDiscountStrategy implements CalculationStrategy {
    
    private static final String STRATEGY_TYPE = "PERCENTAGE_DISCOUNT";
    private static final String DISCOUNT_PERCENTAGE_KEY = "discountPercentage";
    private static final String BASE_AMOUNT_KEY = "baseAmount";
    private static final String PROMOTION_NAME_KEY = "promotionName";
    private static final String PROMOTION_ID_KEY = "promotionId";
    private static final String VALID_DAYS_KEY = "validDays";
    
    @Override
    public BigDecimal calculate(CustomerProfile customer, Map<String, Object> parameters) {
        if (!validateParameters(parameters)) {
            throw new IllegalArgumentException("Invalid parameters for percentage discount calculation");
        }
        
        BigDecimal discountPercentage = getDiscountPercentage(parameters);
        BigDecimal baseAmount = getBaseAmount(parameters, customer);
        
        // 計算折扣金額 = 基準金額 * 折扣百分比 / 100
        return baseAmount.multiply(discountPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public PromotionResult createPromotionResult(CustomerProfile customer, BigDecimal calculatedAmount, Map<String, Object> parameters) {
        String promotionId = getParameterAsString(parameters, PROMOTION_ID_KEY, "PERCENTAGE_DISCOUNT_" + System.currentTimeMillis());
        String promotionName = getParameterAsString(parameters, PROMOTION_NAME_KEY, "百分比折扣優惠");
        BigDecimal discountPercentage = getDiscountPercentage(parameters);
        Integer validDays = getParameterAsInteger(parameters, VALID_DAYS_KEY, 30);
        
        LocalDateTime validUntil = LocalDateTime.now().plusDays(validDays);
        
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("strategyType", STRATEGY_TYPE);
        additionalDetails.put("discountPercentage", discountPercentage);
        additionalDetails.put("baseAmount", getBaseAmount(parameters, customer));
        additionalDetails.put("customerId", customer.getCustomerId());
        additionalDetails.put("calculationTimestamp", LocalDateTime.now());
        
        String description = String.format("享受 %.1f%% 的折扣優惠，折扣金額為 %s 元", 
                                         discountPercentage.doubleValue(), 
                                         calculatedAmount.toString());
        
        return new PromotionResult(
            promotionId,
            promotionName,
            STRATEGY_TYPE,
            calculatedAmount,
            discountPercentage,
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
        
        // 驗證折扣百分比
        Object discountPercentageObj = parameters.get(DISCOUNT_PERCENTAGE_KEY);
        if (discountPercentageObj == null) {
            return false;
        }
        
        try {
            BigDecimal discountPercentage = convertToBigDecimal(discountPercentageObj);
            if (discountPercentage.compareTo(BigDecimal.ZERO) <= 0 || 
                discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        // 驗證基準金額（可選，如果沒有提供會使用客戶年收入）
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
    
    private BigDecimal getDiscountPercentage(Map<String, Object> parameters) {
        return convertToBigDecimal(parameters.get(DISCOUNT_PERCENTAGE_KEY));
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
}