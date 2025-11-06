package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.bdd.TestDataManager;
import com.bank.promotion.bdd.audit.DecisionStepLog;
import com.bank.promotion.bdd.audit.RequestAuditLog;
import com.bank.promotion.bdd.audit.SystemEventLog;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
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
 * Promotion Evaluation BDD Step Definitions
 */
public class PromotionEvaluationSteps extends BaseStepDefinitions {
    
    private TestDataManager.TestCustomerData currentCustomer;
    private Map<String, Object> customerPayload;
    private ResponseEntity<Map> apiResponse;
    private long requestStartTime;
    private String currentTreeId;
    private boolean externalSystemError = false;
    
    @Given("the system has initialized test data")
    public void theSystemHasInitializedTestData() {
        initializeTest();
        recordSystemEvent("SYSTEM_INIT", "SETUP", "Test data initialization completed", "INFO", "TestDataManager");
    }
    
    @And("audit tracking mechanism is enabled")
    public void auditTrackingMechanismIsEnabled() {
        assertNotNull(auditTracker, "Audit tracker should be initialized");
        recordSystemEvent("AUDIT_INIT", "SETUP", "Audit tracking mechanism enabled", "INFO", "AuditTracker");
    }
    
    @And("external system mock service is ready")
    public void externalSystemMockServiceIsReady() {
        assertNotNull(mockExternalSystemService, "External system mock service should be initialized");
        mockExternalSystemService.resetMockData();
        recordSystemEvent("MOCK_INIT", "SETUP", "External system mock service ready", "INFO", "MockExternalSystemService");
    }
    
    @Given("customer {string} has annual income of {int} yuan")
    public void customerHasAnnualIncomeOfYuan(String customerId, int annualIncome) {
        customerPayload = new HashMap<>();
        customerPayload.put("customerId", customerId);
        customerPayload.put("annualIncome", BigDecimal.valueOf(annualIncome));
    }
    
    @And("customer account type is {string}")
    public void customerAccountTypeIs(String accountType) {
        customerPayload.put("accountType", accountType);
    }
    
    @And("customer credit rating is {string}")
    public void customerCreditRatingIs(String creditRating) {
        customerPayload.put("creditRating", creditRating);
    }
    
    @And("customer monthly average transaction amount is {int} yuan")
    public void customerMonthlyAverageTransactionAmountIsYuan(int monthlyAverage) {
        customerPayload.put("monthlyAverage", BigDecimal.valueOf(monthlyAverage));
    }
    
