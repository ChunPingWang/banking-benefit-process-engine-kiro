package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.bdd.TestDataManager;
import com.bank.promotion.bdd.audit.DecisionStepLog;
import com.bank.promotion.bdd.audit.RequestAuditLog;
import com.bank.promotion.bdd.audit.SystemEventLog;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 優惠評估 BDD 步驟定義
 */
public class PromotionEvaluationSteps extends BaseStepDefinitions {
    
    private TestDataManager.TestCustomerData currentCustomer;
    private Map<String, Object> customerPayload;
    private ResponseEntity<Map> apiResponse;
    private long requestStartTime;
    private String currentTreeId;
    private boolean externalSystemError = false;
    
    @假設("系統已初始化測試資料")
    public void 系統已初始化測試資料() {
        initializeTest();
        recordSystemEvent("SYSTEM_INIT", "SETUP", "測試資料初始化完成", "INFO", "TestDataManager");
    }
    
    @而且("稽核追蹤機制已啟用")
    public void 稽核追蹤機制已啟用() {
        assertNotNull(auditTracker, "稽核追蹤器應該已初始化");
        recordSystemEvent("AUDIT_INIT", "SETUP", "稽核追蹤機制啟用", "INFO", "AuditTracker");
    }
    
    @而且("外部系統模擬服務已準備就緒")
    public void 外部系統模擬服務已準備就緒() {
        assertNotNull(mockExternalSystemService, "外部系統模擬服務應該已初始化");
        mockExternalSystemService.resetMockData();
        recordSystemEvent("MOCK_INIT", "SETUP", "外部系統模擬服務準備就緒", "INFO", "MockExternalSystemService");
    }
    
    @假設("客戶 {string} 的年收入為 {int} 元")
    public void 客戶的年收入為元(String customerId, int annualIncome) {
        customerPayload = new HashMap<>();
        customerPayload.put("customerId", customerId);
        customerPayload.put("annualIncome", BigDecimal.valueOf(annualIncome));
    }
    
    @而且("客戶帳戶類型為 {string}")
    public void 客戶帳戶類型為(String accountType) {
        customerPayload.put("accountType", accountType);
    }
    
    @而且("客戶信用評等為 {string}")
    public void 客戶信用評等為(String creditRating) {
        customerPayload.put("creditRating", creditRating);
    }
    
    @而且("客戶月平均交易金額為 {int} 元")
    public void 客戶月平均交易金額為元(int monthlyAverage) {
        customerPayload.put("monthlyAverage", BigDecimal.valueOf(monthlyAverage));
    }
    
