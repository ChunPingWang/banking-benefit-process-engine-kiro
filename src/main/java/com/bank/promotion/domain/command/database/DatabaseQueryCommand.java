package com.bank.promotion.domain.command.database;

import com.bank.promotion.domain.command.AbstractNodeCommand;
import com.bank.promotion.domain.command.external.DatabaseExternalSystemAdapter;
import com.bank.promotion.domain.command.external.ExternalSystemRequest;
import com.bank.promotion.domain.command.external.ExternalSystemResponse;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 資料庫查詢命令
 * 透過資料庫查詢執行條件判斷或計算邏輯
 */
public class DatabaseQueryCommand extends AbstractNodeCommand {
    
    private final DatabaseExternalSystemAdapter adapter;
    private final boolean isConditionCommand;
    private final int timeoutSeconds;
    
    public DatabaseQueryCommand(NodeConfiguration configuration) {
        super(configuration);
        
        this.isConditionCommand = "CONDITION".equals(configuration.getNodeType());
        this.timeoutSeconds = getIntParameter("timeoutSeconds", 30);
        
        // 建立資料庫適配器
        String connectionString = getStringParameter("connectionString", "");
        if (connectionString.isEmpty()) {
            throw new IllegalArgumentException("Database connection string cannot be empty");
        }
        
        this.adapter = new DatabaseExternalSystemAdapter(connectionString, configuration.getParameters());
    }
    
    @Override
    protected NodeResult doExecute(ExecutionContext context) {
        try {
            // 建立資料庫查詢請求
            ExternalSystemRequest request = buildDatabaseRequest(context);
            
            // 執行資料庫查詢
            ExternalSystemResponse response = adapter.call(request, timeoutSeconds, TimeUnit.SECONDS);
            
            // 處理查詢結果
            if (response.isSuccess()) {
                return handleSuccessResponse(response, context);
            } else {
                return NodeResult.failure("Database query failed: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            return NodeResult.failure("Database query execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 建立資料庫查詢請求
     */
    private ExternalSystemRequest buildDatabaseRequest(ExecutionContext context) {
        ExternalSystemRequest.Builder builder = ExternalSystemRequest.builder();
        
        // 設定客戶資料
        if (context.getCustomerPayload() != null) {
            builder.addParameter("customerId", context.getCustomerPayload().getCustomerId())
                   .addParameter("accountType", context.getCustomerPayload().getAccountType())
                   .addParameter("annualIncome", context.getCustomerPayload().getAnnualIncome())
                   .addParameter("creditScore", context.getCustomerPayload().getCreditScore())
                   .addParameter("accountBalance", context.getCustomerPayload().getAccountBalance())
                   .addParameter("transactionHistory", context.getCustomerPayload().getTransactionHistory());
        }
        
        // 設定上下文資料
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        
        // 設定查詢參數
        for (Map.Entry<String, Object> entry : configuration.getParameters().entrySet()) {
            if (!entry.getKey().equals("connectionString") && 
                !entry.getKey().equals("queryTemplate") && 
                !entry.getKey().equals("timeoutSeconds")) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        
        return builder.build();
    }
    
    /**
     * 處理成功的查詢回應
     */
    private NodeResult handleSuccessResponse(ExternalSystemResponse response, ExecutionContext context) {
        if (isConditionCommand) {
            return handleConditionResult(response);
        } else {
            return handleCalculationResult(response, context);
        }
    }
    
    /**
     * 處理條件查詢結果
     */
    private NodeResult handleConditionResult(ExternalSystemResponse response) {
        Object conditionResult = response.getData("conditionResult");
        
        if (conditionResult instanceof Boolean) {
            return NodeResult.success((Boolean) conditionResult);
        }
        
        if (conditionResult instanceof Number) {
            boolean result = ((Number) conditionResult).doubleValue() != 0.0;
            return NodeResult.success(result);
        }
        
        if (conditionResult instanceof String) {
            String strResult = ((String) conditionResult).toLowerCase().trim();
            boolean result = "true".equals(strResult) || "yes".equals(strResult) || "1".equals(strResult);
            return NodeResult.success(result);
        }
        
        // 檢查其他可能的條件欄位
        Object resultCount = response.getData("resultCount");
        if (resultCount instanceof Number) {
            boolean result = ((Number) resultCount).intValue() > 0;
            return NodeResult.success(result);
        }
        
        // 預設：有查詢結果表示條件成立
        return NodeResult.success(!response.getData().isEmpty());
    }
    
    /**
     * 處理計算查詢結果
     */
    private NodeResult handleCalculationResult(ExternalSystemResponse response, ExecutionContext context) {
        // 從查詢結果中提取優惠資訊
        Object amountObj = response.getData("discountAmount");
        BigDecimal discountAmount = convertToBigDecimal(amountObj);
        
        String promotionName = (String) response.getData().getOrDefault("promotionName", "資料庫查詢優惠");
        String promotionType = (String) response.getData().getOrDefault("promotionType", "DATABASE_QUERY");
        String description = (String) response.getData().getOrDefault("description", "透過資料庫查詢計算的優惠");
        
        // 建立優惠結果
        PromotionResult promotionResult = createPromotionResult(
            discountAmount, promotionName, promotionType, description, response.getData(), context
        );
        
        return NodeResult.success(promotionResult);
    }
    
    /**
     * 建立優惠結果
     */
    private PromotionResult createPromotionResult(BigDecimal discountAmount, String promotionName, 
                                                String promotionType, String description, 
                                                Map<String, Object> queryResult, ExecutionContext context) {
        
        int validityDays = getIntParameter("validityDays", 30);
        
        // 計算優惠百分比
        BigDecimal discountPercentage = BigDecimal.ZERO;
        if (context.getCustomerPayload() != null && context.getCustomerPayload().getAccountBalance() != null) {
            BigDecimal accountBalance = context.getCustomerPayload().getAccountBalance();
            if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                discountPercentage = discountAmount.divide(accountBalance, 4, BigDecimal.ROUND_HALF_UP)
                                                 .multiply(BigDecimal.valueOf(100));
            }
        }
        
        // 建立額外資訊
        Map<String, Object> additionalInfo = Map.of(
            "calculationMethod", "DatabaseQuery",
            "connectionString", getStringParameter("connectionString", ""),
            "queryTemplate", getStringParameter("queryTemplate", ""),
            "nodeId", configuration.getNodeId(),
            "queryResult", queryResult
        );
        
        return new PromotionResult(
            UUID.randomUUID().toString(),
            promotionName,
            promotionType,
            discountAmount,
            discountPercentage,
            description,
            LocalDateTime.now().plusDays(validityDays),
            additionalInfo,
            true
        );
    }
    
    /**
     * 轉換為 BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public String getCommandType() {
        return isConditionCommand ? "DATABASE_QUERY_CONDITION" : "DATABASE_QUERY_CALCULATION";
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證命令類型
        if (!"DATABASE_QUERY".equals(configuration.getCommandType())) {
            return false;
        }
        
        // 驗證必要參數
        String connectionString = getStringParameter("connectionString", "");
        if (connectionString.isEmpty()) {
            return false;
        }
        
        String queryTemplate = getStringParameter("queryTemplate", "");
        if (queryTemplate.isEmpty()) {
            return false;
        }
        
        return true;
    }
}