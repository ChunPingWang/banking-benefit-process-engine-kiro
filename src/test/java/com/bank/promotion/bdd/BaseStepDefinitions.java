package com.bank.promotion.bdd;

import com.bank.promotion.bdd.audit.TestAuditTracker;
import com.bank.promotion.bdd.mock.MockExternalSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * BDD 測試基礎類別
 * 提供共用的測試設定和工具方法
 */
public class BaseStepDefinitions {
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected TestAuditTracker auditTracker;
    
    @Autowired
    protected MockExternalSystemService mockExternalSystemService;
    
    @Autowired
    protected TestDataManager testDataManager;
    
    protected String baseUrl;
    protected String currentRequestId;
    
    protected void initializeTest() {
        baseUrl = "http://localhost:" + port;
        testDataManager.initializeTestData();
    }
    
    protected void cleanupTest() {
        testDataManager.cleanupTestData();
        currentRequestId = null;
    }
    
    /**
     * 獲取 API 端點完整 URL
     */
    protected String getApiUrl(String endpoint) {
        return baseUrl + endpoint;
    }
    
    /**
     * 開始請求追蹤
     */
    protected String startRequestTracking(String endpoint, String method, Object payload) {
        currentRequestId = auditTracker.startRequestTracking(endpoint, method, payload);
        return currentRequestId;
    }
    
    /**
     * 記錄決策步驟
     */
    protected void recordDecisionStep(String treeId, String nodeId, String nodeType,
                                    Object inputData, Object outputData, 
                                    long executionTimeMs, String status) {
        if (currentRequestId != null) {
            auditTracker.recordDecisionStep(currentRequestId, treeId, nodeId, nodeType,
                inputData, outputData, executionTimeMs, status);
        }
    }
    
    /**
     * 記錄系統事件
     */
    protected void recordSystemEvent(String eventType, String eventCategory,
                                   Object eventDetails, String severityLevel, String sourceComponent) {
        if (currentRequestId != null) {
            auditTracker.recordSystemEvent(currentRequestId, eventType, eventCategory,
                eventDetails, severityLevel, sourceComponent);
        }
    }
    
    /**
     * 完成請求追蹤
     */
    protected void completeRequestTracking(Object responsePayload, int responseStatus, long processingTimeMs) {
        if (currentRequestId != null) {
            auditTracker.completeRequestTracking(currentRequestId, responsePayload, 
                responseStatus, processingTimeMs);
        }
    }
}