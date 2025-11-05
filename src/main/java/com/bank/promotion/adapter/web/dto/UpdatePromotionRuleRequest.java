package com.bank.promotion.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;

/**
 * 更新優惠規則請求DTO
 */
public class UpdatePromotionRuleRequest {
    
    @NotBlank(message = "規則名稱不能為空")
    @Size(max = 100, message = "規則名稱長度不能超過100個字符")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "規則類型不能為空")
    @Pattern(regexp = "SPEL|DROOLS|HARDCODED", message = "規則類型必須為 SPEL、DROOLS 或 HARDCODED")
    @JsonProperty("ruleType")
    private String ruleType;
    
    @NotBlank(message = "規則內容不能為空")
    @JsonProperty("ruleContent")
    private String ruleContent;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    @NotBlank(message = "狀態不能為空")
    @Pattern(regexp = "ACTIVE|INACTIVE|DRAFT", message = "狀態必須為 ACTIVE、INACTIVE 或 DRAFT")
    @JsonProperty("status")
    private String status;
    
    public UpdatePromotionRuleRequest() {
    }
    
    public UpdatePromotionRuleRequest(String name, String ruleType, String ruleContent, 
                                    Map<String, Object> parameters, String status) {
        this.name = name;
        this.ruleType = ruleType;
        this.ruleContent = ruleContent;
        this.parameters = parameters;
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    
    public String getRuleContent() {
        return ruleContent;
    }
    
    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatePromotionRuleRequest that = (UpdatePromotionRuleRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(ruleType, that.ruleType) &&
               Objects.equals(ruleContent, that.ruleContent) &&
               Objects.equals(parameters, that.parameters) &&
               Objects.equals(status, that.status);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, ruleType, ruleContent, parameters, status);
    }
    
    @Override
    public String toString() {
        return "UpdatePromotionRuleRequest{" +
               "name='" + name + '\'' +
               ", ruleType='" + ruleType + '\'' +
               ", ruleContent='" + ruleContent + '\'' +
               ", parameters=" + parameters +
               ", status='" + status + '\'' +
               '}';
    }
}