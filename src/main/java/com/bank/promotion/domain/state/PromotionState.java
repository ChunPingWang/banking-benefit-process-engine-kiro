package com.bank.promotion.domain.state;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 優惠狀態抽象類別
 * 定義優惠在不同狀態下的行為
 */
public abstract class PromotionState {
    
    protected final String stateName;
    protected final String description;
    
    protected PromotionState(String stateName, String description) {
        this.stateName = stateName;
        this.description = description;
    }
    
    /**
     * 處理優惠評估請求
     * 
     * @param context 優惠上下文
     * @param customer 客戶檔案
     * @param parameters 評估參數
     * @return 優惠結果
     */
    public abstract PromotionResult evaluate(PromotionContext context, CustomerProfile customer, Map<String, Object> parameters);
    
    /**
     * 啟用優惠
     * 
     * @param context 優惠上下文
     * @return 狀態轉換結果
     */
    public abstract StateTransitionResult activate(PromotionContext context);
    
    /**
     * 暫停優惠
     * 
     * @param context 優惠上下文
     * @param reason 暫停原因
     * @return 狀態轉換結果
     */
    public abstract StateTransitionResult suspend(PromotionContext context, String reason);
    
    /**
     * 過期優惠
     * 
     * @param context 優惠上下文
     * @return 狀態轉換結果
     */
    public abstract StateTransitionResult expire(PromotionContext context);
    
    /**
     * 檢查是否可以轉換到指定狀態
     * 
     * @param targetState 目標狀態
     * @return 是否可以轉換
     */
    public abstract boolean canTransitionTo(PromotionState targetState);
    
    /**
     * 檢查狀態是否有效
     * 
     * @param context 優惠上下文
     * @return 狀態是否有效
     */
    public abstract boolean isValid(PromotionContext context);
    
    /**
     * 處理狀態進入事件
     * 
     * @param context 優惠上下文
     * @param previousState 前一個狀態
     */
    public void onEnter(PromotionContext context, PromotionState previousState) {
        // 預設實作：記錄狀態變更事件
        context.addStateChangeEvent(
            String.format("進入狀態: %s (從 %s)", 
                         this.stateName, 
                         previousState != null ? previousState.getStateName() : "初始狀態"),
            LocalDateTime.now()
        );
    }
    
    /**
     * 處理狀態離開事件
     * 
     * @param context 優惠上下文
     * @param nextState 下一個狀態
     */
    public void onExit(PromotionContext context, PromotionState nextState) {
        // 預設實作：記錄狀態變更事件
        context.addStateChangeEvent(
            String.format("離開狀態: %s (轉向 %s)", 
                         this.stateName, 
                         nextState != null ? nextState.getStateName() : "未知狀態"),
            LocalDateTime.now()
        );
    }
    
    /**
     * 獲取狀態名稱
     * 
     * @return 狀態名稱
     */
    public String getStateName() {
        return stateName;
    }
    
    /**
     * 獲取狀態描述
     * 
     * @return 狀態描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否為活躍狀態
     * 
     * @return 是否為活躍狀態
     */
    public abstract boolean isActive();
    
    /**
     * 檢查是否為終止狀態
     * 
     * @return 是否為終止狀態
     */
    public abstract boolean isTerminal();
    
    @Override
    public String toString() {
        return String.format("PromotionState{stateName='%s', description='%s'}", stateName, description);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionState that = (PromotionState) o;
        return stateName.equals(that.stateName);
    }
    
    @Override
    public int hashCode() {
        return stateName.hashCode();
    }
}