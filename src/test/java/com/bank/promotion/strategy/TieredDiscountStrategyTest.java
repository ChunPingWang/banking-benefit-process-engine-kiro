package com.bank.promotion.strategy;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.strategy.TieredDiscountStrategy;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TieredDiscountStrategyTest {
    
    private TieredDiscountStrategy strategy;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        strategy = new TieredDiscountStrategy();
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001",
            "VIP",
            BigDecimal.valueOf(2000000),
            800,
            "台北",
            100
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @Test
    void shouldCalculateTieredDiscountForHighTier() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        parameters.put("baseAmount", BigDecimal.valueOf(2000000)); // High tier
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then - Should apply 15% discount for amounts >= 2000000
        assertThat(result).isEqualTo(BigDecimal.valueOf(300000.00).setScale(2));
    }
    
    @Test
    void shouldCalculateTieredDiscountForMidTier() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        parameters.put("baseAmount", BigDecimal.valueOf(1500000)); // Mid tier
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then - Should apply 10% discount for amounts >= 1000000 but < 2000000
        assertThat(result).isEqualTo(BigDecimal.valueOf(150000.00).setScale(2));
    }
    
    @Test
    void shouldCalculateTieredDiscountForLowTier() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        parameters.put("baseAmount", BigDecimal.valueOf(800000)); // Low tier
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then - Should apply 5% discount for amounts >= 500000 but < 1000000
        assertThat(result).isEqualTo(BigDecimal.valueOf(40000.00).setScale(2));
    }
    
    @Test
    void shouldReturnZeroForAmountBelowAllTiers() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        parameters.put("baseAmount", BigDecimal.valueOf(300000)); // Below all tiers
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    void shouldUseCustomerAnnualIncomeAsDefaultBaseAmount() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        // No baseAmount specified, should use customer's annual income (2,000,000)
        
        // When
        BigDecimal result = strategy.calculate(testCustomer, parameters);
        
        // Then - Should apply highest tier (15%) to customer's annual income
        BigDecimal expectedDiscount = testCustomer.getBasicInfo().getAnnualIncome()
                                                 .multiply(BigDecimal.valueOf(15))
                                                 .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualTo(expectedDiscount);
    }
    
    @Test
    void shouldCreatePromotionResultWithTierDetails() {
        // Given
        Map<String, Object> parameters = createTieredParameters();
        parameters.put("baseAmount", BigDecimal.valueOf(1500000));
        parameters.put("promotionName", "階層式VIP優惠");
        parameters.put("promotionId", "TIERED_VIP_001");
        
        BigDecimal calculatedAmount = strategy.calculate(testCustomer, parameters);
        
        // When
        PromotionResult result = strategy.createPromotionResult(testCustomer, calculatedAmount, parameters);
        
        // Then
        assertThat(result.getPromotionId()).isEqualTo("TIERED_VIP_001");
        assertThat(result.getPromotionName()).isEqualTo("階層式VIP優惠");
        assertThat(result.getPromotionType()).isEqualTo("TIERED_DISCOUNT");
        assertThat(result.getDiscountAmount()).isEqualTo(calculatedAmount);
        assertThat(result.getDiscountPercentage()).isEqualTo(BigDecimal.valueOf(10)); // Mid tier
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDescription()).contains("階層式折扣優惠");
        assertThat(result.getDescription()).contains("1500000");
        assertThat(result.getDescription()).contains("10.0%");
    }
    
    @Test
    void shouldValidateParametersCorrectly() {
        // Valid parameters
        Map<String, Object> validParams = createTieredParameters();
        assertThat(strategy.validateParameters(validParams)).isTrue();
        
        // Invalid: null parameters
        assertThat(strategy.validateParameters(null)).isFalse();
        
        // Invalid: empty parameters
        assertThat(strategy.validateParameters(new HashMap<>())).isFalse();
        
        // Invalid: missing tiers
        Map<String, Object> missingTiers = new HashMap<>();
        missingTiers.put("baseAmount", BigDecimal.valueOf(1000));
        assertThat(strategy.validateParameters(missingTiers)).isFalse();
        
        // Invalid: empty tiers list
        Map<String, Object> emptyTiers = new HashMap<>();
        emptyTiers.put("tiers", List.of());
        assertThat(strategy.validateParameters(emptyTiers)).isFalse();
    }
    
    @Test
    void shouldThrowExceptionForInvalidParameters() {
        // Given
        Map<String, Object> invalidParameters = new HashMap<>();
        invalidParameters.put("tiers", "invalid_tiers_format");
        
        // When & Then
        assertThatThrownBy(() -> strategy.calculate(testCustomer, invalidParameters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid parameters");
    }
    
    @Test
    void shouldReturnCorrectStrategyType() {
        // When & Then
        assertThat(strategy.getStrategyType()).isEqualTo("TIERED_DISCOUNT");
    }
    
    private Map<String, Object> createTieredParameters() {
        Map<String, Object> parameters = new HashMap<>();
        
        // Create tier structure
        List<Map<String, Object>> tiers = List.of(
            Map.of(
                "minAmount", BigDecimal.valueOf(2000000),
                "discountPercentage", BigDecimal.valueOf(15),
                "description", "VIP頂級客戶"
            ),
            Map.of(
                "minAmount", BigDecimal.valueOf(1000000),
                "discountPercentage", BigDecimal.valueOf(10),
                "description", "VIP客戶"
            ),
            Map.of(
                "minAmount", BigDecimal.valueOf(500000),
                "discountPercentage", BigDecimal.valueOf(5),
                "description", "優質客戶"
            )
        );
        
        parameters.put("tiers", tiers);
        return parameters;
    }
}