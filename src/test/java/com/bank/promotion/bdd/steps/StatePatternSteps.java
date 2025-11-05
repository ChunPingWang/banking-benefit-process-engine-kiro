package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.state.ActivePromotionState;
import com.bank.promotion.domain.state.ExpiredPromotionState;
import com.bank.promotion.domain.state.PromotionContext;
import com.bank.promotion.domain.state.PromotionStateManager;
import com.bank.promotion.domain.state.StateTransitionResult;
import com.bank.promotion.domain.state.SuspendedPromotionState;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 狀態模式 BDD 步驟定義
 */
public class StatePatternSteps extends BaseStepDefinitions {
    
    @Autowired
    private PromotionStateManager stateManager;
    
    private PromotionContext promotionContext;
    private CustomerProfile testCustomer;
    private StateTransitionResult transitionResult;
    private PromotionResult evaluationResult;
    private Exception lastException;
    
    @假設("系統建立了一個 {string} 狀態的優惠")
    public void 系統建立了一個狀態的優惠(String initialState) {
        var state = stateManager.createState(initialState);
        promotionContext = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", state);
        
        recordSystemEvent("PROMOTION_CREATED", "SETUP",
            Map.of("promotionId", "PROMO001", "initialState", initialState),
            "INFO", "PromotionStateManager");
    }
    
    @而且("優惠有效期限設定為 {int} 天後")
    public void 優惠有效期限設定為天後(int days) {
        LocalDateTime validUntil = LocalDateTime.now().plusDays(days);
        promotionContext.setProperty("validUntil", validUntil);
        
        recordSystemEvent("PROMOTION_VALIDITY_SET", "CONFIGURATION",
            Map.of("promotionId", promotionContext.getPromotionId(), "validUntil", validUntil),
            "INFO", "PromotionContext");
    }
    
    @而且("優惠有效期限設定為 {int} 天前")
    public void 優惠有效期限設定為天前(int days) {
        LocalDateTime validUntil = LocalDateTime.now().minusDays(days);
        promotionContext.setProperty("validUntil", validUntil);
        
        recordSystemEvent("PROMOTION_VALIDITY_SET", "CONFIGURATION",
            Map.of("promotionId", promotionContext.getPromotionId(), "validUntil", validUntil),
            "INFO", "PromotionContext");
    }
    
    @而且("優惠最大暫停期限設定為 {int} 天")
    public void 優惠最大暫停期限設定為天(int maxSuspensionDays) {
        promotionContext.setProperty("maxSuspensionDays", maxSuspensionDays);
    }
    
