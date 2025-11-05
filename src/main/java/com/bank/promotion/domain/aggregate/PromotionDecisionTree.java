package com.bank.promotion.domain.aggregate;

import com.bank.promotion.domain.entity.DecisionNode;
import com.bank.promotion.domain.entity.ConditionNode;
import com.bank.promotion.domain.entity.CalculationNode;
import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import com.bank.promotion.domain.exception.DecisionTreeExecutionException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 優惠決策樹聚合根
 * 管理決策樹的完整生命週期和執行邏輯
 */
public class PromotionDecisionTree {
    
    private final String id;
    private final String name;
    private TreeStatus status;
    private String rootNodeId;
    private final Map<String, DecisionNode> nodes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public PromotionDecisionTree(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = validateName(name);
        this.status = TreeStatus.DRAFT;
        this.nodes = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PromotionDecisionTree(String id, String name, TreeStatus status, String rootNodeId,
                                Map<String, DecisionNode> nodes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = validateId(id);
        this.name = validateName(name);
        this.status = status != null ? status : TreeStatus.DRAFT;
        this.rootNodeId = rootNodeId;
        this.nodes = nodes != null ? new HashMap<>(nodes) : new HashMap<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Decision tree ID cannot be null or empty");
        }
        return id.trim();
    }
    
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Decision tree name cannot be null or empty");
        }
        return name.trim();
    }
    
    /**
     * 評估客戶優惠資格
     * 遍歷決策樹並返回優惠結果
     */
    public PromotionResult evaluate(CustomerPayload customerPayload) {
        if (customerPayload == null) {
            throw new IllegalArgumentException("Customer payload cannot be null");
        }
        
        if (!isActive()) {
            throw new DecisionTreeExecutionException("Decision tree is not active", id);
        }
        
        if (rootNodeId == null || !nodes.containsKey(rootNodeId)) {
            throw new DecisionTreeExecutionException("Root node not found or not configured", id, rootNodeId);
        }
        
        try {
            ExecutionContext context = new ExecutionContext(customerPayload, createContextData(customerPayload));
            return traverseTree(rootNodeId, context, new HashSet<>());
        } catch (Exception e) {
            throw new DecisionTreeExecutionException("Failed to evaluate decision tree", id, rootNodeId, e);
        }
    }
    
    private Map<String, Object> createContextData(CustomerPayload customerPayload) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("customerId", customerPayload.getCustomerId());
        contextData.put("accountType", customerPayload.getAccountType());
        contextData.put("annualIncome", customerPayload.getAnnualIncome());
        contextData.put("creditScore", customerPayload.getCreditScore());
        contextData.put("region", customerPayload.getRegion());
        contextData.put("transactionCount", customerPayload.getTransactionCount());
        contextData.put("evaluationTime", LocalDateTime.now());
        return contextData;
    }
    
    private PromotionResult traverseTree(String nodeId, ExecutionContext context, Set<String> visitedNodes) {
        // 防止無限循環
        if (visitedNodes.contains(nodeId)) {
            throw new DecisionTreeExecutionException("Circular reference detected in decision tree", id, nodeId);
        }
        visitedNodes.add(nodeId);
        
        DecisionNode node = nodes.get(nodeId);
        if (node == null) {
            throw new DecisionTreeExecutionException("Node not found", id, nodeId);
        }
        
        NodeResult result = node.execute(context);
        if (!result.isSuccess()) {
            throw new DecisionTreeExecutionException("Node execution failed: " + result.getErrorMessage(), id, nodeId);
        }
        
        Object resultValue = result.getResult();
        
        // 如果是計算節點，返回優惠結果
        if (node instanceof CalculationNode) {
            if (resultValue instanceof PromotionResult) {
                return (PromotionResult) resultValue;
            } else {
                throw new DecisionTreeExecutionException("Calculation node must return PromotionResult", id, nodeId);
            }
        }
        
        // 如果是條件節點，繼續遍歷
        if (node instanceof ConditionNode) {
            if (resultValue instanceof String) {
                String nextNodeId = (String) resultValue;
                if (nextNodeId != null && !nextNodeId.trim().isEmpty()) {
                    return traverseTree(nextNodeId, context, visitedNodes);
                }
            }
            throw new DecisionTreeExecutionException("Condition node must return valid next node ID", id, nodeId);
        }
        
        throw new DecisionTreeExecutionException("Unknown node type", id, nodeId);
    }
    
    /**
     * 添加節點到決策樹
     */
    public void addNode(DecisionNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        
        if (!id.equals(node.getTreeId())) {
            throw new IllegalArgumentException("Node tree ID does not match this tree");
        }
        
        nodes.put(node.getId(), node);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 移除節點
     */
    public void removeNode(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        
        if (nodeId.equals(rootNodeId)) {
            throw new IllegalArgumentException("Cannot remove root node");
        }
        
        nodes.remove(nodeId);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 設定根節點
     */
    public void setRootNode(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Root node ID cannot be null or empty");
        }
        
        if (!nodes.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node with ID " + nodeId + " does not exist in this tree");
        }
        
        this.rootNodeId = nodeId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 驗證樹結構一致性
     */
    public ValidationResult validateTreeStructure() {
        List<String> errors = new ArrayList<>();
        
        // 檢查根節點
        if (rootNodeId == null) {
            errors.add("Root node is not set");
        } else if (!nodes.containsKey(rootNodeId)) {
            errors.add("Root node does not exist in tree");
        }
        
        // 檢查節點配置
        for (DecisionNode node : nodes.values()) {
            if (!node.isValidConfiguration()) {
                errors.add("Invalid configuration for node: " + node.getId());
            }
        }
        
        // 檢查節點引用
        for (DecisionNode node : nodes.values()) {
            if (node instanceof ConditionNode) {
                ConditionNode conditionNode = (ConditionNode) node;
                String trueNodeId = conditionNode.getTrueNodeId();
                String falseNodeId = conditionNode.getFalseNodeId();
                
                if (trueNodeId != null && !nodes.containsKey(trueNodeId)) {
                    errors.add("True node reference not found for condition node: " + node.getId());
                }
                
                if (falseNodeId != null && !nodes.containsKey(falseNodeId)) {
                    errors.add("False node reference not found for condition node: " + node.getId());
                }
            }
        }
        
        // 檢查是否有孤立節點
        Set<String> reachableNodes = findReachableNodes();
        for (String nodeId : nodes.keySet()) {
            if (!reachableNodes.contains(nodeId)) {
                errors.add("Unreachable node found: " + nodeId);
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private Set<String> findReachableNodes() {
        Set<String> reachable = new HashSet<>();
        if (rootNodeId != null) {
            findReachableNodesRecursive(rootNodeId, reachable);
        }
        return reachable;
    }
    
    private void findReachableNodesRecursive(String nodeId, Set<String> visited) {
        if (nodeId == null || visited.contains(nodeId) || !nodes.containsKey(nodeId)) {
            return;
        }
        
        visited.add(nodeId);
        DecisionNode node = nodes.get(nodeId);
        
        if (node instanceof ConditionNode) {
            ConditionNode conditionNode = (ConditionNode) node;
            findReachableNodesRecursive(conditionNode.getTrueNodeId(), visited);
            findReachableNodesRecursive(conditionNode.getFalseNodeId(), visited);
        }
    }
    
    /**
     * 啟用決策樹
     */
    public void activate() {
        ValidationResult validation = validateTreeStructure();
        if (!validation.isValid()) {
            throw new IllegalStateException("Cannot activate tree with validation errors: " + validation.getErrors());
        }
        
        this.status = TreeStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用決策樹
     */
    public void deactivate() {
        this.status = TreeStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return TreeStatus.ACTIVE.equals(status);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public TreeStatus getStatus() {
        return status;
    }
    
    public String getRootNodeId() {
        return rootNodeId;
    }
    
    public Map<String, DecisionNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
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
        PromotionDecisionTree that = (PromotionDecisionTree) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PromotionDecisionTree{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", status=" + status +
               ", rootNodeId='" + rootNodeId + '\'' +
               ", nodeCount=" + nodes.size() +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

/**
 * 決策樹狀態枚舉
 */
enum TreeStatus {
    DRAFT,      // 草稿
    ACTIVE,     // 啟用
    INACTIVE    // 停用
}

/**
 * 驗證結果
 */
class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    
    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}