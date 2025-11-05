package com.bank.promotion.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.strategy.FixedAmountStrategy;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class FixedAmountStrategyTest {
    
    private FixedAmountStrategy strategy;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        strategy = new FixedAmountStrategy();
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001",
            "PREMIUM",
            BigDecimal.valueOf(800000),
            700,
            "台北",
            30
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
        
        // Add some transaction records to simulate customer activity
        testCustomer.addTransactionRecord(
            new com.bank.promotion.domain.aggregate.TransactionRecord("TXN001", BigDecimal.valueOf(50000), "PURCHASE", java.time.LocalDateTime.now())
        );
        testCustomer.addTransactionRecord(
            new com.bank.promotion.domain.aggregate.TransactionRecord("TXN002", BigDecimal.valueOf(30000), "PURCHASE", java.time.LocalDateTime.now())
        );
    }
    
    @Test
    void shouldReturnFixedAmountWhenCustomerMeetsMinimumRequirement() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(5000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(50000)); // Customer has 80000 total
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(5000));
    }
    
    @Test
    void shouldReturnZeroWhenCustomerDoesNotMeetMinimumRequirement() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(5000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(100000)); // Customer only has 80000 total
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    void shouldApplyMaxDiscountLimitWhenFixedAmountExceedsLimit() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(10000));
        parameters.put("maxDiscountAmount", BigDecimal.valueOf(7000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(50000));
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(7000));
    }
    
    @Test
    void shouldUseDefaultMinPurchaseAmountWhenNotSpecified() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(3000));
        // No minPurchaseAmount specified, should default to 0
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(3000));
    }
    
    @Test
    void shouldUseAnnualIncomeEstimateWhenNoTransactionHistory() {
        // Given - Create customer with no transaction history
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST002", "BASIC", BigDecimal.valueOf(500000), 600, "台中", 0
        );
        CustomerProfile customerWithoutTransactions = new CustomerProfile("CUST002", customerPayload);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(2000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(40000)); // 10% of 500000 = 50000, so should qualify
        
        // When
        BigDecimal result = strategy.calculate(customerWithoutTransactions, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(2000));
    }
    
    @Test
    void shouldCreatePromotionResultWithCorrectEligibility() {
        // Given - Customer meets requirements
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(4000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(50000));
        parameters.put("promotionName", "新戶開戶優惠");
        parameters.put("promotionId", "NEW_CUSTOMER_001");
        
        BigDecimal calculatedAmount = strategy.calculate(testCustomer, parameters);
        
        // When
        PromotionResult result = strategy.createPromotionResult(testCustomer, calculatedAmount, parameters);
        
        // Then
        assertThat(result.getPromotionId()).isEqualTo("NEW_CUSTOMER_001");
        assertThat(result.getPromotionName()).isEqualTo("新戶開戶優惠");
        assertThat(result.getPromotionType()).isEqualTo("FIXED_AMOUNT");
        assertThat(result.getDiscountAmount()).isEqualTo(calculatedAmount);
        assertThat(result.getDiscountPercentage()).isNull(); // Fixed amount doesn't use percentage
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDescription()).contains("固定金額優惠 4000 元");
    }
    
    @Test
    void shouldCreatePromotionResultWithIneligibilityMessage() {
        // Given - Customer doesn't meet requirements
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fixedAmount", BigDecimal.valueOf(4000));
        parameters.put("minPurchaseAmount", BigDecimal.valueOf(200000)); // Too high
        parameters.put("promotionName", "高額消費優惠");
        
        BigDecimal calculatedAmount = strategy.calculate(testCustomer, parameters);
        
        // When
        PromotionResult result = strategy.createPromotionResult(testCustomer, calculatedAmount, parameters);
        
        // Then
        assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDescription()).contains("不符合最低購買金額要求");
        assertThat(result.getDescription()).contains("200000");
    }
    
    @Test
    void shouldValidateParametersCorrectly() {
        // Valid parameters
        Map<String, Object> validParams = new HashMap<>();
        validParams.put("fixedAmount", BigDecimal.valueOf(1000));
        assertThat(strategy.validateParameters(validParams)).isTrue();
        
        // Invalid: null parameters
        assertThat(strategy.validateParameters(null)).isFalse();
        
        // Invalid: empty parameters
        assertThat(strategy.validateParameters(new HashMap<>())).isFalse();
        
        // Invalid: missing fixed amount
        Map<String, Object> missingAmount = new HashMap<>();
        missingAmount.put("minPurchaseAmount", BigDecimal.valueOf(1000));
        assertThat(strategy.validateParameters(missingAmount)).isFalse();
        
        // Invalid: negative fixed amount
        Map<String, Object> negativeAmount = new HashMap<>();
        negativeAmount.put("fixedAmount", BigDecimal.valueOf(-100));
        assertThat(strategy.validateParameters(negativeAmount)).isFalse();
        
        // Invalid: zero fixed amount
        Map<String, Object> zeroAmount = new HashMap<>();
        zeroAmount.put("fixedAmount", BigDecimal.ZERO);
        assertThat(strategy.validateParameters(zeroAmount)).isFalse();
        
        // Valid: max discount less than fixed amount (used for limiting actual discount)
        Map<String, Object> validMaxDiscount = new HashMap<>();
        validMaxDiscount.put("fixedAmount", BigDecimal.valueOf(1000));
        validMaxDiscount.put("maxDiscountAmount", BigDecimal.valueOf(500));
        assertThat(strategy.validateParameters(validMaxDiscount)).isTrue();
    }
    
    @Test
    void shouldThrowExceptionForInvalidParameters() {
        // Given
        Map<String, Object> invalidParameters = new HashMap<>();
        invalidParameters.put("fixedAmount", BigDecimal.valueOf(-1000));
        
        // When & Then
        assertThatThrownBy(() -> strategy.calculate(testCustomer, invalidParameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid parameters");
    }
    
    @Test
    void shouldReturnCorrectStrategyType() {
        // When & Then
        assertThat(strategy.getStrategyType()).isEqualTo("FIXED_AMOUNT");
    }
}