    @而且("測試客戶資料已準備")
    public void 測試客戶資料已準備() {
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(1000000), 750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @當("嘗試啟用優惠")
    public void 嘗試啟用優惠() {
        try {
            transitionResult = stateManager.activatePromotion(promotionContext);
            recordSystemEvent("PROMOTION_ACTIVATION_ATTEMPT", "STATE_TRANSITION",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "currentState", promotionContext.getCurrentState().getStateName(),
                       "result", transitionResult.isSuccess()),
                "INFO", "PromotionStateManager");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("嘗試暫停優惠，原因為 {string}")
    public void 嘗試暫停優惠原因為(String reason) {
        try {
            transitionResult = stateManager.suspendPromotion(promotionContext, reason);
            recordSystemEvent("PROMOTION_SUSPENSION_ATTEMPT", "STATE_TRANSITION",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "currentState", promotionContext.getCurrentState().getStateName(),
                       "reason", reason,
                       "result", transitionResult.isSuccess()),
                "INFO", "PromotionStateManager");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("嘗試使優惠過期")
    public void 嘗試使優惠過期() {
        try {
            transitionResult = stateManager.expirePromotion(promotionContext);
            recordSystemEvent("PROMOTION_EXPIRATION_ATTEMPT", "STATE_TRANSITION",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "currentState", promotionContext.getCurrentState().getStateName(),
                       "result", transitionResult.isSuccess()),
                "INFO", "PromotionStateManager");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("系統自動檢查優惠過期狀態")
    public void 系統自動檢查優惠過期狀態() {
        boolean handled = stateManager.checkAndHandleExpiration(promotionContext);
        recordSystemEvent("AUTOMATIC_EXPIRATION_CHECK", "SYSTEM_CHECK",
            Map.of("promotionId", promotionContext.getPromotionId(),
                   "handled", handled,
                   "currentState", promotionContext.getCurrentState().getStateName()),
            "INFO", "PromotionStateManager");
    }
    
    @當("客戶請求優惠評估")
    public void 客戶請求優惠評估() {
        try {
            Map<String, Object> parameters = new HashMap<>();
            evaluationResult = promotionContext.getCurrentState().evaluate(promotionContext, testCustomer, parameters);
            
            recordSystemEvent("PROMOTION_EVALUATION", "BUSINESS_LOGIC",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "customerId", testCustomer.getCustomerId(),
                       "currentState", promotionContext.getCurrentState().getStateName(),
                       "isEligible", evaluationResult.isEligible()),
                "INFO", "PromotionState");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @那麼("狀態轉換應該成功")
    public void 狀態轉換應該成功() {
        assertNotNull(transitionResult, "轉換結果不應該為空");
        assertTrue(transitionResult.isSuccess(), 
            "狀態轉換應該成功，失敗原因: " + transitionResult.getMessage());
    }
    
    @那麼("狀態轉換應該失敗")
    public void 狀態轉換應該失敗() {
        assertNotNull(transitionResult, "轉換結果不應該為空");
        assertFalse(transitionResult.isSuccess(), "狀態轉換應該失敗");
    }
    
    @而且("轉換失敗訊息應該包含 {string}")
    public void 轉換失敗訊息應該包含(String expectedMessage) {
        assertNotNull(transitionResult, "轉換結果不應該為空");
        assertNotNull(transitionResult.getMessage(), "轉換訊息不應該為空");
        assertTrue(transitionResult.getMessage().contains(expectedMessage),
            "轉換訊息應該包含 '" + expectedMessage + "'，實際訊息: " + transitionResult.getMessage());
    }
    
    @而且("優惠當前狀態應該是 {string}")
    public void 優惠當前狀態應該是(String expectedState) {
        assertEquals(expectedState, promotionContext.getCurrentState().getStateName(),
            "優惠狀態應該是 " + expectedState);
    }
    
    @而且("優惠應該標示為活躍")
    public void 優惠應該標示為活躍() {
        assertTrue(promotionContext.getCurrentState().isActive(), "優惠應該標示為活躍");
    }
    
    @而且("優惠應該標示為非活躍")
    public void 優惠應該標示為非活躍() {
        assertFalse(promotionContext.getCurrentState().isActive(), "優惠應該標示為非活躍");
    }
    
    @而且("優惠應該標示為終止狀態")
    public void 優惠應該標示為終止狀態() {
        assertTrue(promotionContext.getCurrentState().isTerminal(), "優惠應該標示為終止狀態");
    }
    
    @而且("優惠應該標示為非終止狀態")
    public void 優惠應該標示為非終止狀態() {
        assertFalse(promotionContext.getCurrentState().isTerminal(), "優惠應該標示為非終止狀態");
    }
    
    @而且("優惠評估結果應該符合條件")
    public void 優惠評估結果應該符合條件() {
        assertNotNull(evaluationResult, "評估結果不應該為空");
        assertTrue(evaluationResult.isEligible(), "優惠評估結果應該符合條件");
    }
    
    @而且("優惠評估結果應該不符合條件")
    public void 優惠評估結果應該不符合條件() {
        assertNotNull(evaluationResult, "評估結果不應該為空");
        assertFalse(evaluationResult.isEligible(), "優惠評估結果應該不符合條件");
    }
    
    @而且("評估結果描述應該包含 {string}")
    public void 評估結果描述應該包含(String expectedText) {
        assertNotNull(evaluationResult, "評估結果不應該為空");
        assertNotNull(evaluationResult.getDescription(), "評估描述不應該為空");
        assertTrue(evaluationResult.getDescription().contains(expectedText),
            "評估描述應該包含 '" + expectedText + "'，實際描述: " + evaluationResult.getDescription());
    }
    
    @而且("優惠上下文應該記錄暫停原因")
    public void 優惠上下文應該記錄暫停原因() {
        String suspensionReason = promotionContext.getTypedProperty("suspensionReason", String.class);
        assertNotNull(suspensionReason, "暫停原因應該已記錄");
    }
    
    @而且("優惠上下文應該記錄暫停時間")
    public void 優惠上下文應該記錄暫停時間() {
        LocalDateTime suspendedAt = promotionContext.getTypedProperty("suspendedAt", LocalDateTime.class);
        assertNotNull(suspendedAt, "暫停時間應該已記錄");
    }
    
    @而且("優惠上下文應該記錄過期時間")
    public void 優惠上下文應該記錄過期時間() {
        LocalDateTime expiredAt = promotionContext.getTypedProperty("expiredAt", LocalDateTime.class);
        assertNotNull(expiredAt, "過期時間應該已記錄");
    }
    
    @而且("優惠上下文應該記錄生命週期天數")
    public void 優惠上下文應該記錄生命週期天數() {
        Long lifetimeDays = promotionContext.getTypedProperty("lifetimeDays", Long.class);
        assertNotNull(lifetimeDays, "生命週期天數應該已記錄");
        assertTrue(lifetimeDays >= 0, "生命週期天數應該為非負數");
    }
    
    @而且("狀態變更歷史應該包含 {int} 個事件")
    public void 狀態變更歷史應該包含個事件(int expectedCount) {
        var stateChangeHistory = promotionContext.getStateChangeHistory();
        assertTrue(stateChangeHistory.size() >= expectedCount,
            "狀態變更歷史應該至少包含 " + expectedCount + " 個事件，實際: " + stateChangeHistory.size());
    }
    
    @而且("狀態變更歷史應該包含 {string} 事件")
    public void 狀態變更歷史應該包含事件(String expectedEventDescription) {
        var stateChangeHistory = promotionContext.getStateChangeHistory();
        boolean foundEvent = stateChangeHistory.stream()
            .anyMatch(event -> event.getDescription().contains(expectedEventDescription));
        assertTrue(foundEvent, 
            "狀態變更歷史應該包含包含 '" + expectedEventDescription + "' 的事件");
    }
    
    @當("手動轉換優惠狀態到 {string}")
    public void 手動轉換優惠狀態到(String targetState) {
        try {
            transitionResult = stateManager.transitionTo(promotionContext, targetState);
            recordSystemEvent("MANUAL_STATE_TRANSITION", "STATE_TRANSITION",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "fromState", promotionContext.getCurrentState().getStateName(),
                       "toState", targetState,
                       "result", transitionResult.isSuccess()),
                "INFO", "PromotionStateManager");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("手動轉換優惠狀態到 {string}，參數為 {string}")
    public void 手動轉換優惠狀態到參數為(String targetState, String parameter) {
        try {
            transitionResult = stateManager.transitionTo(promotionContext, targetState, parameter);
            recordSystemEvent("MANUAL_STATE_TRANSITION_WITH_PARAM", "STATE_TRANSITION",
                Map.of("promotionId", promotionContext.getPromotionId(),
                       "fromState", promotionContext.getCurrentState().getStateName(),
                       "toState", targetState,
                       "parameter", parameter,
                       "result", transitionResult.isSuccess()),
                "INFO", "PromotionStateManager");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @當("查詢優惠狀態統計資訊")
    public void 查詢優惠狀態統計資訊() {
        Map<String, Object> statistics = stateManager.getStateStatistics(promotionContext);
        recordSystemEvent("STATE_STATISTICS_QUERY", "QUERY",
            Map.of("promotionId", promotionContext.getPromotionId(), "statistics", statistics),
            "INFO", "PromotionStateManager");
    }
    
    @那麼("統計資訊應該包含當前狀態")
    public void 統計資訊應該包含當前狀態() {
        Map<String, Object> statistics = stateManager.getStateStatistics(promotionContext);
        assertNotNull(statistics.get("currentState"), "統計資訊應該包含當前狀態");
        assertEquals(promotionContext.getCurrentState().getStateName(), 
                    statistics.get("currentState"), "統計資訊中的當前狀態應該正確");
    }
    
    @而且("統計資訊應該包含狀態變更次數")
    public void 統計資訊應該包含狀態變更次數() {
        Map<String, Object> statistics = stateManager.getStateStatistics(promotionContext);
        assertNotNull(statistics.get("stateChangeCount"), "統計資訊應該包含狀態變更次數");
        assertTrue((Integer) statistics.get("stateChangeCount") > 0, "狀態變更次數應該大於0");
    }
    
    @而且("統計資訊應該包含創建時間和更新時間")
    public void 統計資訊應該包含創建時間和更新時間() {
        Map<String, Object> statistics = stateManager.getStateStatistics(promotionContext);
        assertNotNull(statistics.get("createdAt"), "統計資訊應該包含創建時間");
        assertNotNull(statistics.get("updatedAt"), "統計資訊應該包含更新時間");
    }
}