package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.domain.aggregate.PromotionDecisionTree;
import com.bank.promotion.domain.valueobject.PromotionResult;
import com.bank.promotion.application.service.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 評估優惠命令處理器
 */
@Component
public class EvaluatePromotionCommandHandler {
    
    private final AuditService auditService;
    
    @Autowired
    public EvaluatePromotionCommandHandler(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * 處理評估優惠命令
     */
    public PromotionResult handle(EvaluatePromotionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        try {
            // 創建模擬決策樹進行評估
            PromotionDecisionTree decisionTree = createMockDecisionTree(command.getTreeId());
            
            // 執行決策樹評估
            PromotionResult result = evaluatePromotion(command);
            
            // 記錄稽核軌跡
            auditService.recordPromotionEvaluation(
                command.getRequestId(), 
                command.getCustomerPayload(), 
                result,
                System.currentTimeMillis()
            );
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate promotion: " + e.getMessage(), e);
        }
    }
    
    private PromotionDecisionTree createMockDecisionTree(String treeId) {
        return new PromotionDecisionTree(treeId != null ? treeId : "default-tree");
    }
    
    private PromotionResult evaluatePromotion(EvaluatePromotionCommand command) {
        var customerPayload = command.getCustomerPayload();
        
        // 基於客戶資料進行優惠評估邏輯
        if (customerPayload.getCreditScore() >= 700 && 
            customerPayload.getAnnualIncome().compareTo(BigDecimal.valueOf(1000000)) >= 0) {
            
            // VIP 客戶優惠
            return new PromotionResult(
                "promo-vip-001",
                "VIP專屬優惠",
                "VIP",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(10.0),
                "VIP客戶專屬高額優惠",
                LocalDateTime.now().plusMonths(3),
                Map.of("tier", "VIP", "category", "premium"),
                true
            );
        } else if (customerPayload.getCreditScore() >= 600) {
            
            // 一般客戶優惠
            return new PromotionResult(
                "promo-general-001",
                "一般客戶優惠",
                "GENERAL",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(5.0),
                "一般客戶優惠方案",
                LocalDateTime.now().plusMonths(1),
                Map.of("tier", "GENERAL", "category", "standard"),
                true
            );
        } else {
            
            // 不符合條件
            return new PromotionResult(
                "no-promotion",
                "無符合優惠",
                "NONE",
                null,
                null,
                "目前沒有符合條件的優惠方案",
                null,
                Map.of("reason", "insufficient_credit_score"),
                false
            );
        }
    }
}