package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Request Log
 * Maps to request_logs table
 */
@Entity
@Table(name = "request_logs",
       indexes = {
           @Index(name = "idx_request_id", columnList = "request_id"),
           @Index(name = "idx_endpoint_date", columnList = "api_endpoint, created_at")
       })
public class RequestLogEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "request_id", length = 36, nullable = false, unique = true)
    private String requestId;

    @Column(name = "api_endpoint", length = 200, nullable = false)
    private String apiEndpoint;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "request_payload", columnDefinition = "TEXT", nullable = false)
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "requestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditTrailEntity> auditTrails = new ArrayList<>();

    @OneToMany(mappedBy = "requestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DecisionStepEntity> decisionSteps = new ArrayList<>();

    // Default constructor
    public RequestLogEntity() {}

    // Constructor with required fields
    public RequestLogEntity(String id, String requestId, String apiEndpoint, String httpMethod, String requestPayload) {
        this.id = id;
        this.requestId = requestId;
        this.apiEndpoint = apiEndpoint;
        this.httpMethod = httpMethod;
        this.requestPayload = requestPayload;
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

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<AuditTrailEntity> getAuditTrails() {
        return auditTrails;
    }

    public void setAuditTrails(List<AuditTrailEntity> auditTrails) {
        this.auditTrails = auditTrails;
    }

    public List<DecisionStepEntity> getDecisionSteps() {
        return decisionSteps;
    }

    public void setDecisionSteps(List<DecisionStepEntity> decisionSteps) {
        this.decisionSteps = decisionSteps;
    }

    // Helper methods
    public void addAuditTrail(AuditTrailEntity auditTrail) {
        auditTrails.add(auditTrail);
        auditTrail.setRequestId(this.requestId);
    }

    public void addDecisionStep(DecisionStepEntity decisionStep) {
        decisionSteps.add(decisionStep);
        decisionStep.setRequestId(this.requestId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestLogEntity)) return false;
        RequestLogEntity that = (RequestLogEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RequestLogEntity{" +
                "id='" + id + '\'' +
                ", requestId='" + requestId + '\'' +
                ", apiEndpoint='" + apiEndpoint + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", responseStatus=" + responseStatus +
                ", processingTimeMs=" + processingTimeMs +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }
}