package com.bank.promotion.domain.command;

import com.bank.promotion.domain.valueobject.NodeConfiguration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 命令註冊器
 * 提供執行時動態註冊和管理命令的能力
 */
@Component
public class CommandRegistry {
    
    private final Map<String, Function<NodeConfiguration, NodeCommand>> registeredCommands;
    private final CommandFactory commandFactory;
    
    public CommandRegistry() {
        this.registeredCommands = new ConcurrentHashMap<>();
        this.commandFactory = new CommandFactory();
    }
    
    /**
     * 註冊命令創建器
     * 
     * @param commandType 命令類型
     * @param creator 命令創建器
     */
    public void registerCommand(String commandType, Function<NodeConfiguration, NodeCommand> creator) {
        if (commandType == null || commandType.trim().isEmpty()) {
            throw new IllegalArgumentException("Command type cannot be null or empty");
        }
        if (creator == null) {
            throw new IllegalArgumentException("Command creator cannot be null");
        }
        
        String normalizedType = commandType.trim().toUpperCase();
        registeredCommands.put(normalizedType, creator);
        
        // 同時註冊到工廠
        commandFactory.registerCommand(normalizedType, creator);
    }
    
    /**
     * 取消註冊命令
     * 
     * @param commandType 命令類型
     * @return 是否成功取消註冊
     */
    public boolean unregisterCommand(String commandType) {
        if (commandType == null || commandType.trim().isEmpty()) {
            return false;
        }
        
        String normalizedType = commandType.trim().toUpperCase();
        return registeredCommands.remove(normalizedType) != null;
    }
    
    /**
     * 檢查命令是否已註冊
     * 
     * @param commandType 命令類型
     * @return 是否已註冊
     */
    public boolean isCommandRegistered(String commandType) {
        if (commandType == null || commandType.trim().isEmpty()) {
            return false;
        }
        
        String normalizedType = commandType.trim().toUpperCase();
        return registeredCommands.containsKey(normalizedType);
    }
    
    /**
     * 創建命令實例
     * 
     * @param configuration 節點配置
     * @return 命令實例
     */
    public NodeCommand createCommand(NodeConfiguration configuration) {
        return commandFactory.createCommand(configuration);
    }
    
    /**
     * 獲取命令工廠實例
     * 
     * @return 命令工廠
     */
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }
    
    /**
     * 獲取所有已註冊的命令類型
     * 
     * @return 已註冊的命令類型
     */
    public Map<String, String> getAllRegisteredCommands() {
        Map<String, String> commands = new ConcurrentHashMap<>();
        for (String commandType : registeredCommands.keySet()) {
            commands.put(commandType, "Active");
        }
        return commands;
    }
    
    /**
     * 清除所有註冊的命令
     */
    public void clearAllCommands() {
        registeredCommands.clear();
    }
    
    /**
     * 獲取已註冊命令的數量
     * 
     * @return 命令數量
     */
    public int getRegisteredCommandCount() {
        return registeredCommands.size();
    }
}