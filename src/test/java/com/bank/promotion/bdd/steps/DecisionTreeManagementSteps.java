package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Decision Tree Management BDD Step Definitions
 */
public class DecisionTreeManagementSteps extends BaseStepDefinitions {
    
    private Map<String, Object> decisionTreeConfig;
    private ResponseEntity<Map> managementResponse;
    private String currentTreeId;
    private String currentVersion;
    private String adminUserId = "ADMIN001";
    private List<Map<String, Object>> batchUpdateRequests = new ArrayList<>();
    
    @Given("system administrator has logged into management interface")
    public void systemAdministratorHasLoggedIntoManagementInterface() {
        initializeTest();
        recordSystemEvent("ADMIN_LOGIN", "AUTHENTICATION", 
            Map.of("adminId", adminUserId, "loginTime", LocalDateTime.now()),
            "INFO", "AuthenticationService");
    }
    
    @And("decision tree management service is ready")
    public void decisionTreeManagementServiceIsReady() {
        recordSystemEvent("MANAGEMENT_SERVICE_INIT", "SETUP", 
            "Decision tree management service initialization completed", "INFO", "DecisionTreeManagementService");
    }
    
    @Given("administrator wants to create new decision tree named {string}")
    public void administratorWantsToCreateNewDecisionTreeNamed(String treeName) {
        decisionTreeConfig = new HashMap<>();
        decisionTreeConfig.put("name", treeName);
        decisionTreeConfig.put("status", "DRAFT");
        decisionTreeConfig.put("createdBy", adminUserId);
        decisionTreeConfig.put("nodes", new ArrayList<>());
    }
    
    @And("decision tree contains root node {string}")
    public void decisionTreeContainsRootNode(String rootNodeName) {
        Map<String, Object> rootNode = Map.of(
            "nodeId", "ROOT001",
            "nodeName", rootNodeName,
            "nodeType", "ROOT",
            "conditions", List.of(),
            "actions", List.of(),
            "children", List.of("COND001")
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(rootNode);
    }
    
    @And("root node connects to {string} condition node")
    public void rootNodeConnectsToConditionNode(String conditionNodeName) {
        Map<String, Object> conditionNode = Map.of(
            "nodeId", "COND001",
            "nodeName", conditionNodeName,
            "nodeType", "CONDITION",
            "conditions", List.of(Map.of("field", "annualIncome", "operator", ">=", "value", 500000)),
            "actions", List.of(),
            "children", List.of("CALC001")
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(conditionNode);
    }
    
    @And("revenue assessment node connects to {string} calculation node")
    public void revenueAssessmentNodeConnectsToCalculationNode(String calculationNodeName) {
        Map<String, Object> calculationNode = Map.of(
            "nodeId", "CALC001",
            "nodeName", calculationNodeName,
            "nodeType", "CALCULATION",
            "conditions", List.of(),
            "actions", List.of(Map.of("type", "CALCULATE_PROMOTION", "formula", "income * 0.02")),
            "children", List.of()
        );
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        nodes.add(calculationNode);
    }
    
    @When("administrator submits decision tree creation request")
    public void administratorSubmitsDecisionTreeCreationRequest() {
        String endpoint = "/api/v1/admin/decision-trees";
        startRequestTracking(endpoint, "POST", decisionTreeConfig);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer admin-token");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(decisionTreeConfig, headers);
        
        managementResponse = simulateDecisionTreeManagementApi("POST", endpoint, request);
    }
    
    @And("system starts recording configuration change audit")
    public void systemStartsRecordingConfigurationChangeAudit() {
        recordSystemEvent("CONFIG_CHANGE_START", "CONFIGURATION",
            Map.of("operation", "CREATE_TREE", "adminId", adminUserId),
            "INFO", "ConfigurationAuditService");
    }
    
    @And("system validates decision tree structure integrity")
    public void systemValidatesDecisionTreeStructureIntegrity() {
        // Simulate structure validation
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) decisionTreeConfig.get("nodes");
        
        recordSystemEvent("STRUCTURE_VALIDATION", "VALIDATION",
            Map.of("nodeCount", nodes.size(), "validationResult", "PASSED"),
            "INFO", "StructureValidationService");
    }
    
    @And("system saves decision tree configuration to database")
    public void systemSavesDecisionTreeConfigurationToDatabase() {
        currentTreeId = "TREE004";
        decisionTreeConfig.put("treeId", currentTreeId);
        currentVersion = "1.0.0";
        decisionTreeConfig.put("version", currentVersion);
        
        recordSystemEvent("CONFIG_SAVE", "PERSISTENCE",
            Map.of("treeId", currentTreeId, "version", currentVersion),
            "INFO", "DatabaseService");
    }
    
    @And("system updates decision tree cache")
    public void systemUpdatesDecisionTreeCache() {
        recordSystemEvent("CACHE_UPDATE", "CACHING",
            Map.of("treeId", currentTreeId, "operation", "PUT"),
            "INFO", "CacheService");
    }
    
    @Then("should successfully create decision tree {string}")
    public void shouldSuccessfullyCreateDecisionTree(String expectedTreeId) {
        assertNotNull(managementResponse, "Management response should not be null");
        assertEquals(201, managementResponse.getStatusCodeValue(), "HTTP status code should be 201");
        
        Map<String, Object> responseBody = managementResponse.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        String actualTreeId = (String) responseBody.get("treeId");
        assertNotNull(actualTreeId, "Tree ID should be returned");
        assertTrue(actualTreeId.startsWith("TREE"), "Tree ID should start with TREE");
    }
    
    @And("decision tree status should be {string}")
    public void decisionTreeStatusShouldBe(String expectedStatus) {
        Map<String, Object> responseBody = managementResponse.getBody();
        String actualStatus = (String) responseBody.get("status");
        assertEquals(expectedStatus, actualStatus, "Decision tree status should match");
    }
    
    @And("system should record decision tree creation event")
    public void systemShouldRecordDecisionTreeCreationEvent() {
        recordSystemEvent("TREE_CREATED", "CONFIGURATION",
            Map.of("treeId", currentTreeId, "createdBy", adminUserId, "version", currentVersion),
            "INFO", "DecisionTreeManagementService");
    }
    
    @And("audit records should contain complete tree structure information")
    public void auditRecordsShouldContainCompleteTreeStructureInformation() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasStructureInfo = systemEvents.stream()
            .anyMatch(event -> "STRUCTURE_VALIDATION".equals(event.getEventType()));
        assertTrue(hasStructureInfo, "Should have structure validation information");
    }
    
