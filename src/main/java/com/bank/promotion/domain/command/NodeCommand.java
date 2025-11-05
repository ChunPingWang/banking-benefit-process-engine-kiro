package com.bank.promotion.domain.command;

import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;

/**
 * 節點命令介面
 * 定義所有節點命令的統一介面，實現Command Pattern
 */
public interface NodeCommand {
    
    /**
     * 執行命令
     * 
     * @param context 執行上下文，包含客戶資料和上下文資訊
     * @return 節點執行結果
     */
    NodeResult execute(ExecutionContext context);
    
    /**
     * 獲取命令類型
     * 
     * @return 命令類型標識
     */
    String getCommandType();
    
    /**
     * 驗證命令配置是否有效
     * 
     * @return 配置是否有效
     */
    boolean isValidConfiguration();
}