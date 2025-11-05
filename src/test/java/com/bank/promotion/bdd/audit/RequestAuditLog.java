package com.bank.promotion.bdd.audit;

import java.time.LocalDateTime;

/**
 * 請求稽核日誌
 * 記錄API請求的完整生命週期
 */
public class RequestAuditLog {
    private final String requestId;
    private final String apiEndpoint;
    private final String httpMethod;
    private final Object requestPayload;
    private final LocalDateTime createdAt;
    
    private Object responsePayload;
    private Integer responseStatus;
    private Long processingTimeMs;
    private LocalDateTime completedAt;
    
    public RequestAuditLog(String requestId, String apiEndpoint, String httpMethod, 
                          Object requestPayload, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.apiEndpoint = apiEndpoint;
        this.httpMethod = httpMethod;
        this.requestPayload = requestPayload;
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public String getApiEndpoint() { return apiEndpoint; }
    public String getHttpMethod() { return httpMethod; }
    public Object getRequestPayload() { return requestPayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Object getResponsePayload() { return responsePayload; }
    public Integer getResponseStatus() { return responseStatus; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    
    // Setters
    public void setResponsePayload(Object responsePayload) { this.responsePayload = responsePayload; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}