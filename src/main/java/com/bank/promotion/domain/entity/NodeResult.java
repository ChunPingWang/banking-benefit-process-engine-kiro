package com.bank.promotion.domain.entity;

/**
 * 節點執行結果
 * 封裝節點執行的結果和狀態資訊
 */
public class NodeResult {
    
    private final boolean success;
    private final Object result;
    private final String errorMessage;
    
    private NodeResult(boolean success, Object result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 建立成功結果
     */
    public static NodeResult success(Object result) {
        return new NodeResult(true, result, null);
    }
    
    /**
     * 建立失敗結果
     */
    public static NodeResult failure(String errorMessage) {
        return new NodeResult(false, null, errorMessage);
    }
    
    /**
     * 建立失敗結果（帶異常）
     */
    public static NodeResult failure(String errorMessage, Throwable cause) {
        String fullMessage = errorMessage;
        if (cause != null) {
            fullMessage += ": " + cause.getMessage();
        }
        return new NodeResult(false, null, fullMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Object getResult() {
        return result;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return "NodeResult{" +
               "success=" + success +
               ", result=" + result +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}