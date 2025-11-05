package com.bank.promotion.application.command;

import java.util.Map;
import java.util.Objects;

/**
 * 更新優惠規則命令
 */
public final class UpdatePromotionRuleCommand {
    
    private final String ruleId;
    private final String name;
    private final String ruleType;
    private final String ruleContent;
    private final Map<String, Object> parameters;
    private final String status;
    
    public UpdatePromotionRuleCommand(String ruleId, String name, String ruleType, 
                                    String ruleContent, Map<String, Object> parameters, String status) {
        this.ruleId = validateRuleId(ruleId);
        this.name = validateName(name);
        this.ruleType = validateRuleType(ruleType);
        this.ruleContent = validateRuleContent(ruleContent);
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.status = validateStatus(status);
    }
    
    private String validateRuleId(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule ID cannot be null or empty");
        }
        return ruleId.trim();
    }
    
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty");
        }
        return name.trim();
    }
    
    private String validateRuleType(String ruleType) {
        if (ruleType == null || ruleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule type cannot be null or empty");
        }
        return ruleType.trim();
    }
    
    private String validateRuleContent(String ruleContent) {
        if (ruleContent == null || ruleContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule content cannot be null or empty");
        }
        return ruleContent.trim();
    }
    
    private String validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        return status.trim();
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getRuleType() {
        return ruleType;
    }
    
    public String getRuleContent() {
        return ruleContent;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getStatus() {
        return status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatePromotionRuleCommand that = (UpdatePromotionRuleCommand) o;
        return Objects.equals(ruleId, that.ruleId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(ruleType, that.ruleType) &&
               Objects.equals(ruleContent, that.ruleContent) &&
               Objects.equals(parameters, that.parameters) &&
               Objects.equals(status, that.status);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ruleId, name, ruleType, ruleContent, parameters, status);
    }
    
    @Override
    public String toString() {
        return "UpdatePromotionRuleCommand{" +
               "ruleId='" + ruleId + '\'' +
               ", name='" + name + '\'' +
               ", ruleType='" + ruleType + '\'' +
               ", ruleContent='" + ruleContent + '\'' +
               ", parameters=" + parameters +
               ", status='" + status + '\'' +
               '}';
    }
}