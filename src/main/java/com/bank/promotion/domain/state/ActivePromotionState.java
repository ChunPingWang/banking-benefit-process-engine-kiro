package com.bank.promotion.domain.state;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 活躍優惠狀態
 * 優惠處於活躍狀態，可以正常評估和提供優惠
 */
public class ActivePromotionState extends PromotionState {
    
    public static final String STATE_NAME = "ACTIVE";
    
    public ActivePromotionState() {
        super(STATE_NAME, "優惠活躍狀態，可正常提供優惠服務");
    }
    
    @Override
    public PromotionResult evaluate(PromotionContext context, CustomerProfile customer, Map<String, Object> parameters) {
        // 檢查優惠是否仍然有效
        if (!isValid(context)) {
            // 如果優惠已過期，自動轉換到過期狀態
            context.transitionTo(new ExpiredPromotionState());
            return createExpiredPromotionResult(context, customer);
        }
        
        // 在活躍狀態下，正常處理優惠評估
        // 這裡會委託給具體的計算策略來處理
        return createActivePromotionResult(context, customer, parameters);
    }
    
    @Override
    public StateTransitionResult activate(PromotionContext context) {
        // 已經是活躍狀態，無需轉換
        return StateTransitionResult.failure("優惠已經處於活躍狀態");
    }
    
    @Override
    public StateTransitionResult suspend(PromotionContext context, String reason) {
        // 從活躍狀態轉換到暫停狀態
        SuspendedPromotionState suspendedState = new SuspendedPromotionState(reason);
        return context.transitionTo(suspendedState);
    }
    
    @Override
    public StateTransitionResult expire(PromotionContext context) {
        // 從活躍狀態轉換到過期狀態
        ExpiredPromotionState expiredState = new ExpiredPromotionState();
        return context.transitionTo(expiredState);
    }
    
    @Override
    public boolean canTransitionTo(PromotionState targetState) {
        // 活躍狀態可以轉換到暫停狀態或過期狀態
        return targetState instanceof SuspendedPromotionState || 
               targetState instanceof ExpiredPromotionState;
    }
    
    @Override
    public boolean isValid(PromotionContext context) {
        // 檢查優惠是否仍在有效期內
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        if (validUntil != null) {
            return LocalDateTime.now().isBefore(validUntil);
        }
        
        // 如果沒有設定有效期，預設為有效
        return true;
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
    
    @Override
    public boolean isTerminal() {
        return false;
    }
    
    @Override
    public void onEnter(PromotionContext context, PromotionState previousState) {
        super.onEnter(context, previousState);
        
        // 記錄啟用時間
        context.setProperty("activatedAt", LocalDateTime.now());
        
        // 如果是從暫停狀態恢復，記錄恢復時間
        if (previousState instanceof SuspendedPromotionState) {
            context.setProperty("resumedAt", LocalDateTime.now());
            context.addStateChangeEvent("優惠從暫停狀態恢復為活躍狀態", LocalDateTime.now());
        }
    }
    
    @Override
    public void onExit(PromotionContext context, PromotionState nextState) {
        super.onExit(context, nextState);
        
        // 記錄離開活躍狀態的時間
        context.setProperty("lastActiveAt", LocalDateTime.now());
        
        // 根據目標狀態記錄不同的事件
        if (nextState instanceof SuspendedPromotionState) {
            context.addStateChangeEvent("優惠從活躍狀態轉為暫停狀態", LocalDateTime.now());
        } else if (nextState instanceof ExpiredPromotionState) {
            context.addStateChangeEvent("優惠從活躍狀態轉為過期狀態", LocalDateTime.now());
        }
    }
    
    private PromotionResult createActivePromotionResult(PromotionContext context, CustomerProfile customer, Map<String, Object> parameters) {
        // 創建一個基本的活躍優惠結果
        // 實際實作中會根據具體的計算策略來生成結果
        
        String promotionId = context.getPromotionId();
        String promotionName = context.getPromotionName();
        String promotionType = context.getPromotionType();
        
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        if (validUntil == null) {
            validUntil = LocalDateTime.now().plusDays(30); // 預設30天有效期
        }
        
        Map<String, Object> additionalDetails = Map.of(
            "state", STATE_NAME,
            "customerId", customer.getCustomerId(),
            "evaluationTimestamp", LocalDateTime.now(),
            "contextProperties", context.getAllProperties()
        );
        
        return new PromotionResult(
            promotionId,
            promotionName,
            promotionType,
            null, // 折扣金額需要由具體策略計算
            null, // 折扣百分比需要由具體策略計算
            "優惠處於活躍狀態，可正常使用",
            validUntil,
            additionalDetails,
            true
        );
    }
    
    private PromotionResult createExpiredPromotionResult(PromotionContext context, CustomerProfile customer) {
        String promotionId = context.getPromotionId();
        String promotionName = context.getPromotionName();
        String promotionType = context.getPromotionType();
        
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        
        Map<String, Object> additionalDetails = Map.of(
            "state", "EXPIRED",
            "customerId", customer.getCustomerId(),
            "evaluationTimestamp", LocalDateTime.now(),
            "expiredAt", validUntil != null ? validUntil : LocalDateTime.now()
        );
        
        return new PromotionResult(
            promotionId,
            promotionName,
            promotionType,
            null,
            null,
            "優惠已過期，無法使用",
            validUntil,
            additionalDetails,
            false
        );
    }
}