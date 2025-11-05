package com.bank.promotion.domain.state;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.valueobject.PromotionResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 暫停優惠狀態
 * 優惠被暫時停用，不提供優惠服務，但可以重新啟用
 */
public class SuspendedPromotionState extends PromotionState {
    
    public static final String STATE_NAME = "SUSPENDED";
    
    private final String suspensionReason;
    private final LocalDateTime suspendedAt;
    
    public SuspendedPromotionState() {
        this("系統暫停");
    }
    
    public SuspendedPromotionState(String suspensionReason) {
        super(STATE_NAME, "優惠暫停狀態，暫時無法提供優惠服務");
        this.suspensionReason = suspensionReason != null ? suspensionReason : "未指定原因";
        this.suspendedAt = LocalDateTime.now();
    }
    
    @Override
    public PromotionResult evaluate(PromotionContext context, CustomerProfile customer, Map<String, Object> parameters) {
        // 在暫停狀態下，不提供優惠服務
        return createSuspendedPromotionResult(context, customer);
    }
    
    @Override
    public StateTransitionResult activate(PromotionContext context) {
        // 從暫停狀態轉換回活躍狀態
        
        // 檢查優惠是否已過期
        if (!isStillValidForActivation(context)) {
            // 如果已過期，直接轉換到過期狀態
            ExpiredPromotionState expiredState = new ExpiredPromotionState();
            StateTransitionResult expiredResult = context.transitionTo(expiredState);
            if (expiredResult.isSuccess()) {
                return StateTransitionResult.failure("優惠已過期，無法重新啟用，已自動轉換為過期狀態");
            } else {
                return StateTransitionResult.failure("優惠已過期且狀態轉換失敗");
            }
        }
        
        ActivePromotionState activeState = new ActivePromotionState();
        return context.transitionTo(activeState);
    }
    
    @Override
    public StateTransitionResult suspend(PromotionContext context, String reason) {
        // 已經是暫停狀態
        if (reason != null && !reason.equals(this.suspensionReason)) {
            // 更新暫停原因
            context.setProperty("suspensionReason", reason);
            context.addStateChangeEvent("更新暫停原因: " + reason, LocalDateTime.now());
            return StateTransitionResult.success("已更新暫停原因");
        }
        
        return StateTransitionResult.failure("優惠已經處於暫停狀態");
    }
    
    @Override
    public StateTransitionResult expire(PromotionContext context) {
        // 從暫停狀態轉換到過期狀態
        ExpiredPromotionState expiredState = new ExpiredPromotionState();
        return context.transitionTo(expiredState);
    }
    
    @Override
    public boolean canTransitionTo(PromotionState targetState) {
        // 暫停狀態可以轉換到活躍狀態或過期狀態
        return targetState instanceof ActivePromotionState || 
               targetState instanceof ExpiredPromotionState;
    }
    
    @Override
    public boolean isValid(PromotionContext context) {
        // 暫停狀態下，優惠本身可能仍然有效（只是被暫停）
        // 檢查是否超過最大暫停期限
        Integer maxSuspensionDays = context.getTypedProperty("maxSuspensionDays", Integer.class);
        if (maxSuspensionDays != null && maxSuspensionDays > 0) {
            LocalDateTime maxSuspensionUntil = suspendedAt.plusDays(maxSuspensionDays);
            return LocalDateTime.now().isBefore(maxSuspensionUntil);
        }
        
        // 檢查優惠本身的有效期
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        if (validUntil != null) {
            return LocalDateTime.now().isBefore(validUntil);
        }
        
        return true;
    }
    
    @Override
    public boolean isActive() {
        return false;
    }
    
    @Override
    public boolean isTerminal() {
        return false;
    }
    
    @Override
    public void onEnter(PromotionContext context, PromotionState previousState) {
        super.onEnter(context, previousState);
        
        // 記錄暫停相關資訊
        context.setProperty("suspendedAt", suspendedAt);
        context.setProperty("suspensionReason", suspensionReason);
        context.setProperty("previousStateBeforeSuspension", previousState.getStateName());
        
        context.addStateChangeEvent(
            String.format("優惠被暫停，原因: %s", suspensionReason), 
            suspendedAt
        );
    }
    
    @Override
    public void onExit(PromotionContext context, PromotionState nextState) {
        super.onExit(context, nextState);
        
        // 記錄暫停結束時間
        LocalDateTime suspensionEndedAt = LocalDateTime.now();
        context.setProperty("suspensionEndedAt", suspensionEndedAt);
        
        // 計算暫停持續時間
        long suspensionDurationHours = java.time.Duration.between(suspendedAt, suspensionEndedAt).toHours();
        context.setProperty("suspensionDurationHours", suspensionDurationHours);
        
        if (nextState instanceof ActivePromotionState) {
            context.addStateChangeEvent(
                String.format("優惠暫停結束，重新啟用（暫停時長: %d 小時）", suspensionDurationHours), 
                suspensionEndedAt
            );
        } else if (nextState instanceof ExpiredPromotionState) {
            context.addStateChangeEvent("優惠從暫停狀態直接轉為過期狀態", suspensionEndedAt);
        }
    }
    
    private boolean isStillValidForActivation(PromotionContext context) {
        // 檢查優惠本身是否仍在有效期內
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        if (validUntil != null && LocalDateTime.now().isAfter(validUntil)) {
            return false;
        }
        
        // 檢查是否超過最大暫停期限
        Integer maxSuspensionDays = context.getTypedProperty("maxSuspensionDays", Integer.class);
        if (maxSuspensionDays != null && maxSuspensionDays > 0) {
            LocalDateTime maxSuspensionUntil = suspendedAt.plusDays(maxSuspensionDays);
            if (LocalDateTime.now().isAfter(maxSuspensionUntil)) {
                return false;
            }
        }
        
        return true;
    }
    
    private PromotionResult createSuspendedPromotionResult(PromotionContext context, CustomerProfile customer) {
        String promotionId = context.getPromotionId();
        String promotionName = context.getPromotionName();
        String promotionType = context.getPromotionType();
        
        LocalDateTime validUntil = context.getTypedProperty("validUntil", LocalDateTime.class);
        
        Map<String, Object> additionalDetails = Map.of(
            "state", STATE_NAME,
            "customerId", customer.getCustomerId(),
            "evaluationTimestamp", LocalDateTime.now(),
            "suspensionReason", suspensionReason,
            "suspendedAt", suspendedAt,
            "contextProperties", context.getAllProperties()
        );
        
        String description = String.format("優惠已暫停，暫停原因: %s，暫停時間: %s", 
                                         suspensionReason, 
                                         suspendedAt.toString());
        
        return new PromotionResult(
            promotionId,
            promotionName,
            promotionType,
            null,
            null,
            description,
            validUntil,
            additionalDetails,
            false
        );
    }
    
    public String getSuspensionReason() {
        return suspensionReason;
    }
    
    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }
}