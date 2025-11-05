package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for Promotion History
 * Maps to promotion_history table
 */
@Entity
@Table(name = "promotion_history", 
       indexes = {
           @Index(name = "idx_customer_date", columnList = "customer_id, executed_at")
       })
public class PromotionHistoryEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "customer_id", length = 50, nullable = false)
    private String customerId;

    @Column(name = "promotion_id", length = 36, nullable = false)
    private String promotionId;

    @Column(name = "promotion_result", columnDefinition = "TEXT", nullable = false)
    private String promotionResult;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    // Default constructor
    public PromotionHistoryEntity() {}

    // Constructor with required fields
    public PromotionHistoryEntity(String id, String customerId, String promotionId, String promotionResult) {
        this.id = id;
        this.customerId = customerId;
        this.promotionId = promotionId;
        this.promotionResult = promotionResult;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionResult() {
        return promotionResult;
    }

    public void setPromotionResult(String promotionResult) {
        this.promotionResult = promotionResult;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionHistoryEntity)) return false;
        PromotionHistoryEntity that = (PromotionHistoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PromotionHistoryEntity{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", promotionId='" + promotionId + '\'' +
                ", executedAt=" + executedAt +
                '}';
    }
}