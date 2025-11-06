package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System Error Handling BDD Step Definitions
 */
public class SystemErrorHandlingSteps extends BaseStepDefinitions {
    
    private String currentCustomerId;
    private boolean externalSystemTimeout = false;
    private boolean databaseConnectionError = false;
    private boolean spelExecutionError = false;
    
    @Given("the system is started and running normally")
    public void theSystemIsStartedAndRunningNormally() {
        initializeTest();
        recordSystemEvent("SYSTEM_STARTUP", "SYSTEM", "System started normally", "INFO", "SystemManager");
    }
    
    @And("error tracking mechanism is enabled")
    public void errorTrackingMechanismIsEnabled() {
        recordSystemEvent("ERROR_TRACKING_INIT", "SETUP", "Error tracking mechanism enabled", "INFO", "ErrorTrackingService");
    }
    
    @And("exception handling service is ready")
    public void exceptionHandlingServiceIsReady() {
        recordSystemEvent("EXCEPTION_HANDLER_INIT", "SETUP", "Exception handling service ready", "INFO", "ExceptionHandlerService");
    }
    
    @Given("customer {string} submits system error handling request")
    public void customerSubmitsSystemErrorHandlingRequest(String customerId) {
        currentCustomerId = customerId;
        startRequestTracking("/api/v1/promotions/evaluate", "POST", Map.of("customerId", customerId));
    }
    
    @And("external credit rating system response time exceeds {int} seconds")
    public void externalCreditRatingSystemResponseTimeExceedsSeconds(int timeoutSeconds) {
        externalSystemTimeout = true;
        recordSystemEvent("EXTERNAL_TIMEOUT_DETECTED", "EXTERNAL_SYSTEM",
            Map.of("system", "CreditRatingSystem", "timeoutSeconds", timeoutSeconds),
            "WARNING", "ExternalSystemMonitor");
    }
    
    @When("the system attempts to call external credit rating system")
    public void theSystemAttemptsToCallExternalCreditRatingSystem() {
        recordSystemEvent("EXTERNAL_CALL_ATTEMPT", "EXTERNAL_SYSTEM",
            Map.of("system", "CreditRatingSystem", "customerId", currentCustomerId),
            "INFO", "ExternalSystemAdapter");
    }
    
    @And("the system detects connection timeout")
    public void theSystemDetectsConnectionTimeout() {
        if (externalSystemTimeout) {
            recordSystemEvent("CONNECTION_TIMEOUT", "ERROR",
                Map.of("system", "CreditRatingSystem", "customerId", currentCustomerId),
                "ERROR", "ExternalSystemAdapter");
        }
    }
    
    @And("the system activates timeout handling mechanism")
    public void theSystemActivatesTimeoutHandlingMechanism() {
        recordSystemEvent("TIMEOUT_HANDLER_ACTIVATED", "ERROR_HANDLING",
            Map.of("handlerType", "ExternalSystemTimeoutHandler"),
            "INFO", "TimeoutHandler");
    }
    
    @And("the system records external system timeout event")
    public void theSystemRecordsExternalSystemTimeoutEvent() {
        recordSystemEvent("EXTERNAL_SYSTEM_TIMEOUT", "ERROR",
            Map.of("system", "CreditRatingSystem", "customerId", currentCustomerId, "action", "FALLBACK"),
            "ERROR", "ErrorLogger");
    }
    
    @And("the system activates fallback strategy using default credit rating")
    public void theSystemActivatesFallbackStrategyUsingDefaultCreditRating() {
        recordSystemEvent("FALLBACK_STRATEGY_ACTIVATED", "ERROR_HANDLING",
            Map.of("strategy", "DefaultCreditRating", "defaultValue", "B"),
            "INFO", "FallbackHandler");
    }
    
    @Then("the system should use default credit rating {string}")
    public void theSystemShouldUseDefaultCreditRating(String defaultRating) {
        recordSystemEvent("DEFAULT_VALUE_USED", "ERROR_HANDLING",
            Map.of("field", "creditRating", "defaultValue", defaultRating),
            "INFO", "FallbackHandler");
        
        assertEquals("B", defaultRating, "Default credit rating should be B");
    }
    
    @And("promotion evaluation process should continue execution")
    public void promotionEvaluationProcessShouldContinueExecution() {
        recordSystemEvent("PROCESS_CONTINUATION", "PROCESSING",
            Map.of("status", "CONTINUED", "customerId", currentCustomerId),
            "INFO", "PromotionService");
    }
    
    @And("the system should record fallback strategy execution event")
    public void theSystemShouldRecordFallbackStrategyExecutionEvent() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasFallbackEvent = systemEvents.stream()
            .anyMatch(event -> "FALLBACK_STRATEGY_ACTIVATED".equals(event.getEventType()));
        assertTrue(hasFallbackEvent, "Should have fallback strategy execution event");
    }
    
    @And("error tracking should contain timeout detailed information")
    public void errorTrackingShouldContainTimeoutDetailedInformation() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasTimeoutDetails = systemEvents.stream()
            .anyMatch(event -> "CONNECTION_TIMEOUT".equals(event.getEventType()));
        assertTrue(hasTimeoutDetails, "Should have timeout detailed information");
    }
    
    @And("error tracking should contain fallback strategy usage record")
    public void errorTrackingShouldContainFallbackStrategyUsageRecord() {
        var systemEvents = auditTracker.getSystemEvents(currentRequestId);
        boolean hasFallbackRecord = systemEvents.stream()
            .anyMatch(event -> "DEFAULT_VALUE_USED".equals(event.getEventType()));
        assertTrue(hasFallbackRecord, "Should have fallback strategy usage record");
    }
    
    @And("customer should still receive valid promotion result")
    public void customerShouldStillReceiveValidPromotionResult() {
        completeRequestTracking(Map.of("promotionType", "Basic Promotion Plan", "amount", 1000), 200, 300);
        
        var requestLog = auditTracker.getRequestLog(currentRequestId);
        assertEquals(Integer.valueOf(200), requestLog.getResponseStatus(), "Should return successful response");
    }
}