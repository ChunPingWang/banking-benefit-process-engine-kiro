package com.bank.promotion.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.strategy.PercentageDiscountStrategy;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class PercentageDiscountStrategyTest {
    
    private PercentageDiscountStrategy strategy;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001",
            "VIP",
            BigDecimal.valueOf(1000000),
            750,
            "台北",
            50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @Test
    void shouldCalculatePercentageDiscountCorrectly() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("discountPercentage", BigDecimal.valueOf(10));
        parameters.put("baseAmount", BigDecimal.valueOf(100000));
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(10000.00).setScale(2));
    }
    
    @Test
    void shouldUseCustomerAnnualIncomeAsDefaultBaseAmount() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("discountPercentage", BigDecimal.valueOf(5));
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        BigDecimal expectedDiscount = testCustomer.getBasicInfo().getAnnualIncome()
                                                 .multiply(BigDecimal.valueOf(5))
                                                 .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualTo(expectedDiscount);
    }
    
    @Test
    void shouldCreatePromotionResultWithCorrectDetails() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("discountPercentage", BigDecimal.valueOf(15));
        parameters.put("baseAmount", BigDecimal.valueOf(50000));
        parameters.put("promotionName", "VIP專屬折扣");
        parameters.put("promotionId", "VIP_DISCOUNT_001");
        
        BigDecimal calculatedAmount = strategy.calculate(testCustomer, parameters);
        
        // When
        PromotionResult result = strategy.createPromotionResult(testCustomer, calculatedAmount, parameters);
        
        // Then
        assertThat(result.getPromotionId()).isEqualTo("VIP_DISCOUNT_001");
        assertThat(result.getPromotionName()).isEqualTo("VIP專屬折扣");
        assertThat(result.getPromotionType()).isEqualTo("PERCENTAGE_DISCOUNT");
        assertThat(result.getDiscountAmount()).isEqualTo(calculatedAmount);
        assertThat(result.getDiscountPercentage()).isEqualTo(BigDecimal.valueOf(15));
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDescription()).contains("15.0%");
    }
    
    @Test
    void shouldValidateParametersCorrectly() {
        // Valid parameters
        Map<String, Object> validParams = new HashMap<>();
        validParams.put("discountPercentage", BigDecimal.valueOf(10));
        assertThat(strategy.validateParameters(validParams)).isTrue();
        
        // Invalid: null parameters
        assertThat(strategy.validateParameters(null)).isFalse();
        
        // Invalid: empty parameters
        assertThat(strategy.validateParameters(new HashMap<>())).isFalse();
        
        // Invalid: missing discount percentage
        Map<String, Object> missingPercentage = new HashMap<>();
        missingPercentage.put("baseAmount", BigDecimal.valueOf(1000));
        assertThat(strategy.validateParameters(missingPercentage)).isFalse();
        
        // Invalid: negative discount percentage
        Map<String, Object> negativePercentage = new HashMap<>();
        negativePercentage.put("discountPercentage", BigDecimal.valueOf(-5));
        assertThat(strategy.validateParameters(negativePercentage)).isFalse();
        
        // Invalid: discount percentage over 100
        Map<String, Object> overHundredPercentage = new HashMap<>();
        overHundredPercentage.put("discountPercentage", BigDecimal.valueOf(150));
        assertThat(strategy.validateParameters(overHundredPercentage)).isFalse();
    }
    
    @Test
    void shouldThrowExceptionForInvalidParameters() {
        // Given
        Map<String, Object> invalidParameters = new HashMap<>();
        invalidParameters.put("discountPercentage", BigDecimal.valueOf(-10));
        
        // When & Then
        assertThatThrownBy(() -> strategy.calculate(testCustomer, invalidParameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid parameters");
    }
    
    @Test
    void shouldHandleDifferentNumberTypes() {
        // Given - using different number types
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("discountPercentage", 10.5); // Double
        parameters.put("baseAmount", 100000L); // Long
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(10500.00).setScale(2));
    }
    
    @Test
    void shouldReturnCorrectStrategyType() {
        // When & Then
        assertThat(strategy.getStrategyType()).isEqualTo("PERCENTAGE_DISCOUNT");
    }
}