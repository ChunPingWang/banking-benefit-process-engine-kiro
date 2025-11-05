package com.bank.promotion.domain.exception;

/**
 * 規則評估異常
 * 當規則評估過程中發生錯誤時拋出
 */
public class RuleEvaluationException extends PromotionSystemException {
    
    private final String ruleId;
    private final String ruleType;
    
    public RuleEvaluationException(String message, String ruleId) {
        super(message, "RULE_EVALUATION_ERROR");
        this.ruleId = ruleId;
        this.ruleType = null;
    }
    
    public RuleEvaluationException(String message, String ruleId, String ruleType) {
        super(message, "RULE_EVALUATION_ERROR");
        this.ruleId = ruleId;
        this.ruleType = ruleType;
    }
    
    public RuleEvaluationException(String message, String ruleId, String ruleType, Throwable cause) {
        super(message, "RULE_EVALUATION_ERROR", cause);
        this.ruleId = ruleId;
        this.ruleType = ruleType;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getRuleType() {
        return ruleType;
    }
}