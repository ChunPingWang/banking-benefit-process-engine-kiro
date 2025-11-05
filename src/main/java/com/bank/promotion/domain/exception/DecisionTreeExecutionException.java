package com.bank.promotion.domain.exception;

/**
 * 決策樹執行異常
 * 當決策樹執行過程中發生錯誤時拋出
 */
public class DecisionTreeExecutionException extends PromotionSystemException {
    
    private final String treeId;
    private final String nodeId;
    
    public DecisionTreeExecutionException(String message, String treeId) {
        super(message, "DECISION_TREE_EXECUTION_ERROR");
        this.treeId = treeId;
        this.nodeId = null;
    }
    
    public DecisionTreeExecutionException(String message, String treeId, String nodeId) {
        super(message, "DECISION_TREE_EXECUTION_ERROR");
        this.treeId = treeId;
        this.nodeId = nodeId;
    }
    
    public DecisionTreeExecutionException(String message, String treeId, String nodeId, Throwable cause) {
        super(message, "DECISION_TREE_EXECUTION_ERROR", cause);
        this.treeId = treeId;
        this.nodeId = nodeId;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public String getNodeId() {
        return nodeId;
    }
}