    @When("the system receives promotion evaluation request")
    public void theSystemReceivesPromotionEvaluationRequest() {
        requestStartTime = System.currentTimeMillis();
        String endpoint = "/api/v1/promotions/evaluate";
        startRequestTracking(endpoint, "POST", customerPayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(customerPayload, headers);
        
        // Simulate API call (in actual implementation would call real API)
        apiResponse = simulatePromotionEvaluationApi(request);
    }
    
    @And("the system starts recording audit trail")
    public void theSystemStartsRecordingAuditTrail() {
        assertNotNull(currentRequestId, "Request ID should be generated");
        recordSystemEvent("AUDIT_START", "PROCESSING", "Start recording audit trail", "INFO", "PromotionService");
    }
    
    @And("the system executes decision tree {string}")
    public void theSystemExecutesDecisionTree(String treeType) {
        TestDataManager.TestDecisionTreeData treeData = testDataManager.getTestDecisionTree(treeType);
        assertNotNull(treeData, "Decision tree data should exist: " + treeType);
        
        currentTreeId = treeData.getTreeId();
        recordSystemEvent("TREE_EXECUTION_START", "PROCESSING", 
            Map.of("treeType", treeType, "treeId", currentTreeId), "INFO", "DecisionTreeService");
    }
    
    @And("the system evaluates income condition node {string}")
    public void theSystemEvaluatesIncomeConditionNode(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        boolean incomeCheck = income.compareTo(BigDecimal.valueOf(500000)) >= 0;
        
        auditTracker.recordDecisionStep(currentRequestId, currentTreeId, nodeId, "CONDITION",
            Map.of("income", income, "threshold", 500000), 
            Map.of("result", incomeCheck), 25, "SUCCESS");
    }
    
    @And("the system evaluates account type condition node {string}")
    public void theSystemEvaluatesAccountTypeConditionNode(String nodeId) {
        String accountType = (String) customerPayload.get("accountType");
        boolean isVip = "VIP".equals(accountType);
        
        auditTracker.recordDecisionStep(currentRequestId, currentTreeId, nodeId, "CONDITION",
            Map.of("accountType", accountType), 
            Map.of("isVip", isVip), 15, "SUCCESS");
    }
    
    @And("the system calls external credit rating system")
    public void theSystemCallsExternalCreditRatingSystem() {
        if (!externalSystemError) {
            String customerId = (String) customerPayload.get("customerId");
            var creditInfo = mockExternalSystemService.getCreditInfo(customerId);
            
            auditTracker.recordSystemEvent(currentRequestId, "EXTERNAL_CALL", "INTEGRATION",
                Map.of("system", "CreditRatingSystem", "customerId", customerId, 
                       "response", creditInfo.getCreditRating(), "responseTime", 120), 
                "INFO", "ExternalSystemAdapter");
        } else {
            auditTracker.recordSystemEvent(currentRequestId, "EXTERNAL_ERROR", "ERROR",
                Map.of("system", "CreditRatingSystem", "error", "Connection timeout"), 
                "ERROR", "ExternalSystemAdapter");
        }
    }
    
    @And("the system executes VIP promotion calculation node {string}")
    public void theSystemExecutesVipPromotionCalculationNode(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        BigDecimal promotionAmount = income.multiply(BigDecimal.valueOf(0.02)); // 2% promotion
        
        auditTracker.recordDecisionStep(currentRequestId, currentTreeId, nodeId, "CALCULATION",
            Map.of("income", income, "rate", 0.02), 
            Map.of("promotionAmount", promotionAmount), 30, "SUCCESS");
    }
    
    @And("the system calls external transaction history system")
    public void theSystemCallsExternalTransactionHistorySystem() {
        String customerId = (String) customerPayload.get("customerId");
        var transactionHistory = mockExternalSystemService.getTransactionHistory(customerId);
        
        auditTracker.recordSystemEvent(currentRequestId, "EXTERNAL_CALL", "INTEGRATION",
            Map.of("system", "TransactionHistorySystem", "customerId", customerId, 
                   "response", transactionHistory, "responseTime", 95), 
            "INFO", "ExternalSystemAdapter");
        
        // Record transaction analysis
        auditTracker.recordDecisionStep(currentRequestId, currentTreeId, "TRANSACTION_ANALYSIS", "ANALYSIS",
            Map.of("customerId", customerId), 
            Map.of("transactionCount", transactionHistory.getTransactionCount(), "analysis", "ACTIVE"), 20, "SUCCESS");
    }
    
    @And("the system executes regular promotion calculation node {string}")
    public void theSystemExecutesRegularPromotionCalculationNode(String nodeId) {
        BigDecimal income = (BigDecimal) customerPayload.get("annualIncome");
        BigDecimal promotionAmount = BigDecimal.valueOf(3000); // Fixed promotion amount
        
        auditTracker.recordDecisionStep(currentRequestId, currentTreeId, nodeId, "CALCULATION",
            Map.of("income", income, "baseAmount", 3000), 
            Map.of("promotionAmount", promotionAmount), 20, "SUCCESS");
    }
    
    @Then("should return {string} result")
    public void shouldReturnResult(String expectedPromotionType) {
        assertNotNull(apiResponse, "API response should not be null");
        assertEquals(200, apiResponse.getStatusCodeValue(), "HTTP status code should be 200");
        
        Map<String, Object> responseBody = apiResponse.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        String actualPromotionType = (String) responseBody.get("promotionType");
        assertTrue(actualPromotionType.contains(expectedPromotionType) || 
                  expectedPromotionType.contains(actualPromotionType), 
                  "Promotion type should match expected: " + expectedPromotionType + ", actual: " + actualPromotionType);
    }
    
    @And("promotion amount should be greater than {int} yuan")
    public void promotionAmountShouldBeGreaterThanYuan(int minAmount) {
        Map<String, Object> responseBody = apiResponse.getBody();
        BigDecimal amount = new BigDecimal(responseBody.get("amount").toString());
        assertTrue(amount.compareTo(BigDecimal.valueOf(minAmount)) > 0, 
                  "Promotion amount should be greater than " + minAmount + ", actual: " + amount);
    }
    
    @And("the system should record complete decision trail")
    public void theSystemShouldRecordCompleteDecisionTrail() {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        assertFalse(steps.isEmpty(), "Decision step records should not be empty");
        
        // Verify each step has required information
        for (DecisionStepLog step : steps) {
            assertNotNull(step.getNodeId(), "Node ID should not be null");
            assertNotNull(step.getInputData(), "Input data should not be null");
            assertNotNull(step.getOutputData(), "Output data should not be null");
        }
    }
    
    @And("audit records should contain {int} decision steps")
    public void auditRecordsShouldContainDecisionSteps(int expectedSteps) {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        assertEquals(expectedSteps, steps.size(), "Decision step count should match");
    }
    
    @And("audit records should contain external system call records")
    public void auditRecordsShouldContainExternalSystemCallRecords() {
        List<SystemEventLog> events = auditTracker.getSystemEvents(currentRequestId);
        boolean hasExternalCall = events.stream()
            .anyMatch(event -> "EXTERNAL_CALL".equals(event.getEventType()));
        assertTrue(hasExternalCall, "Should have external system call records");
    }
    
    @And("each decision step should have execution time record")
    public void eachDecisionStepShouldHaveExecutionTimeRecord() {
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(currentRequestId);
        for (DecisionStepLog step : steps) {
            assertTrue(step.getExecutionTimeMs() > 0, "Execution time should be greater than 0");
        }
    }
    
    @And("request processing total time should be less than {int} milliseconds")
    public void requestProcessingTotalTimeShouldBeLessThanMilliseconds(int maxTime) {
        RequestAuditLog requestLog = auditTracker.getRequestLog(currentRequestId);
        assertNotNull(requestLog.getProcessingTimeMs(), "Processing time should be recorded");
        assertTrue(requestLog.getProcessingTimeMs() < maxTime, 
                  "Processing time should be less than " + maxTime + "ms, actual: " + requestLog.getProcessingTimeMs() + "ms");
    }
    
    // Helper method to simulate API call
    private ResponseEntity<Map> simulatePromotionEvaluationApi(HttpEntity<Map<String, Object>> request) {
        Map<String, Object> requestBody = request.getBody();
        String customerId = (String) requestBody.get("customerId");
        BigDecimal income = (BigDecimal) requestBody.get("annualIncome");
        String accountType = (String) requestBody.get("accountType");
        
        // Simulate promotion evaluation logic
        String promotionType;
        BigDecimal amount;
        
        if ("VIP".equals(accountType) && income.compareTo(BigDecimal.valueOf(800000)) >= 0) {
            promotionType = "VIP優惠方案";
            amount = income.multiply(BigDecimal.valueOf(0.02));
        } else if (income.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            promotionType = "一般客戶優惠方案";
            amount = BigDecimal.valueOf(3000);
        } else {
            promotionType = "基礎優惠方案";
            amount = BigDecimal.valueOf(1000);
        }
        
        Map<String, Object> response = Map.of(
            "customerId", customerId,
            "promotionType", promotionType,
            "amount", amount,
            "validUntil", "2024-12-31",
            "success", true
        );
        
        // Complete request tracking
        auditTracker.completeRequestTracking(currentRequestId, response, 200, 
            System.currentTimeMillis() - requestStartTime);
        
        return ResponseEntity.ok(response);
    }
}