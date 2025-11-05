package com.bank.promotion.domain.state;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 過期優惠狀態
 * 優惠已過期，無法再提供優惠服務，這是一個終止狀態
 */
public class ExpiredPromotionState extends PromotionState {
    
    public static final String STATE_NAME = "EXPIRED";
    
    private final LocalDateTime expiredAt;
    
    public ExpiredPromotionState() {
        super(STATE_NAME, "優惠已過期，無法再提供優惠服務");
        this.expiredAt = LocalDateTime.now();
    }
    
    public ExpiredPromotionState(LocalDateTime expiredAt) {
        super(STATE_NAME, "優惠已過期，無法再提供優惠服務");
        this.expiredAt = expiredAt != null ? expiredAt : LocalDateTime.now();
    }
    
    @Override
    public PromotionResult evaluate(PromotionContext context, CustomerProfile customer, Map<String, Object> parameters) {
        // 過期狀態下，不提供任何優惠服務
        return createExpiredPromotionResult(context, customer);
    }
    
    @Override
    public StateTransitionResult activate(PromotionContext context) {
        // 過期狀態無法重新啟用
        return StateTransitionResult.failure("優惠已過期，無法重新啟用");
    }
    
    @Override
    public StateTransitionResult suspend(PromotionContext context, String reason) {
        // 過期狀態無法暫停
        return StateTransitionResult.failure("優惠已過期，無法執行暫停操作");
    }
    
    @Override
    public StateTransitionResult expire(PromotionContext context) {
        // 已經是過期狀態
        return StateTransitionResult.failure("優惠已經處於過期狀態");
    }
    
    @Override
    public boolean canTransitionTo(PromotionState targetState) {
        // 過期狀態是終止狀態，無法轉換到其他狀態
        return false;
    }
    
    @Override
    public boolean isValid(PromotionContext context) {
        // 過期狀態下，優惠無效
        return false;
    }
    
    @Override
    public boolean isActive() {
        return false;
    }
    
    @Override
    public boolean isTerminal() {
        return true;
    }
    
    @Override
    public void onEnter(PromotionContext context, PromotionState previousState) {
        super.onEnter(context, previousState);
        
        // 記錄過期相關資訊
        context.setProperty("expiredAt", expiredAt);
        context.setProperty("previousStateBeforeExpiration", previousState.getStateName());
        
        // 計算優惠的總生命週期
        LocalDateTime createdAt = context.getCreatedAt();
        if (createdAt != null) {
            long lifetimeDays = java.time.Duration.between(createdAt, expiredAt).toDays();
            context.setProperty("lifetimeDays", lifetimeDays);
        }
        
        // 記錄從哪個狀態轉換而來
        String transitionReason;
        if (previousState instanceof ActivePromotionState) {
            transitionReason = "優惠從活躍狀態自然過期";
        } else if (previousState instanceof SuspendedPromotionState) {
            transitionReason = "優惠在暫停狀態期間過期";
        } else {
            transitionReason = "優惠過期";
        }
        
        context.addStateChangeEvent(transitionReason, expiredAt);
        
        // 標記為不可逆轉的狀態變更
        context.setProperty("isTerminalState", true);
        context.setProperty("canBeReactivated", false);
    }
    
    @Override
    public void onExit(PromotionContext context, PromotionState nextState) {
        // 過期狀態是終止狀態，理論上不應該有離開事件
        // 但為了完整性，仍然記錄這個異常情況
        super.onExit(context, nextState);
        
        context.addStateChangeEvent(
            String.format("警告: 過期狀態嘗試轉換到 %s（這不應該發生）", 
                         nextState.getStateName()), 
            LocalDateTime.now()
        );
    }
    
    private PromotionResult createExpiredPromotionResult(PromotionContext context, CustomerProfile customer) {
        String promotionId = context.getPromotionId();
        String promotionName = context.getPromotionName();
        String promotionType = context.getPromotionType();
        
        LocalDateTime originalValidUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        Long lifetimeDays = context.getTypedProperty("lifetimeDays", Long.class);
        
        Map<String, Object> additionalDetails = Map.of(
            "state", STATE_NAME,
            "customerId", customer.getCustomerId(),
            "evaluationTimestamp", LocalDateTime.now(),
            "expiredAt", expiredAt,
            "originalValidUntil", originalValidUntil != null ? originalValidUntil : expiredAt,
            "lifetimeDays", lifetimeDays != null ? lifetimeDays : 0L,
            "contextProperties", context.getAllProperties()
        );
        
        String description = String.format("優惠已於 %s 過期，無法使用", expiredAt.toString());
        if (lifetimeDays != null) {
            description += String.format("（優惠生命週期: %d 天）", lifetimeDays);
        }
        
        return new PromotionResult(
            promotionId,
            promotionName,
            promotionType,
            null,
            null,
            description,
            originalValidUntil,
            additionalDetails,
            false
        );
    }
    
    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}