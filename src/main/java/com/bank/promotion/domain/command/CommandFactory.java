package com.bank.promotion.domain.command;

import com.bank.promotion.domain.valueobject.NodeConfiguration;
import com.bank.promotion.domain.exception.PromotionSystemException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 命令工廠
 * 負責創建和管理不同類型的節點命令
 */
@Component
public class CommandFactory {
    
    private final Map<String, Function<NodeConfiguration, NodeCommand>> commandCreators;
    
    public CommandFactory() {
        this.commandCreators = new HashMap<>();
        registerDefaultCommands();
    }
    
    /**
     * 註冊預設命令創建器
     */
    private void registerDefaultCommands() {
        // SpEL 命令
        registerCommand("SPEL_CONDITION", config -> new com.bank.promotion.domain.command.spel.SpELConditionCommand(config));
        registerCommand("SPEL_CALCULATION", config -> new com.bank.promotion.domain.command.spel.SpELCalculationCommand(config));
        
        // Drools 命令
        registerCommand("DROOLS_CONDITION", config -> new com.bank.promotion.domain.command.drools.DroolsRuleCommand(config));
        registerCommand("DROOLS_CALCULATION", config -> new com.bank.promotion.domain.command.drools.DroolsRuleCommand(config));
        
        // 外部系統命令
        registerCommand("EXTERNAL_SYSTEM_CONDITION", config -> new com.bank.promotion.domain.command.external.ExternalSystemCommand(config));
        registerCommand("EXTERNAL_SYSTEM_CALCULATION", config -> new com.bank.promotion.domain.command.external.ExternalSystemCommand(config));
        
        // 資料庫查詢命令
        registerCommand("DATABASE_QUERY_CONDITION", config -> new com.bank.promotion.domain.command.database.DatabaseQueryCommand(config));
        registerCommand("DATABASE_QUERY_CALCULATION", config -> new com.bank.promotion.domain.command.database.DatabaseQueryCommand(config));
    }
    
    /**
     * 註冊命令創建器
     * 
     * @param commandType 命令類型
     * @param creator 命令創建器函數
     */
    public void registerCommand(String commandType, Function<NodeConfiguration, NodeCommand> creator) {
        if (commandType == null || commandType.trim().isEmpty()) {
            throw new IllegalArgumentException("Command type cannot be null or empty");
        }
        if (creator == null) {
            throw new IllegalArgumentException("Command creator cannot be null");
        }
        
        commandCreators.put(commandType.trim(), creator);
    }
    
    /**
     * 創建節點命令
     * 
     * @param configuration 節點配置
     * @return 對應的節點命令
     * @throws PromotionSystemException 當命令類型不支援時
     */
    public NodeCommand createCommand(NodeConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Node configuration cannot be null");
        }
        
        String commandKey = buildCommandKey(configuration);
        Function<NodeConfiguration, NodeCommand> creator = commandCreators.get(commandKey);
        
        if (creator == null) {
            throw new PromotionSystemException(
                "Unsupported command type: " + commandKey + 
                " for node type: " + configuration.getNodeType()
            );
        }
        
        try {
            NodeCommand command = creator.apply(configuration);
            if (!command.isValidConfiguration()) {
                throw new PromotionSystemException(
                    "Invalid configuration for command type: " + commandKey
                );
            }
            return command;
        } catch (Exception e) {
            throw new PromotionSystemException(
                "Failed to create command for type: " + commandKey, e
            );
        }
    }
    
    /**
     * 建構命令鍵值
     * 結合節點類型和命令類型來唯一識別命令
     */
    private String buildCommandKey(NodeConfiguration configuration) {
        String nodeType = configuration.getNodeType();
        String commandType = configuration.getCommandType();
        
        // 根據節點類型和命令類型組合成唯一的命令鍵值
        if ("CONDITION".equals(nodeType)) {
            return commandType + "_CONDITION";
        } else if ("CALCULATION".equals(nodeType)) {
            return commandType + "_CALCULATION";
        } else {
            return commandType;
        }
    }
    
    /**
     * 檢查是否支援指定的命令類型
     * 
     * @param configuration 節點配置
     * @return 是否支援該命令類型
     */
    public boolean isCommandSupported(NodeConfiguration configuration) {
        if (configuration == null) {
            return false;
        }
        
        String commandKey = buildCommandKey(configuration);
        return commandCreators.containsKey(commandKey);
    }
    
    /**
     * 獲取所有已註冊的命令類型
     * 
     * @return 已註冊的命令類型集合
     */
    public Map<String, String> getRegisteredCommands() {
        Map<String, String> commands = new HashMap<>();
        for (String commandKey : commandCreators.keySet()) {
            commands.put(commandKey, "Registered");
        }
        return commands;
    }
}