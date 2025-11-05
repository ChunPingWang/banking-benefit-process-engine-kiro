package com.bank.promotion.domain.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 計算策略工廠
 * 負責創建和管理不同類型的計算策略
 */
@Component
public class CalculationStrategyFactory {
    
    private final Map<String, CalculationStrategy> strategies;
    
    public CalculationStrategyFactory() {
        this.strategies = new HashMap<>();
        initializeStrategies();
    }
    
    /**
     * 初始化所有可用的策略
     */
    private void initializeStrategies() {
        // 註冊百分比折扣策略
        PercentageDiscountStrategy percentageStrategy = new PercentageDiscountStrategy();
        strategies.put(percentageStrategy.getStrategyType(), percentageStrategy);
        
        // 註冊階層式折扣策略
        TieredDiscountStrategy tieredStrategy = new TieredDiscountStrategy();
        strategies.put(tieredStrategy.getStrategyType(), tieredStrategy);
        
        // 註冊固定金額策略
        FixedAmountStrategy fixedAmountStrategy = new FixedAmountStrategy();
        strategies.put(fixedAmountStrategy.getStrategyType(), fixedAmountStrategy);
    }
    
    /**
     * 根據策略類型獲取計算策略
     * 
     * @param strategyType 策略類型
     * @return 對應的計算策略
     * @throws IllegalArgumentException 如果策略類型不存在
     */
    public CalculationStrategy getStrategy(String strategyType) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            throw new IllegalArgumentException("Strategy type cannot be null or empty");
        }
        
        CalculationStrategy strategy = strategies.get(strategyType.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy type: " + strategyType);
        }
        
        return strategy;
    }
    
    /**
     * 檢查是否支援指定的策略類型
     * 
     * @param strategyType 策略類型
     * @return 是否支援該策略類型
     */
    public boolean isStrategySupported(String strategyType) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            return false;
        }
        
        return strategies.containsKey(strategyType.toUpperCase());
    }
    
    /**
     * 獲取所有支援的策略類型
     * 
     * @return 支援的策略類型集合
     */
    public Set<String> getSupportedStrategyTypes() {
        return strategies.keySet();
    }
    
    /**
     * 註冊新的計算策略
     * 
     * @param strategy 要註冊的策略
     * @throws IllegalArgumentException 如果策略為 null 或策略類型已存在
     */
    public void registerStrategy(CalculationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        
        String strategyType = strategy.getStrategyType();
        if (strategyType == null || strategyType.trim().isEmpty()) {
            throw new IllegalArgumentException("Strategy type cannot be null or empty");
        }
        
        if (strategies.containsKey(strategyType.toUpperCase())) {
            throw new IllegalArgumentException("Strategy type already exists: " + strategyType);
        }
        
        strategies.put(strategyType.toUpperCase(), strategy);
    }
    
    /**
     * 移除指定的計算策略
     * 
     * @param strategyType 要移除的策略類型
     * @return 是否成功移除
     */
    public boolean removeStrategy(String strategyType) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            return false;
        }
        
        return strategies.remove(strategyType.toUpperCase()) != null;
    }
    
    /**
     * 獲取策略數量
     * 
     * @return 已註冊的策略數量
     */
    public int getStrategyCount() {
        return strategies.size();
    }
    
    /**
     * 清空所有策略（主要用於測試）
     */
    public void clearAllStrategies() {
        strategies.clear();
    }
    
    /**
     * 重新初始化預設策略
     */
    public void reinitializeDefaultStrategies() {
        clearAllStrategies();
        initializeStrategies();
    }
}