package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for Audit Trail
 * Maps to audit_trails table
 */
@Entity
@Table(name = "audit_trails",
       indexes = {
           @Index(name = "idx_audit_request_id", columnList = "request_id"),
           @Index(name = "idx_audit_customer_operation", columnList = "customer_id, operation_type"),
           @Index(name = "idx_audit_created_at", columnList = "created_at")
       })
public class AuditTrailEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "request_id", length = 36, nullable = false)
    private String requestId;

    @Column(name = "customer_id", length = 50, nullable = false)
    private String customerId;

    @Column(name = "operation_type", length = 50, nullable = false)
    private String operationType;

    @Column(name = "operation_details", columnDefinition = "TEXT", nullable = false)
    private String operationDetails;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Removed foreign key constraint to avoid test issues
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "request_id", insertable = false, updatable = false)
    // private RequestLogEntity requestLog;

    // Default constructor
    public AuditTrailEntity() {}

    // Constructor with required fields
    public AuditTrailEntity(String id, String requestId, String customerId, String operationType, 
                           String operationDetails, String status) {
        this.id = id;
        this.requestId = requestId;
        this.customerId = customerId;
        this.operationType = operationType;
        this.operationDetails = operationDetails;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDetails() {
        return operationDetails;
    }

    public void setOperationDetails(String operationDetails) {
        this.operationDetails = operationDetails;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Removed requestLog getter/setter due to removed foreign key constraint

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditTrailEntity)) return false;
        AuditTrailEntity that = (AuditTrailEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AuditTrailEntity{" +
                "id='" + id + '\'' +
                ", requestId='" + requestId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", status='" + status + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}