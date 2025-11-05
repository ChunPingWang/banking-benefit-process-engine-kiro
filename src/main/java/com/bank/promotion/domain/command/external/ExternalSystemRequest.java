package com.bank.promotion.domain.command.external;

import java.util.HashMap;
import java.util.Map;

/**
 * 外部系統請求
 * 封裝發送到外部系統的請求資料
 */
public class ExternalSystemRequest {
    
    private final Map<String, Object> parameters;
    private final Map<String, String> headers;
    private final String requestId;
    
    private ExternalSystemRequest(Builder builder) {
        this.parameters = new HashMap<>(builder.parameters);
        this.headers = new HashMap<>(builder.headers);
        this.requestId = builder.requestId;
    }
    
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
    
    public String getHeader(String key) {
        return headers.get(key);
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Map<String, Object> parameters = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();
        private String requestId = java.util.UUID.randomUUID().toString();
        
        public Builder addParameter(String key, Object value) {
            if (key != null && value != null) {
                parameters.put(key, value);
            }
            return this;
        }
        
        public Builder addParameters(Map<String, Object> params) {
            if (params != null) {
                parameters.putAll(params);
            }
            return this;
        }
        
        public Builder addHeader(String key, String value) {
            if (key != null && value != null) {
                headers.put(key, value);
            }
            return this;
        }
        
        public Builder addHeaders(Map<String, String> hdrs) {
            if (hdrs != null) {
                headers.putAll(hdrs);
            }
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public ExternalSystemRequest build() {
            return new ExternalSystemRequest(this);
        }
    }
    
    @Override
    public String toString() {
        return "ExternalSystemRequest{" +
               "requestId='" + requestId + '\'' +
               ", parameters=" + parameters +
               ", headers=" + headers +
               '}';
    }
}