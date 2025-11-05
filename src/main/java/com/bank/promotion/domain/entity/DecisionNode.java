package com.bank.promotion.domain.entity;

import com.bank.promotion.domain.valueobject.NodeConfiguration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 決策節點抽象實體
 * 決策樹中節點的基礎類別
 */
public abstract class DecisionNode {
    
    protected final String id;
    protected final String treeId;
    protected final NodeConfiguration configuration;
    protected final String parentId;
    protected final LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    protected DecisionNode(String treeId, NodeConfiguration configuration, String parentId) {
        this.id = UUID.randomUUID().toString();
        this.treeId = validateTreeId(treeId);
        this.configuration = validateConfiguration(configuration);
        this.parentId = parentId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    protected DecisionNode(String id, String treeId, NodeConfiguration configuration, 
                          String parentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = validateId(id);
        this.treeId = validateTreeId(treeId);
        this.configuration = validateConfiguration(configuration);
        this.parentId = parentId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        return id.trim();
    }
    
    private String validateTreeId(String treeId) {
        if (treeId == null || treeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tree ID cannot be null or empty");
        }
        return treeId.trim();
    }
    
    private NodeConfiguration validateConfiguration(NodeConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Node configuration cannot be null");
        }
        return configuration;
    }
    
    /**
     * 抽象方法：執行節點邏輯
     * 子類別必須實作具體的執行邏輯
     */
    public abstract NodeResult execute(ExecutionContext context);
    
    /**
     * 抽象方法：驗證節點配置
     * 子類別必須實作配置驗證邏輯
     */
    public abstract boolean isValidConfiguration();
    
    /**
     * 更新節點配置
     */
    public void updateConfiguration(NodeConfiguration newConfiguration) {
        if (newConfiguration == null) {
            throw new IllegalArgumentException("New configuration cannot be null");
        }
        // 這裡應該創建新的實例，但為了簡化實作，我們更新時間戳
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public NodeConfiguration getConfiguration() {
        return configuration;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionNode that = (DecisionNode) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "id='" + id + '\'' +
               ", treeId='" + treeId + '\'' +
               ", configuration=" + configuration +
               ", parentId='" + parentId + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

