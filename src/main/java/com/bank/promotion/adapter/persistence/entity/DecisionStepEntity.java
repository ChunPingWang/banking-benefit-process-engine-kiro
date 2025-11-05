package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for Decision Step
 * Maps to decision_steps table
 */
@Entity
@Table(name = "decision_steps",
       indexes = {
           @Index(name = "idx_decision_request_step", columnList = "request_id, step_order"),
           @Index(name = "idx_decision_tree_node", columnList = "tree_id, node_id")
       })
public class DecisionStepEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "request_id", length = 36, nullable = false)
    private String requestId;

    @Column(name = "tree_id", length = 36, nullable = false)
    private String treeId;

    @Column(name = "node_id", length = 36, nullable = false)
    private String nodeId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "node_type", length = 20, nullable = false)
    private String nodeType;

    @Column(name = "input_data", columnDefinition = "TEXT", nullable = false)
    private String inputData;

    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private RequestLogEntity requestLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", insertable = false, updatable = false)
    private DecisionTreeEntity decisionTree;

    // Default constructor
    public DecisionStepEntity() {}

    // Constructor with required fields
    public DecisionStepEntity(String id, String requestId, String treeId, String nodeId, 
                             Integer stepOrder, String nodeType, String inputData, String status) {
        this.id = id;
        this.requestId = requestId;
        this.treeId = treeId;
        this.nodeId = nodeId;
        this.stepOrder = stepOrder;
        this.nodeType = nodeType;
        this.inputData = inputData;
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

    public String getTreeId() {
        return treeId;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
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

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public RequestLogEntity getRequestLog() {
        return requestLog;
    }

    public void setRequestLog(RequestLogEntity requestLog) {
        this.requestLog = requestLog;
    }

    public DecisionTreeEntity getDecisionTree() {
        return decisionTree;
    }

    public void setDecisionTree(DecisionTreeEntity decisionTree) {
        this.decisionTree = decisionTree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionStepEntity)) return false;
        DecisionStepEntity that = (DecisionStepEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DecisionStepEntity{" +
                "id='" + id + '\'' +
                ", requestId='" + requestId + '\'' +
                ", treeId='" + treeId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", stepOrder=" + stepOrder +
                ", nodeType='" + nodeType + '\'' +
                ", status='" + status + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}