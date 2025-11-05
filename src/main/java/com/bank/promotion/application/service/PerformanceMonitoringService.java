package com.bank.promotion.application.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 效能監控服務
 * 記錄系統操作的效能指標
 */
@Service
public class PerformanceMonitoringService {
    
    private final Map<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    
    /**
     * 記錄操作執行時間
     */
    public void recordOperationTime(String operationType, long executionTimeMs) {
        if (operationType == null || operationType.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation type cannot be null or empty");
        }
        
        operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics())
                       .recordSuccess(executionTimeMs);
    }
    
    /**
     * 記錄操作錯誤
     */
    public void recordOperationError(String operationType, long executionTimeMs, Exception error) {
        if (operationType == null || operationType.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation type cannot be null or empty");
        }
        
        operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics())
                       .recordError(executionTimeMs, error);
    }
    
    /**
     * 獲取操作指標
     */
    public OperationMetrics getOperationMetrics(String operationType) {
        return operationMetrics.get(operationType);
    }
    
    /**
     * 獲取所有操作指標
     */
    public Map<String, OperationMetrics> getAllOperationMetrics() {
        return Map.copyOf(operationMetrics);
    }
    
    /**
     * 重置指標
     */
    public void resetMetrics() {
        operationMetrics.clear();
    }
    
    /**
     * 操作指標類別
     */
    public static class OperationMetrics {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private volatile String lastError;
        private volatile LocalDateTime lastErrorTime;
        
        public void recordSuccess(long executionTimeMs) {
            totalCount.incrementAndGet();
            successCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTimeMs);
            updateMinMax(executionTimeMs);
        }
        
        public void recordError(long executionTimeMs, Exception error) {
            totalCount.incrementAndGet();
            errorCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTimeMs);
            updateMinMax(executionTimeMs);
            this.lastError = error != null ? error.getMessage() : "Unknown error";
            this.lastErrorTime = LocalDateTime.now();
        }
        
        private void updateMinMax(long executionTimeMs) {
            minExecutionTime.updateAndGet(current -> Math.min(current, executionTimeMs));
            maxExecutionTime.updateAndGet(current -> Math.max(current, executionTimeMs));
        }
        
        public long getTotalCount() {
            return totalCount.get();
        }
        
        public long getSuccessCount() {
            return successCount.get();
        }
        
        public long getErrorCount() {
            return errorCount.get();
        }
        
        public double getSuccessRate() {
            long total = totalCount.get();
            return total > 0 ? (double) successCount.get() / total * 100 : 0.0;
        }
        
        public double getAverageExecutionTime() {
            long total = totalCount.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
        }
        
        public long getMinExecutionTime() {
            long min = minExecutionTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime.get();
        }
        
        public String getLastError() {
            return lastError;
        }
        
        public LocalDateTime getLastErrorTime() {
            return lastErrorTime;
        }
        
        @Override
        public String toString() {
            return "OperationMetrics{" +
                   "totalCount=" + totalCount.get() +
                   ", successCount=" + successCount.get() +
                   ", errorCount=" + errorCount.get() +
                   ", successRate=" + String.format("%.2f%%", getSuccessRate()) +
                   ", avgExecutionTime=" + String.format("%.2fms", getAverageExecutionTime()) +
                   ", minExecutionTime=" + getMinExecutionTime() + "ms" +
                   ", maxExecutionTime=" + getMaxExecutionTime() + "ms" +
                   '}';
        }
    }
}