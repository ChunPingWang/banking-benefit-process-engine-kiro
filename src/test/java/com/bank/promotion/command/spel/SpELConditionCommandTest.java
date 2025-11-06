package com.bank.promotion.command.spel;

import com.bank.promotion.domain.command.spel.SpELConditionCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
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

import org.mockito.Mockito;

/**
 * SpEL 條件命令單元測試
 */
class SpELConditionCommandTest {
    
    private ExecutionContext mockContext;
    private CustomerPayload mockCustomer;
    
    @BeforeEach
    void setUp() {
        mockContext = Mockito.mock(ExecutionContext.class);
        mockCustomer = Mockito.mock(CustomerPayload.class);
        
        when(mockContext.getCustomerPayload()).thenReturn(mockCustomer);
        when(mockContext.getContextData()).thenReturn(Map.of());
    }
    
    @Test
    void shouldReturnTrueWhenCreditScoreConditionMet() {
        // Given
        NodeConfiguration config = createTestConfiguration("#creditScore > 700");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getCreditScore()).thenReturn(750);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldReturnFalseWhenCreditScoreConditionNotMet() {
        // Given
        NodeConfiguration config = createTestConfiguration("#creditScore > 700");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getCreditScore()).thenReturn(650);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(false);
    }
    
    @Test
    void shouldEvaluateComplexExpression() {
        // Given
        String expression = "#creditScore > 700 and #annualIncome > 1000000";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getCreditScore()).thenReturn(750);
        when(mockCustomer.getAnnualIncome()).thenReturn(BigDecimal.valueOf(1500000));
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldEvaluateAccountTypeCondition() {
        // Given
        String expression = "#accountType == 'VIP'";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getAccountType()).thenReturn("VIP");
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldEvaluateAccountBalanceCondition() {
        // Given
        String expression = "#accountBalance >= 500000";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getAccountBalance()).thenReturn(BigDecimal.valueOf(600000));
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldUseContextVariables() {
        // Given
        String expression = "#contextValue > 100";
        NodeConfiguration config = createTestConfiguration(expression);
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockContext.getContextData()).thenReturn(Map.of("contextValue", 150));
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldUseConfigurationParameters() {
        // Given
        String expression = "#creditScore > #param_minScore";
        Map<String, Object> parameters = Map.of("minScore", 700);
        NodeConfiguration config = createTestConfiguration(expression, parameters);
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        when(mockCustomer.getCreditScore()).thenReturn(750);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldHandleNullCustomerPayload() {
        // Given
        NodeConfiguration config = createTestConfiguration("#creditScore > 700");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        ExecutionContext nullContext = Mockito.mock(ExecutionContext.class);
        when(nullContext.getCustomerPayload()).thenReturn(null);
        when(nullContext.getContextData()).thenReturn(Map.of());
        
        // When
        NodeResult result = command.execute(nullContext);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
    }
    
    @Test
    void shouldHandleInvalidExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration("#invalidProperty");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        ExecutionContext invalidContext = Mockito.mock(ExecutionContext.class);
        CustomerPayload invalidCustomer = Mockito.mock(CustomerPayload.class);
        when(invalidContext.getCustomerPayload()).thenReturn(invalidCustomer);
        when(invalidContext.getContextData()).thenReturn(Map.of());
        
        // When
        NodeResult result = command.execute(invalidContext);
        
        // Then
        // SpEL 可能會將不存在的屬性評估為 null，然後轉換為 false
        // 這仍然是一個成功的操作，只是結果為 false
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(false);
    }
    
    @Test
    void shouldThrowExceptionForNullExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration(null);
        
        // When & Then
        assertThatThrownBy(() -> new SpELConditionCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SpEL expression cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyExpression() {
        // Given
        NodeConfiguration config = createTestConfiguration("");
        
        // When & Then
        assertThatThrownBy(() -> new SpELConditionCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SpEL expression cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForInvalidSyntax() {
        // Given
        NodeConfiguration config = createTestConfiguration("#{invalid syntax}");
        
        // When & Then
        assertThatThrownBy(() -> new SpELConditionCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid SpEL expression");
    }
    
    @Test
    void shouldReturnCorrectCommandType() {
        // Given
        NodeConfiguration config = createTestConfiguration("true");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        // When
        String commandType = command.getCommandType();
        
        // Then
        assertThat(commandType).isEqualTo("SPEL_CONDITION");
    }
    
    @Test
    void shouldValidateCorrectConfiguration() {
        // Given
        NodeConfiguration config = createTestConfiguration("#creditScore > 700");
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        // When
        boolean isValid = command.isValidConfiguration();
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldInvalidateWrongNodeType() {
        // Given
        NodeConfiguration config = new NodeConfiguration(
                "test-node", "CALCULATION", "#creditScore > 700",
                "SPEL", Map.of(), "Test configuration"
        );
        SpELConditionCommand command = new SpELConditionCommand(config);
        
        // When
        boolean isValid = command.isValidConfiguration();
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void shouldInvalidateWrongCommandType() {
        // Given
        NodeConfiguration config = new NodeConfiguration(
                "test-node", "CONDITION", "#creditScore > 700",
                "DROOLS", Map.of(), "Test configuration"
        );
        SpELConditionCommand command = new SpELConditionCommand(config);
        
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
                "test-node", "CONDITION", expression,
                "SPEL", parameters, "Test SpEL condition"
        );
    }
}