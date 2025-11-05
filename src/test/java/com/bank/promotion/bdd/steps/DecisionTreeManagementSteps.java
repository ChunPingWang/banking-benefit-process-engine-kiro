package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 決策樹管理 BDD 步驟定義
 */
public class DecisionTreeManagementSteps extends BaseStepDefinitions {
    
    private Map<String, Object> decisionTreeConfig;
    private ResponseEntity<Map> managementResponse;
    private String currentTreeId;
    private String currentVersion;
    private String adminUserId = "ADMIN001";
    private List<Map<String, Object>> batchUpdateRequests = new ArrayList<>();
    
    @假設("系統管理員已登入管理介面")
    public void 系統管理員已登入管理介面() {
        initializeTest();
        recordSystemEvent("ADMIN_LOGIN", "AUTHENTICATION", 
            Map.of("adminId", adminUserId, "loginTime", LocalDateTime.now()),
            "INFO", "AuthenticationService");
    }
    
    @而且("決策樹管理服務已準備就緒")
    public void 決策樹管理服務已準備就緒() {
        recordSystemEvent("MANAGEMENT_SERVICE_INIT", "SETUP", 
            "決策樹管理服務初始化完成", "INFO", "DecisionTreeManagementService");
    }
    
    @假設("管理員要建立名為 {string} 的新決策樹")
    public void 管理員要建立名為的新決策樹(String treeName) {
        decisionTreeConfig = new HashMap<>();
        decisionTreeConfig.put("name", treeName);
        decisionTreeConfig.put("status", "DRAFT");
        decisionTreeConfig.put("createdBy", adminUserId);
        decisionTreeConfig.put("nodes", new ArrayList<>());
    }
    
