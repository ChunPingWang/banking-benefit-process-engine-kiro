package com.bank.promotion.application.service.audit;

import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 稽核服務
 * 負責收集、儲存和查詢稽核資料
 */
@Service
public class AuditService {
    
    // 暫時使用記憶體儲存，實際應該使用資料庫
    private final Map<String, List<AuditTrail>> auditTrails = new ConcurrentHashMap<>();
    
    /**
     * 記錄優惠評估過程
     */
    public void recordPromotionEvaluation(String requestId, CustomerPayload customerPayload, 
                                        PromotionResult result, long executionTimeMs) {
        if (requestId == null || customerPayload == null || result == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        try {
            Map<String, Object> operationDetails = Map.of(
                "customerId", customerPayload.getCustomerId(),
                "accountType", customerPayload.getAccountType(),
                "promotionId", result.getPromotionId(),
                "promotionName", result.getPromotionName(),
                "discountAmount", result.getDiscountAmount() != null ? result.getDiscountAmount() : 0,
                "isEligible", result.isEligible()
            );
            
            AuditTrail auditTrail = new AuditTrail(
                requestId,
                customerPayload.getCustomerId(),
                "PROMOTION_EVALUATION",
                operationDetails,
                (int) executionTimeMs,
                result.isEligible() ? "SUCCESS" : "NO_PROMOTION",
                null
            );
            
            auditTrails.computeIfAbsent(requestId, k -> new ArrayList<>()).add(auditTrail);
            
        } catch (Exception e) {
            // 稽核記錄失敗不應該影響主要業務流程
            System.err.println("Failed to record audit trail: " + e.getMessage());
        }
    }
    
    /**
     * 記錄決策樹執行步驟
     */
    public void recordDecisionStep(String requestId, String treeId, String nodeId, 
                                 String nodeType, Object inputData, Object outputData, 
                                 long executionTimeMs, String status, String errorMessage) {
        if (requestId == null || treeId == null || nodeId == null) {
            throw new IllegalArgumentException("Request ID, tree ID and node ID cannot be null");
        }
        
        try {
            Map<String, Object> operationDetails = Map.of(
                "treeId", treeId,
                "nodeId", nodeId,
                "nodeType", nodeType != null ? nodeType : "UNKNOWN",
                "inputData", inputData != null ? inputData.toString() : "",
                "outputData", outputData != null ? outputData.toString() : ""
            );
            
            AuditTrail auditTrail = new AuditTrail(
                requestId,
                "SYSTEM", // 系統內部操作
                "DECISION_STEP",
                operationDetails,
                (int) executionTimeMs,
                status != null ? status : "UNKNOWN",
                errorMessage
            );
            
            auditTrails.computeIfAbsent(requestId, k -> new ArrayList<>()).add(auditTrail);
            
        } catch (Exception e) {
            System.err.println("Failed to record decision step: " + e.getMessage());
        }
    }
    
    /**
     * 記錄外部系統呼叫
     */
    public void recordExternalSystemCall(String requestId, String customerId, String systemName,
                                       String endpoint, Object requestData, Object responseData,
                                       long executionTimeMs, String status, String errorMessage) {
        if (requestId == null || systemName == null) {
            throw new IllegalArgumentException("Request ID and system name cannot be null");
        }
        
        try {
            Map<String, Object> operationDetails = Map.of(
                "systemName", systemName,
                "endpoint", endpoint != null ? endpoint : "",
                "requestData", requestData != null ? requestData.toString() : "",
                "responseData", responseData != null ? responseData.toString() : ""
            );
            
            AuditTrail auditTrail = new AuditTrail(
                requestId,
                customerId != null ? customerId : "SYSTEM",
                "EXTERNAL_SYSTEM_CALL",
                operationDetails,
                (int) executionTimeMs,
                status != null ? status : "UNKNOWN",
                errorMessage
            );
            
            auditTrails.computeIfAbsent(requestId, k -> new ArrayList<>()).add(auditTrail);
            
        } catch (Exception e) {
            System.err.println("Failed to record external system call: " + e.getMessage());
        }
    }
    
    /**
     * 記錄資料庫查詢
     */
    public void recordDatabaseQuery(String requestId, String customerId, String queryType,
                                  String query, Object result, long executionTimeMs,
                                  String status, String errorMessage) {
        if (requestId == null || queryType == null) {
            throw new IllegalArgumentException("Request ID and query type cannot be null");
        }
        
        try {
            Map<String, Object> operationDetails = Map.of(
                "queryType", queryType,
                "query", query != null ? query : "",
                "resultCount", result != null ? 1 : 0
            );
            
            AuditTrail auditTrail = new AuditTrail(
                requestId,
                customerId != null ? customerId : "SYSTEM",
                "DATABASE_QUERY",
                operationDetails,
                (int) executionTimeMs,
                status != null ? status : "UNKNOWN",
                errorMessage
            );
            
            auditTrails.computeIfAbsent(requestId, k -> new ArrayList<>()).add(auditTrail);
            
        } catch (Exception e) {
            System.err.println("Failed to record database query: " + e.getMessage());
        }
    }
    
    /**
     * 查詢稽核軌跡
     */
    public List<AuditTrail> getAuditTrails(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        
        return auditTrails.getOrDefault(requestId, new ArrayList<>());
    }
    
    /**
     * 查詢客戶相關的稽核軌跡
     */
    public List<AuditTrail> getCustomerAuditTrails(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        List<AuditTrail> customerTrails = new ArrayList<>();
        
        for (List<AuditTrail> trails : auditTrails.values()) {
            for (AuditTrail trail : trails) {
                if (customerId.equals(trail.getCustomerId())) {
                    if (startDate == null || trail.getCreatedAt().isAfter(startDate)) {
                        if (endDate == null || trail.getCreatedAt().isBefore(endDate)) {
                            customerTrails.add(trail);
                        }
                    }
                }
            }
        }
        
        return customerTrails;
    }
    
    /**
     * 清理過期的稽核資料
     */
    public void cleanupExpiredAuditData(LocalDateTime cutoffDate) {
        if (cutoffDate == null) {
            throw new IllegalArgumentException("Cutoff date cannot be null");
        }
        
        auditTrails.entrySet().removeIf(entry -> {
            List<AuditTrail> trails = entry.getValue();
            trails.removeIf(trail -> trail.getCreatedAt().isBefore(cutoffDate));
            return trails.isEmpty();
        });
    }
}