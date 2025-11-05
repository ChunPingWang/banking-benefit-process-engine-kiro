package com.bank.promotion.domain.state;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 優惠上下文
 * 維護優惠的狀態和相關資訊
 */
public class PromotionContext {
    
    private final String promotionId;
    private final String promotionName;
    private final String promotionType;
    private PromotionState currentState;
    private final Map<String, Object> properties;
    private final List<StateChangeEvent> stateChangeHistory;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public PromotionContext(String promotionId, String promotionName, String promotionType, PromotionState initialState) {
        this.promotionId = validatePromotionId(promotionId);
        this.promotionName = validatePromotionName(promotionName);
        this.promotionType = validatePromotionType(promotionType);
        this.currentState = validateState(initialState);
        this.properties = new ConcurrentHashMap<>();
        this.stateChangeHistory = Collections.synchronizedList(new ArrayList<>());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 記錄初始狀態
        addStateChangeEvent("初始化優惠，設定初始狀態: " + initialState.getStateName(), this.createdAt);
    }
    
    private String validatePromotionId(String promotionId) {
        if (promotionId == null || promotionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion ID cannot be null or empty");
        }
        return promotionId.trim();
    }
    
    private String validatePromotionName(String promotionName) {
        if (promotionName == null || promotionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion name cannot be null or empty");
        }
        return promotionName.trim();
    }
    
    private String validatePromotionType(String promotionType) {
        if (promotionType == null || promotionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion type cannot be null or empty");
        }
        return promotionType.trim();
    }
    
    private PromotionState validateState(PromotionState state) {
        if (state == null) {
            throw new IllegalArgumentException("Promotion state cannot be null");
        }
        return state;
    }
    
    /**
     * 轉換到新狀態
     * 
     * @param newState 新狀態
     * @return 狀態轉換結果
     */
    public StateTransitionResult transitionTo(PromotionState newState) {
        if (newState == null) {
            return StateTransitionResult.failure("目標狀態不能為 null");
        }
        
        if (currentState.equals(newState)) {
            return StateTransitionResult.failure("目標狀態與當前狀態相同");
        }
        
        if (!currentState.canTransitionTo(newState)) {
            return StateTransitionResult.failure(
                String.format("無法從狀態 %s 轉換到狀態 %s", 
                             currentState.getStateName(), 
                             newState.getStateName())
            );
        }
        
        try {
            PromotionState previousState = currentState;
            
            // 執行狀態離開邏輯
            previousState.onExit(this, newState);
            
            // 更新當前狀態
            this.currentState = newState;
            this.updatedAt = LocalDateTime.now();
            
            // 執行狀態進入邏輯
            newState.onEnter(this, previousState);
            
            return StateTransitionResult.success(
                String.format("成功從狀態 %s 轉換到狀態 %s", 
                             previousState.getStateName(), 
                             newState.getStateName())
            );
            
        } catch (Exception e) {
            return StateTransitionResult.failure(
                String.format("狀態轉換失敗: %s", e.getMessage())
            );
        }
    }
    
    /**
     * 添加狀態變更事件
     * 
     * @param description 事件描述
     * @param timestamp 事件時間
     */
    public void addStateChangeEvent(String description, LocalDateTime timestamp) {
        StateChangeEvent event = new StateChangeEvent(description, timestamp, currentState.getStateName());
        stateChangeHistory.add(event);
    }
    
    /**
     * 設定屬性
     * 
     * @param key 屬性鍵
     * @param value 屬性值
     */
    public void setProperty(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Property key cannot be null or empty");
        }
        
        properties.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 獲取屬性
     * 
     * @param key 屬性鍵
     * @return 屬性值
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * 獲取屬性（帶預設值）
     * 
     * @param key 屬性鍵
     * @param defaultValue 預設值
     * @param <T> 屬性值類型
     * @return 屬性值或預設值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        Object value = properties.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 獲取指定類型的屬性
     * 
     * @param key 屬性鍵
     * @param type 屬性類型
     * @param <T> 屬性值類型
     * @return 屬性值或 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getTypedProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    /**
     * 移除屬性
     * 
     * @param key 屬性鍵
     * @return 被移除的屬性值
     */
    public Object removeProperty(String key) {
        Object removedValue = properties.remove(key);
        if (removedValue != null) {
            this.updatedAt = LocalDateTime.now();
        }
        return removedValue;
    }
    
    /**
     * 檢查是否包含指定屬性
     * 
     * @param key 屬性鍵
     * @return 是否包含該屬性
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * 獲取所有屬性的副本
     * 
     * @return 屬性映射的副本
     */
    public Map<String, Object> getAllProperties() {
        return Map.copyOf(properties);
    }
    
    // Getters
    public String getPromotionId() {
        return promotionId;
    }
    
    public String getPromotionName() {
        return promotionName;
    }
    
    public String getPromotionType() {
        return promotionType;
    }
    
    public PromotionState getCurrentState() {
        return currentState;
    }
    
    public List<StateChangeEvent> getStateChangeHistory() {
        return Collections.unmodifiableList(stateChangeHistory);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public String toString() {
        return String.format("PromotionContext{promotionId='%s', promotionName='%s', promotionType='%s', currentState=%s, propertiesCount=%d, stateChangeHistoryCount=%d}", 
                           promotionId, promotionName, promotionType, currentState.getStateName(), properties.size(), stateChangeHistory.size());
    }
    
    /**
     * 狀態變更事件內部類別
     */
    public static class StateChangeEvent {
        private final String description;
        private final LocalDateTime timestamp;
        private final String stateName;
        
        public StateChangeEvent(String description, LocalDateTime timestamp, String stateName) {
            this.description = description;
            this.timestamp = timestamp;
            this.stateName = stateName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getStateName() {
            return stateName;
        }
        
        @Override
        public String toString() {
            return String.format("StateChangeEvent{description='%s', timestamp=%s, stateName='%s'}", 
                               description, timestamp, stateName);
        }
    }
}