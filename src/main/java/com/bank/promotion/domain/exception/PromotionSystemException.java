package com.bank.promotion.domain.exception;

/**
 * 優惠系統基礎異常類別
 * 所有領域異常的根異常
 */
public class PromotionSystemException extends RuntimeException {
    
    private final String errorCode;
    
    public PromotionSystemException(String message) {
        super(message);
        this.errorCode = "PROMOTION_SYSTEM_ERROR";
    }
    
    public PromotionSystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PromotionSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PROMOTION_SYSTEM_ERROR";
    }
    
    public PromotionSystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}