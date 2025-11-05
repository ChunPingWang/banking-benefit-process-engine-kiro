package com.bank.promotion.command.spel;

import com.bank.promotion.domain.command.spel.SpELCalculationCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * SpEL 計算命令單元測試
 */
@ExtendWith(MockitoExtension.class)
class SpELCalculationCommandTest {
    
    @Mock
    private ExecutionContext mockContext;
    
    @Mock
    private CustomerPayload mockCustomer;
    
    @BeforeEach
    void setUp() {
        when(mockContext.getCustomerPayload()).thenReturn(mockCustomer);
        when(mockContext.getContextData()).thenReturn(Map.of());
        
        // 設定預設客戶資料
        when(mockCustomer.getCustomerId()).thenReturn("CUST001");
        when(mockCustomer.getAccountType()).thenReturn("VIP");
        when(mockCustomer.getAnnualIncome()).thenReturn(BigDecimal.valueOf(2000000));
        when(mockCustomer.getCreditScore()).thenReturn(750);
        when(mockCustomer.getAccountBalance()).thenReturn(BigDecimal.valueOf(500000));
        when(mockCustomer.getTransactionHistory()).thenReturn(List.of());
    }
    
    @Test
    void shouldCalculatePercentageDiscount() {
        // Given
        String expression = "#{accountBalance * 0.05}"; // 5% 折扣
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isInstanceOf(PromotionResult.class);
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(25000.0));
        assertThat(promotionResult.getPromotionName()).isEqualTo("SpEL計算優惠");
        assertThat(promotionResult.isEligible()).isTrue();
    }
    
    @Test
    void shouldCalculateFixedAmount() {
        // Given
        String expression = "1000"; // 固定金額
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(1000.0));
    }
    
    @Test
    void shouldCalculateBasedOnCreditScore() {
        // Given
        String expression = "#{creditScore > 700 ? 5000 : 1000}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(5000.0));
    }
    
    @Test
    void shouldUseMinFunction() {
        // Given
        String expression = "#{min.min(accountBalance * 0.1, 10000)}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(10000.0));
    }
    
    @Test
    void shouldUseMaxFunction() {
        // Given
        String expression = "#{max.max(1000, creditScore)}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(1000.0));
    }
    
    @Test
    void shouldUseRoundFunction() {
        // Given
        String expression = "#{round.round(accountBalance * 0.0567, 0)}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(28350.0));
    }
    
    @Test
    void shouldUseConfigurationParameters() {
        // Given
        String expression = "#{accountBalance * param_discountRate}";
        Map<String, Object> parameters = Map.of(
                "discountRate", 0.03,
                "promotionName", "自定義優惠",
                "validityDays", 60
        );
        NodeConfiguration config = createTestConfiguration(expression, parameters);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(15000.0));
        assertThat(promotionResult.getPromotionName()).isEqualTo("自定義優惠");
    }
    
    @Test
    void shouldUseContextVariables() {
        // Given
        String expression = "#{contextMultiplier * 1000}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        when(mockContext.getContextData()).thenReturn(Map.of("contextMultiplier", 2.5));
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(2500.0));
    }
    
    @Test
    void shouldCalculateDiscountPercentage() {
        // Given
        String expression = "2500"; // 固定折扣金額
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(2500.0));
        // 2500 / 500000 * 100 = 0.5%
        assertThat(promotionResult.getDiscountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }
    
    @Test
    void shouldHandleNullCustomerPayload() {
        // Given
        NodeConfiguration config = createTestConfiguration("#{accountBalance * 0.05}");
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        when(mockContext.getCustomerPayload()).thenReturn(null);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("SpEL calculation failed");
    }
    
    @Test
    void shouldHandleInvalidExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration("#{invalidProperty}");
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("SpEL calculation failed");
    }
    
    @Test
    void shouldReturnZeroForNullResult() {
        // Given
        String expression = "#{null}";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    void shouldThrowExceptionForNullExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration(null);
        
        // When & Then
        assertThatThrownBy(() -> new SpELCalculationCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SpEL expression cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration("");
        
        // When & Then
        assertThatThrownBy(() -> new SpELCalculationCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SpEL expression cannot be null or empty");
    }
    
    @Test
    void shouldReturnCorrectCommandType() {
        // Given
        NodeConfiguration config = createTestConfiguration("1000");
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        String commandType = command.getCommandType();
        
        // Then
        assertThat(commandType).isEqualTo("SPEL_CALCULATION");
    }
    
    @Test
    void shouldValidateCorrectConfiguration() {
        // Given
        NodeConfiguration config = createTestConfiguration("#{accountBalance * 0.05}");
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        boolean isValid = command.isValidConfiguration();
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldInvalidateWrongNodeType() {
        // Given
        NodeConfiguration config = new NodeConfiguration(
                "test-node", "CONDITION", "#{accountBalance * 0.05}",
                "SPEL", Map.of(), "Test configuration"
        );
        SpELCalculationCommand command = new SpELCalculationCommand(config);
        
        // When
        boolean isValid = command.isValidConfiguration();
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    /**
     * 建立測試用的節點配置
     */
    private NodeConfiguration createTestConfiguration(String expression) {
        return createTestConfiguration(expression, Map.of());
    }
    
    /**
     * 建立測試用的節點配置（含參數）
     */
    private NodeConfiguration createTestConfiguration(String expression, Map<String, Object> parameters) {
        return new NodeConfiguration(
                "test-node", "CALCULATION", expression,
                "SPEL", parameters, "Test SpEL calculation"
        );
    }
}