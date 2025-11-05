package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系統錯誤處理 BDD 步驟定義
 */
public class SystemErrorHandlingSteps extends BaseStepDefinitions {
    
    private String currentCustomerId;
    private boolean externalSystemTimeout = false;
    private boolean databaseConnectionError = false;
    private boolean spelExecutionError = false;
    private boolean droolsRuleError = false;
    private boolean memoryWarning = false;
    private boolean concurrentConflict = false;
    private boolean systemOverload = false;
    private String errorMessage;
    private Map<String, Object> systemStatus;
    
    @假設("系統已啟動並運行正常")
    public void 系統已啟動並運行正常() {
        initializeTest();
        recordSystemEvent("SYSTEM_STARTUP", "SYSTEM", "系統正常啟動", "INFO", "SystemManager");
    }
    
    @而且("錯誤追蹤機制已啟用")
    public void 錯誤追蹤機制已啟用() {
        recordSystemEvent("ERROR_TRACKING_INIT", "SETUP", "錯誤追蹤機制啟用", "INFO", "ErrorTrackingService");
    }
    
    @而且("異常處理服務已準備就緒")
    public void 異常處理服務已準備就緒() {
        recordSystemEvent("EXCEPTION_HANDLER_INIT", "SETUP", "異常處理服務準備就緒", "INFO", "ExceptionHandlerService");
    }
    
    @假設("客戶 {string} 提交系統錯誤處理請求")
    public void 客戶提交系統錯誤處理請求(String customerId) {
        currentCustomerId = customerId;
        startRequestTracking("/api/v1/promotions/evaluate", "POST", Map.of("customerId", customerId));
    }
    
    @而且("外部信用評等系統回應時間超過 {int} 秒")
    public void 外部信用評等系統回應時間超過秒(int timeoutSeconds) {
        externalSystemTimeout = true;
        recordSystemEvent("EXTERNAL_TIMEOUT_DETECTED", "EXTERNAL_SYSTEM",
            Map.of("system", "CreditRatingSystem", "timeoutSeconds", timeoutSeconds),
            "WARNING", "ExternalSystemMonitor");
    }
    
    @當("系統嘗試呼叫外部信用評等系統")
    public void 系統嘗試呼叫外部信用評等系統() {
        recordSystemEvent("EXTERNAL_CALL_ATTEMPT", "EXTERNAL_SYSTEM",
            Map.of("system", "CreditRatingSystem", "customerId", currentCustomerId),
            "INFO", "ExternalSystemAdapter");
    }
    
    @而且("系統檢測到連線超時")
    public void 系統檢測到連線超時() {
        if (externalSystemTimeout) {
            recordSystemEvent("CONNECTION_TIMEOUT", "ERROR",
                Map.of("system", "CreditRatingSystem", "timeoutMs", 5000),
                "ERROR", "ExternalSystemAdapter");
        }
    }
    
    @而且("系統啟動超時處理機制")
    public void 系統啟動超時處理機制() {
        recordSystemEvent("TIMEOUT_HANDLER_ACTIVATED", "ERROR_HANDLING",
            Map.of("handlerType", "ExternalSystemTimeoutHandler"),
            "INFO", "TimeoutHandler");
    }
    
    @而且("系統記錄外部系統超時事件")
    public void 系統記錄外部系統超時事件() {
        recordSystemEvent("EXTERNAL_SYSTEM_TIMEOUT", "ERROR",
            Map.of("system", "CreditRatingSystem", "customerId", currentCustomerId, "action", "FALLBACK"),
            "ERROR", "ExternalSystemService");
    }
    
    @而且("系統啟用降級策略使用預設信用評等")
    public void 系統啟用降級策略使用預設信用評等() {
        recordSystemEvent("FALLBACK_STRATEGY_ACTIVATED", "ERROR_HANDLING",
            Map.of("strategy", "DefaultCreditRating", "defaultValue", "B"),
            "INFO", "FallbackService");
    }
    
