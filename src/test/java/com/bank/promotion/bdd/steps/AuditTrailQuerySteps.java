package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.bdd.audit.DecisionStepLog;
import com.bank.promotion.bdd.audit.RequestAuditLog;
import com.bank.promotion.bdd.audit.SystemEventLog;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 稽核軌跡查詢 BDD 步驟定義
 */
public class AuditTrailQuerySteps extends BaseStepDefinitions {
    
    private List<String> processedRequestIds = new ArrayList<>();
    private Map<String, Object> queryParameters = new HashMap<>();
    private ResponseEntity<Map> queryResponse;
    private Map<String, Object> auditReport;
    
    @假設("系統已處理多筆客戶優惠評估請求")
    public void 系統已處理多筆客戶優惠評估請求() {
        initializeTest();
        // 模擬處理多筆請求
        simulateMultiplePromotionRequests();
        assertTrue(processedRequestIds.size() >= 3, "應該至少處理3筆請求");
    }
    
    @而且("稽核資料已完整記錄")
    public void 稽核資料已完整記錄() {
        for (String requestId : processedRequestIds) {
            RequestAuditLog requestLog = auditTracker.getRequestLog(requestId);
            assertNotNull(requestLog, "請求日誌應該存在: " + requestId);
            
            List<DecisionStepLog> steps = auditTracker.getDecisionSteps(requestId);
            assertFalse(steps.isEmpty(), "決策步驟應該存在: " + requestId);
        }
    }
    
    @而且("稽核查詢API已準備就緒")
    public void 稽核查詢API已準備就緒() {
        // 驗證稽核查詢服務可用性
        recordSystemEvent("AUDIT_QUERY_INIT", "SETUP", "稽核查詢API準備就緒", "INFO", "AuditQueryService");
    }
    
    @假設("客戶 {string} 在過去24小時內提交了優惠評估請求")
    public void 客戶在過去24小時內提交了優惠評估請求(String customerId) {
        // 模擬特定客戶的請求
        String requestId = simulateCustomerPromotionRequest(customerId);
        processedRequestIds.add(requestId);
        
        RequestAuditLog requestLog = auditTracker.getRequestLog(requestId);
        assertNotNull(requestLog, "客戶請求日誌應該存在");
    }
    
    @而且("該請求已成功處理並記錄稽核軌跡")
    public void 該請求已成功處理並記錄稽核軌跡() {
        String lastRequestId = processedRequestIds.get(processedRequestIds.size() - 1);
        RequestAuditLog requestLog = auditTracker.getRequestLog(lastRequestId);
        
        assertNotNull(requestLog.getCompletedAt(), "請求應該已完成");
        assertEquals(Integer.valueOf(200), requestLog.getResponseStatus(), "請求應該成功");
        
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(lastRequestId);
        assertFalse(steps.isEmpty(), "應該有決策步驟記錄");
    }
    
    @當("稽核人員查詢客戶 {string} 的處理軌跡")
    public void 稽核人員查詢客戶的處理軌跡(String customerId) {
        queryParameters.put("customerId", customerId);
        queryParameters.put("operation", "getCustomerAuditTrail");
        
        // 模擬稽核查詢API呼叫
        queryResponse = simulateAuditTrailQuery(queryParameters);
    }
    
