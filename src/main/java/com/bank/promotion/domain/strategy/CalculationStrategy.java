package com.bank.promotion.domain.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 計算策略介面
 * 定義不同的優惠計算策略
 */
public interface CalculationStrategy {
    
    /**
     * 計算優惠結果
     * 
     * @param customer 客戶檔案
     * @param parameters 計算參數
     * @return 計算後的優惠金額
     */
    BigDecimal calculate(CustomerProfile customer, Map<String, Object> parameters);
    
    /**
     * 建立優惠結果
     * 
     * @param customer 客戶檔案
     * @param calculatedAmount 計算後的優惠金額
     * @param parameters 計算參數
     * @return 完整的優惠結果
     */
    PromotionResult createPromotionResult(CustomerProfile customer, BigDecimal calculatedAmount, Map<String, Object> parameters);
    
    /**
     * 取得策略類型
     * 
     * @return 策略類型名稱
     */
    String getStrategyType();
    
    /**
     * 驗證計算參數
     * 
     * @param parameters 計算參數
     * @return 參數是否有效
     */
    boolean validateParameters(Map<String, Object> parameters);
}