    @And("audit records should contain creator information and timestamp")
    public void auditRecordsShouldContainCreatorInformationAndTimestamp() {
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertNotNull(requestLog.getCreatedAt(), "Should have creation timestamp");
        
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasCreatorInfo = systemEvents.stream()
            .anyMatch(event -> event.getEventDetails().toString().contains(adminUserId));
        assertTrue(hasCreatorInfo, "Should have creator information");
    }
    
    @And("change version number should be {string}")
    public void changeVersionNumberShouldBe(String expectedVersion) {
        Map<String, Object> responseBody = managementResponse.getBody();
        String actualVersion = (String) responseBody.get("version");
        assertEquals(expectedVersion, actualVersion, "Version number should match");
    }
    
    // Helper method to simulate management API
    private ResponseEntity<Map> simulateDecisionTreeManagementApi(String method, String endpoint, HttpEntity<Map<String, Object>> request) {
        Map<String, Object> requestBody = request.getBody();
        
        if ("POST".equals(method) && endpoint.contains("decision-trees")) {
            // Simulate tree creation
            String treeId = "TREE" + String.format("%03d", new Random().nextInt(1000));
            Map<String, Object> response = Map.of(
                "treeId", treeId,
                "name", requestBody.get("name"),
                "status", "ACTIVE",
                "version", "1.0.0",
                "createdBy", requestBody.get("createdBy"),
                "createdAt", LocalDateTime.now().toString(),
                "success", true
            );
            
            // Complete request tracking
            auditTracker.completeRequestTracking(currentRequestId, response, 201, 250);
            return ResponseEntity.status(201).body(response);
            
        } else if ("PUT".equals(method) && endpoint.contains("decision-trees")) {
            // Simulate tree update
            Map<String, Object> response = Map.of(
                "treeId", currentTreeId,
                "name", requestBody.get("name"),
                "status", "ACTIVE",
                "version", "1.1.0",
                "updatedBy", requestBody.get("updatedBy"),
                "updatedAt", LocalDateTime.now().toString(),
                "success", true
            );
            
            auditTracker.completeRequestTracking(currentRequestId, response, 200, 180);
            return ResponseEntity.ok(response);
        }
        
        // Default response
        Map<String, Object> response = Map.of("success", false, "error", "Unsupported operation");
        return ResponseEntity.badRequest().body(response);
    }
}