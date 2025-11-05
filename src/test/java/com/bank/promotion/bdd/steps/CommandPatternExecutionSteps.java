package com.bank.promotion.bdd.steps;

import com.bank.promotion.command.mock.MockExternalSystemAdapter;
import com.bank.promotion.domain.command.CommandFactory;
import com.bank.promotion.domain.command.CommandRegistry;
import com.bank.promotion.domain.command.NodeCommand;
import com.bank.promotion.domain.command.database.DatabaseQueryCommand;
import com.bank.promotion.domain.command.drools.DroolsRuleCommand;
import com.bank.promotion.domain.command.external.ExternalSystemCommand;
import com.bank.promotion.domain.command.external.ExternalSystemResponse;
import com.bank.promotion.domain.command.spel.SpELCalculationCommand;
import com.bank.promotion.domain.command.spel.SpELConditionCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.exception.PromotionSystemException;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.而且;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Command Pattern 執行測試步驟定義
 */
@SpringBootTest
@ActiveProfiles("test")
public class CommandPatternExecutionSteps {
    
    @Autowired
    private CommandFactory commandFactory;
    
    @Autowired
    private CommandRegistry commandRegistry;
    
    @Autowired
    private MockExternalSystemAdapter mockExternalSystemAdapter;
    
    private NodeConfiguration currentConfiguration;
    private NodeCommand currentCommand;
    private ExecutionContext executionContext;
    private NodeResult executionResult;
    private CustomerPayload customerPayload;
    private Map<String, Object> contextData = new HashMap<>();
    private Map<String, Object> configurationParameters = new HashMap<>();
    private Exception lastException;
    private long executionStartTime;
    private long executionEndTime;
    
    @假設("系統已初始化完成")
    public void 系統已初始化完成() {
        // 清理之前的測試資料
        contextData.clear();
        configurationParameters.clear();
        mockExternalSystemAdapter.clearAllMocks();
        lastException = null;
        executionResult = null;
        
        // 建立預設客戶資料
        customerPayload = new CustomerPayload(
                "CUST001", "VIP", 
                BigDecimal.valueOf(2000000), 750, 
                "台北", 50,
                BigDecimal.valueOf(500000), 
                List.of()
        );
    }
    
    @而且("測試資料已準備就緒")
    public void 測試資料已準備就緒() {
        // 建立執行上下文
        executionContext = new ExecutionContext(customerPayload, contextData);
    }
    
    @假設("系統配置了 SpEL 條件命令 {string}")
    public void 系統配置了SpEL條件命令(String expression) {
        currentConfiguration = new NodeConfiguration(
                "test-spel-condition", "CONDITION", expression,
                "SPEL", configurationParameters, "Test SpEL condition"
        );
    }
    
    @假設("系統配置了 SpEL 計算命令 {string}")
    public void 系統配置了SpEL計算命令(String expression) {
        currentConfiguration = new NodeConfiguration(
                "test-spel-calculation", "CALCULATION", expression,
                "SPEL", configurationParameters, "Test SpEL calculation"
        );
    }
    
    @而且("客戶信用評分為 {int}")
    public void 客戶信用評分為(int creditScore) {
        customerPayload = new CustomerPayload(
                customerPayload.getCustomerId(),
                customerPayload.getAccountType(),
                customerPayload.getAnnualIncome(),
                creditScore,
                customerPayload.getRegion(),
                customerPayload.getTransactionCount(),
                customerPayload.getAccountBalance(),
                customerPayload.getTransactionHistory()
        );
        executionContext = new ExecutionContext(customerPayload, contextData);
    }
    
    @而且("客戶帳戶餘額為 {int} 元")
    public void 客戶帳戶餘額為元(int accountBalance) {
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
        executionContext = new ExecutionContext(customerPayload, contextData);
    }
    
    @而且("客戶年收入為 {int} 元")
    public void 客戶年收入為元(int annualIncome) {
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
        executionContext = new ExecutionContext(customerPayload, contextData);
    }
    
    @而且("配置參數 {string} 為 {double}")
    public void 配置參數為(String paramName, double paramValue) {
        configurationParameters.put(paramName, paramValue);
    }
    
    @而且("配置參數 {string} 為 {string}")
    public void 配置參數為字串(String paramName, String paramValue) {
        configurationParameters.put(paramName, paramValue);
    }
    
