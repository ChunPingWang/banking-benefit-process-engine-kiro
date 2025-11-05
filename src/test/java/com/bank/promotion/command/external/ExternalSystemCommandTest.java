package com.bank.promotion.command.external;

import com.bank.promotion.command.mock.MockExternalSystemAdapter;
import com.bank.promotion.domain.command.external.ExternalSystemCommand;
import com.bank.promotion.domain.command.external.ExternalSystemResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 外部系統命令單元測試
 */
@ExtendWith(MockitoExtension.class)
class ExternalSystemCommandTest {
    
    @Mock
    private ExecutionContext mockContext;
    
    @Mock
    private CustomerPayload mockCustomer;
    
    private MockExternalSystemAdapter mockAdapter;
    
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
        
        mockAdapter = new MockExternalSystemAdapter();
    }
    
    @Test
    void shouldExecuteConditionCommandSuccessfully() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 回應
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(
                Map.of("conditionResult", true)
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldExecuteCalculationCommandSuccessfully() {
        // Given
        NodeConfiguration config = createCalculationConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 回應
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(
                Map.of(
                        "discountAmount", 5000.0,
                        "promotionName", "外部系統優惠",
                        "promotionType", "EXTERNAL_CALCULATED"
                )
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isInstanceOf(PromotionResult.class);
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(5000.0));
        assertThat(promotionResult.getPromotionName()).isEqualTo("外部系統優惠");
        assertThat(promotionResult.getPromotionType()).isEqualTo("EXTERNAL_CALCULATED");
    }
    
    @Test
    void shouldHandleConditionWithStringResult() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 回應 - 字串結果
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(
                Map.of("conditionResult", "true")
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldHandleConditionWithNumericResult() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 回應 - 數字結果
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(
                Map.of("conditionResult", 1)
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldHandleEmptyResponseAsTrue() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 回應 - 有資料但沒有 conditionResult
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(
                Map.of("someData", "value")
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(true);
    }
    
    @Test
    void shouldUseFallbackWhenExternalSystemFails() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "http://test-endpoint",
                "systemType", "HTTP",
                "enableFallback", true,
                "fallbackConditionValue", false
        );
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint", parameters);
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 失敗回應
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockFailureResponse(
                "External system unavailable"
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(false); // 降級值
    }
    
    @Test
    void shouldUseFallbackForCalculationCommand() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "http://test-endpoint",
                "systemType", "HTTP",
                "enableFallback", true,
                "fallbackDiscountAmount", "1000",
                "fallbackPromotionName", "降級優惠"
        );
        NodeConfiguration config = createCalculationConfiguration("http://test-endpoint", parameters);
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 失敗回應
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockFailureResponse(
                "External system unavailable"
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isInstanceOf(PromotionResult.class);
        
        PromotionResult promotionResult = (PromotionResult) result.getResult();
        assertThat(promotionResult.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(1000.0));
        assertThat(promotionResult.getPromotionName()).isEqualTo("降級優惠");
        assertThat(promotionResult.getPromotionType()).isEqualTo("FALLBACK");
    }
    
    @Test
    void shouldFailWhenFallbackDisabled() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "http://test-endpoint",
                "systemType", "HTTP",
                "enableFallback", false
        );
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint", parameters);
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // 配置 Mock 失敗回應
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockFailureResponse(
                "External system unavailable"
        );
        mockAdapter.configureMockResponse("http://test-endpoint", mockResponse);
        
        // 使用反射設定 Mock 適配器
        ReflectionTestUtils.setField(command, "adapter", mockAdapter);
        
        // When
        NodeResult result = command.execute(mockContext);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("External system call failed");
    }
    
    @Test
    void shouldThrowExceptionForEmptyEndpoint() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "",
                "systemType", "HTTP"
        );
        NodeConfiguration config = createConditionConfiguration("", parameters);
        
        // When & Then
        assertThatThrownBy(() -> new ExternalSystemCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("External system endpoint cannot be empty");
    }
    
    @Test
    void shouldThrowExceptionForUnsupportedSystemType() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "http://test-endpoint",
                "systemType", "UNSUPPORTED"
        );
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint", parameters);
        
        // When & Then
        assertThatThrownBy(() -> new ExternalSystemCommand(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported external system type");
    }
    
    @Test
    void shouldReturnCorrectCommandTypeForCondition() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // When
        String commandType = command.getCommandType();
        
        // Then
        assertThat(commandType).isEqualTo("EXTERNAL_SYSTEM_CONDITION");
    }
    
    @Test
    void shouldReturnCorrectCommandTypeForCalculation() {
        // Given
        NodeConfiguration config = createCalculationConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // When
        String commandType = command.getCommandType();
        
        // Then
        assertThat(commandType).isEqualTo("EXTERNAL_SYSTEM_CALCULATION");
    }
    
    @Test
    void shouldValidateCorrectConfiguration() {
        // Given
        NodeConfiguration config = createConditionConfiguration("http://test-endpoint");
        ExternalSystemCommand command = new ExternalSystemCommand(config);
        
        // When
        boolean isValid = command.isValidConfiguration();
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void shouldInvalidateEmptyEndpoint() {
        // Given
        Map<String, Object> parameters = Map.of(
                "endpoint", "",
                "systemType", "HTTP"
        );
        NodeConfiguration config = new NodeConfiguration(
                "test-node", "CONDITION", null,
                "EXTERNAL_SYSTEM", parameters, "Test configuration"
        );
        
        // When & Then
        assertThatThrownBy(() -> new ExternalSystemCommand(config))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    /**
     * 建立條件命令配置
     */
    private NodeConfiguration createConditionConfiguration(String endpoint) {
        Map<String, Object> parameters = Map.of(
                "endpoint", endpoint,
                "systemType", "HTTP"
        );
        return createConditionConfiguration(endpoint, parameters);
    }
    
    /**
     * 建立條件命令配置（含參數）
     */
    private NodeConfiguration createConditionConfiguration(String endpoint, Map<String, Object> parameters) {
        return new NodeConfiguration(
                "test-node", "CONDITION", null,
                "EXTERNAL_SYSTEM", parameters, "Test external system condition"
        );
    }
    
    /**
     * 建立計算命令配置
     */
    private NodeConfiguration createCalculationConfiguration(String endpoint) {
        Map<String, Object> parameters = Map.of(
                "endpoint", endpoint,
                "systemType", "HTTP"
        );
        return createCalculationConfiguration(endpoint, parameters);
    }
    
    /**
     * 建立計算命令配置（含參數）
     */
    private NodeConfiguration createCalculationConfiguration(String endpoint, Map<String, Object> parameters) {
        return new NodeConfiguration(
                "test-node", "CALCULATION", null,
                "EXTERNAL_SYSTEM", parameters, "Test external system calculation"
        );
    }
}