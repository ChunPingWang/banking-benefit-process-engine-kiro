package com.bank.promotion.domain.exception;

/**
 * 外部系統異常
 * 當呼叫外部系統時發生錯誤時拋出
 */
public class ExternalSystemException extends PromotionSystemException {
    
    private final String systemName;
    private final String operation;
    
    public ExternalSystemException(String message, String systemName) {
        super(message, "EXTERNAL_SYSTEM_ERROR");
        this.systemName = systemName;
        this.operation = null;
    }
    
    public ExternalSystemException(String message, String systemName, String operation) {
        super(message, "EXTERNAL_SYSTEM_ERROR");
        this.systemName = systemName;
        this.operation = operation;
    }
    
    public ExternalSystemException(String message, String systemName, String operation, Throwable cause) {
        super(message, "EXTERNAL_SYSTEM_ERROR", cause);
        this.systemName = systemName;
        this.operation = operation;
    }
    
    public String getSystemName() {
        return systemName;
    }
    
    public String getOperation() {
        return operation;
    }
}