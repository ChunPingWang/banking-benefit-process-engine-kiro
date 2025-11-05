package com.bank.promotion.bdd.audit;

import java.time.LocalDateTime;

/**
 * 系統事件日誌
 * 記錄系統內部重要操作和狀態變更
 */
public class SystemEventLog {
    private final String requestId;
    private final String eventType;
    private final String eventCategory;
    private final Object eventDetails;
    private final String severityLevel;
    private final String sourceComponent;
    private final LocalDateTime createdAt;
    
    public SystemEventLog(String requestId, String eventType, String eventCategory,
                         Object eventDetails, String severityLevel, String sourceComponent,
                         LocalDateTime createdAt) {
        this.requestId = requestId;
        this.eventType = eventType;
        this.eventCategory = eventCategory;
        this.eventDetails = eventDetails;
        this.severityLevel = severityLevel;
        this.sourceComponent = sourceComponent;
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public String getEventType() { return eventType; }
    public String getEventCategory() { return eventCategory; }
    public Object getEventDetails() { return eventDetails; }
    public String getSeverityLevel() { return severityLevel; }
    public String getSourceComponent() { return sourceComponent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}