package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for System Event
 * Maps to system_events table
 */
@Entity
@Table(name = "system_events",
       indexes = {
           @Index(name = "idx_event_type_date", columnList = "event_type, created_at"),
           @Index(name = "idx_event_correlation_id", columnList = "correlation_id"),
           @Index(name = "idx_event_severity_date", columnList = "severity_level, created_at")
       })
public class SystemEventEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Column(name = "event_category", length = 30, nullable = false)
    private String eventCategory;

    @Column(name = "event_details", columnDefinition = "TEXT", nullable = false)
    private String eventDetails;

    @Column(name = "severity_level", length = 20, nullable = false)
    private String severityLevel;

    @Column(name = "source_component", length = 100, nullable = false)
    private String sourceComponent;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public SystemEventEntity() {}

    // Constructor with required fields
    public SystemEventEntity(String id, String eventType, String eventCategory, String eventDetails, 
                           String severityLevel, String sourceComponent) {
        this.id = id;
        this.eventType = eventType;
        this.eventCategory = eventCategory;
        this.eventDetails = eventDetails;
        this.severityLevel = severityLevel;
        this.sourceComponent = sourceComponent;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemEventEntity)) return false;
        SystemEventEntity that = (SystemEventEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SystemEventEntity{" +
                "id='" + id + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventCategory='" + eventCategory + '\'' +
                ", severityLevel='" + severityLevel + '\'' +
                ", sourceComponent='" + sourceComponent + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}