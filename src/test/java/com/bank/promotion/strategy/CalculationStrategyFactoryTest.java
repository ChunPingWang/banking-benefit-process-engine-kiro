package com.bank.promotion.strategy;

import com.bank.promotion.domain.strategy.CalculationStrategy;
import com.bank.promotion.domain.strategy.CalculationStrategyFactory;
import com.bank.promotion.domain.strategy.FixedAmountStrategy;
import com.bank.promotion.domain.strategy.PercentageDiscountStrategy;
import com.bank.promotion.domain.strategy.TieredDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CalculationStrategyFactoryTest {
    
    private CalculationStrategyFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new CalculationStrategyFactory();
    }
    
    @Test
    void shouldReturnPercentageDiscountStrategy() {
        // When
        CalculationStrategy strategy = factory.getStrategy("PERCENTAGE_DISCOUNT");
        
        // Then
        assertThat(strategy).isInstanceOf(PercentageDiscountStrategy.class);
        assertThat(strategy.getStrategyType()).isEqualTo("PERCENTAGE_DISCOUNT");
    }
    
    @Test
    void shouldReturnTieredDiscountStrategy() {
        // When
        CalculationStrategy strategy = factory.getStrategy("TIERED_DISCOUNT");
        
        // Then
        assertThat(strategy).isInstanceOf(TieredDiscountStrategy.class);
        assertThat(strategy.getStrategyType()).isEqualTo("TIERED_DISCOUNT");
    }
    
    @Test
    void shouldReturnFixedAmountStrategy() {
        // When
        CalculationStrategy strategy = factory.getStrategy("FIXED_AMOUNT");
        
        // Then
        assertThat(strategy).isInstanceOf(FixedAmountStrategy.class);
        assertThat(strategy.getStrategyType()).isEqualTo("FIXED_AMOUNT");
    }
    
    @Test
    void shouldBeCaseInsensitive() {
        // When & Then
        assertThat(factory.getStrategy("percentage_discount")).isInstanceOf(PercentageDiscountStrategy.class);
        assertThat(factory.getStrategy("Tiered_Discount")).isInstanceOf(TieredDiscountStrategy.class);
        assertThat(factory.getStrategy("fixed_amount")).isInstanceOf(FixedAmountStrategy.class);
    }
    
    @Test
    void shouldThrowExceptionForUnknownStrategyType() {
        // When & Then
        assertThatThrownBy(() -> factory.getStrategy("UNKNOWN_STRATEGY"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown strategy type: UNKNOWN_STRATEGY");
    }
    
    @Test
    void shouldThrowExceptionForNullStrategyType() {
        // When & Then
        assertThatThrownBy(() -> factory.getStrategy(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strategy type cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyStrategyType() {
        // When & Then
        assertThatThrownBy(() -> factory.getStrategy(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strategy type cannot be null or empty");
        
        assertThatThrownBy(() -> factory.getStrategy("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strategy type cannot be null or empty");
    }
    
    @Test
    void shouldReturnTrueForSupportedStrategyTypes() {
        // When & Then
        assertThat(factory.isStrategySupported("PERCENTAGE_DISCOUNT")).isTrue();
        assertThat(factory.isStrategySupported("TIERED_DISCOUNT")).isTrue();
        assertThat(factory.isStrategySupported("FIXED_AMOUNT")).isTrue();
        assertThat(factory.isStrategySupported("percentage_discount")).isTrue(); // Case insensitive
    }
    
    @Test
    void shouldReturnFalseForUnsupportedStrategyTypes() {
        // When & Then
        assertThat(factory.isStrategySupported("UNKNOWN_STRATEGY")).isFalse();
        assertThat(factory.isStrategySupported(null)).isFalse();
        assertThat(factory.isStrategySupported("")).isFalse();
        assertThat(factory.isStrategySupported("   ")).isFalse();
    }
    
    @Test
    void shouldReturnAllSupportedStrategyTypes() {
        // When
        var supportedTypes = factory.getSupportedStrategyTypes();
        
        // Then
        assertThat(supportedTypes).hasSize(3);
        assertThat(supportedTypes).contains("PERCENTAGE_DISCOUNT", "TIERED_DISCOUNT", "FIXED_AMOUNT");
    }
    
    @Test
    void shouldRegisterNewStrategy() {
        // Given
        CalculationStrategy customStrategy = new CalculationStrategy() {
            @Override
            public java.math.BigDecimal calculate(com.bank.promotion.domain.aggregate.CustomerProfile customer, java.util.Map<String, Object> parameters) {
                return java.math.BigDecimal.ZERO;
            }
            
            @Override
            public com.bank.promotion.domain.valueobject.PromotionResult createPromotionResult(com.bank.promotion.domain.aggregate.CustomerProfile customer, java.math.BigDecimal calculatedAmount, java.util.Map<String, Object> parameters) {
                return null;
            }
            
            @Override
            public String getStrategyType() {
                return "CUSTOM_STRATEGY";
            }
            
            @Override
            public boolean validateParameters(java.util.Map<String, Object> parameters) {
                return true;
            }
        };
        
        // When
        factory.registerStrategy(customStrategy);
        
        // Then
        assertThat(factory.isStrategySupported("CUSTOM_STRATEGY")).isTrue();
        assertThat(factory.getStrategy("CUSTOM_STRATEGY")).isEqualTo(customStrategy);
        assertThat(factory.getStrategyCount()).isEqualTo(4);
    }
    
    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateStrategy() {
        // Given
        CalculationStrategy duplicateStrategy = new PercentageDiscountStrategy();
        
        // When & Then
        assertThatThrownBy(() -> factory.registerStrategy(duplicateStrategy))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strategy type already exists: PERCENTAGE_DISCOUNT");
    }
    
    @Test
    void shouldThrowExceptionWhenRegisteringNullStrategy() {
        // When & Then
        assertThatThrownBy(() -> factory.registerStrategy(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strategy cannot be null");
    }
    
    @Test
    void shouldRemoveStrategy() {
        // Given
        assertThat(factory.isStrategySupported("PERCENTAGE_DISCOUNT")).isTrue();
        
        // When
        boolean removed = factory.removeStrategy("PERCENTAGE_DISCOUNT");
        
        // Then
        assertThat(removed).isTrue();
        assertThat(factory.isStrategySupported("PERCENTAGE_DISCOUNT")).isFalse();
        assertThat(factory.getStrategyCount()).isEqualTo(2);
    }
    
    @Test
    void shouldReturnFalseWhenRemovingNonExistentStrategy() {
        // When
        boolean removed = factory.removeStrategy("NON_EXISTENT");
        
        // Then
        assertThat(removed).isFalse();
        assertThat(factory.getStrategyCount()).isEqualTo(3);
    }
    
    @Test
    void shouldClearAllStrategies() {
        // Given
        assertThat(factory.getStrategyCount()).isEqualTo(3);
        
        // When
        factory.clearAllStrategies();
        
        // Then
        assertThat(factory.getStrategyCount()).isEqualTo(0);
        assertThat(factory.getSupportedStrategyTypes()).isEmpty();
    }
    
    @Test
    void shouldReinitializeDefaultStrategies() {
        // Given
        factory.clearAllStrategies();
        assertThat(factory.getStrategyCount()).isEqualTo(0);
        
        // When
        factory.reinitializeDefaultStrategies();
        
        // Then
        assertThat(factory.getStrategyCount()).isEqualTo(3);
        assertThat(factory.isStrategySupported("PERCENTAGE_DISCOUNT")).isTrue();
        assertThat(factory.isStrategySupported("TIERED_DISCOUNT")).isTrue();
        assertThat(factory.isStrategySupported("FIXED_AMOUNT")).isTrue();
    }
}