package com.bank.promotion.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Decision Tree
 * Maps to decision_trees table
 */
@Entity
@Table(name = "decision_trees")
public class DecisionTreeEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "root_node_id", length = 36)
    private String rootNodeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "treeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DecisionNodeEntity> nodes = new ArrayList<>();

    // Default constructor
    public DecisionTreeEntity() {}

    // Constructor with required fields
    public DecisionTreeEntity(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(String rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<DecisionNodeEntity> getNodes() {
        return nodes;
    }

    public void setNodes(List<DecisionNodeEntity> nodes) {
        this.nodes = nodes;
    }

    // Helper methods
    public void addNode(DecisionNodeEntity node) {
        nodes.add(node);
        node.setTreeId(this.id);
    }

    public void removeNode(DecisionNodeEntity node) {
        nodes.remove(node);
        node.setTreeId(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionTreeEntity)) return false;
        DecisionTreeEntity that = (DecisionTreeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DecisionTreeEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", rootNodeId='" + rootNodeId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}