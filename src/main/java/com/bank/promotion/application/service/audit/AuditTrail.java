package com.bank.promotion.application.service.audit;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 稽核軌跡實體
 * 記錄系統處理過程中每個步驟的詳細資訊
 */
public class AuditTrail {
    
    private final String id;
    private final String requestId;
    private final String customerId;
    private final String operationType;
    private final Map<String, Object> operationDetails;
    private final Integer executionTimeMs;
    private final String status;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    
    public AuditTrail(String requestId, String customerId, String operationType,
                     Map<String, Object> operationDetails, Integer executionTimeMs,
                     String status, String errorMessage) {
        this.id = UUID.randomUUID().toString();
        this.requestId = validateRequestId(requestId);
        this.customerId = validateCustomerId(customerId);
        this.operationType = validateOperationType(operationType);
        this.operationDetails = operationDetails != null ? Map.copyOf(operationDetails) : Map.of();
        this.executionTimeMs = executionTimeMs;
        this.status = validateStatus(status);
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }
    
    private String validateRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        return requestId.trim();
    }
    
    private String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        return customerId.trim();
    }
    
    private String validateOperationType(String operationType) {
        if (operationType == null || operationType.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation type cannot be null or empty");
        }
        return operationType.trim();
    }
    
    private String validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        return status.trim();
    }
    
    public String getId() {
        return id;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public Map<String, Object> getOperationDetails() {
        return operationDetails;
    }
    
    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditTrail that = (AuditTrail) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AuditTrail{" +
               "id='" + id + '\'' +
               ", requestId='" + requestId + '\'' +
               ", customerId='" + customerId + '\'' +
               ", operationType='" + operationType + '\'' +
               ", executionTimeMs=" + executionTimeMs +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}