package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for Decision Node
 * Maps to decision_nodes table
 */
@Entity
@Table(name = "decision_nodes")
public class DecisionNodeEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "tree_id", length = 36, nullable = false)
    private String treeId;

    @Column(name = "node_type", length = 20, nullable = false)
    private String nodeType;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "configuration", columnDefinition = "TEXT", nullable = false)
    private String configuration;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", insertable = false, updatable = false)
    private DecisionTreeEntity decisionTree;

    // Default constructor
    public DecisionNodeEntity() {}

    // Constructor with required fields
    public DecisionNodeEntity(String id, String treeId, String nodeType, String configuration) {
        this.id = id;
        this.treeId = treeId;
        this.nodeType = nodeType;
        this.configuration = configuration;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTreeId() {
        return treeId;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        if (!(o instanceof DecisionNodeEntity)) return false;
        DecisionNodeEntity that = (DecisionNodeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DecisionNodeEntity{" +
                "id='" + id + '\'' +
                ", treeId='" + treeId + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", parentId='" + parentId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}