package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.UpdatePromotionRuleCommand;
import org.springframework.stereotype.Component;

/**
 * 更新優惠規則命令處理器
 */
@Component
public class UpdatePromotionRuleCommandHandler {
    
    /**
     * 處理更新優惠規則命令
     */
    public void handle(UpdatePromotionRuleCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        try {
            // TODO: 透過 Repository 查找並更新優惠規則
            // PromotionRule rule = promotionRuleRepository.findById(command.getRuleId())
            //     .orElseThrow(() -> new PromotionRuleNotFoundException(command.getRuleId()));
            
            // rule.updateRule(command.getName(), command.getRuleType(), 
            //                command.getRuleContent(), command.getParameters(), command.getStatus());
            
            // promotionRuleRepository.save(rule);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update promotion rule: " + e.getMessage(), e);
        }
    }
}