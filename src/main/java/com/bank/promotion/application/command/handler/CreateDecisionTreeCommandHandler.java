package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import com.bank.promotion.domain.aggregate.PromotionDecisionTree;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
            
            // 生成唯一ID並模擬保存操作
            String treeId = "tree-" + UUID.randomUUID().toString().substring(0, 8);
            
            // 模擬保存成功
            return treeId;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create decision tree: " + e.getMessage(), e);
        }
    }
}