package com.bank.promotion.adapter.web.exception;

import com.bank.promotion.adapter.web.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全域異常處理器
 * 統一處理API異常並返回標準化錯誤回應
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 處理請求參數驗證異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", "請求資料驗證失敗", errors));
    }
    
    /**
     * 處理約束驗證異常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        String errorMessage = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("CONSTRAINT_VIOLATION", errorMessage));
    }
    
    /**
     * 處理非法參數異常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
    }
    
    /**
     * 處理運行時異常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("RUNTIME_ERROR", "系統處理時發生錯誤: " + ex.getMessage()));
    }
    
    /**
     * 處理一般異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("SYSTEM_ERROR", "系統發生未預期的錯誤"));
    }
}