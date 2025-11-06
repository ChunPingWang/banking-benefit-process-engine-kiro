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
            // 驗證規則ID存在性
            if (command.getRuleId() == null || command.getRuleId().trim().isEmpty()) {
                throw new IllegalArgumentException("Rule ID cannot be null or empty");
            }
            
            // 驗證規則類型
            if (!isValidRuleType(command.getRuleType())) {
                throw new IllegalArgumentException("Invalid rule type: " + command.getRuleType());
            }
            
            // 驗證狀態
            if (!isValidStatus(command.getStatus())) {
                throw new IllegalArgumentException("Invalid status: " + command.getStatus());
            }
            
            // 模擬更新成功
            // 在實際實作中，這裡會透過 Repository 更新資料庫
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update promotion rule: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidRuleType(String ruleType) {
        return "SPEL".equals(ruleType) || "DROOLS".equals(ruleType) || "HARDCODED".equals(ruleType);
    }
    
    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "INACTIVE".equals(status) || "DRAFT".equals(status);
    }
}