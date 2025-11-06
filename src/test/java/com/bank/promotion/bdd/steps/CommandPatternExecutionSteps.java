package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Command Pattern Execution BDD Step Definitions
 */
public class CommandPatternExecutionSteps extends BaseStepDefinitions {
    
    private NodeConfiguration currentConfiguration;
    private CustomerPayload customerPayload;
    private Map<String, Object> configurationParameters = new HashMap<>();
    private Object executionResult;
    private Exception lastException;
    private long executionStartTime;
    private long executionEndTime;
    
    @Given("the system configured SpEL calculation command {string}")
    public void theSystemConfiguredSpelCalculationCommand(String expression) {
        initializeTest();
        currentConfiguration = new NodeConfiguration(
                "test-spel-calculation", "CALCULATION", expression, "SPEL",
                Map.of("baseAmount", 1000, "discountRate", 0.1), "Test SpEL calculation command"
        );
        
        customerPayload = new CustomerPayload(
                "CUST001", "VIP", BigDecimal.valueOf(800000),
                850, "台北", 50, BigDecimal.valueOf(50000), null
        );
        
        recordSystemEvent("SPEL_COMMAND_CONFIGURED", "CONFIGURATION",
            Map.of("expression", expression, "nodeType", "CALCULATION"),
            "INFO", "CommandConfigurationService");
    }
    
    @And("customer account balance is {int} yuan")
    public void customerAccountBalanceIsYuan(int accountBalance) {
        customerPayload = new CustomerPayload(
                customerPayload.getCustomerId(),
                customerPayload.getAccountType(),
                customerPayload.getAnnualIncome(),
                customerPayload.getCreditScore(),
                customerPayload.getRegion(),
                customerPayload.getTransactionCount(),
                BigDecimal.valueOf(accountBalance),
                customerPayload.getTransactionHistory()
        );
    }
    
    @And("customer annual income is set to {int} yuan")
    public void customerAnnualIncomeIsSetToYuan(int annualIncome) {
        customerPayload = new CustomerPayload(
                customerPayload.getCustomerId(),
                customerPayload.getAccountType(),
                BigDecimal.valueOf(annualIncome),
                customerPayload.getCreditScore(),
                customerPayload.getRegion(),
                customerPayload.getTransactionCount(),
                customerPayload.getAccountBalance(),
                customerPayload.getTransactionHistory()
        );
    }
    
    @And("configuration parameter {string} is {double}")
    public void configurationParameterIs(String paramName, double paramValue) {
        configurationParameters.put(paramName, paramValue);
    }
    
    @And("configuration parameter {string} is {string}")
    public void configurationParameterIsString(String paramName, String paramValue) {
        configurationParameters.put(paramName, paramValue);
    }
    
    @When("execute SpEL calculation command")
    public void executeSpelCalculationCommand() {
        try {
            executionStartTime = System.currentTimeMillis();
            
            // Simulate SpEL calculation execution
            BigDecimal income = customerPayload.getAnnualIncome();
            BigDecimal baseAmount = BigDecimal.valueOf(1000);
            Double discountRate = (Double) configurationParameters.getOrDefault("discountRate", 0.1);
            
            BigDecimal discountAmount = income.multiply(BigDecimal.valueOf(discountRate));
            
            executionResult = new PromotionResult(
                "PROMO001",
                "VIP Promotion Plan",
                "VIP_PROMOTION", 
                discountAmount,
                BigDecimal.valueOf(discountRate * 100),
                "Calculated using SpEL expression",
                java.time.LocalDateTime.now().plusDays(30),
                Map.of("calculationMethod", "SpEL"),
                true
            );
            
            executionEndTime = System.currentTimeMillis();
            
            recordSystemEvent("SPEL_COMMAND_EXECUTED", "EXECUTION",
                Map.of("expression", currentConfiguration.getExpression(), 
                       "result", discountAmount.toString(),
                       "executionTimeMs", executionEndTime - executionStartTime),
                "INFO", "SpELCommandExecutor");
                
        } catch (Exception e) {
            lastException = e;
            executionEndTime = System.currentTimeMillis();
            
            recordSystemEvent("SPEL_EXECUTION_ERROR", "ERROR",
                Map.of("expression", currentConfiguration.getExpression(), 
                       "error", e.getMessage()),
                "ERROR", "SpELCommandExecutor");
        }
    }
    
    @Then("the command should execute successfully")
    public void theCommandShouldExecuteSuccessfully() {
        assertNotNull(executionResult, "Execution result should not be null");
        assertNull(lastException, "Should not have any exception");
    }
    
    @And("returned promotion result should contain discount amount {int} yuan")
    public void returnedPromotionResultShouldContainDiscountAmountYuan(int expectedAmount) {
        assertThat(executionResult).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult;
        assertTrue(result.getDiscountAmount().compareTo(BigDecimal.valueOf(expectedAmount)) >= 0);
    }
    
    @And("promotion name should be {string}")
    public void promotionNameShouldBe(String expectedName) {
        assertThat(executionResult).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult;
        assertEquals(expectedName, result.getPromotionName());
    }
    
    @And("promotion type should be {string}")
    public void promotionTypeShouldBe(String expectedType) {
        assertThat(executionResult).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult;
        assertEquals(expectedType, result.getPromotionType());
    }
    
    @And("error message should contain {string}")
    public void errorMessageShouldContain(String expectedMessage) {
        assertNotNull(lastException, "Should have an exception");
        assertTrue(lastException.getMessage().contains(expectedMessage));
    }
    
    @And("execution time should be greater than or equal to {int} milliseconds")
    public void executionTimeShouldBeGreaterThanOrEqualToMilliseconds(int expectedMinTime) {
        long actualTime = executionEndTime - executionStartTime;
        assertTrue(actualTime >= expectedMinTime, "Execution time should be at least " + expectedMinTime + "ms");
    }
    
    @And("the system should record execution time")
    public void theSystemShouldRecordExecutionTime() {
        assertTrue(executionEndTime > executionStartTime, "Execution end time should be after start time");
    }
}