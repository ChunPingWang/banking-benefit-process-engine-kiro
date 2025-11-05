package com.bank.promotion.domain.command.external;

/**
 * 外部系統異常
 * 當外部系統呼叫失敗時拋出的異常
 */
public class ExternalSystemException extends Exception {
    
    private final String systemType;
    private final String endpoint;
    private final int statusCode;
    
    public ExternalSystemException(String message) {
        super(message);
        this.systemType = null;
        this.endpoint = null;
        this.statusCode = -1;
    }
    
    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
        this.systemType = null;
        this.endpoint = null;
        this.statusCode = -1;
    }
    
    public ExternalSystemException(String message, String systemType, String endpoint) {
        super(message);
        this.systemType = systemType;
        this.endpoint = endpoint;
        this.statusCode = -1;
    }
    
    public ExternalSystemException(String message, String systemType, String endpoint, int statusCode) {
        super(message);
        this.systemType = systemType;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
    }
    
    public ExternalSystemException(String message, Throwable cause, String systemType, String endpoint, int statusCode) {
        super(message, cause);
        this.systemType = systemType;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
    }
    
    public String getSystemType() {
        return systemType;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExternalSystemException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (systemType != null) {
            sb.append(", systemType='").append(systemType).append('\'');
        }
        if (endpoint != null) {
            sb.append(", endpoint='").append(endpoint).append('\'');
        }
        if (statusCode != -1) {
            sb.append(", statusCode=").append(statusCode);
        }
        sb.append('}');
        return sb.toString();
    }
}