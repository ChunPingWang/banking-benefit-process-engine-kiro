package com.bank.promotion.domain.entity;

import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.valueobject.PromotionResult;
import java.time.LocalDateTime;

/**
 * 計算節點實體 (計算因子)
 * 負責計算優惠金額或優惠內容的葉節點
 */
public class CalculationNode extends DecisionNode {
    
    public CalculationNode(String treeId, NodeConfiguration configuration, String parentId) {
        super(treeId, configuration, parentId);
        validateCalculationNodeConfiguration();
    }
    
    public CalculationNode(String id, String treeId, NodeConfiguration configuration, 
                          String parentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, treeId, configuration, parentId, createdAt, updatedAt);
        validateCalculationNodeConfiguration();
    }
    
    private void validateCalculationNodeConfiguration() {
        if (!"CALCULATION".equals(configuration.getNodeType())) {
            throw new IllegalArgumentException("CalculationNode must have CALCULATION node type");
        }
    }
    
    @Override
    public NodeResult execute(ExecutionContext context) {
        try {
            // 執行計算邏輯並返回優惠結果
            PromotionResult promotionResult = calculatePromotion(context);
            return NodeResult.success(promotionResult);
            
        } catch (Exception e) {
            return NodeResult.failure("Failed to calculate promotion: " + e.getMessage());
        }
    }
    
    private PromotionResult calculatePromotion(ExecutionContext context) {
        String commandType = configuration.getCommandType();
        
        switch (commandType) {
            case "SPEL":
                return calculateSpELPromotion(context);
            case "DROOLS":
                return calculateDroolsPromotion(context);
            case "EXTERNAL_SYSTEM":
                return calculateExternalSystemPromotion(context);
            case "DATABASE_QUERY":
                return calculateDatabaseQueryPromotion(context);
            default:
                throw new IllegalArgumentException("Unsupported command type: " + commandType);
        }
    }
    
    private PromotionResult calculateSpELPromotion(ExecutionContext context) {
        // SpEL 表達式計算邏輯 (將在後續任務中實作)
        // 目前返回預設優惠結果
        return createDefaultPromotionResult("SpEL計算優惠");
    }
    
    private PromotionResult calculateDroolsPromotion(ExecutionContext context) {
        // Drools 規則計算邏輯 (將在後續任務中實作)
        // 目前返回預設優惠結果
        return createDefaultPromotionResult("Drools規則優惠");
    }
    
    private PromotionResult calculateExternalSystemPromotion(ExecutionContext context) {
        // 外部系統計算邏輯 (將在後續任務中實作)
        // 目前返回預設優惠結果
        return createDefaultPromotionResult("外部系統優惠");
    }
    
    private PromotionResult calculateDatabaseQueryPromotion(ExecutionContext context) {
        // 資料庫查詢計算邏輯 (將在後續任務中實作)
        // 目前返回預設優惠結果
        return createDefaultPromotionResult("資料庫查詢優惠");
    }
    
    private PromotionResult createDefaultPromotionResult(String promotionName) {
        return new PromotionResult(
            java.util.UUID.randomUUID().toString(),
            promotionName,
            "DEFAULT",
            java.math.BigDecimal.valueOf(100),
            java.math.BigDecimal.valueOf(5),
            "預設優惠方案",
            LocalDateTime.now().plusDays(30),
            java.util.Map.of("source", "CalculationNode"),
            true
        );
    }
    
    @Override
    public boolean isValidConfiguration() {
        if (configuration == null) {
            return false;
        }
        
        // 驗證節點類型
        if (!"CALCULATION".equals(configuration.getNodeType())) {
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
}