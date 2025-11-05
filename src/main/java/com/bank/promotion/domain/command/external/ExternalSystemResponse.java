package com.bank.promotion.domain.command.external;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 外部系統回應
 * 封裝從外部系統接收的回應資料
 */
public class ExternalSystemResponse {
    
    private final boolean success;
    private final Map<String, Object> data;
    private final String errorMessage;
    private final int statusCode;
    private final LocalDateTime responseTime;
    private final long executionTimeMs;
    
    private ExternalSystemResponse(Builder builder) {
        this.success = builder.success;
        this.data = new HashMap<>(builder.data);
        this.errorMessage = builder.errorMessage;
        this.statusCode = builder.statusCode;
        this.responseTime = builder.responseTime != null ? builder.responseTime : LocalDateTime.now();
        this.executionTimeMs = builder.executionTimeMs;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public LocalDateTime getResponseTime() {
        return responseTime;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public static Builder success() {
        return new Builder().success(true);
    }
    
    public static Builder failure(String errorMessage) {
        return new Builder().success(false).errorMessage(errorMessage);
    }
    
    public static class Builder {
        private boolean success = false;
        private final Map<String, Object> data = new HashMap<>();
        private String errorMessage;
        private int statusCode = 200;
        private LocalDateTime responseTime;
        private long executionTimeMs = 0;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder addData(String key, Object value) {
            if (key != null && value != null) {
                data.put(key, value);
            }
            return this;
        }
        
        public Builder addData(Map<String, Object> dataMap) {
            if (dataMap != null) {
                data.putAll(dataMap);
            }
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        
        public Builder responseTime(LocalDateTime responseTime) {
            this.responseTime = responseTime;
            return this;
        }
        
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public ExternalSystemResponse build() {
            return new ExternalSystemResponse(this);
        }
    }
    
    @Override
    public String toString() {
        return "ExternalSystemResponse{" +
               "success=" + success +
               ", data=" + data +
               ", errorMessage='" + errorMessage + '\'' +
               ", statusCode=" + statusCode +
               ", responseTime=" + responseTime +
               ", executionTimeMs=" + executionTimeMs +
               '}';
    }
}