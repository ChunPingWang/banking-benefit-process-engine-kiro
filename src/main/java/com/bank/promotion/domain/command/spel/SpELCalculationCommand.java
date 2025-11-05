package com.bank.promotion.domain.command.spel;

import com.bank.promotion.domain.command.AbstractNodeCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * SpEL 計算命令
 * 使用 Spring Expression Language 計算優惠結果
 */
public class SpELCalculationCommand extends AbstractNodeCommand {
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final SpELExpressionCache EXPRESSION_CACHE = new SpELExpressionCache();
    private final Expression expression;
    
    public SpELCalculationCommand(NodeConfiguration configuration) {
        super(configuration);
        
        if (configuration.getExpression() == null || configuration.getExpression().trim().isEmpty()) {
            throw new IllegalArgumentException("SpEL expression cannot be null or empty");
        }
        
        try {
            this.expression = EXPRESSION_CACHE.getExpression(configuration.getExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SpEL expression: " + configuration.getExpression(), e);
        }
    }
    
    @Override
    protected NodeResult doExecute(ExecutionContext context) {
        try {
            // 建立 SpEL 評估上下文
            StandardEvaluationContext evaluationContext = createEvaluationContext(context);
            
            // 評估表達式
            Object result = expression.getValue(evaluationContext);
            
            // 建立優惠結果
            PromotionResult promotionResult = createPromotionResult(result, context);
            
            return NodeResult.success(promotionResult);
            
        } catch (Exception e) {
            return NodeResult.failure("SpEL calculation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 建立 SpEL 評估上下文
     * 將客戶資料和上下文資料設定為 SpEL 變數
     */
    private StandardEvaluationContext createEvaluationContext(ExecutionContext context) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        
        // 設定客戶資料變數
        if (context.getCustomerPayload() != null) {
            evaluationContext.setVariable("customer", context.getCustomerPayload());
            
            // 為了方便使用，也可以直接設定客戶資料的各個欄位
            evaluationContext.setVariable("customerId", context.getCustomerPayload().getCustomerId());
            evaluationContext.setVariable("accountType", context.getCustomerPayload().getAccountType());
            evaluationContext.setVariable("annualIncome", context.getCustomerPayload().getAnnualIncome());
            evaluationContext.setVariable("creditScore", context.getCustomerPayload().getCreditScore());
            evaluationContext.setVariable("accountBalance", context.getCustomerPayload().getAccountBalance());
            evaluationContext.setVariable("transactionHistory", context.getCustomerPayload().getTransactionHistory());
        }
        
        // 設定上下文資料變數
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            evaluationContext.setVariable(entry.getKey(), entry.getValue());
        }
        
        // 設定配置參數變數
        for (Map.Entry<String, Object> entry : configuration.getParameters().entrySet()) {
            evaluationContext.setVariable("param_" + entry.getKey(), entry.getValue());
        }
        
        // 設定常用的計算函數
        evaluationContext.setVariable("min", new MinFunction());
        evaluationContext.setVariable("max", new MaxFunction());
        evaluationContext.setVariable("round", new RoundFunction());
        
        return evaluationContext;
    }
    
    /**
     * 根據表達式結果建立優惠結果
     */
    private PromotionResult createPromotionResult(Object calculationResult, ExecutionContext context) {
        // 從配置參數中獲取優惠資訊
        String promotionName = getStringParameter("promotionName", "SpEL計算優惠");
        String promotionType = getStringParameter("promotionType", "SPEL_CALCULATED");
        String description = getStringParameter("description", "透過SpEL表達式計算的優惠");
        int validityDays = getIntParameter("validityDays", 30);
        
        // 轉換計算結果為優惠金額
        BigDecimal discountAmount = convertToBigDecimal(calculationResult);
        
        // 計算優惠百分比 (假設基於帳戶餘額)
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
            "calculationMethod", "SpEL",
            "expression", configuration.getExpression(),
            "calculatedValue", calculationResult,
            "nodeId", configuration.getNodeId()
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
     * 將計算結果轉換為 BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object result) {
        if (result == null) {
            return BigDecimal.ZERO;
        }
        
        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        }
        
        if (result instanceof Number) {
            return BigDecimal.valueOf(((Number) result).doubleValue());
        }
        
        if (result instanceof String) {
            try {
                return new BigDecimal((String) result);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public String getCommandType() {
        return "SPEL_CALCULATION";
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證節點類型
        if (!"CALCULATION".equals(configuration.getNodeType())) {
            return false;
        }
        
        // 驗證命令類型
        if (!"SPEL".equals(configuration.getCommandType())) {
            return false;
        }
        
        // 驗證表達式
        if (configuration.getExpression() == null || configuration.getExpression().trim().isEmpty()) {
            return false;
        }
        
        // 嘗試解析表達式以驗證語法
        try {
            EXPRESSION_CACHE.getExpression(configuration.getExpression());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 內部輔助類別：數學函數
    public static class MinFunction {
        public double min(double a, double b) {
            return Math.min(a, b);
        }
        
        public BigDecimal min(BigDecimal a, BigDecimal b) {
            return a.min(b);
        }
    }
    
    public static class MaxFunction {
        public double max(double a, double b) {
            return Math.max(a, b);
        }
        
        public BigDecimal max(BigDecimal a, BigDecimal b) {
            return a.max(b);
        }
    }
    
    public static class RoundFunction {
        public double round(double value, int scale) {
            return BigDecimal.valueOf(value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        
        public BigDecimal round(BigDecimal value, int scale) {
            return value.setScale(scale, BigDecimal.ROUND_HALF_UP);
        }
    }
}