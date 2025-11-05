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

class SuspendedPromotionStateTest {
    
    private SuspendedPromotionState suspendedState;
    private PromotionContext context;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        suspendedState = new SuspendedPromotionState("系統維護");
        context = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", suspendedState);
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(1000000), 750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @Test
    void shouldHaveCorrectStateProperties() {
        // Then
        assertThat(suspendedState.getStateName()).isEqualTo("SUSPENDED");
        assertThat(suspendedState.getDescription()).isEqualTo("優惠暫停狀態，暫時無法提供優惠服務");
        assertThat(suspendedState.isActive()).isFalse();
        assertThat(suspendedState.isTerminal()).isFalse();
        assertThat(suspendedState.getSuspensionReason()).isEqualTo("系統維護");
        assertThat(suspendedState.getSuspendedAt()).isNotNull();
    }
    
    @Test
    void shouldCreateWithDefaultReasonWhenNoneProvided() {
        // Given
        SuspendedPromotionState defaultState = new SuspendedPromotionState();
        
        // Then
        assertThat(defaultState.getSuspensionReason()).isEqualTo("系統暫停");
    }
    
    @Test
    void shouldCreateWithNullReasonHandling() {
        // Given
        SuspendedPromotionState nullReasonState = new SuspendedPromotionState(null);
        
        // Then
        assertThat(nullReasonState.getSuspensionReason()).isEqualTo("未指定原因");
    }
    
    @Test
    void shouldReturnSuspendedPromotionResult() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = suspendedState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPromotionId()).isEqualTo("PROMO001");
        assertThat(result.getPromotionName()).isEqualTo("測試優惠");
        assertThat(result.getPromotionType()).isEqualTo("TEST_PROMOTION");
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDescription()).contains("優惠已暫停");
        assertThat(result.getDescription()).contains("系統維護");
    }
    
    @Test
    void shouldTransitionToActiveWhenValid() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        
        // When
        StateTransitionResult result = suspendedState.activate(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ActivePromotionState.class);
    }
    
    @Test
    void shouldTransitionToExpiredWhenActivatingExpiredPromotion() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().minusDays(1));
        
        // When
        StateTransitionResult result = suspendedState.activate(context);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已過期");
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldUpdateSuspensionReasonWhenSuspendingAgain() {
        // When
        StateTransitionResult result = suspendedState.suspend(context, "新的暫停原因");
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("已更新暫停原因");
        assertThat(context.getProperty("suspensionReason")).isEqualTo("新的暫停原因");
    }
    
    @Test
    void shouldFailToSuspendWithSameReason() {
        // When
        StateTransitionResult result = suspendedState.suspend(context, "系統維護");
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已經處於暫停狀態");
    }
    
    @Test
    void shouldTransitionToExpiredState() {
        // When
        StateTransitionResult result = suspendedState.expire(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldAllowTransitionToActiveAndExpired() {
        // Then
        assertThat(suspendedState.canTransitionTo(new ActivePromotionState())).isTrue();
        assertThat(suspendedState.canTransitionTo(new ExpiredPromotionState())).isTrue();
        assertThat(suspendedState.canTransitionTo(new SuspendedPromotionState())).isFalse();
    }
    
    @Test
    void shouldBeValidWhenWithinValidPeriod() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        
        // When & Then
        assertThat(suspendedState.isValid(context)).isTrue();
    }
    
    @Test
    void shouldBeInvalidWhenExpired() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().minusDays(1));
        
        // When & Then
        assertThat(suspendedState.isValid(context)).isFalse();
    }
    
    @Test
    void shouldBeInvalidWhenExceedsMaxSuspensionPeriod() {
        // Given
        context.setProperty("maxSuspensionDays", 7);
        
        // Create a suspended state that was suspended 8 days ago
        SuspendedPromotionState oldSuspendedState = new SuspendedPromotionState("測試") {
            @Override
            public LocalDateTime getSuspendedAt() {
                return LocalDateTime.now().minusDays(8);
            }
        };
        
        PromotionContext testContext = new PromotionContext("TEST", "測試", "TEST", oldSuspendedState);
        testContext.setProperty("maxSuspensionDays", 7);
        
        // When & Then
        assertThat(oldSuspendedState.isValid(testContext)).isFalse();
    }
    
    @Test
    void shouldBeValidWhenNoValidUntilSet() {
        // Given - No validUntil property set
        
        // When & Then
        assertThat(suspendedState.isValid(context)).isTrue();
    }
    
    @Test
    void shouldSetSuspensionPropertiesOnEnter() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new ActivePromotionState());
        SuspendedPromotionState newSuspendedState = new SuspendedPromotionState("測試暫停");
        
        // When
        newContext.transitionTo(newSuspendedState);
        
        // Then
        assertThat(newContext.getProperty("suspendedAt")).isNotNull();
        assertThat(newContext.getProperty("suspensionReason")).isEqualTo("測試暫停");
        assertThat(newContext.getProperty("previousStateBeforeSuspension")).isEqualTo("ACTIVE");
    }
    
    @Test
    void shouldCalculateSuspensionDurationOnExit() {
        // Given
        ActivePromotionState activeState = new ActivePromotionState();
        
        // When
        context.transitionTo(activeState);
        
        // Then
        assertThat(context.getProperty("suspensionEndedAt")).isNotNull();
        assertThat(context.getProperty("suspensionDurationHours")).isNotNull();
        
        Long durationHours = context.getTypedProperty("suspensionDurationHours", Long.class);
        assertThat(durationHours).isGreaterThanOrEqualTo(0L);
    }
    
    @Test
    void shouldIncludeSuspensionDetailsInPromotionResult() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = suspendedState.evaluate(context, testCustomer, parameters);
        
        // Then
        Map<String, Object> additionalDetails = result.getAdditionalDetails();
        assertThat(additionalDetails).containsEntry("state", "SUSPENDED");
        assertThat(additionalDetails).containsEntry("suspensionReason", "系統維護");
        assertThat(additionalDetails).containsKey("suspendedAt");
        assertThat(additionalDetails).containsEntry("customerId", "CUST001");
    }
    
    @Test
    void shouldFailToActivateWhenExceedsMaxSuspensionPeriod() {
        // Given
        context.setProperty("maxSuspensionDays", 1);
        
        // Create a suspended state that was suspended 2 days ago
        SuspendedPromotionState oldSuspendedState = new SuspendedPromotionState("測試") {
            @Override
            public LocalDateTime getSuspendedAt() {
                return LocalDateTime.now().minusDays(2);
            }
        };
        
        PromotionContext testContext = new PromotionContext("TEST", "測試", "TEST", oldSuspendedState);
        testContext.setProperty("maxSuspensionDays", 1);
        
        // When
        StateTransitionResult result = oldSuspendedState.activate(testContext);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(testContext.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
}