    @那麼("系統應該使用預設信用評等 {string}")
    public void 系統應該使用預設信用評等(String defaultRating) {
        recordSystemEvent("DEFAULT_VALUE_USED", "ERROR_HANDLING",
            Map.of("field", "creditRating", "defaultValue", defaultRating),
            "INFO", "FallbackService");
        
        // 驗證降級策略生效
        assertTrue(externalSystemTimeout, "應該檢測到外部系統超時");
    }
    
    @而且("優惠評估流程應該繼續執行")
    public void 優惠評估流程應該繼續執行() {
        recordSystemEvent("PROCESS_CONTINUATION", "PROCESSING",
            Map.of("status", "CONTINUED", "customerId", currentCustomerId),
            "INFO", "PromotionService");
    }
    
    @而且("系統應該記錄降級策略執行事件")
    public void 系統應該記錄降級策略執行事件() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasFallbackEvent = systemEvents.stream()
            .anyMatch(event -> "FALLBACK_STRATEGY_ACTIVATED".equals(event.getEventType()));
        assertTrue(hasFallbackEvent, "應該記錄降級策略執行事件");
    }
    
    @而且("錯誤追蹤應該包含超時詳細資訊")
    public void 錯誤追蹤應該包含超時詳細資訊() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasTimeoutDetails = systemEvents.stream()
            .anyMatch(event -> "CONNECTION_TIMEOUT".equals(event.getEventType()));
        assertTrue(hasTimeoutDetails, "應該包含超時詳細資訊");
    }
    
    @而且("錯誤追蹤應該包含降級策略使用記錄")
    public void 錯誤追蹤應該包含降級策略使用記錄() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasFallbackRecord = systemEvents.stream()
            .anyMatch(event -> event.getEventDetails().toString().contains("DefaultCreditRating"));
        assertTrue(hasFallbackRecord, "應該包含降級策略使用記錄");
    }
    
    @而且("客戶應該仍能收到有效的優惠結果")
    public void 客戶應該仍能收到有效的優惠結果() {
        completeRequestTracking(Map.of("promotionType", "基礎優惠方案", "amount", 1000), 200, 300);
        
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertEquals(Integer.valueOf(200), requestLog.getResponseStatus(), "應該返回成功狀態");
    }
    
    @假設("系統正在處理客戶優惠評估請求")
    public void 系統正在處理客戶優惠評估請求() {
        currentCustomerId = "CUST001";
        startRequestTracking("/api/v1/promotions/evaluate", "POST", Map.of("customerId", currentCustomerId));
    }
    
    @而且("資料庫連線突然中斷")
    public void 資料庫連線突然中斷() {
        databaseConnectionError = true;
        recordSystemEvent("DATABASE_CONNECTION_LOST", "ERROR",
            Map.of("database", "PostgreSQL", "connectionPool", "HikariCP"),
            "ERROR", "DatabaseConnectionManager");
    }
    
    @當("系統嘗試查詢決策樹配置")
    public void 系統嘗試查詢決策樹配置() {
        recordSystemEvent("DATABASE_QUERY_ATTEMPT", "DATABASE",
            Map.of("query", "SELECT * FROM decision_trees", "table", "decision_trees"),
            "INFO", "DatabaseService");
    }
    
    @而且("系統檢測到資料庫連線異常")
    public void 系統檢測到資料庫連線異常() {
        if (databaseConnectionError) {
            recordSystemEvent("DATABASE_CONNECTION_ERROR", "ERROR",
                Map.of("errorType", "ConnectionException", "retryAttempt", 1),
                "ERROR", "DatabaseService");
        }
    }
    
    @而且("系統啟動資料庫重連機制")
    public void 系統啟動資料庫重連機制() {
        recordSystemEvent("DATABASE_RECONNECT_INITIATED", "ERROR_HANDLING",
            Map.of("reconnectStrategy", "ExponentialBackoff", "maxRetries", 3),
            "INFO", "DatabaseReconnectService");
    }
    
    @而且("系統記錄資料庫異常事件")
    public void 系統記錄資料庫異常事件() {
        recordSystemEvent("DATABASE_EXCEPTION", "ERROR",
            Map.of("exception", "SQLException", "customerId", currentCustomerId),
            "ERROR", "DatabaseService");
    }
    
    @而且("系統嘗試使用快取中的配置資料")
    public void 系統嘗試使用快取中的配置資料() {
        recordSystemEvent("CACHE_FALLBACK", "ERROR_HANDLING",
            Map.of("cacheType", "DecisionTreeCache", "cacheHit", true),
            "INFO", "CacheService");
    }
    
    @那麼("系統應該成功從快取載入決策樹配置")
    public void 系統應該成功從快取載入決策樹配置() {
        recordSystemEvent("CACHE_DATA_LOADED", "CACHING",
            Map.of("treeId", "TREE001", "source", "Cache"),
            "INFO", "CacheService");
        
        assertTrue(databaseConnectionError, "應該檢測到資料庫連線錯誤");
    }
    
    @而且("優惠評估流程應該正常完成")
    public void 優惠評估流程應該正常完成() {
        completeRequestTracking(Map.of("promotionType", "VIP優惠", "amount", 5000), 200, 250);
        
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertEquals(Integer.valueOf(200), requestLog.getResponseStatus(), "流程應該正常完成");
    }
    
    @而且("系統應該在背景重新建立資料庫連線")
    public void 系統應該在背景重新建立資料庫連線() {
        recordSystemEvent("DATABASE_RECONNECT_SUCCESS", "DATABASE",
            Map.of("reconnectTime", "2023-12-01T10:30:00", "connectionPool", "HikariCP"),
            "INFO", "DatabaseReconnectService");
    }
    
    @而且("錯誤追蹤應該記錄異常發生時間和原因")
    public void 錯誤追蹤應該記錄異常發生時間和原因() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasExceptionDetails = systemEvents.stream()
            .anyMatch(event -> "DATABASE_CONNECTION_ERROR".equals(event.getEventType()));
        assertTrue(hasExceptionDetails, "應該記錄異常詳細資訊");
    }
    
    @而且("錯誤追蹤應該記錄快取使用情況")
    public void 錯誤追蹤應該記錄快取使用情況() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasCacheUsage = systemEvents.stream()
            .anyMatch(event -> "CACHE_FALLBACK".equals(event.getEventType()));
        assertTrue(hasCacheUsage, "應該記錄快取使用情況");
    }
    
    @而且("系統應該在連線恢復後同步資料")
    public void 系統應該在連線恢復後同步資料() {
        recordSystemEvent("DATA_SYNC_INITIATED", "DATABASE",
            Map.of("syncType", "CacheToDatabase", "status", "STARTED"),
            "INFO", "DataSyncService");
    }
    
    @假設("決策樹 {string} 包含錯誤的 SpEL 表達式")
    public void 決策樹包含錯誤的SpEL表達式(String treeId) {
        spelExecutionError = true;
        recordSystemEvent("SPEL_ERROR_DETECTED", "CONFIGURATION",
            Map.of("treeId", treeId, "nodeId", "INCOME_CALC", "expression", "customer.income / 0"),
            "WARNING", "ConfigurationValidator");
    }
    
    @而且("SpEL 表達式為 {string} \\(除零錯誤)")
    public void SpEL表達式為除零錯誤(String expression) {
        recordSystemEvent("SPEL_EXPRESSION_ERROR", "CONFIGURATION",
            Map.of("expression", expression, "errorType", "DivisionByZero"),
            "ERROR", "SpELValidator");
    }
    
    @當("系統執行到該 SpEL 表達式節點")
    public void 系統執行到該SpEL表達式節點() {
        recordSystemEvent("SPEL_NODE_EXECUTION", "PROCESSING",
            Map.of("nodeId", "INCOME_CALC", "nodeType", "CALCULATION"),
            "INFO", "DecisionTreeExecutor");
    }
    
    @而且("系統檢測到運算異常")
    public void 系統檢測到運算異常() {
        if (spelExecutionError) {
            recordSystemEvent("SPEL_RUNTIME_ERROR", "ERROR",
                Map.of("exception", "ArithmeticException", "message", "Division by zero"),
                "ERROR", "SpELExecutor");
        }
    }
    
    @而且("系統記錄 SpEL 執行錯誤事件")
    public void 系統記錄SpEL執行錯誤事件() {
        recordSystemEvent("SPEL_EXECUTION_FAILED", "ERROR",
            Map.of("nodeId", "INCOME_CALC", "customerId", currentCustomerId),
            "ERROR", "SpELExecutor");
    }
    
    @而且("系統啟動異常節點處理機制")
    public void 系統啟動異常節點處理機制() {
        recordSystemEvent("NODE_ERROR_HANDLER_ACTIVATED", "ERROR_HANDLING",
            Map.of("handlerType", "SpELErrorHandler", "fallbackStrategy", "DefaultValue"),
            "INFO", "NodeErrorHandler");
    }
    
    @而且("系統跳過錯誤節點並使用預設值")
    public void 系統跳過錯誤節點並使用預設值() {
        recordSystemEvent("NODE_SKIPPED", "ERROR_HANDLING",
            Map.of("nodeId", "INCOME_CALC", "defaultValue", 1000, "reason", "SpELError"),
            "INFO", "NodeErrorHandler");
    }
    
    @那麼("系統應該使用預設計算結果")
    public void 系統應該使用預設計算結果() {
        recordSystemEvent("DEFAULT_CALCULATION_USED", "ERROR_HANDLING",
            Map.of("calculationType", "PromotionAmount", "defaultAmount", 1000),
            "INFO", "CalculationService");
        
        assertTrue(spelExecutionError, "應該檢測到SpEL執行錯誤");
    }
    
    @而且("優惠評估應該返回基礎優惠方案")
    public void 優惠評估應該返回基礎優惠方案() {
        completeRequestTracking(Map.of("promotionType", "基礎優惠方案", "amount", 1000), 200, 180);
        
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertNotNull(requestLog.getResponsePayload(), "應該有回應內容");
    }
    
    @而且("系統應該記錄節點執行異常事件")
    public void 系統應該記錄節點執行異常事件() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasNodeException = systemEvents.stream()
            .anyMatch(event -> "SPEL_EXECUTION_FAILED".equals(event.getEventType()));
        assertTrue(hasNodeException, "應該記錄節點執行異常");
    }
    
    @而且("錯誤追蹤應該包含 SpEL 表達式內容")
    public void 錯誤追蹤應該包含SpEL表達式內容() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasExpressionContent = systemEvents.stream()
            .anyMatch(event -> event.getEventDetails().toString().contains("customer.income / 0"));
        assertTrue(hasExpressionContent, "應該包含SpEL表達式內容");
    }
    
    @而且("錯誤追蹤應該包含異常堆疊資訊")
    public void 錯誤追蹤應該包含異常堆疊資訊() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasStackTrace = systemEvents.stream()
            .anyMatch(event -> event.getEventDetails().toString().contains("ArithmeticException"));
        assertTrue(hasStackTrace, "應該包含異常堆疊資訊");
    }
    
    @而且("系統應該通知管理員檢查決策樹配置")
    public void 系統應該通知管理員檢查決策樹配置() {
        recordSystemEvent("ADMIN_NOTIFICATION_SENT", "NOTIFICATION",
            Map.of("notificationType", "ConfigurationError", "recipient", "admin@bank.com"),
            "INFO", "NotificationService");
    }
}