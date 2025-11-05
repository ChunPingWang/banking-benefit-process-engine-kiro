package com.bank.promotion.bdd.audit;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 測試用稽核追蹤器
 * 記錄測試過程中的所有操作和決策步驟
 */
@Component
public class TestAuditTracker {
    
    private final Map<String, RequestAuditLog> requestLogs = new ConcurrentHashMap<>();
    private final Map<String, List<DecisionStepLog>> decisionSteps = new ConcurrentHashMap<>();
    private final Map<String, List<SystemEventLog>> systemEvents = new ConcurrentHashMap<>();
    
    /**
     * 開始追蹤新的請求
     */
    public String startRequestTracking(String apiEndpoint, String httpMethod, Object requestPayload) {
        String requestId = UUID.randomUUID().toString();
        RequestAuditLog log = new RequestAuditLog(
            requestId, 
            apiEndpoint, 
            httpMethod, 
            requestPayload, 
            LocalDateTime.now()
        );
        requestLogs.put(requestId, log);
        decisionSteps.put(requestId, new ArrayList<>());
        systemEvents.put(requestId, new ArrayList<>());
        return requestId;
    }
    
    /**
     * 記錄決策步驟
     */
    public void recordDecisionStep(String requestId, String treeId, String nodeId, 
                                 String nodeType, Object inputData, Object outputData, 
                                 long executionTimeMs, String status) {
        DecisionStepLog step = new DecisionStepLog(
            requestId, treeId, nodeId, nodeType, inputData, outputData, 
            executionTimeMs, status, LocalDateTime.now()
        );
        decisionSteps.computeIfAbsent(requestId, k -> new ArrayList<>()).add(step);
    }
    
    /**
     * 記錄系統事件
     */
    public void recordSystemEvent(String requestId, String eventType, String eventCategory, 
                                Object eventDetails, String severityLevel, String sourceComponent) {
        SystemEventLog event = new SystemEventLog(
            requestId, eventType, eventCategory, eventDetails, 
            severityLevel, sourceComponent, LocalDateTime.now()
        );
        systemEvents.computeIfAbsent(requestId, k -> new ArrayList<>()).add(event);
    }
    
    /**
     * 完成請求追蹤
     */
    public void completeRequestTracking(String requestId, Object responsePayload, 
                                      int responseStatus, long processingTimeMs) {
        RequestAuditLog log = requestLogs.get(requestId);
        if (log != null) {
            log.setResponsePayload(responsePayload);
            log.setResponseStatus(responseStatus);
            log.setProcessingTimeMs(processingTimeMs);
            log.setCompletedAt(LocalDateTime.now());
        }
    }
    
    /**
     * 獲取請求日誌
     */
    public RequestAuditLog getRequestLog(String requestId) {
        return requestLogs.get(requestId);
    }
    
    /**
     * 獲取決策步驟
     */
    public List<DecisionStepLog> getDecisionSteps(String requestId) {
        return decisionSteps.getOrDefault(requestId, Collections.emptyList());
    }
    
    /**
     * 獲取系統事件
     */
    public List<SystemEventLog> getSystemEvents(String requestId) {
        return systemEvents.getOrDefault(requestId, Collections.emptyList());
    }
    
    /**
     * 清理測試資料
     */
    public void clearAll() {
        requestLogs.clear();
        decisionSteps.clear();
        systemEvents.clear();
    }
    
    /**
     * 獲取所有請求ID
     */
    public Set<String> getAllRequestIds() {
        return new HashSet<>(requestLogs.keySet());
    }
}