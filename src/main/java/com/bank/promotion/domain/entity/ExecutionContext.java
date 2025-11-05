package com.bank.promotion.domain.entity;

import com.bank.promotion.domain.valueobject.CustomerPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * 決策樹執行上下文
 * 包含執行過程中的客戶資料和上下文資訊
 */
public class ExecutionContext {
    
    private final CustomerPayload customerPayload;
    private final Map<String, Object> contextData;
    
    public ExecutionContext(CustomerPayload customerPayload, Map<String, Object> contextData) {
        this.customerPayload = customerPayload;
        this.contextData = contextData != null ? new HashMap<>(contextData) : new HashMap<>();
    }
    
    public CustomerPayload getCustomerPayload() {
        return customerPayload;
    }
    
    public Map<String, Object> getContextData() {
        return new HashMap<>(contextData);
    }
    
    public Object getContextValue(String key) {
        return contextData.get(key);
    }
    
    public void setContextValue(String key, Object value) {
        contextData.put(key, value);
    }
    
    public boolean hasContextValue(String key) {
        return contextData.containsKey(key);
    }
}