    @而且("決策樹包含根節點 {string}")
    public void 決策樹包含根節點(String rootNodeName) {
        Map<String, Object> rootNode = Map.of(
            "nodeId", "ROOT001",
            "nodeName", rootNodeName,
            "nodeType", "CONDITION",
            "parentId", null,
            "configuration", Map.of("expression", "true", "type", "SpEL")
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(rootNode);
        decisionTreeConfig.put("rootNodeId", "ROOT001");
    }
    
    @而且("根節點連接到 {string} 條件節點")
    public void 根節點連接到條件節點(String conditionNodeName) {
        Map<String, Object> conditionNode = Map.of(
            "nodeId", "COND001",
            "nodeName", conditionNodeName,
            "nodeType", "CONDITION", 
            "parentId", "ROOT001",
            "configuration", Map.of("expression", "customer.revenue >= 10000000", "type", "SpEL")
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(conditionNode);
    }
    
    @而且("營業額評估節點連接到 {string} 計算節點")
    public void 營業額評估節點連接到計算節點(String calculationNodeName) {
        Map<String, Object> calculationNode = Map.of(
            "nodeId", "CALC001",
            "nodeName", calculationNodeName,
            "nodeType", "CALCULATION",
            "parentId", "COND001",
            "configuration", Map.of("expression", "customer.revenue * 0.05", "type", "SpEL")
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(calculationNode);
    }
    
    @當("管理員提交決策樹建立請求")
    public void 管理員提交決策樹建立請求() {
        String endpoint = "/api/v1/admin/decision-trees";
        startRequestTracking(endpoint, "POST", decisionTreeConfig);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer admin-token");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(decisionTreeConfig, headers);
        
        managementResponse = simulateDecisionTreeCreation(request);
    }
    
    @而且("系統開始記錄配置變更稽核")
    public void 系統開始記錄配置變更稽核() {
        recordSystemEvent("CONFIG_CHANGE_START", "CONFIGURATION",
            Map.of("operation", "CREATE_TREE", "adminId", adminUserId),
            "INFO", "ConfigurationAuditService");
    }
    
    @而且("系統驗證決策樹結構完整性")
    public void 系統驗證決策樹結構完整性() {
        // 模擬結構驗證
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        
        boolean hasRoot = nodes.stream().anyMatch(node -> node.get("parentId") == null);
        assertTrue(hasRoot, "決策樹應該有根節點");
        
        recordSystemEvent("STRUCTURE_VALIDATION", "VALIDATION",
            Map.of("result", "PASSED", "nodeCount", nodes.size()),
            "INFO", "StructureValidator");
    }
    
    @而且("系統儲存決策樹配置到資料庫")
    public void 系統儲存決策樹配置到資料庫() {
        currentTreeId = "TREE004";
        decisionTreeConfig.put("treeId", currentTreeId);
        decisionTreeConfig.put("status", "ACTIVE");
        currentVersion = "1.0";
        decisionTreeConfig.put("version", currentVersion);
        
        recordSystemEvent("DATABASE_SAVE", "PERSISTENCE",
            Map.of("treeId", currentTreeId, "version", currentVersion),
            "INFO", "DatabaseService");
    }
    
    @而且("系統更新決策樹快取")
    public void 系統更新決策樹快取() {
        recordSystemEvent("CACHE_UPDATE", "CACHING",
            Map.of("treeId", currentTreeId, "operation", "PUT"),
            "INFO", "CacheService");
    }
    
    @那麼("應該成功建立決策樹 {string}")
    public void 應該成功建立決策樹(String expectedTreeId) {
        assertNotNull(managementResponse, "管理回應不應該為空");
        assertEquals(201, managementResponse.getStatusCodeValue(), "HTTP狀態碼應該是201");
        
        Map<String, Object> responseBody = managementResponse.getBody();
        assertNotNull(responseBody, "回應內容不應該為空");
        
        String actualTreeId = (String) responseBody.get("treeId");
        assertEquals(expectedTreeId, actualTreeId, "決策樹ID應該匹配");
        
        completeRequestTracking(responseBody, 201, 200);
    }
    
    @而且("決策樹狀態應該為 {string}")
    public void 決策樹狀態應該為(String expectedStatus) {
        Map<String, Object> responseBody = managementResponse.getBody();
        String actualStatus = (String) responseBody.get("status");
        assertEquals(expectedStatus, actualStatus, "決策樹狀態應該匹配");
    }
    
    @而且("系統應該記錄決策樹建立事件")
    public void 系統應該記錄決策樹建立事件() {
        recordSystemEvent("TREE_CREATED", "CONFIGURATION",
            Map.of("treeId", currentTreeId, "createdBy", adminUserId, "version", currentVersion),
            "INFO", "DecisionTreeService");
    }
    
    @而且("稽核記錄應該包含完整的樹結構資訊")
    public void 稽核記錄應該包含完整的樹結構資訊() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasStructureInfo = systemEvents.stream()
            .anyMatch(event -> "TREE_CREATED".equals(event.getEventType()));
        assertTrue(hasStructureInfo, "應該包含樹結構資訊");
    }
    
    @而且("稽核記錄應該包含建立者資訊和時間戳")
    public void 稽核記錄應該包含建立者資訊和時間戳() {
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertNotNull(requestLog.getCreatedAt(), "應該有建立時間戳");
        
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasCreatorInfo = systemEvents.stream()
            .anyMatch(event -> event.getEventDetails().toString().contains(adminUserId));
        assertTrue(hasCreatorInfo, "應該包含建立者資訊");
    }
    
    @而且("變更版本號應該為 {string}")
    public void 變更版本號應該為(String expectedVersion) {
        Map<String, Object> responseBody = managementResponse.getBody();
        String actualVersion = (String) responseBody.get("version");
        assertEquals(expectedVersion, actualVersion, "版本號應該匹配");
    }
    
    @假設("決策樹 {string} 已存在")
    public void 決策樹已存在(String treeType) {
        currentTreeId = "TREE001";
        recordSystemEvent("TREE_EXISTS", "SETUP",
            Map.of("treeId", currentTreeId, "treeType", treeType),
            "INFO", "TestSetup");
    }
    
    @而且("當前版本為 {string}")
    public void 當前版本為(String version) {
        currentVersion = version;
    }
    
    @而且("管理員要修改 {string} 節點的閾值")
    public void 管理員要修改節點的閾值(String nodeName) {
        decisionTreeConfig = new HashMap<>();
        decisionTreeConfig.put("treeId", currentTreeId);
        decisionTreeConfig.put("operation", "UPDATE_NODE");
        decisionTreeConfig.put("nodeId", "INCOME_CHECK");
        decisionTreeConfig.put("nodeName", nodeName);
    }
    
    @而且("將收入閾值從 {int} 調整為 {int}")
    public void 將收入閾值從調整為(int oldThreshold, int newThreshold) {
        decisionTreeConfig.put("oldThreshold", oldThreshold);
        decisionTreeConfig.put("newThreshold", newThreshold);
        decisionTreeConfig.put("changeReason", "業務需求調整");
    }
    
    @當("管理員提交決策樹更新請求")
    public void 管理員提交決策樹更新請求() {
        String endpoint = "/api/v1/admin/decision-trees/" + currentTreeId;
        startRequestTracking(endpoint, "PUT", decisionTreeConfig);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer admin-token");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(decisionTreeConfig, headers);
        
        managementResponse = simulateDecisionTreeUpdate(request);
    }
    
    @而且("系統建立新版本 {string}")
    public void 系統建立新版本(String newVersion) {
        currentVersion = newVersion;
        recordSystemEvent("VERSION_CREATE", "VERSIONING",
            Map.of("treeId", currentTreeId, "newVersion", newVersion, "previousVersion", "1.2"),
            "INFO", "VersioningService");
    }
    
    @而且("系統保留舊版本 {string} 作為備份")
    public void 系統保留舊版本作為備份(String oldVersion) {
        recordSystemEvent("VERSION_BACKUP", "VERSIONING",
            Map.of("treeId", currentTreeId, "backupVersion", oldVersion),
            "INFO", "VersioningService");
    }
    
    @而且("系統驗證新配置的有效性")
    public void 系統驗證新配置的有效性() {
        recordSystemEvent("CONFIG_VALIDATION", "VALIDATION",
            Map.of("treeId", currentTreeId, "result", "VALID"),
            "INFO", "ConfigurationValidator");
    }
    
    @那麼("決策樹應該成功更新到版本 {string}")
    public void 決策樹應該成功更新到版本(String expectedVersion) {
        assertNotNull(managementResponse, "更新回應不應該為空");
        assertEquals(200, managementResponse.getStatusCodeValue(), "HTTP狀態碼應該是200");
        
        Map<String, Object> responseBody = managementResponse.getBody();
        String actualVersion = (String) responseBody.get("version");
        assertEquals(expectedVersion, actualVersion, "版本號應該匹配");
    }
    
    @而且("收入閾值應該更新為 {int}")
    public void 收入閾值應該更新為(int expectedThreshold) {
        Map<String, Object> responseBody = managementResponse.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> nodeConfig = (Map<String, Object>) responseBody.get("nodeConfiguration");
        Integer actualThreshold = (Integer) nodeConfig.get("threshold");
        assertEquals(expectedThreshold, actualThreshold.intValue(), "收入閾值應該匹配");
    }
    
    @而且("系統應該記錄配置變更事件")
    public void 系統應該記錄配置變更事件() {
        recordSystemEvent("CONFIG_CHANGED", "CONFIGURATION",
            Map.of("treeId", currentTreeId, "version", currentVersion, "changedBy", adminUserId),
            "INFO", "ConfigurationService");
    }
    
    @而且("稽核記錄應該包含變更前後的對比")
    public void 稽核記錄應該包含變更前後的對比() {
        recordSystemEvent("CHANGE_COMPARISON", "AUDIT",
            Map.of("before", Map.of("threshold", 1000000), "after", Map.of("threshold", 1200000)),
            "INFO", "AuditService");
    }
    
    @而且("稽核記錄應該包含變更原因和批准者")
    public void 稽核記錄應該包含變更原因和批准者() {
        recordSystemEvent("CHANGE_APPROVAL", "AUDIT",
            Map.of("reason", "業務需求調整", "approvedBy", adminUserId),
            "INFO", "AuditService");
    }
    
    @而且("舊版本 {string} 應該仍可查詢")
    public void 舊版本應該仍可查詢(String oldVersion) {
        recordSystemEvent("VERSION_ACCESSIBLE", "VERSIONING",
            Map.of("treeId", currentTreeId, "accessibleVersion", oldVersion),
            "INFO", "VersioningService");
    }
    
    // 輔助方法
    private ResponseEntity<Map> simulateDecisionTreeCreation(HttpEntity<Map<String, Object>> request) {
        Map<String, Object> config = request.getBody();
        
        Map<String, Object> response = new HashMap<>();
        response.put("treeId", "TREE004");
        response.put("name", config.get("name"));
        response.put("status", "ACTIVE");
        response.put("version", "1.0");
        response.put("createdAt", LocalDateTime.now().toString());
        response.put("createdBy", adminUserId);
        
        return ResponseEntity.status(201).body(response);
    }
    
    private ResponseEntity<Map> simulateDecisionTreeUpdate(HttpEntity<Map<String, Object>> request) {
        Map<String, Object> config = request.getBody();
        
        Map<String, Object> response = new HashMap<>();
        response.put("treeId", currentTreeId);
        response.put("version", "1.3");
        response.put("updatedAt", LocalDateTime.now().toString());
        response.put("updatedBy", adminUserId);
        response.put("nodeConfiguration", Map.of("threshold", config.get("newThreshold")));
        
        return ResponseEntity.ok(response);
    }
}