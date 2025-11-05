package com.bank.promotion.domain.command;

import com.bank.promotion.domain.entity.ExecutionContext;
import com.bank.promotion.domain.entity.NodeResult;
import com.bank.promotion.domain.valueobject.NodeConfiguration;

/**
 * 抽象節點命令基礎類別
 * 提供命令的通用功能和模板方法
 */
public abstract class AbstractNodeCommand implements NodeCommand {
    
    protected final NodeConfiguration configuration;
    
    protected AbstractNodeCommand(NodeConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Node configuration cannot be null");
        }
        this.configuration = configuration;
    }
    
    @Override
    public final NodeResult execute(ExecutionContext context) {
        if (context == null) {
            return NodeResult.failure("Execution context cannot be null");
        }
        
        if (!isValidConfiguration()) {
            return NodeResult.failure("Invalid command configuration");
        }
        
        try {
            // 執行前置處理
            preExecute(context);
            
            // 執行核心邏輯
            NodeResult result = doExecute(context);
            
            // 執行後置處理
            postExecute(context, result);
            
            return result;
            
        } catch (Exception e) {
            return handleExecutionError(e, context);
        }
    }
    
    /**
     * 執行核心邏輯的抽象方法
     * 子類別必須實作具體的執行邏輯
     * 
     * @param context 執行上下文
     * @return 執行結果
     */
    protected abstract NodeResult doExecute(ExecutionContext context);
    
    /**
     * 執行前置處理
     * 子類別可以覆寫此方法來實作自定義的前置邏輯
     * 
     * @param context 執行上下文
     */
    protected void preExecute(ExecutionContext context) {
        // 預設實作為空，子類別可以覆寫
    }
    
    /**
     * 執行後置處理
     * 子類別可以覆寫此方法來實作自定義的後置邏輯
     * 
     * @param context 執行上下文
     * @param result 執行結果
     */
    protected void postExecute(ExecutionContext context, NodeResult result) {
        // 預設實作為空，子類別可以覆寫
    }
    
    /**
     * 處理執行錯誤
     * 子類別可以覆寫此方法來實作自定義的錯誤處理邏輯
     * 
     * @param exception 發生的異常
     * @param context 執行上下文
     * @return 錯誤結果
     */
    protected NodeResult handleExecutionError(Exception exception, ExecutionContext context) {
        String errorMessage = String.format(
            "Command execution failed for type %s: %s", 
            getCommandType(), 
            exception.getMessage()
        );
        return NodeResult.failure(errorMessage, exception);
    }
    
    /**
     * 獲取配置參數
     * 
     * @param key 參數鍵
     * @param defaultValue 預設值
     * @return 參數值
     */
    protected Object getConfigurationParameter(String key, Object defaultValue) {
        if (configuration.getParameters() == null) {
            return defaultValue;
        }
        return configuration.getParameters().getOrDefault(key, defaultValue);
    }
    
    /**
     * 獲取字串類型的配置參數
     * 
     * @param key 參數鍵
     * @param defaultValue 預設值
     * @return 字串參數值
     */
    protected String getStringParameter(String key, String defaultValue) {
        Object value = getConfigurationParameter(key, defaultValue);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 獲取布林類型的配置參數
     * 
     * @param key 參數鍵
     * @param defaultValue 預設值
     * @return 布林參數值
     */
    protected boolean getBooleanParameter(String key, boolean defaultValue) {
        Object value = getConfigurationParameter(key, defaultValue);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    /**
     * 獲取整數類型的配置參數
     * 
     * @param key 參數鍵
     * @param defaultValue 預設值
     * @return 整數參數值
     */
    protected int getIntParameter(String key, int defaultValue) {
        Object value = getConfigurationParameter(key, defaultValue);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 獲取節點配置
     * 
     * @return 節點配置
     */
    protected NodeConfiguration getConfiguration() {
        return configuration;
    }
}