package com.bank.promotion.domain.state;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 狀態轉換結果值物件
 * 包含轉換是否成功和相關訊息
 */
public final class StateTransitionResult {
    
    private final boolean success;
    private final String message;
    private final LocalDateTime timestamp;
    
    private StateTransitionResult(boolean success, String message) {
        this.success = success;
        this.message = message != null ? message : "";
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 創建成功的轉換結果
     * 
     * @param message 成功訊息
     * @return 成功的轉換結果
     */
    public static StateTransitionResult success(String message) {
        return new StateTransitionResult(true, message);
    }
    
    /**
     * 創建成功的轉換結果（無訊息）
     * 
     * @return 成功的轉換結果
     */
    public static StateTransitionResult success() {
        return new StateTransitionResult(true, "狀態轉換成功");
    }
    
    /**
     * 創建失敗的轉換結果
     * 
     * @param message 失敗訊息
     * @return 失敗的轉換結果
     */
    public static StateTransitionResult failure(String message) {
        return new StateTransitionResult(false, message);
    }
    
    /**
     * 創建失敗的轉換結果（預設訊息）
     * 
     * @return 失敗的轉換結果
     */
    public static StateTransitionResult failure() {
        return new StateTransitionResult(false, "狀態轉換失敗");
    }
    
    /**
     * 檢查轉換是否成功
     * 
     * @return 轉換是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 檢查轉換是否失敗
     * 
     * @return 轉換是否失敗
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 獲取轉換訊息
     * 
     * @return 轉換訊息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 獲取轉換時間戳
     * 
     * @return 轉換時間戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTransitionResult that = (StateTransitionResult) o;
        return success == that.success && 
               Objects.equals(message, that.message) && 
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(success, message, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("StateTransitionResult{success=%s, message='%s', timestamp=%s}", 
                           success, message, timestamp);
    }
}