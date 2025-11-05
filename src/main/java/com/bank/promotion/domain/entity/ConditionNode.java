package com.bank.promotion.domain.entity;

import com.bank.promotion.domain.valueobject.NodeConfiguration;
import java.time.LocalDateTime;

/**
 * 條件節點實體 (條件因子)
 * 負責評估客戶是否符合特定條件的決策節點
 */
public class ConditionNode extends DecisionNode {
    
    private final String trueNodeId;
    private final String falseNodeId;
    
    public ConditionNode(String treeId, NodeConfiguration configuration, String parentId,
                        String trueNodeId, String falseNodeId) {
        super(treeId, configuration, parentId);
        this.trueNodeId = trueNodeId;
        this.falseNodeId = falseNodeId;
        validateConditionNodeConfiguration();
    }
    
    public ConditionNode(String id, String treeId, NodeConfiguration configuration, 
                        String parentId, LocalDateTime createdAt, LocalDateTime updatedAt,
                        String trueNodeId, String falseNodeId) {
        super(id, treeId, configuration, parentId, createdAt, updatedAt);
        this.trueNodeId = trueNodeId;
        this.falseNodeId = falseNodeId;
        validateConditionNodeConfiguration();
    }
    
    private void validateConditionNodeConfiguration() {
        if (!"CONDITION".equals(configuration.getNodeType())) {
            throw new IllegalArgumentException("ConditionNode must have CONDITION node type");
        }
    }
    
    @Override
    public NodeResult execute(ExecutionContext context) {
        try {
            // 根據命令類型執行不同的條件評估邏輯
            boolean conditionResult = evaluateCondition(context);
            
            // 返回下一個要執行的節點ID
            String nextNodeId = conditionResult ? trueNodeId : falseNodeId;
            return NodeResult.success(nextNodeId);
            
        } catch (Exception e) {
            return NodeResult.failure("Failed to evaluate condition: " + e.getMessage());
        }
    }
    
    private boolean evaluateCondition(ExecutionContext context) {
        String commandType = configuration.getCommandType();
        
        switch (commandType) {
            case "SPEL":
                return evaluateSpELCondition(context);
            case "DROOLS":
                return evaluateDroolsCondition(context);
            case "EXTERNAL_SYSTEM":
                return evaluateExternalSystemCondition(context);
            case "DATABASE_QUERY":
                return evaluateDatabaseQueryCondition(context);
            default:
                throw new IllegalArgumentException("Unsupported command type: " + commandType);
        }
    }
    
    private boolean evaluateSpELCondition(ExecutionContext context) {
        // SpEL 表達式評估邏輯 (將在後續任務中實作)
        // 目前返回預設值
        return true;
    }
    
    private boolean evaluateDroolsCondition(ExecutionContext context) {
        // Drools 規則評估邏輯 (將在後續任務中實作)
        // 目前返回預設值
        return true;
    }
    
    private boolean evaluateExternalSystemCondition(ExecutionContext context) {
        // 外部系統呼叫邏輯 (將在後續任務中實作)
        // 目前返回預設值
        return true;
    }
    
    private boolean evaluateDatabaseQueryCondition(ExecutionContext context) {
        // 資料庫查詢邏輯 (將在後續任務中實作)
        // 目前返回預設值
        return true;
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證節點類型
        if (!"CONDITION".equals(configuration.getNodeType())) {
            return false;
        }
        
        // 驗證命令類型
        String commandType = configuration.getCommandType();
        if (commandType == null || 
            (!commandType.equals("SPEL") && 
             !commandType.equals("DROOLS") && 
             !commandType.equals("EXTERNAL_SYSTEM") && 
             !commandType.equals("DATABASE_QUERY"))) {
            return false;
        }
        
        // 驗證表達式 (對於 SpEL 和 Drools)
        if (("SPEL".equals(commandType) || "DROOLS".equals(commandType)) && 
            (configuration.getExpression() == null || configuration.getExpression().trim().isEmpty())) {
            return false;
        }
        
        return true;
    }
    
    public String getTrueNodeId() {
        return trueNodeId;
    }
    
    public String getFalseNodeId() {
        return falseNodeId;
    }
}