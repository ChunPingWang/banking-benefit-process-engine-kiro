package com.bank.promotion.domain.state;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 優惠狀態管理器
 * 負責管理優惠狀態的創建、轉換和驗證
 */
@Component
public class PromotionStateManager {
    
    private final Map<String, Class<? extends PromotionState>> stateRegistry;
    
    public PromotionStateManager() {
        this.stateRegistry = new HashMap<>();
        initializeStateRegistry();
    }
    
    /**
     * 初始化狀態註冊表
     */
    private void initializeStateRegistry() {
        stateRegistry.put(ActivePromotionState.STATE_NAME, ActivePromotionState.class);
        stateRegistry.put(SuspendedPromotionState.STATE_NAME, SuspendedPromotionState.class);
        stateRegistry.put(ExpiredPromotionState.STATE_NAME, ExpiredPromotionState.class);
    }
    
    /**
     * 創建指定類型的狀態實例
     * 
     * @param stateName 狀態名稱
     * @return 狀態實例
     * @throws IllegalArgumentException 如果狀態類型不存在
     */
    public PromotionState createState(String stateName) {
        return createState(stateName, null);
    }
    
    /**
     * 創建指定類型的狀態實例（帶參數）
     * 
     * @param stateName 狀態名稱
     * @param parameter 狀態參數（如暫停原因）
     * @return 狀態實例
     * @throws IllegalArgumentException 如果狀態類型不存在
     */
    public PromotionState createState(String stateName, String parameter) {
        if (stateName == null || stateName.trim().isEmpty()) {
            throw new IllegalArgumentException("State name cannot be null or empty");
        }
        
        Class<? extends PromotionState> stateClass = stateRegistry.get(stateName.toUpperCase());
        if (stateClass == null) {
            throw new IllegalArgumentException("Unknown state type: " + stateName);
        }
        
        try {
            if (stateClass == ActivePromotionState.class) {
                return new ActivePromotionState();
            } else if (stateClass == SuspendedPromotionState.class) {
                return parameter != null ? 
                    new SuspendedPromotionState(parameter) : 
                    new SuspendedPromotionState();
            } else if (stateClass == ExpiredPromotionState.class) {
                return new ExpiredPromotionState();
            } else {
                // 使用反射創建實例（預設建構子）
                return stateClass.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create state instance: " + stateName, e);
        }
    }
    
    /**
     * 創建預設的初始狀態（活躍狀態）
     * 
     * @return 活躍狀態實例
     */
    public PromotionState createDefaultInitialState() {
        return new ActivePromotionState();
    }
    
    /**
     * 檢查狀態轉換是否有效
     * 
     * @param fromState 源狀態
     * @param toState 目標狀態
     * @return 轉換是否有效
     */
    public boolean isValidTransition(PromotionState fromState, PromotionState toState) {
        if (fromState == null || toState == null) {
            return false;
        }
        
        return fromState.canTransitionTo(toState);
    }
    
    /**
     * 檢查狀態是否存在
     * 
     * @param stateName 狀態名稱
     * @return 狀態是否存在
     */
    public boolean isStateSupported(String stateName) {
        if (stateName == null || stateName.trim().isEmpty()) {
            return false;
        }
        
        return stateRegistry.containsKey(stateName.toUpperCase());
    }
    
    /**
     * 獲取所有支援的狀態名稱
     * 
     * @return 支援的狀態名稱集合
     */
    public Set<String> getSupportedStateNames() {
        return stateRegistry.keySet();
    }
    
    /**
     * 執行狀態轉換
     * 
     * @param context 優惠上下文
     * @param targetStateName 目標狀態名稱
     * @return 轉換結果
     */
    public StateTransitionResult transitionTo(PromotionContext context, String targetStateName) {
        return transitionTo(context, targetStateName, null);
    }
    
    /**
     * 執行狀態轉換（帶參數）
     * 
     * @param context 優惠上下文
     * @param targetStateName 目標狀態名稱
     * @param parameter 狀態參數
     * @return 轉換結果
     */
    public StateTransitionResult transitionTo(PromotionContext context, String targetStateName, String parameter) {
        if (context == null) {
            return StateTransitionResult.failure("優惠上下文不能為 null");
        }
        
        if (!isStateSupported(targetStateName)) {
            return StateTransitionResult.failure("不支援的狀態類型: " + targetStateName);
        }
        
        try {
            PromotionState targetState = createState(targetStateName, parameter);
            return context.transitionTo(targetState);
        } catch (Exception e) {
            return StateTransitionResult.failure("狀態轉換失敗: " + e.getMessage());
        }
    }
    
    /**
     * 啟用優惠
     * 
     * @param context 優惠上下文
     * @return 轉換結果
     */
    public StateTransitionResult activatePromotion(PromotionContext context) {
        if (context == null) {
            return StateTransitionResult.failure("優惠上下文不能為 null");
        }
        
        return context.getCurrentState().activate(context);
    }
    
    /**
     * 暫停優惠
     * 
     * @param context 優惠上下文
     * @param reason 暫停原因
     * @return 轉換結果
     */
    public StateTransitionResult suspendPromotion(PromotionContext context, String reason) {
        if (context == null) {
            return StateTransitionResult.failure("優惠上下文不能為 null");
        }
        
        return context.getCurrentState().suspend(context, reason);
    }
    
    /**
     * 使優惠過期
     * 
     * @param context 優惠上下文
     * @return 轉換結果
     */
    public StateTransitionResult expirePromotion(PromotionContext context) {
        if (context == null) {
            return StateTransitionResult.failure("優惠上下文不能為 null");
        }
        
        return context.getCurrentState().expire(context);
    }
    
    /**
     * 檢查並自動處理過期優惠
     * 
     * @param context 優惠上下文
     * @return 是否執行了狀態轉換
     */
    public boolean checkAndHandleExpiration(PromotionContext context) {
        if (context == null) {
            return false;
        }
        
        PromotionState currentState = context.getCurrentState();
        
        // 如果已經是過期狀態，無需處理
        if (currentState instanceof ExpiredPromotionState) {
            return false;
        }
        
        // 檢查是否應該過期
        if (!currentState.isValid(context)) {
            StateTransitionResult result = expirePromotion(context);
            return result.isSuccess();
        }
        
        return false;
    }
    
    /**
     * 獲取狀態統計資訊
     * 
     * @param context 優惠上下文
     * @return 狀態統計資訊
     */
    public Map<String, Object> getStateStatistics(PromotionContext context) {
        if (context == null) {
            return Map.of();
        }
        
        Map<String, Object> statistics = new HashMap<>();
        
        PromotionState currentState = context.getCurrentState();
        statistics.put("currentState", currentState.getStateName());
        statistics.put("isActive", currentState.isActive());
        statistics.put("isTerminal", currentState.isTerminal());
        statistics.put("isValid", currentState.isValid(context));
        
        // 狀態變更歷史統計
        var stateChangeHistory = context.getStateChangeHistory();
        statistics.put("stateChangeCount", stateChangeHistory.size());
        
        if (!stateChangeHistory.isEmpty()) {
            statistics.put("firstStateChange", stateChangeHistory.get(0).getTimestamp());
            statistics.put("lastStateChange", stateChangeHistory.get(stateChangeHistory.size() - 1).getTimestamp());
        }
        
        // 時間統計
        statistics.put("createdAt", context.getCreatedAt());
        statistics.put("updatedAt", context.getUpdatedAt());
        
        // 特定狀態的統計
        LocalDateTime activatedAt = context.getTypedProperty("activatedAt", LocalDateTime.class);
        if (activatedAt != null) {
            statistics.put("activatedAt", activatedAt);
        }
        
        LocalDateTime suspendedAt = context.getTypedProperty("suspendedAt", LocalDateTime.class);
        if (suspendedAt != null) {
            statistics.put("suspendedAt", suspendedAt);
            Long suspensionDurationHours = context.getTypedProperty("suspensionDurationHours", Long.class);
            if (suspensionDurationHours != null) {
                statistics.put("suspensionDurationHours", suspensionDurationHours);
            }
        }
        
        LocalDateTime expiredAt = context.getTypedProperty("expiredAt", LocalDateTime.class);
        if (expiredAt != null) {
            statistics.put("expiredAt", expiredAt);
            Long lifetimeDays = context.getTypedProperty("lifetimeDays", Long.class);
            if (lifetimeDays != null) {
                statistics.put("lifetimeDays", lifetimeDays);
            }
        }
        
        return statistics;
    }
}