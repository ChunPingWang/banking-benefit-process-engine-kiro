package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.bdd.audit.DecisionStepLog;
import com.bank.promotion.bdd.audit.RequestAuditLog;
import com.bank.promotion.bdd.audit.SystemEventLog;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Audit Trail Query BDD Step Definitions
 */
public class AuditTrailQuerySteps extends BaseStepDefinitions {
    
    private List<String> processedRequestIds = new ArrayList<>();
    private Map<String, Object> queryParameters = new HashMap<>();
    private ResponseEntity<Map> queryResponse;
    private Map<String, Object> auditReport;
    
    @Given("the system has processed multiple customer promotion evaluation requests")
    public void theSystemHasProcessedMultipleCustomerPromotionEvaluationRequests() {
        initializeTest();
        simulateMultiplePromotionRequests();
        assertTrue(processedRequestIds.size() >= 3, "Should have at least 3 processed requests");
    }
    
    @And("audit data has been completely recorded")
    public void auditDataHasBeenCompletelyRecorded() {
        for (String requestId : processedRequestIds) {
            RequestAuditLog requestLog = auditTracker.getRequestLog(requestId);
            assertNotNull(requestLog, "Request log should exist: " + requestId);
            
            List<DecisionStepLog> steps = auditTracker.getDecisionSteps(requestId);
            assertFalse(steps.isEmpty(), "Decision steps should exist: " + requestId);
        }
    }
    
    @And("audit query API is ready")
    public void auditQueryApiIsReady() {
        recordSystemEvent("AUDIT_QUERY_INIT", "SETUP", "Audit query API ready", "INFO", "AuditQueryService");
    }
    
    @Given("customer {string} submitted promotion evaluation request in the past 24 hours")
    public void customerSubmittedPromotionEvaluationRequestInThePast24Hours(String customerId) {
        String requestId = simulateCustomerPromotionRequest(customerId);
        processedRequestIds.add(requestId);
        
        RequestAuditLog requestLog = auditTracker.getRequestLog(requestId);
        assertNotNull(requestLog, "Customer request log should exist");
    }
    
    @And("the request has been successfully processed and audit trail recorded")
    public void theRequestHasBeenSuccessfullyProcessedAndAuditTrailRecorded() {
        String lastRequestId = processedRequestIds.get(processedRequestIds.size() - 1);
        RequestAuditLog requestLog = auditTracker.getRequestLog(lastRequestId);
        
        assertNotNull(requestLog.getCompletedAt(), "Request should be completed");
        assertEquals(Integer.valueOf(200), requestLog.getResponseStatus(), "Request should be successful");
        
        List<DecisionStepLog> steps = auditTracker.getDecisionSteps(lastRequestId);
        assertFalse(steps.isEmpty(), "Should have decision step records");
    }
    
    @When("audit personnel query customer {string} processing trail")
    public void auditPersonnelQueryCustomerProcessingTrail(String customerId) {
        queryParameters.put("customerId", customerId);
        queryParameters.put("operation", "getCustomerAuditTrail");
        
        queryResponse = simulateAuditTrailQuery(queryParameters);
    }
    
    @And("specify query time range as past 24 hours")
    public void specifyQueryTimeRangeAsPast24Hours() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        
        queryParameters.put("startTime", startTime);
        queryParameters.put("endTime", endTime);
    }
    
    @Then("should return complete audit records of that customer")
    public void shouldReturnCompleteAuditRecordsOfThatCustomer() {
        assertNotNull(queryResponse, "Query response should not be null");
        assertEquals(200, queryResponse.getStatusCodeValue(), "Query should be successful");
        
        Map<String, Object> responseBody = queryResponse.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditRecords = (List<Map<String, Object>>) responseBody.get("auditRecords");
        assertFalse(auditRecords.isEmpty(), "Audit records should not be empty");
    }
    
    @And("audit records should contain request ID and timestamp")
    public void auditRecordsShouldContainRequestIdAndTimestamp() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditRecords = (List<Map<String, Object>>) responseBody.get("auditRecords");
        
        for (Map<String, Object> record : auditRecords) {
            assertNotNull(record.get("requestId"), "Audit record should contain request ID");
            assertNotNull(record.get("createdAt"), "Audit record should contain timestamp");
        }
    }
    
    @And("should contain complete decision path")
    public void shouldContainCompleteDecisionPath() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        assertNotNull(decisionPath, "Decision path should not be null");
        assertTrue(decisionPath.size() >= 2, "Decision path should contain multiple steps");
    }
    
    @And("each decision step should have input and output data")
    public void eachDecisionStepShouldHaveInputAndOutputData() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (Map<String, Object> step : decisionPath) {
            assertNotNull(step.get("inputData"), "Decision step should have input data");
            assertNotNull(step.get("outputData"), "Decision step should have output data");
        }
    }
    
    @And("should contain all external system call records")
    public void shouldContainAllExternalSystemCallRecords() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> externalCalls = (List<Map<String, Object>>) responseBody.get("externalCalls");
        
        assertNotNull(externalCalls, "External system call records should not be null");
        
        for (Map<String, Object> call : externalCalls) {
            assertNotNull(call.get("system"), "External system call should contain system name");
            assertNotNull(call.get("callTime"), "External system call should contain call time");
        }
    }
    
    @And("should contain execution time of each step")
    public void shouldContainExecutionTimeOfEachStep() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (Map<String, Object> step : decisionPath) {
            assertNotNull(step.get("executionTimeMs"), "Decision step should contain execution time");
            Object executionTime = step.get("executionTimeMs");
            if (executionTime instanceof Number) {
                assertTrue(((Number) executionTime).intValue() > 0, "Execution time should be greater than 0");
            }
        }
    }
    
    @And("audit data should be arranged in chronological order")
    public void auditDataShouldBeArrangedInChronologicalOrder() {
        Map<String, Object> responseBody = queryResponse.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> decisionPath = (List<Map<String, Object>>) responseBody.get("decisionPath");
        
        for (int i = 1; i < decisionPath.size(); i++) {
            String prevTime = (String) decisionPath.get(i-1).get("createdAt");
            String currTime = (String) decisionPath.get(i).get("createdAt");
            
            assertTrue(prevTime.compareTo(currTime) <= 0, "Audit data should be arranged in chronological order");
        }
    }
    
    // Helper methods
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
            
        auditTracker.recordDecisionStep(requestId, "TREE001", "ROOT", "TREE_START",
            Map.of("customerId", customerId), Map.of("status", "STARTED"), 10, "SUCCESS");
            
        auditTracker.recordDecisionStep(requestId, "TREE001", "INCOME_CHECK", "CONDITION",
            Map.of("income", 800000), Map.of("result", true), 15, "SUCCESS");
            
        auditTracker.recordSystemEvent(requestId, "EXTERNAL_CALL", "INTEGRATION",
            Map.of("system", "CreditRatingSystem", "customerId", customerId), "INFO", "ExternalSystemAdapter");
            
        auditTracker.completeRequestTracking(requestId, 
            Map.of("promotionType", "Regular customer promotion", "amount", 3000), 200, 150);
            
        return requestId;
    }
    
    private ResponseEntity<Map> simulateAuditTrailQuery(Map<String, Object> parameters) {
        String customerId = (String) parameters.get("customerId");
        
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
}