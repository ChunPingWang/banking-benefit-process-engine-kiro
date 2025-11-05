package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import com.bank.promotion.domain.aggregate.PromotionDecisionTree;
import org.springframework.stereotype.Component;

/**
 * 創建決策樹命令處理器
 */
@Component
public class CreateDecisionTreeCommandHandler {
    
    /**
     * 處理創建決策樹命令
     */
    public String handle(CreateDecisionTreeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        try {
            // 創建新的決策樹聚合
            PromotionDecisionTree decisionTree = new PromotionDecisionTree(command.getName());
            
            // TODO: 透過 Repository 保存決策樹
            // decisionTreeRepository.save(decisionTree);
            
            return decisionTree.getId();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create decision tree: " + e.getMessage(), e);
        }
    }
}