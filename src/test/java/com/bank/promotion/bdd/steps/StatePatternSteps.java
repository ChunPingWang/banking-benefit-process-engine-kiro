package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.domain.state.PromotionContext;
import com.bank.promotion.domain.state.ActivePromotionState;
import com.bank.promotion.domain.state.SuspendedPromotionState;
import com.bank.promotion.domain.state.ExpiredPromotionState;
import com.bank.promotion.domain.state.StateTransitionResult;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * State Pattern BDD Step Definitions
 */
public class StatePatternSteps extends BaseStepDefinitions {
    
    private PromotionContext promotionContext;
    private StateTransitionResult transitionResult;
    
    @Given("the system created a promotion with {string} state")
    public void theSystemCreatedPromotionWithState(String initialState) {
        initializeTest();
        
        switch (initialState) {
            case "ACTIVE":
                promotionContext = new PromotionContext("PROMO001", "Test Promotion", "TEST_PROMOTION", new ActivePromotionState());
                break;
            case "SUSPENDED":
                promotionContext = new PromotionContext("PROMO001", "Test Promotion", "TEST_PROMOTION", new SuspendedPromotionState());
                break;
            case "EXPIRED":
                promotionContext = new PromotionContext("PROMO001", "Test Promotion", "TEST_PROMOTION", new ExpiredPromotionState());
                break;
            default:
                promotionContext = new PromotionContext("PROMO001", "Test Promotion", "TEST_PROMOTION", new ActivePromotionState());
        }
        
        recordSystemEvent("PROMOTION_CREATED", "STATE_MANAGEMENT", 
            Map.of("promotionId", "PROMO001", "initialState", initialState), 
            "INFO", "StateManager");
    }
    
    @And("promotion validity period is set to {int} days later")
    public void promotionValidityPeriodIsSetToDaysLater(int days) {
        LocalDateTime validUntil = LocalDateTime.now().plusDays(days);
        promotionContext.setProperty("validUntil", validUntil);
        
        recordSystemEvent("VALIDITY_PERIOD_SET", "STATE_MANAGEMENT",
            Map.of("promotionId", "PROMO001", "validUntil", validUntil.toString()),
            "INFO", "StateManager");
    }
    
    @And("promotion validity period is set to {int} days ago")
    public void promotionValidityPeriodIsSetToDaysAgo(int days) {
        LocalDateTime validUntil = LocalDateTime.now().minusDays(days);
        promotionContext.setProperty("validUntil", validUntil);
        
        recordSystemEvent("VALIDITY_PERIOD_SET", "STATE_MANAGEMENT",
            Map.of("promotionId", "PROMO001", "validUntil", validUntil.toString()),
            "INFO", "StateManager");
    }
    
    @And("promotion maximum suspension period is set to {int} days")
    public void promotionMaximumSuspensionPeriodIsSetToDays(int maxSuspensionDays) {
        promotionContext.setProperty("maxSuspensionDays", maxSuspensionDays);
    }
    
    @And("test customer data is prepared")
    public void testCustomerDataIsPrepared() {
        Map<String, Object> customerData = Map.of(
            "customerId", "CUST001",
            "accountType", "VIP",
            "annualIncome", 800000
        );
        promotionContext.setProperty("customerData", customerData);
    }
    
    @When("the system attempts to suspend the promotion")
    public void theSystemAttemptsToSuspendPromotion() {
        transitionResult = promotionContext.getCurrentState().suspend(promotionContext, "Manual suspension for testing");
        
        recordSystemEvent("STATE_TRANSITION_ATTEMPT", "STATE_MANAGEMENT",
            Map.of("promotionId", "PROMO001", "action", "SUSPEND", "fromState", promotionContext.getCurrentState().getStateName()),
            "INFO", "StateManager");
    }
    
    @When("the system attempts to activate the promotion")
    public void theSystemAttemptsToActivatePromotion() {
        transitionResult = promotionContext.getCurrentState().activate(promotionContext);
        
        recordSystemEvent("STATE_TRANSITION_ATTEMPT", "STATE_MANAGEMENT",
            Map.of("promotionId", "PROMO001", "action", "ACTIVATE", "fromState", promotionContext.getCurrentState().getStateName()),
            "INFO", "StateManager");
    }
    
    @When("the system attempts to expire the promotion")
    public void theSystemAttemptsToExpirePromotion() {
        transitionResult = promotionContext.getCurrentState().expire(promotionContext);
        
        recordSystemEvent("STATE_TRANSITION_ATTEMPT", "STATE_MANAGEMENT",
            Map.of("promotionId", "PROMO001", "action", "EXPIRE", "fromState", promotionContext.getCurrentState().getStateName()),
            "INFO", "StateManager");
    }
    
    @When("the system checks promotion validity period")
    public void theSystemChecksPromotionValidityPeriod() {
        LocalDateTime validUntil = (LocalDateTime) promotionContext.getProperty("validUntil");
        if (validUntil != null && validUntil.isBefore(LocalDateTime.now())) {
            transitionResult = promotionContext.getCurrentState().expire(promotionContext);
            
            recordSystemEvent("AUTO_EXPIRY_TRIGGERED", "STATE_MANAGEMENT",
                Map.of("promotionId", "PROMO001", "reason", "VALIDITY_EXPIRED", "validUntil", validUntil.toString()),
                "INFO", "StateManager");
        }
    }
    
    @Then("promotion state transition should be successful")
    public void promotionStateTransitionShouldBeSuccessful() {
        assertNotNull(transitionResult, "Transition result should not be null");
        assertTrue(transitionResult.isSuccess(), "State transition should be successful");
    }
    
    @Then("promotion state transition should fail")
    public void promotionStateTransitionShouldFail() {
        assertNotNull(transitionResult, "Transition result should not be null");
        assertFalse(transitionResult.isSuccess(), "State transition should fail");
    }
    
    @And("transition message should contain {string}")
    public void transitionMessageShouldContain(String expectedMessage) {
        assertNotNull(transitionResult, "Transition result should not be null");
        String message = transitionResult.getMessage();
        assertTrue(message.contains(expectedMessage), 
            "Transition message should contain '" + expectedMessage + "', actual message: " + message);
    }
    
    @And("promotion current state should be {string}")
    public void promotionCurrentStateShouldBe(String expectedState) {
        assertEquals(expectedState, promotionContext.getCurrentState().getStateName(),
            "Promotion state should be " + expectedState);
    }
    
    @And("promotion should be marked as active")
    public void promotionShouldBeMarkedAsActive() {
        assertTrue(promotionContext.getCurrentState().isActive(), "Promotion should be active");
    }
    
    @And("promotion should be marked as suspended")
    public void promotionShouldBeMarkedAsSuspended() {
        assertFalse(promotionContext.getCurrentState().isActive(), "Promotion should not be active");
        assertEquals("SUSPENDED", promotionContext.getCurrentState().getStateName(), "Promotion should be suspended");
    }
    
    @And("promotion should be marked as expired")
    public void promotionShouldBeMarkedAsExpired() {
        assertFalse(promotionContext.getCurrentState().isActive(), "Promotion should not be active");
        assertEquals("EXPIRED", promotionContext.getCurrentState().getStateName(), "Promotion should be expired");
    }
}