    @當("執行 SpEL 條件命令")
    public void 執行SpEL條件命令() {
        try {
            executionStartTime = System.currentTimeMillis();
            currentCommand = new SpELConditionCommand(currentConfiguration);
            executionResult = currentCommand.execute(executionContext);
            executionEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("執行 SpEL 計算命令")
    public void 執行SpEL計算命令() {
        try {
            executionStartTime = System.currentTimeMillis();
            currentCommand = new SpELCalculationCommand(currentConfiguration);
            executionResult = currentCommand.execute(executionContext);
            executionEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @那麼("命令應該成功執行")
    public void 命令應該成功執行() {
        assertThat(lastException).isNull();
        assertThat(executionResult).isNotNull();
        assertThat(executionResult.isSuccess()).isTrue();
    }
    
    @那麼("命令應該執行失敗")
    public void 命令應該執行失敗() {
        assertThat(executionResult).isNotNull();
        assertThat(executionResult.isSuccess()).isFalse();
    }
    
    @而且("返回結果應該為 true")
    public void 返回結果應該為true() {
        assertThat(executionResult.getResult()).isEqualTo(true);
    }
    
    @而且("返回結果應該為 false")
    public void 返回結果應該為false() {
        assertThat(executionResult.getResult()).isEqualTo(false);
    }
    
    @而且("命令類型應該為 {string}")
    public void 命令類型應該為(String expectedType) {
        assertThat(currentCommand.getCommandType()).isEqualTo(expectedType);
    }
    
    @而且("返回優惠結果應該包含折扣金額 {int} 元")
    public void 返回優惠結果應該包含折扣金額元(int expectedAmount) {
        assertThat(executionResult.getResult()).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult.getResult();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(expectedAmount));
    }
    
    @而且("優惠名稱應該為 {string}")
    public void 優惠名稱應該為(String expectedName) {
        assertThat(executionResult.getResult()).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult.getResult();
        assertThat(result.getPromotionName()).isEqualTo(expectedName);
    }
    
    @而且("優惠類型應該為 {string}")
    public void 優惠類型應該為(String expectedType) {
        assertThat(executionResult.getResult()).isInstanceOf(PromotionResult.class);
        PromotionResult result = (PromotionResult) executionResult.getResult();
        assertThat(result.getPromotionType()).isEqualTo(expectedType);
    }
    
    @而且("錯誤訊息應該包含 {string}")
    public void 錯誤訊息應該包含(String expectedMessage) {
        assertThat(executionResult.getErrorMessage()).contains(expectedMessage);
    }
    
    // Drools 相關步驟
    @假設("系統配置了 Drools 規則命令 {string}")
    public void 系統配置了Drools規則命令(String ruleName) {
        configurationParameters.put("ruleName", ruleName);
    }
    
    @假設("系統配置了 Drools 計算命令 {string}")
    public void 系統配置了Drools計算命令(String ruleName) {
        configurationParameters.put("ruleName", ruleName);
    }
    
    @而且("Drools 規則內容為:")
    public void Drools規則內容為(String ruleContent) {
        currentConfiguration = new NodeConfiguration(
                "test-drools-rule", "CONDITION", ruleContent,
                "DROOLS", configurationParameters, "Test Drools rule"
        );
    }
    
    @當("執行 Drools 規則命令")
    public void 執行Drools規則命令() {
        try {
            executionStartTime = System.currentTimeMillis();
            currentCommand = new DroolsRuleCommand(currentConfiguration);
            executionResult = currentCommand.execute(executionContext);
            executionEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("執行 Drools 計算命令")
    public void 執行Drools計算命令() {
        // 建立計算類型的配置
        currentConfiguration = new NodeConfiguration(
                "test-drools-calculation", "CALCULATION", currentConfiguration.getExpression(),
                "DROOLS", configurationParameters, "Test Drools calculation"
        );
        
        try {
            executionStartTime = System.currentTimeMillis();
            currentCommand = new DroolsRuleCommand(currentConfiguration);
            executionResult = currentCommand.execute(executionContext);
            executionEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    // 外部系統相關步驟
    @假設("系統配置了外部系統條件命令呼叫信用評估服務")
    public void 系統配置了外部系統條件命令呼叫信用評估服務() {
        configurationParameters.put("systemType", "HTTP");
        configurationParameters.put("enableFallback", true);
    }
    
    @假設("系統配置了外部系統計算命令呼叫優惠計算服務")
    public void 系統配置了外部系統計算命令呼叫優惠計算服務() {
        configurationParameters.put("systemType", "HTTP");
        configurationParameters.put("enableFallback", true);
    }
    
    @而且("外部系統端點為 {string}")
    public void 外部系統端點為(String endpoint) {
        configurationParameters.put("endpoint", endpoint);
    }
    
    @而且("外部信用評估服務返回結果:")
    public void 外部信用評估服務返回結果(DataTable dataTable) {
        Map<String, Object> responseData = new HashMap<>();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 嘗試轉換為適當的資料類型
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    responseData.put(key, Boolean.parseBoolean(value));
                } else {
                    try {
                        responseData.put(key, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        responseData.put(key, value);
                    }
                }
            }
        }
        
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(responseData);
        String endpoint = (String) configurationParameters.get("endpoint");
        mockExternalSystemAdapter.configureMockResponse(endpoint, mockResponse);
        
        // 建立外部系統條件命令配置
        currentConfiguration = new NodeConfiguration(
                "test-external-condition", "CONDITION", null,
                "EXTERNAL_SYSTEM", configurationParameters, "Test external system condition"
        );
    }
    
    @而且("外部優惠計算服務返回結果:")
    public void 外部優惠計算服務返回結果(DataTable dataTable) {
        Map<String, Object> responseData = new HashMap<>();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 嘗試轉換為適當的資料類型
                try {
                    responseData.put(key, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    responseData.put(key, value);
                }
            }
        }
        
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockSuccessResponse(responseData);
        String endpoint = (String) configurationParameters.get("endpoint");
        mockExternalSystemAdapter.configureMockResponse(endpoint, mockResponse);
        
        // 建立外部系統計算命令配置
        currentConfiguration = new NodeConfiguration(
                "test-external-calculation", "CALCULATION", null,
                "EXTERNAL_SYSTEM", configurationParameters, "Test external system calculation"
        );
    }
    
    @而且("外部信用評估服務不可用")
    public void 外部信用評估服務不可用() {
        String endpoint = (String) configurationParameters.get("endpoint");
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockFailureResponse(
                "External system unavailable"
        );
        mockExternalSystemAdapter.configureMockResponse(endpoint, mockResponse);
    }
    
    @而且("外部優惠計算服務不可用")
    public void 外部優惠計算服務不可用() {
        String endpoint = (String) configurationParameters.get("endpoint");
        ExternalSystemResponse mockResponse = MockExternalSystemAdapter.createMockFailureResponse(
                "External system unavailable"
        );
        mockExternalSystemAdapter.configureMockResponse(endpoint, mockResponse);
    }
    
    @而且("系統啟用降級策略")
    public void 系統啟用降級策略() {
        configurationParameters.put("enableFallback", true);
    }
    
    @而且("降級條件值設定為 false")
    public void 降級條件值設定為false() {
        configurationParameters.put("fallbackConditionValue", false);
    }
    
    @而且("降級折扣金額設定為 {int} 元")
    public void 降級折扣金額設定為元(int amount) {
        configurationParameters.put("fallbackDiscountAmount", String.valueOf(amount));
    }
    
    @而且("降級優惠名稱設定為 {string}")
    public void 降級優惠名稱設定為(String name) {
        configurationParameters.put("fallbackPromotionName", name);
    }
    
    @當("執行外部系統命令")
    public void 執行外部系統命令() {
        try {
            executionStartTime = System.currentTimeMillis();
            currentCommand = new ExternalSystemCommand(currentConfiguration);
            executionResult = currentCommand.execute(executionContext);
            executionEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @那麼("命令應該執行降級邏輯")
    public void 命令應該執行降級邏輯() {
        assertThat(executionResult).isNotNull();
        assertThat(executionResult.isSuccess()).isTrue();
    }
    
    @而且("系統應該記錄降級事件")
    public void 系統應該記錄降級事件() {
        // 這裡可以檢查日誌或事件記錄
        // 目前簡化實作，只檢查結果中是否包含降級相關資訊
        if (executionResult.getResult() instanceof PromotionResult) {
            PromotionResult result = (PromotionResult) executionResult.getResult();
            assertThat(result.getAdditionalDetails()).containsKey("fallbackReason");
        }
    }
    
    // 命令工廠相關步驟
    @假設("命令工廠已初始化")
    public void 命令工廠已初始化() {
        assertThat(commandFactory).isNotNull();
    }
    
    @當("使用工廠創建 {string} 類型的命令")
    public void 使用工廠創建類型的命令(String commandType) {
        try {
            // 根據命令類型建立適當的配置
            NodeConfiguration config = createConfigurationForCommandType(commandType);
            currentCommand = commandFactory.createCommand(config);
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @那麼("應該成功創建 SpEL 條件命令")
    public void 應該成功創建SpEL條件命令() {
        assertThat(currentCommand).isInstanceOf(SpELConditionCommand.class);
    }
    
    @那麼("應該成功創建 SpEL 計算命令")
    public void 應該成功創建SpEL計算命令() {
        assertThat(currentCommand).isInstanceOf(SpELCalculationCommand.class);
    }
    
    @那麼("應該成功創建 Drools 規則命令")
    public void 應該成功創建Drools規則命令() {
        assertThat(currentCommand).isInstanceOf(DroolsRuleCommand.class);
    }
    
    @那麼("應該成功創建外部系統命令")
    public void 應該成功創建外部系統命令() {
        assertThat(currentCommand).isInstanceOf(ExternalSystemCommand.class);
    }
    
    @那麼("應該成功創建資料庫查詢命令")
    public void 應該成功創建資料庫查詢命令() {
        assertThat(currentCommand).isInstanceOf(DatabaseQueryCommand.class);
    }
    
    @那麼("應該拋出 {string} 異常")
    public void 應該拋出異常(String expectedMessage) {
        assertThat(lastException).isNotNull();
        assertThat(lastException).isInstanceOf(PromotionSystemException.class);
        assertThat(lastException.getMessage()).contains(expectedMessage);
    }
    
    @而且("執行時間應該大於等於 {int} 毫秒")
    public void 執行時間應該大於等於毫秒(int expectedMinTime) {
        long actualTime = executionEndTime - executionStartTime;
        assertThat(actualTime).isGreaterThanOrEqualTo(expectedMinTime);
    }
    
    @而且("系統應該記錄執行時間")
    public void 系統應該記錄執行時間() {
        assertThat(executionEndTime).isGreaterThan(executionStartTime);
    }
    
    /**
     * 根據命令類型建立適當的配置
     */
    private NodeConfiguration createConfigurationForCommandType(String commandType) {
        switch (commandType) {
            case "SPEL_CONDITION":
                return new NodeConfiguration(
                        "test-node", "CONDITION", "#{true}",
                        "SPEL", Map.of(), "Test configuration"
                );
            case "SPEL_CALCULATION":
                return new NodeConfiguration(
                        "test-node", "CALCULATION", "1000",
                        "SPEL", Map.of(), "Test configuration"
                );
            case "DROOLS_CONDITION":
                return new NodeConfiguration(
                        "test-node", "CONDITION", "rule \"test\" when then end",
                        "DROOLS", Map.of("ruleName", "test"), "Test configuration"
                );
            case "DROOLS_CALCULATION":
                return new NodeConfiguration(
                        "test-node", "CALCULATION", "rule \"test\" when then end",
                        "DROOLS", Map.of("ruleName", "test"), "Test configuration"
                );
            case "EXTERNAL_SYSTEM_CONDITION":
                return new NodeConfiguration(
                        "test-node", "CONDITION", null,
                        "EXTERNAL_SYSTEM", Map.of("endpoint", "http://test", "systemType", "HTTP"), "Test configuration"
                );
            case "EXTERNAL_SYSTEM_CALCULATION":
                return new NodeConfiguration(
                        "test-node", "CALCULATION", null,
                        "EXTERNAL_SYSTEM", Map.of("endpoint", "http://test", "systemType", "HTTP"), "Test configuration"
                );
            case "DATABASE_QUERY_CONDITION":
                return new NodeConfiguration(
                        "test-node", "CONDITION", null,
                        "DATABASE_QUERY", Map.of("connectionString", "jdbc:h2:mem:test", "queryTemplate", "SELECT 1"), "Test configuration"
                );
            case "DATABASE_QUERY_CALCULATION":
                return new NodeConfiguration(
                        "test-node", "CALCULATION", null,
                        "DATABASE_QUERY", Map.of("connectionString", "jdbc:h2:mem:test", "queryTemplate", "SELECT 1"), "Test configuration"
                );
            default:
                return new NodeConfiguration(
                        "test-node", "CONDITION", null,
                        commandType, Map.of(), "Test configuration"
                );
        }
    }
}