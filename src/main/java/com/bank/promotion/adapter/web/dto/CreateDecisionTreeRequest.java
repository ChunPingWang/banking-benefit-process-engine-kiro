package com.bank.promotion.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * 創建決策樹請求DTO
 */
public class CreateDecisionTreeRequest {
    
    @NotBlank(message = "決策樹名稱不能為空")
    @Size(max = 100, message = "決策樹名稱長度不能超過100個字符")
    @JsonProperty("name")
    private String name;
    
    @Size(max = 500, message = "描述長度不能超過500個字符")
    @JsonProperty("description")
    private String description;
    
    public CreateDecisionTreeRequest() {
    }
    
    public CreateDecisionTreeRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateDecisionTreeRequest that = (CreateDecisionTreeRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
    
    @Override
    public String toString() {
        return "CreateDecisionTreeRequest{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}