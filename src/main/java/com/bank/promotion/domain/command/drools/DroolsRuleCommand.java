package com.bank.promotion.domain.command.drools;

import com.bank.promotion.domain.command.AbstractNodeCommand;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Drools 規則命令
 * 使用 Drools 規則引擎執行業務規則
 */
public class DroolsRuleCommand extends AbstractNodeCommand {
    
    private static final DroolsRuleManager RULE_MANAGER = new DroolsRuleManager();
    private final String ruleName;
    private final boolean isConditionRule;
    
    public DroolsRuleCommand(NodeConfiguration configuration) {
        super(configuration);
        
        if (configuration.getExpression() == null || configuration.getExpression().trim().isEmpty()) {
            throw new IllegalArgumentException("Drools rule content cannot be null or empty");
        }
        
        this.ruleName = getStringParameter("ruleName", "DefaultRule_" + UUID.randomUUID().toString());
        this.isConditionRule = "CONDITION".equals(configuration.getNodeType());
        
        // 註冊規則到管理器
        if (!RULE_MANAGER.registerRule(this.ruleName, configuration.getExpression())) {
            throw new IllegalArgumentException("Failed to register Drools rule: " + this.ruleName);
        }
    }
    

    
    @Override
    protected NodeResult doExecute(ExecutionContext context) {
        KieSession kieSession = null;
        try {
            // 從管理器獲取 KieContainer
            KieContainer kieContainer = RULE_MANAGER.getRuleContainer(ruleName);
            if (kieContainer == null) {
                return NodeResult.failure("Drools rule not found: " + ruleName);
            }
            
            // 建立 KieSession
            kieSession = kieContainer.newKieSession();
            
            // 準備規則執行的資料
            Map<String, Object> results = new HashMap<>();
            kieSession.setGlobal("results", results);
            
            // 插入客戶資料作為事實
            if (context.getCustomerPayload() != null) {
                kieSession.insert(context.getCustomerPayload());
            }
            
            // 插入上下文資料作為事實
            for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
                kieSession.insert(entry.getValue());
            }
            
            // 執行規則
            int firedRules = kieSession.fireAllRules();
            
            if (firedRules == 0) {
                return NodeResult.failure("No Drools rules were fired");
            }
            
            // 處理執行結果
            if (isConditionRule) {
                return handleConditionResult(results);
            } else {
                return handleCalculationResult(results, context);
            }
            
        } catch (Exception e) {
            return NodeResult.failure("Drools rule execution failed: " + e.getMessage(), e);
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
    
    /**
     * 處理條件規則的執行結果
     */
    private NodeResult handleConditionResult(Map<String, Object> results) {
        Object conditionResult = results.get("conditionResult");
        
        if (conditionResult instanceof Boolean) {
            return NodeResult.success((Boolean) conditionResult);
        }
        
        // 如果沒有明確的布林結果，檢查是否有其他結果
        if (!results.isEmpty()) {
            // 有結果表示條件成立
            return NodeResult.success(true);
        }
        
        // 沒有結果表示條件不成立
        return NodeResult.success(false);
    }
    
    /**
     * 處理計算規則的執行結果
     */
    private NodeResult handleCalculationResult(Map<String, Object> results, ExecutionContext context) {
        Object calculationResult = results.get("calculationResult");
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (calculationResult instanceof BigDecimal) {
            discountAmount = (BigDecimal) calculationResult;
        } else if (calculationResult instanceof Number) {
            discountAmount = BigDecimal.valueOf(((Number) calculationResult).doubleValue());
        }
        
        // 建立優惠結果
        PromotionResult promotionResult = createPromotionResult(discountAmount, results, context);
        return NodeResult.success(promotionResult);
    }
    
    /**
     * 建立優惠結果
     */
    private PromotionResult createPromotionResult(BigDecimal discountAmount, Map<String, Object> ruleResults, ExecutionContext context) {
        // 從配置參數中獲取優惠資訊
        String promotionName = getStringParameter("promotionName", "Drools規則優惠");
        String promotionType = getStringParameter("promotionType", "DROOLS_CALCULATED");
        String description = getStringParameter("description", "透過Drools規則計算的優惠");
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
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("calculationMethod", "Drools");
        additionalInfo.put("ruleName", ruleName);
        additionalInfo.put("nodeId", configuration.getNodeId());
        additionalInfo.putAll(ruleResults);
        
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
    
    @Override
    public String getCommandType() {
        return isConditionRule ? "DROOLS_CONDITION" : "DROOLS_CALCULATION";
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證命令類型
        if (!"DROOLS".equals(configuration.getCommandType())) {
            return false;
        }
        
        // 驗證規則內容
        if (configuration.getExpression() == null || configuration.getExpression().trim().isEmpty()) {
            return false;
        }
        
        // 使用規則管理器驗證規則語法
        DroolsRuleManager.RuleValidationResult validationResult = RULE_MANAGER.validateRule(configuration.getExpression());
        return validationResult.isValid();
    }
}