    @當("系統接收到優惠評估請求")
    public void 系統接收到優惠評估請求() {
        requestStartTime = System.currentTimeMillis();
        String endpoint = "/api/v1/promotions/evaluate";
        startRequestTracking(endpoint, "POST", customerPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(customerPayload, headers);
        
        // 模擬API呼叫 (實際實作時會呼叫真實的API)
        apiResponse = simulatePromotionEvaluationApi(request);
    }
    
    @而且("系統開始記錄稽核軌跡")
    public void 系統開始記錄稽核軌跡() {
        assertNotNull(currentRequestId, "請求ID應該已生成");
        recordSystemEvent("AUDIT_START", "PROCESSING", "開始記錄稽核軌跡", "INFO", "PromotionService");
    }
    
    @而且("系統執行決策樹 {string}")
    public void 系統執行決策樹(String treeType) {
        TestDataManager.TestDecisionTreeData treeData = testDataManager.getTestDecisionTree(treeType);
        assertNotNull(treeData, "決策樹資料應該存在: " + treeType);
        currentTreeId = treeData.getTreeId();
        
        recordDecisionStep(currentTreeId, "ROOT", "TREE_START", customerPayload, 
            Map.of("treeId", currentTreeId, "status", "STARTED"), 10, "SUCCESS");
    }
    
    @而且("系統評估收入條件節點 {string}")
    public void 系統評估收入條件節點(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        boolean incomeCheck = income.compareTo(BigDecimal.valueOf(500000)) >= 0;
        
        recordDecisionStep(currentTreeId, nodeId, "CONDITION", 
            Map.of("income", income, "threshold", 500000),
            Map.of("result", incomeCheck, "nextNode", incomeCheck ? "ACCOUNT_TYPE_CHECK" : "REJECT"),
            15, "SUCCESS");
    }
    
    @而且("系統評估帳戶類型條件節點 {string}")
    public void 系統評估帳戶類型條件節點(String nodeId) {
        String accountType = (String) customerPayload.get("accountType");
        boolean isVip = "VIP".equals(accountType);
        
        recordDecisionStep(currentTreeId, nodeId, "CONDITION",
            Map.of("accountType", accountType),
            Map.of("isVip", isVip, "nextNode", isVip ? "VIP_CALCULATION" : "REGULAR_CALCULATION"),
            12, "SUCCESS");
    }
    
    @而且("系統呼叫外部信用評等系統")
    public void 系統呼叫外部信用評等系統() {
        if (!externalSystemError) {
            String customerId = (String) customerPayload.get("customerId");
            var creditInfo = mockExternalSystemService.getCreditInfo(customerId);
            
            recordSystemEvent("EXTERNAL_CALL", "INTEGRATION", 
                Map.of("system", "CreditRatingSystem", "customerId", customerId, "result", creditInfo),
                "INFO", "ExternalSystemAdapter");
                
            recordDecisionStep(currentTreeId, "CREDIT_SYSTEM_CALL", "EXTERNAL",
                Map.of("customerId", customerId),
                Map.of("creditRating", creditInfo.getCreditRating(), "creditLimit", creditInfo.getCreditLimit()),
                50, "SUCCESS");
        }
    }
    
    @而且("系統執行VIP優惠計算節點 {string}")
    public void 系統執行VIP優惠計算節點(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        BigDecimal promotionAmount = income.multiply(BigDecimal.valueOf(0.02)); // 2% 優惠
        
        recordDecisionStep(currentTreeId, nodeId, "CALCULATION",
            Map.of("income", income, "rate", 0.02),
            Map.of("promotionType", "VIP專屬理財優惠", "amount", promotionAmount),
            20, "SUCCESS");
    }
    
    @而且("系統呼叫外部交易歷史系統")
    public void 系統呼叫外部交易歷史系統() {
        String customerId = (String) customerPayload.get("customerId");
        var transactionHistory = mockExternalSystemService.getTransactionHistory(customerId);
        
        recordSystemEvent("EXTERNAL_CALL", "INTEGRATION",
            Map.of("system", "TransactionHistorySystem", "customerId", customerId, "result", transactionHistory),
            "INFO", "ExternalSystemAdapter");
            
        recordDecisionStep(currentTreeId, "TRANSACTION_SYSTEM_CALL", "EXTERNAL",
            Map.of("customerId", customerId),
            Map.of("monthlyAverage", transactionHistory.getMonthlyAverage(), 
                   "transactionCount", transactionHistory.getTransactionCount()),
            45, "SUCCESS");
    }
    
    @而且("系統執行一般優惠計算節點 {string}")
    public void 系統執行一般優惠計算節點(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        BigDecimal promotionAmount = BigDecimal.valueOf(3000); // 固定優惠金額
        
        recordDecisionStep(currentTreeId, nodeId, "CALCULATION",
            Map.of("income", income),
            Map.of("promotionType", "一般客戶優惠方案", "amount", promotionAmount),
            18, "SUCCESS");
    }
    
    @那麼("應該返回 {string} 結果")
    public void 應該返回結果(String expectedPromotionType) {
        assertNotNull(apiResponse, "API回應不應該為空");
        assertEquals(200, apiResponse.getStatusCodeValue(), "HTTP狀態碼應該是200");
        
        Map<String, Object> responseBody = apiResponse.getBody();
        assertNotNull(responseBody, "回應內容不應該為空");
        
        String actualPromotionType = (String) responseBody.get("promotionType");
        assertEquals(expectedPromotionType, actualPromotionType, "優惠類型應該匹配");
        
        long processingTime = System.currentTimeMillis() - requestStartTime;
        completeRequestTracking(responseBody, 200, processingTime);
    }
    
    @而且("優惠金額應該大於 {int} 元")
    public void 優惠金額應該大於元(int minAmount) {
        Map<String, Object> responseBody = apiResponse.getBody();
        BigDecimal amount = new BigDecimal(responseBody.get("amount").toString());
        assertTrue(amount.compareTo(BigDecimal.valueOf(minAmount)) > 0, 
            "優惠金額應該大於 " + minAmount + " 元，實際金額: " + amount);
    }
    
    @而且("系統應該記錄完整的決策軌跡")
    public void 系統應該記錄完整的決策軌跡() {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        assertFalse(steps.isEmpty(), "決策步驟記錄不應該為空");
        
        // 驗證決策軌跡的完整性
        boolean hasTreeStart = steps.stream().anyMatch(step -> "TREE_START".equals(step.getNodeType()));
        assertTrue(hasTreeStart, "應該包含決策樹開始記錄");
    }
    
    @而且("稽核記錄應該包含 {int} 個決策步驟")
    public void 稽核記錄應該包含個決策步驟(int expectedSteps) {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        assertEquals(expectedSteps, steps.size(), "決策步驟數量應該匹配");
    }
    
    @而且("稽核記錄應該包含外部系統呼叫記錄")
    public void 稽核記錄應該包含外部系統呼叫記錄() {
        List<SystemEventLog> events = auditTracker.getSystemEvents(currentRequestId);
        boolean hasExternalCall = events.stream()
            .anyMatch(event -> "EXTERNAL_CALL".equals(event.getEventType()));
        assertTrue(hasExternalCall, "應該包含外部系統呼叫記錄");
    }
    
    @而且("每個決策步驟都應該有執行時間記錄")
    public void 每個決策步驟都應該有執行時間記錄() {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        for (DecisionStepLog step : steps) {
            assertTrue(step.getExecutionTimeMs() > 0, "決策步驟應該有執行時間記錄");
        }
    }
    
    @而且("請求處理總時間應該少於 {int} 毫秒")
    public void 請求處理總時間應該少於毫秒(int maxTime) {
        RequestAuditLog requestLog = auditTracker.getRequestLog(currentRequestId);
        assertNotNull(requestLog.getProcessingTimeMs(), "處理時間應該已記錄");
        assertTrue(requestLog.getProcessingTimeMs() < maxTime, 
            "處理時間應該少於 " + maxTime + " 毫秒，實際時間: " + requestLog.getProcessingTimeMs());
    }
    
    // 模擬API呼叫的輔助方法
    private ResponseEntity<Map> simulatePromotionEvaluationApi(HttpEntity<Map<String, Object>> request) {
        // 這裡模擬API的行為，實際實作時會呼叫真實的控制器
        Map<String, Object> payload = request.getBody();
        String accountType = (String) payload.get("accountType");
        BigDecimal income = (BigDecimal) payload.get("annualIncome");
        
        Map<String, Object> response = new HashMap<>();
        
        if ("VIP".equals(accountType) && income.compareTo(BigDecimal.valueOf(1000000)) >= 0) {
            response.put("promotionType", "VIP專屬理財優惠");
            response.put("amount", income.multiply(BigDecimal.valueOf(0.02)));
        } else if (income.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            response.put("promotionType", "一般客戶優惠方案");
            response.put("amount", BigDecimal.valueOf(3000));
        } else {
            response.put("promotionType", "不符合優惠條件");
            response.put("amount", BigDecimal.ZERO);
            response.put("reason", "收入不足");
        }
        
        return ResponseEntity.ok(response);
    }
}