package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.domain.aggregate.PromotionDecisionTree;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.springframework.stereotype.Component;

/**
 * 評估優惠命令處理器
 */
@Component
public class EvaluatePromotionCommandHandler {
    
    /**
     * 處理評估優惠命令
     */
    public PromotionResult handle(EvaluatePromotionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        try {
            // TODO: 透過 Repository 查找決策樹
            // PromotionDecisionTree decisionTree = decisionTreeRepository.findById(command.getTreeId())
            //     .orElseThrow(() -> new DecisionTreeNotFoundException(command.getTreeId()));
            
            // 暫時創建一個測試用的決策樹
            PromotionDecisionTree decisionTree = new PromotionDecisionTree("Test Tree");
            
            // 執行決策樹評估
            PromotionResult result = decisionTree.evaluate(command.getCustomerPayload());
            
            // TODO: 記錄稽核軌跡
            // auditService.recordPromotionEvaluation(command.getRequestId(), 
            //                                       command.getCustomerPayload(), result);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate promotion: " + e.getMessage(), e);
        }
    }
}