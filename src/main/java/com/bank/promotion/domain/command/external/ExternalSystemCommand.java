package com.bank.promotion.domain.command.external;

import com.bank.promotion.domain.command.AbstractNodeCommand;
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
 * 外部系統命令
 * 負責呼叫外部系統並處理回應
 */
public class ExternalSystemCommand extends AbstractNodeCommand {
    
    private final ExternalSystemAdapter adapter;
    private final boolean isConditionCommand;
    private final int timeoutSeconds;
    private final boolean enableFallback;
    
    public ExternalSystemCommand(NodeConfiguration configuration) {
        super(configuration);
        
        this.isConditionCommand = "CONDITION".equals(configuration.getNodeType());
        this.timeoutSeconds = getIntParameter("timeoutSeconds", 30);
        this.enableFallback = getBooleanParameter("enableFallback", true);
        
        // 建立外部系統適配器
        this.adapter = createAdapter(configuration);
    }
    
    /**
     * 測試專用構造函數，允許注入 mock adapter
     */
    public ExternalSystemCommand(NodeConfiguration configuration, ExternalSystemAdapter adapter) {
        super(configuration);
        
        this.isConditionCommand = "CONDITION".equals(configuration.getNodeType());
        this.timeoutSeconds = getIntParameter("timeoutSeconds", 30);
        this.enableFallback = getBooleanParameter("enableFallback", true);
        this.adapter = adapter;
    }
    
    /**
     * 建立外部系統適配器
     */
    private ExternalSystemAdapter createAdapter(NodeConfiguration configuration) {
        String systemType = getStringParameter("systemType", "HTTP");
        String endpoint = getStringParameter("endpoint", "");
        
        if (endpoint.isEmpty()) {
            throw new IllegalArgumentException("External system endpoint cannot be empty");
        }
        
        switch (systemType.toUpperCase()) {
            case "HTTP":
            case "REST":
                return new HttpExternalSystemAdapter(endpoint, configuration.getParameters());
            case "SOAP":
                return new SoapExternalSystemAdapter(endpoint, configuration.getParameters());
            case "DATABASE":
                return new DatabaseExternalSystemAdapter(endpoint, configuration.getParameters());
            default:
                throw new IllegalArgumentException("Unsupported external system type: " + systemType);
        }
    }
    
    @Override
    protected NodeResult doExecute(ExecutionContext context) {
        try {
            // 準備請求資料
            ExternalSystemRequest request = buildRequest(context);
            
            // 呼叫外部系統
            ExternalSystemResponse response = adapter.call(request, timeoutSeconds, TimeUnit.SECONDS);
            
            // 處理回應
            if (response.isSuccess()) {
                return handleSuccessResponse(response, context);
            } else {
                return handleErrorResponse(response, context);
            }
            
        } catch (Exception e) {
            return handleException(e, context);
        }
    }
    
