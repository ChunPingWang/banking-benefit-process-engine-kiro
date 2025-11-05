package com.bank.promotion.domain.command.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SpEL 表達式快取
 * 快取已解析的 SpEL 表達式以提高效能
 */
@Component
public class SpELExpressionCache {
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private final ConcurrentMap<String, Expression> expressionCache;
    private final int maxCacheSize;
    
    public SpELExpressionCache() {
        this.expressionCache = new ConcurrentHashMap<>();
        this.maxCacheSize = 1000; // 預設最大快取大小
    }
    
    public SpELExpressionCache(int maxCacheSize) {
        this.expressionCache = new ConcurrentHashMap<>();
        this.maxCacheSize = maxCacheSize;
    }
    
    /**
     * 獲取已快取的表達式，如果不存在則解析並快取
     * 
     * @param expressionString 表達式字串
     * @return 解析後的表達式
     * @throws org.springframework.expression.ParseException 當表達式語法錯誤時
     */
    public Expression getExpression(String expressionString) {
        if (expressionString == null || expressionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression string cannot be null or empty");
        }
        
        String normalizedExpression = expressionString.trim();
        
        // 嘗試從快取中獲取
        Expression cachedExpression = expressionCache.get(normalizedExpression);
        if (cachedExpression != null) {
            return cachedExpression;
        }
        
        // 檢查快取大小限制
        if (expressionCache.size() >= maxCacheSize) {
            // 簡單的快取清理策略：清除一半的快取
            clearHalfCache();
        }
        
        // 解析表達式並加入快取
        Expression newExpression = PARSER.parseExpression(normalizedExpression);
        expressionCache.put(normalizedExpression, newExpression);
        
        return newExpression;
    }
    
    /**
     * 檢查表達式是否已快取
     * 
     * @param expressionString 表達式字串
     * @return 是否已快取
     */
    public boolean isCached(String expressionString) {
        if (expressionString == null || expressionString.trim().isEmpty()) {
            return false;
        }
        return expressionCache.containsKey(expressionString.trim());
    }
    
    /**
     * 預載入表達式到快取
     * 
     * @param expressionString 表達式字串
     * @return 是否成功預載入
     */
    public boolean preloadExpression(String expressionString) {
        try {
            getExpression(expressionString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 移除快取中的表達式
     * 
     * @param expressionString 表達式字串
     * @return 是否成功移除
     */
    public boolean removeExpression(String expressionString) {
        if (expressionString == null || expressionString.trim().isEmpty()) {
            return false;
        }
        return expressionCache.remove(expressionString.trim()) != null;
    }
    
    /**
     * 清除所有快取
     */
    public void clearCache() {
        expressionCache.clear();
    }
    
    /**
     * 清除一半的快取 (簡單的LRU策略替代)
     */
    private void clearHalfCache() {
        int targetSize = maxCacheSize / 2;
        int currentSize = expressionCache.size();
        
        if (currentSize <= targetSize) {
            return;
        }
        
        // 移除一半的快取項目
        int toRemove = currentSize - targetSize;
        expressionCache.keySet().stream()
                      .limit(toRemove)
                      .forEach(expressionCache::remove);
    }
    
    /**
     * 獲取快取統計資訊
     * 
     * @return 快取統計資訊
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(
            expressionCache.size(),
            maxCacheSize,
            (double) expressionCache.size() / maxCacheSize
        );
    }
    
    /**
     * 快取統計資訊
     */
    public static class CacheStatistics {
        private final int currentSize;
        private final int maxSize;
        private final double utilizationRate;
        
        public CacheStatistics(int currentSize, int maxSize, double utilizationRate) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.utilizationRate = utilizationRate;
        }
        
        public int getCurrentSize() {
            return currentSize;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public double getUtilizationRate() {
            return utilizationRate;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStatistics{currentSize=%d, maxSize=%d, utilizationRate=%.2f%%}", 
                               currentSize, maxSize, utilizationRate * 100);
        }
    }
}