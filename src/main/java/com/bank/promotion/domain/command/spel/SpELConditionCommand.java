package com.bank.promotion.domain.command.spel;

import com.bank.promotion.domain.command.AbstractNodeCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * SpEL 條件命令
 * 使用 Spring Expression Language 評估條件表達式
 */
public class SpELConditionCommand extends AbstractNodeCommand {
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final SpELExpressionCache EXPRESSION_CACHE = new SpELExpressionCache();
    private final Expression expression;
    
    public SpELConditionCommand(NodeConfiguration configuration) {
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
            // 檢查必要的上下文資料
            if (context.getCustomerPayload() == null) {
                return NodeResult.failure("Customer payload is required for SpEL condition evaluation", 
                    new IllegalArgumentException("Customer payload cannot be null"));
            }
            
            // 建立 SpEL 評估上下文
            StandardEvaluationContext evaluationContext = createEvaluationContext(context);
            
            // 評估表達式
            Object result = expression.getValue(evaluationContext);
            
            // 轉換結果為布林值
            boolean conditionResult = convertToBoolean(result);
            
            return NodeResult.success(conditionResult);
            
        } catch (Exception e) {
            return NodeResult.failure("SpEL condition evaluation failed: " + e.getMessage(), e);
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
        } else {
            // 如果客戶資料為 null，設定預設值以避免 SpEL 表達式錯誤
            evaluationContext.setVariable("customer", null);
            evaluationContext.setVariable("customerId", null);
            evaluationContext.setVariable("accountType", null);
            evaluationContext.setVariable("annualIncome", null);
            evaluationContext.setVariable("creditScore", null);
            evaluationContext.setVariable("accountBalance", null);
            evaluationContext.setVariable("transactionHistory", null);
        }
        
        // 設定上下文資料變數
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            evaluationContext.setVariable(entry.getKey(), entry.getValue());
        }
        
        // 設定配置參數變數
        for (Map.Entry<String, Object> entry : configuration.getParameters().entrySet()) {
            evaluationContext.setVariable("param_" + entry.getKey(), entry.getValue());
        }
        
        return evaluationContext;
    }
    
    /**
     * 將表達式結果轉換為布林值
     */
    private boolean convertToBoolean(Object result) {
        if (result == null) {
            return false;
        }
        
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        
        if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0.0;
        }
        
        if (result instanceof String) {
            String str = ((String) result).trim().toLowerCase();
            return "true".equals(str) || "yes".equals(str) || "1".equals(str);
        }
        
        // 其他類型的物件，非 null 即為 true
        return true;
    }
    
    @Override
    public String getCommandType() {
        return "SPEL_CONDITION";
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證節點類型
        if (!"CONDITION".equals(configuration.getNodeType())) {
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
}