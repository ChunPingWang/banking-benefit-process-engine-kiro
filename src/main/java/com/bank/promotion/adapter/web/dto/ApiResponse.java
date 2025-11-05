package com.bank.promotion.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 標準化API回應格式
 */
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 建立成功回應
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }
    
    /**
     * 建立成功回應（無資料）
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, null);
    }
    
    /**
     * 建立錯誤回應
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
    
    /**
     * 建立錯誤回應（含資料）
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return new ApiResponse<>(false, data, message, errorCode);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponse<?> that = (ApiResponse<?>) o;
        return success == that.success &&
               Objects.equals(data, that.data) &&
               Objects.equals(message, that.message) &&
               Objects.equals(errorCode, that.errorCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(success, data, message, errorCode);
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
               "success=" + success +
               ", data=" + data +
               ", message='" + message + '\'' +
               ", errorCode='" + errorCode + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}