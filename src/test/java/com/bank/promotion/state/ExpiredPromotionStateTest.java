package com.bank.promotion.state;

import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.state.ActivePromotionState;
import com.bank.promotion.domain.state.ExpiredPromotionState;
import com.bank.promotion.domain.state.PromotionContext;
import com.bank.promotion.domain.state.StateTransitionResult;
import com.bank.promotion.domain.state.SuspendedPromotionState;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ExpiredPromotionStateTest {
    
    private ExpiredPromotionState expiredState;
    private PromotionContext context;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        expiredState = new ExpiredPromotionState();
        context = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", expiredState);
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(1000000), 750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @Test
    void shouldHaveCorrectStateProperties() {
        // Then
        assertThat(expiredState.getStateName()).isEqualTo("EXPIRED");
        assertThat(expiredState.getDescription()).isEqualTo("優惠已過期，無法再提供優惠服務");
        assertThat(expiredState.isActive()).isFalse();
        assertThat(expiredState.isTerminal()).isTrue();
        assertThat(expiredState.getExpiredAt()).isNotNull();
    }
    
    @Test
    void shouldCreateWithSpecificExpiredTime() {
        // Given
        LocalDateTime specificTime = LocalDateTime.now().minusDays(1);
        ExpiredPromotionState specificExpiredState = new ExpiredPromotionState(specificTime);
        
        // Then
        assertThat(specificExpiredState.getExpiredAt()).isEqualTo(specificTime);
    }
    
    @Test
    void shouldCreateWithCurrentTimeWhenNullProvided() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        ExpiredPromotionState nullTimeState = new ExpiredPromotionState(null);
        LocalDateTime afterCreation = LocalDateTime.now();
        
        // Then
        assertThat(nullTimeState.getExpiredAt()).isBetween(beforeCreation, afterCreation);
    }
    
    @Test
    void shouldReturnExpiredPromotionResult() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = expiredState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPromotionId()).isEqualTo("PROMO001");
        assertThat(result.getPromotionName()).isEqualTo("測試優惠");
        assertThat(result.getPromotionType()).isEqualTo("TEST_PROMOTION");
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDescription()).contains("優惠已於");
        assertThat(result.getDescription()).contains("過期");
    }
    
    @Test
    void shouldFailToActivate() {
        // When
        StateTransitionResult result = expiredState.activate(context);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已過期，無法重新啟用");
    }
    
    @Test
    void shouldFailToSuspend() {
        // When
        StateTransitionResult result = expiredState.suspend(context, "測試原因");
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已過期，無法執行暫停操作");
    }
    
    @Test
    void shouldFailToExpireAgain() {
        // When
        StateTransitionResult result = expiredState.expire(context);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已經處於過期狀態");
    }
    
    @Test
    void shouldNotAllowAnyTransitions() {
        // Then
        assertThat(expiredState.canTransitionTo(new ActivePromotionState())).isFalse();
        assertThat(expiredState.canTransitionTo(new SuspendedPromotionState())).isFalse();
        assertThat(expiredState.canTransitionTo(new ExpiredPromotionState())).isFalse();
    }
    
    @Test
    void shouldAlwaysBeInvalid() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        
        // When & Then
        assertThat(expiredState.isValid(context)).isFalse();
    }
    
    @Test
    void shouldSetExpiredPropertiesOnEnter() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new ActivePromotionState());
        ExpiredPromotionState newExpiredState = new ExpiredPromotionState();
        
        // When
        newContext.transitionTo(newExpiredState);
        
        // Then
        assertThat(newContext.getProperty("expiredAt")).isNotNull();
        assertThat(newContext.getProperty("previousStateBeforeExpiration")).isEqualTo("ACTIVE");
        assertThat(newContext.getProperty("isTerminalState")).isEqualTo(true);
        assertThat(newContext.getProperty("canBeReactivated")).isEqualTo(false);
    }
    
    @Test
    void shouldCalculateLifetimeOnEnterFromActiveState() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new ActivePromotionState());
        
        // Wait a bit to ensure some lifetime
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ExpiredPromotionState newExpiredState = new ExpiredPromotionState();
        
        // When
        newContext.transitionTo(newExpiredState);
        
        // Then
        Long lifetimeDays = newContext.getTypedProperty("lifetimeDays", Long.class);
        assertThat(lifetimeDays).isNotNull();
        assertThat(lifetimeDays).isGreaterThanOrEqualTo(0L);
    }
    
    @Test
    void shouldSetCorrectTransitionReasonFromActiveState() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new ActivePromotionState());
        ExpiredPromotionState newExpiredState = new ExpiredPromotionState();
        
        // When
        newContext.transitionTo(newExpiredState);
        
        // Then
        var stateChangeHistory = newContext.getStateChangeHistory();
        boolean foundActiveExpiredEvent = stateChangeHistory.stream()
            .anyMatch(event -> event.getDescription().contains("優惠從活躍狀態自然過期"));
        assertThat(foundActiveExpiredEvent).isTrue();
    }
    
    @Test
    void shouldSetCorrectTransitionReasonFromSuspendedState() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new SuspendedPromotionState("測試"));
        ExpiredPromotionState newExpiredState = new ExpiredPromotionState();
        
        // When
        newContext.transitionTo(newExpiredState);
        
        // Then
        var stateChangeHistory = newContext.getStateChangeHistory();
        boolean foundSuspendedExpiredEvent = stateChangeHistory.stream()
            .anyMatch(event -> event.getDescription().contains("優惠在暫停狀態期間過期"));
        assertThat(foundSuspendedExpiredEvent).isTrue();
    }
    
    @Test
    void shouldIncludeExpiredDetailsInPromotionResult() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().minusDays(5));
        context.setProperty("lifetimeDays", 30L);
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = expiredState.evaluate(context, testCustomer, parameters);
        
        // Then
        Map<String, Object> additionalDetails = result.getAdditionalDetails();
        assertThat(additionalDetails).containsEntry("state", "EXPIRED");
        assertThat(additionalDetails).containsKey("expiredAt");
        assertThat(additionalDetails).containsKey("originalValidUntil");
        assertThat(additionalDetails).containsEntry("lifetimeDays", 30L);
        assertThat(additionalDetails).containsEntry("customerId", "CUST001");
        
        // Check description includes lifetime information
        assertThat(result.getDescription()).contains("優惠生命週期: 30 天");
    }
    
    @Test
    void shouldIncludeExpiredAtInDescriptionWhenNoLifetime() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = expiredState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result.getDescription()).contains("優惠已於");
        assertThat(result.getDescription()).contains("過期");
        // Should not contain lifetime info when not available
        assertThat(result.getDescription()).doesNotContain("優惠生命週期");
    }
    
    @Test
    void shouldLogWarningOnUnexpectedExit() {
        // Given
        ActivePromotionState unexpectedNextState = new ActivePromotionState();
        
        // When
        expiredState.onExit(context, unexpectedNextState);
        
        // Then
        var stateChangeHistory = context.getStateChangeHistory();
        boolean foundWarningEvent = stateChangeHistory.stream()
            .anyMatch(event -> event.getDescription().contains("警告: 過期狀態嘗試轉換"));
        assertThat(foundWarningEvent).isTrue();
    }
}