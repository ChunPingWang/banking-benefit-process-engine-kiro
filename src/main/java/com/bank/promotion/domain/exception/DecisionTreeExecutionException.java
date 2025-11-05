package com.bank.promotion.domain.exception;

/**
 * 決策樹執行異常
 * 當決策樹執行過程中發生錯誤時拋出
 */
public class DecisionTreeExecutionException extends RuntimeException {
    
    private final String treeId;
    private final String nodeId;
    
    public DecisionTreeExecutionException(String message, String treeId) {
        super(message);
        this.treeId = treeId;
        this.nodeId = null;
    }
    
    public DecisionTreeExecutionException(String message, String treeId, String nodeId) {
        super(message);
        this.treeId = treeId;
        this.nodeId = nodeId;
    }
    
    public DecisionTreeExecutionException(String message, String treeId, String nodeId, Throwable cause) {
        super(message, cause);
        this.treeId = treeId;
        this.nodeId = nodeId;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (treeId != null) {
            sb.append(" [TreeId: ").append(treeId).append("]");
        }
        if (nodeId != null) {
            sb.append(" [NodeId: ").append(nodeId).append("]");
        }
        return sb.toString();
    }
}