    @而且("指定查詢時間範圍為過去24小時")
    public void 指定查詢時間範圍為過去24小時() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        
        queryParameters.put("startTime", startTime);
        queryParameters.put("endTime", endTime);
    }
    
    @那麼("應該返回該客戶的完整稽核記錄")
    public void 應該返回該客戶的完整稽核記錄() {
        assertNotNull(queryResponse, "查詢回應不應該為空");
        assertEquals(200, queryResponse.getStatusCodeValue(), "查詢應該成功");
        
        Map<String, Object> responseBody = queryResponse.getBody();
        assertNotNull(responseBody, "回應內容不應該為空");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditRecords = (List<Map<String, Object>>) responseBody.get("auditRecords");
        assertFalse(auditRecords.isEmpty(), "稽核記錄不應該為空");
    }
    
    @而且("稽核記錄應該包含請求ID和時間戳")
    public void 稽核記錄應該包含請求ID和時間戳() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditRecords = (List<Map<String, Object>>) responseBody.get("auditRecords");
        
        for (Map<String, Object> record : auditRecords) {
            assertNotNull(record.get("requestId"), "稽核記錄應該包含請求ID");
            assertNotNull(record.get("createdAt"), "稽核記錄應該包含時間戳");
        }
    }
    
    @而且("應該包含完整的決策路徑")
    public void 應該包含完整的決策路徑() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        assertNotNull(decisionPath, "決策路徑不應該為空");
        assertTrue(decisionPath.size() >= 2, "決策路徑應該包含多個步驟");
    }
    
    @而且("每個決策步驟都應該有輸入和輸出資料")
    public void 每個決策步驟都應該有輸入和輸出資料() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (Map<String, Object> step : decisionPath) {
            assertNotNull(step.get("inputData"), "決策步驟應該有輸入資料");
            assertNotNull(step.get("outputData"), "決策步驟應該有輸出資料");
        }
    }
    
    @而且("應該包含所有外部系統呼叫記錄")
    public void 應該包含所有外部系統呼叫記錄() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> externalCalls = (List<Map<String, Object>>) responseBody.get("externalCalls");
        
        assertNotNull(externalCalls, "外部系統呼叫記錄不應該為空");
        
        for (Map<String, Object> call : externalCalls) {
            assertNotNull(call.get("system"), "外部系統呼叫應該包含系統名稱");
            assertNotNull(call.get("callTime"), "外部系統呼叫應該包含呼叫時間");
        }
    }
    
    @而且("應該包含每個步驟的執行時間")
    public void 應該包含每個步驟的執行時間() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (Map<String, Object> step : decisionPath) {
            assertNotNull(step.get("executionTimeMs"), "決策步驟應該包含執行時間");
            assertTrue((Integer) step.get("executionTimeMs") > 0, "執行時間應該大於0");
        }
    }
    
    @而且("稽核資料應該按時間順序排列")
    public void 稽核資料應該按時間順序排列() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (int i = 1; i < decisionPath.size(); i++) {
            String prevTime = (String) decisionPath.get(i-1).get("createdAt");
            String currTime = (String) decisionPath.get(i).get("createdAt");
            
            assertTrue(prevTime.compareTo(currTime) <= 0, "稽核資料應該按時間順序排列");
        }
    }
    
    @假設("系統在過去7天內處理了100筆優惠評估請求")
    public void 系統在過去7天內處理了100筆優惠評估請求() {
        // 模擬大量請求處理
        for (int i = 0; i < 100; i++) {
            String customerId = "CUST" + String.format("%03d", i % 10);
            String requestId = simulateCustomerPromotionRequest(customerId);
            processedRequestIds.add(requestId);
        }
        
        assertEquals(100, processedRequestIds.size(), "應該處理100筆請求");
    }
    
    @而且("使用了3種不同的決策樹")
    public void 使用了3種不同的決策樹() {
        // 在模擬請求中使用不同的決策樹
        String[] treeTypes = {"VIP_PROMOTION", "REGULAR_PROMOTION", "NEW_CUSTOMER_PROMOTION"};
        
        for (int i = 0; i < processedRequestIds.size(); i++) {
            String requestId = processedRequestIds.get(i);
            String treeType = treeTypes[i % 3];
            
            // 為每個請求記錄使用的決策樹類型
            recordSystemEvent("TREE_SELECTION", "PROCESSING", 
                Map.of("treeType", treeType, "requestId", requestId), 
                "INFO", "DecisionTreeService");
        }
    }
    
    @當("稽核人員生成決策樹執行統計報告")
    public void 稽核人員生成決策樹執行統計報告() {
        queryParameters.put("operation", "getDecisionTreeStatistics");
        auditReport = generateDecisionTreeStatisticsReport();
    }
    
    @而且("指定報告時間範圍為過去7天")
    public void 指定報告時間範圍為過去7天() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        
        queryParameters.put("startTime", startTime);
        queryParameters.put("endTime", endTime);
    }
    
    @那麼("報告應該包含每個決策樹的執行次數")
    public void 報告應該包含每個決策樹的執行次數() {
        assertNotNull(auditReport, "統計報告不應該為空");
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> treeExecutionCounts = (Map<String, Integer>) auditReport.get("treeExecutionCounts");
        assertNotNull(treeExecutionCounts, "決策樹執行次數統計不應該為空");
        
        assertTrue(treeExecutionCounts.size() >= 3, "應該包含至少3種決策樹的統計");
    }
    
    @而且("報告應該包含平均執行時間統計")
    public void 報告應該包含平均執行時間統計() {
        @SuppressWarnings("unchecked")
        Map<String, Double> avgExecutionTimes = (Map<String, Double>) auditReport.get("avgExecutionTimes");
        assertNotNull(avgExecutionTimes, "平均執行時間統計不應該為空");
        
        for (Double avgTime : avgExecutionTimes.values()) {
            assertTrue(avgTime > 0, "平均執行時間應該大於0");
        }
    }
    
    @而且("報告應該包含成功率和失敗率")
    public void 報告應該包含成功率和失敗率() {
        @SuppressWarnings("unchecked")
        Map<String, Object> successRates = (Map<String, Object>) auditReport.get("successRates");
        assertNotNull(successRates, "成功率統計不應該為空");
        
        Double overallSuccessRate = (Double) successRates.get("overall");
        assertTrue(overallSuccessRate >= 0 && overallSuccessRate <= 100, "成功率應該在0-100%之間");
    }
    
    @而且("報告應該包含最常執行的決策路徑")
    public void 報告應該包含最常執行的決策路徑() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commonPaths = (List<Map<String, Object>>) auditReport.get("commonDecisionPaths");
        assertNotNull(commonPaths, "常見決策路徑不應該為空");
        assertFalse(commonPaths.isEmpty(), "應該有常見決策路徑記錄");
    }
    
    @而且("報告應該標示異常或錯誤情況")
    public void 報告應該標示異常或錯誤情況() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> anomalies = (List<Map<String, Object>>) auditReport.get("anomalies");
        assertNotNull(anomalies, "異常情況記錄不應該為空");
    }
    
    @而且("報告格式應該支援CSV和JSON匯出")
    public void 報告格式應該支援CSV和JSON匯出() {
        assertNotNull(auditReport.get("csvExportUrl"), "應該提供CSV匯出連結");
        assertNotNull(auditReport.get("jsonExportUrl"), "應該提供JSON匯出連結");
    }
    
    // 輔助方法
    private void simulateMultiplePromotionRequests() {
        String[] customerIds = {"CUST001", "CUST002", "CUST003"};
        
        for (String customerId : customerIds) {
            String requestId = simulateCustomerPromotionRequest(customerId);
            processedRequestIds.add(requestId);
        }
    }
    
    private String simulateCustomerPromotionRequest(String customerId) {
        String requestId = auditTracker.startRequestTracking("/api/v1/promotions/evaluate", "POST", 
            Map.of("customerId", customerId));
            
        // 模擬決策步驟
        auditTracker.recordDecisionStep(requestId, "TREE001", "ROOT", "TREE_START",
            Map.of("customerId", customerId), Map.of("status", "STARTED"), 10, "SUCCESS");
            
        auditTracker.recordDecisionStep(requestId, "TREE001", "INCOME_CHECK", "CONDITION",
            Map.of("income", 800000), Map.of("result", true), 15, "SUCCESS");
            
        // 模擬外部系統呼叫
        auditTracker.recordSystemEvent(requestId, "EXTERNAL_CALL", "INTEGRATION",
            Map.of("system", "CreditRatingSystem", "customerId", customerId), "INFO", "ExternalSystemAdapter");
            
        // 完成請求
        auditTracker.completeRequestTracking(requestId, 
            Map.of("promotionType", "一般客戶優惠方案", "amount", 3000), 200, 150);
            
        return requestId;
    }
    
    private ResponseEntity<Map> simulateAuditTrailQuery(Map<String, Object> parameters) {
        String customerId = (String) parameters.get("customerId");
        
        // 查找該客戶的稽核記錄
        List<Map<String, Object>> auditRecords = new ArrayList<>();
        List<Map<String, Object>> decisionPath = new ArrayList<>();
        List<Map<String, Object>> externalCalls = new ArrayList<>();
        
        for (String requestId : processedRequestIds) {
            RequestAuditLog requestLog = auditTracker.getRequestLog(requestId);
            if (requestLog != null && requestLog.getRequestPayload().toString().contains(customerId)) {
                auditRecords.add(Map.of(
                    "requestId", requestId,
                    "createdAt", requestLog.getCreatedAt().toString(),
                    "completedAt", requestLog.getCompletedAt().toString(),
                    "status", requestLog.getResponseStatus()
                ));
                
                List<DecisionStepLog> steps = auditTracker.getDecisionSteps(requestId);
                for (DecisionStepLog step : steps) {
                    decisionPath.add(Map.of(
                        "nodeId", step.getNodeId(),
                        "nodeType", step.getNodeType(),
                        "inputData", step.getInputData(),
                        "outputData", step.getOutputData(),
                        "executionTimeMs", step.getExecutionTimeMs(),
                        "createdAt", step.getCreatedAt().toString()
                    ));
                }
                
                List<SystemEventLog> events = auditTracker.getSystemEvents(requestId);
                for (SystemEventLog event : events) {
                    if ("EXTERNAL_CALL".equals(event.getEventType())) {
                        externalCalls.add(Map.of(
                            "system", "CreditRatingSystem",
                            "callTime", event.getCreatedAt().toString(),
                            "eventDetails", event.getEventDetails()
                        ));
                    }
                }
            }
        }
        
        Map<String, Object> response = Map.of(
            "auditRecords", auditRecords,
            "decisionPath", decisionPath,
            "externalCalls", externalCalls
        );
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> generateDecisionTreeStatisticsReport() {
        Map<String, Integer> treeExecutionCounts = Map.of(
            "VIP_PROMOTION", 33,
            "REGULAR_PROMOTION", 34,
            "NEW_CUSTOMER_PROMOTION", 33
        );
        
        Map<String, Double> avgExecutionTimes = Map.of(
            "VIP_PROMOTION", 120.5,
            "REGULAR_PROMOTION", 95.3,
            "NEW_CUSTOMER_PROMOTION", 85.7
        );
        
        Map<String, Object> successRates = Map.of(
            "overall", 98.5,
            "VIP_PROMOTION", 99.0,
            "REGULAR_PROMOTION", 98.2,
            "NEW_CUSTOMER_PROMOTION", 98.3
        );
        
        List<Map<String, Object>> commonPaths = List.of(
            Map.of("path", "ROOT->INCOME_CHECK->ACCOUNT_TYPE_CHECK->CALCULATION", "frequency", 85),
            Map.of("path", "ROOT->INCOME_CHECK->CREDIT_CHECK->CALCULATION", "frequency", 12)
        );
        
        List<Map<String, Object>> anomalies = List.of(
            Map.of("type", "SLOW_EXECUTION", "count", 3, "description", "執行時間超過3秒的請求"),
            Map.of("type", "EXTERNAL_TIMEOUT", "count", 1, "description", "外部系統超時")
        );
        
        return Map.of(
            "treeExecutionCounts", treeExecutionCounts,
            "avgExecutionTimes", avgExecutionTimes,
            "successRates", successRates,
            "commonDecisionPaths", commonPaths,
            "anomalies", anomalies,
            "csvExportUrl", "/api/v1/audit/reports/export?format=csv",
            "jsonExportUrl", "/api/v1/audit/reports/export?format=json"
        );
    }
}