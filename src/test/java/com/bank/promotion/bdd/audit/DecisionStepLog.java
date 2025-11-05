package com.bank.promotion.bdd.audit;

import java.time.LocalDateTime;

/**
 * 決策步驟日誌
 * 記錄決策樹中每個節點的執行過程
 */
public class DecisionStepLog {
    private final String requestId;
    private final String treeId;
    private final String nodeId;
    private final String nodeType;
    private final Object inputData;
    private final Object outputData;
    private final long executionTimeMs;
    private final String status;
    private final LocalDateTime createdAt;
    
    public DecisionStepLog(String requestId, String treeId, String nodeId, String nodeType,
                          Object inputData, Object outputData, long executionTimeMs, 
                          String status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.treeId = treeId;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.inputData = inputData;
        this.outputData = outputData;
        this.executionTimeMs = executionTimeMs;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public String getTreeId() { return treeId; }
    public String getNodeId() { return nodeId; }
    public String getNodeType() { return nodeType; }
    public Object getInputData() { return inputData; }
    public Object getOutputData() { return outputData; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}