package com.bank.promotion.domain.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 固定金額優惠策略
 * 提供固定金額的優惠折扣
 */
public class FixedAmountStrategy implements CalculationStrategy {
    
    private static final String STRATEGY_TYPE = "FIXED_AMOUNT";
    private static final String FIXED_AMOUNT_KEY = "fixedAmount";
    private static final String MIN_PURCHASE_AMOUNT_KEY = "minPurchaseAmount";
    private static final String MAX_DISCOUNT_AMOUNT_KEY = "maxDiscountAmount";
    private static final String PROMOTION_NAME_KEY = "promotionName";
    private static final String PROMOTION_ID_KEY = "promotionId";
    private static final String VALID_DAYS_KEY = "validDays";
    
    @Override
    public BigDecimal calculate(CustomerProfile customer, Map<String, Object> parameters) {
        if (!validateParameters(parameters)) {
            throw new IllegalArgumentException("Invalid parameters for fixed amount calculation");
        }
        
        BigDecimal fixedAmount = getFixedAmount(parameters);
        BigDecimal minPurchaseAmount = getMinPurchaseAmount(parameters);
        BigDecimal maxDiscountAmount = getMaxDiscountAmount(parameters);
        
        // 檢查是否符合最低購買金額要求
        BigDecimal customerAmount = getCustomerRelevantAmount(customer);
        if (customerAmount.compareTo(minPurchaseAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        // 應用最大折扣限制
        if (maxDiscountAmount != null && fixedAmount.compareTo(maxDiscountAmount) > 0) {
            return maxDiscountAmount;
        }
        
        return fixedAmount;
    }
    
    @Override
    public PromotionResult createPromotionResult(CustomerProfile customer, BigDecimal calculatedAmount, Map<String, Object> parameters) {
        String promotionId = getParameterAsString(parameters, PROMOTION_ID_KEY, "FIXED_AMOUNT_" + System.currentTimeMillis());
        String promotionName = getParameterAsString(parameters, PROMOTION_NAME_KEY, "固定金額優惠");
        Integer validDays = getParameterAsInteger(parameters, VALID_DAYS_KEY, 30);
        
        LocalDateTime validUntil = LocalDateTime.now().plusDays(validDays);
        BigDecimal fixedAmount = getFixedAmount(parameters);
        BigDecimal minPurchaseAmount = getMinPurchaseAmount(parameters);
        BigDecimal maxDiscountAmount = getMaxDiscountAmount(parameters);
        
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("strategyType", STRATEGY_TYPE);
        additionalDetails.put("originalFixedAmount", fixedAmount);
        additionalDetails.put("minPurchaseAmount", minPurchaseAmount);
        if (maxDiscountAmount != null) {
            additionalDetails.put("maxDiscountAmount", maxDiscountAmount);
        }
        additionalDetails.put("customerRelevantAmount", getCustomerRelevantAmount(customer));
        additionalDetails.put("customerId", customer.getCustomerId());
        additionalDetails.put("calculationTimestamp", LocalDateTime.now());
        
        // 判斷是否符合條件
        boolean isEligible = calculatedAmount.compareTo(BigDecimal.ZERO) > 0;
        
        String description;
        if (isEligible) {
            if (maxDiscountAmount != null && calculatedAmount.equals(maxDiscountAmount)) {
                description = String.format("固定金額優惠 %s 元（已達最大折扣限制 %s 元）", 
                                           calculatedAmount.toString(), 
                                           maxDiscountAmount.toString());
            } else {
                description = String.format("固定金額優惠 %s 元", calculatedAmount.toString());
            }
        } else {
            description = String.format("不符合最低購買金額要求（需滿 %s 元）", minPurchaseAmount.toString());
        }
        
        // 對於固定金額策略，折扣百分比設為 null
        return new PromotionResult(
            promotionId,
            promotionName,
            STRATEGY_TYPE,
            calculatedAmount,
            null, // 固定金額策略不使用百分比
            description,
            validUntil,
            additionalDetails,
            isEligible
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
        
        // 驗證固定金額
        Object fixedAmountObj = parameters.get(FIXED_AMOUNT_KEY);
        if (fixedAmountObj == null) {
            return false;
        }
        
        try {
            BigDecimal fixedAmount = convertToBigDecimal(fixedAmountObj);
            if (fixedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        // 驗證最低購買金額（可選，預設為 0）
        Object minPurchaseAmountObj = parameters.get(MIN_PURCHASE_AMOUNT_KEY);
        if (minPurchaseAmountObj != null) {
            try {
                BigDecimal minPurchaseAmount = convertToBigDecimal(minPurchaseAmountObj);
                if (minPurchaseAmount.compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        // 驗證最大折扣金額（可選）
        Object maxDiscountAmountObj = parameters.get(MAX_DISCOUNT_AMOUNT_KEY);
        if (maxDiscountAmountObj != null) {
            try {
                BigDecimal maxDiscountAmount = convertToBigDecimal(maxDiscountAmountObj);
                if (maxDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                
                // 最大折扣金額可以小於固定金額（用於限制實際折扣）
            } catch (Exception e) {
                return false;
            }
        }
        
        return true;
    }
    
    private BigDecimal getFixedAmount(Map<String, Object> parameters) {
        return convertToBigDecimal(parameters.get(FIXED_AMOUNT_KEY));
    }
    
    private BigDecimal getMinPurchaseAmount(Map<String, Object> parameters) {
        Object minPurchaseAmountObj = parameters.get(MIN_PURCHASE_AMOUNT_KEY);
        if (minPurchaseAmountObj != null) {
            return convertToBigDecimal(minPurchaseAmountObj);
        }
        return BigDecimal.ZERO; // 預設最低購買金額為 0
    }
    
    private BigDecimal getMaxDiscountAmount(Map<String, Object> parameters) {
        Object maxDiscountAmountObj = parameters.get(MAX_DISCOUNT_AMOUNT_KEY);
        if (maxDiscountAmountObj != null) {
            return convertToBigDecimal(maxDiscountAmountObj);
        }
        return null; // 沒有最大折扣限制
    }
    
    private BigDecimal getCustomerRelevantAmount(CustomerProfile customer) {
        // 對於固定金額策略，使用客戶的總交易金額作為參考
        // 如果沒有交易記錄，使用年收入的一定比例作為估算
        BigDecimal totalTransactionAmount = customer.calculateTotalTransactionAmount();
        
        if (totalTransactionAmount.compareTo(BigDecimal.ZERO) > 0) {
            return totalTransactionAmount;
        }
        
        // 如果沒有交易記錄，使用年收入的 10% 作為估算購買金額
        return customer.getBasicInfo().getAnnualIncome()
                      .multiply(BigDecimal.valueOf(0.1));
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