    /**
     * 建立外部系統請求
     */
    private ExternalSystemRequest buildRequest(ExecutionContext context) {
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
        
        // 設定配置參數
        for (Map.Entry<String, Object> entry : configuration.getParameters().entrySet()) {
            if (!entry.getKey().startsWith("system") && !entry.getKey().equals("endpoint")) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        
        return builder.build();
    }
    
    /**
     * 處理成功回應
     */
    private NodeResult handleSuccessResponse(ExternalSystemResponse response, ExecutionContext context) {
        if (isConditionCommand) {
            return handleConditionResponse(response);
        } else {
            return handleCalculationResponse(response, context);
        }
    }
    
    /**
     * 處理條件命令的回應
     */
    private NodeResult handleConditionResponse(ExternalSystemResponse response) {
        Object result = response.getData().get("conditionResult");
        
        if (result instanceof Boolean) {
            return NodeResult.success((Boolean) result);
        }
        
        if (result instanceof String) {
            String strResult = ((String) result).toLowerCase().trim();
            boolean boolResult = "true".equals(strResult) || "yes".equals(strResult) || "1".equals(strResult);
            return NodeResult.success(boolResult);
        }
        
        if (result instanceof Number) {
            boolean boolResult = ((Number) result).doubleValue() != 0.0;
            return NodeResult.success(boolResult);
        }
        
        // 預設情況：有回應資料表示條件成立
        return NodeResult.success(!response.getData().isEmpty());
    }
    
    /**
     * 處理計算命令的回應
     */
    private NodeResult handleCalculationResponse(ExternalSystemResponse response, ExecutionContext context) {
        // 從回應中提取優惠資訊
        Object amountObj = response.getData().get("discountAmount");
        BigDecimal discountAmount = convertToBigDecimal(amountObj);
        
        String promotionName = (String) response.getData().getOrDefault("promotionName", "外部系統優惠");
        String promotionType = (String) response.getData().getOrDefault("promotionType", "EXTERNAL_SYSTEM");
        String description = (String) response.getData().getOrDefault("description", "透過外部系統計算的優惠");
        
        // 建立優惠結果
        PromotionResult promotionResult = createPromotionResult(
            discountAmount, promotionName, promotionType, description, response.getData(), context
        );
        
        return NodeResult.success(promotionResult);
    }
    
    /**
     * 處理錯誤回應
     */
    private NodeResult handleErrorResponse(ExternalSystemResponse response, ExecutionContext context) {
        if (enableFallback) {
            return handleFallback(context, "External system error: " + response.getErrorMessage());
        } else {
            return NodeResult.failure("External system call failed: " + response.getErrorMessage());
        }
    }
    
    /**
     * 處理異常
     */
    private NodeResult handleException(Exception e, ExecutionContext context) {
        if (enableFallback) {
            return handleFallback(context, "External system exception: " + e.getMessage());
        } else {
            return NodeResult.failure("External system call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 處理降級策略
     */
    private NodeResult handleFallback(ExecutionContext context, String reason) {
        if (isConditionCommand) {
            // 條件命令的降級：返回預設值
            boolean fallbackValue = getBooleanParameter("fallbackConditionValue", false);
            return NodeResult.success(fallbackValue);
        } else {
            // 計算命令的降級：返回預設優惠
            BigDecimal fallbackAmount = new BigDecimal(getStringParameter("fallbackDiscountAmount", "0"));
            String fallbackName = getStringParameter("fallbackPromotionName", "降級優惠");
            
            PromotionResult fallbackResult = createPromotionResult(
                fallbackAmount, fallbackName, "FALLBACK", "系統降級優惠：" + reason, 
                Map.of("fallbackReason", reason), context
            );
            
            return NodeResult.success(fallbackResult);
        }
    }
    
    /**
     * 建立優惠結果
     */
    private PromotionResult createPromotionResult(BigDecimal discountAmount, String promotionName, 
                                                String promotionType, String description, 
                                                Map<String, Object> responseData, ExecutionContext context) {
        
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
            "calculationMethod", "ExternalSystem",
            "systemType", getStringParameter("systemType", "HTTP"),
            "endpoint", getStringParameter("endpoint", ""),
            "nodeId", configuration.getNodeId(),
            "responseData", responseData
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
        return isConditionCommand ? "EXTERNAL_SYSTEM_CONDITION" : "EXTERNAL_SYSTEM_CALCULATION";
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證命令類型
        if (!"EXTERNAL_SYSTEM".equals(configuration.getCommandType())) {
            return false;
        }
        
        // 驗證必要參數
        String endpoint = getStringParameter("endpoint", "");
        if (endpoint.isEmpty()) {
            return false;
        }
        
        String systemType = getStringParameter("systemType", "HTTP");
        if (!isValidSystemType(systemType)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 驗證系統類型是否有效
     */
    private boolean isValidSystemType(String systemType) {
        if (systemType == null) {
            return false;
        }
        
        String normalizedType = systemType.toUpperCase();
        return "HTTP".equals(normalizedType) || 
               "REST".equals(normalizedType) || 
               "SOAP".equals(normalizedType) || 
               "DATABASE".equals(normalizedType);
    }
}