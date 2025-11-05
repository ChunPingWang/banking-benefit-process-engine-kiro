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

class ActivePromotionStateTest {
    
    private ActivePromotionState activeState;
    private PromotionContext context;
    private CustomerProfile testCustomer;
    
    @BeforeEach
    void setUp() {
        activeState = new ActivePromotionState();
        context = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", activeState);
        
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(1000000), 750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @Test
    void shouldHaveCorrectStateProperties() {
        // Then
        assertThat(activeState.getStateName()).isEqualTo("ACTIVE");
        assertThat(activeState.getDescription()).isEqualTo("優惠活躍狀態，可正常提供優惠服務");
        assertThat(activeState.isActive()).isTrue();
        assertThat(activeState.isTerminal()).isFalse();
    }
    
    @Test
    void shouldEvaluatePromotionWhenValid() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = activeState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPromotionId()).isEqualTo("PROMO001");
        assertThat(result.getPromotionName()).isEqualTo("測試優惠");
        assertThat(result.getPromotionType()).isEqualTo("TEST_PROMOTION");
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDescription()).contains("優惠處於活躍狀態");
    }
    
    @Test
    void shouldAutoExpireWhenInvalid() {
        // Given - Set promotion as expired
        context.setProperty("validUntil", LocalDateTime.now().minusDays(1));
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = activeState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDescription()).contains("優惠已過期");
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldFailToActivateWhenAlreadyActive() {
        // When
        StateTransitionResult result = activeState.activate(context);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠已經處於活躍狀態");
    }
    
    @Test
    void shouldTransitionToSuspendedState() {
        // When
        StateTransitionResult result = activeState.suspend(context, "系統維護");
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(SuspendedPromotionState.class);
    }
    
    @Test
    void shouldTransitionToExpiredState() {
        // When
        StateTransitionResult result = activeState.expire(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldAllowTransitionToSuspendedAndExpired() {
        // Then
        assertThat(activeState.canTransitionTo(new SuspendedPromotionState())).isTrue();
        assertThat(activeState.canTransitionTo(new ExpiredPromotionState())).isTrue();
        assertThat(activeState.canTransitionTo(new ActivePromotionState())).isFalse();
    }
    
    @Test
    void shouldBeValidWhenWithinValidPeriod() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        
        // When & Then
        assertThat(activeState.isValid(context)).isTrue();
    }
    
    @Test
    void shouldBeInvalidWhenExpired() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().minusDays(1));
        
        // When & Then
        assertThat(activeState.isValid(context)).isFalse();
    }
    
    @Test
    void shouldBeValidWhenNoValidUntilSet() {
        // Given - No validUntil property set
        
        // When & Then
        assertThat(activeState.isValid(context)).isTrue();
    }
    
    @Test
    void shouldSetActivatedAtOnEnter() {
        // Given
        PromotionContext newContext = new PromotionContext("PROMO002", "新優惠", "NEW_PROMOTION", new SuspendedPromotionState());
        ActivePromotionState newActiveState = new ActivePromotionState();
        
        // When
        newContext.transitionTo(newActiveState);
        
        // Then
        assertThat(newContext.getProperty("activatedAt")).isNotNull();
        assertThat(newContext.getProperty("activatedAt")).isInstanceOf(LocalDateTime.class);
    }
    
    @Test
    void shouldSetResumedAtWhenComingFromSuspended() {
        // Given
        SuspendedPromotionState suspendedState = new SuspendedPromotionState("測試暫停");
        context.transitionTo(suspendedState);
        
        ActivePromotionState newActiveState = new ActivePromotionState();
        
        // When
        context.transitionTo(newActiveState);
        
        // Then
        assertThat(context.getProperty("resumedAt")).isNotNull();
        assertThat(context.getProperty("resumedAt")).isInstanceOf(LocalDateTime.class);
    }
    
    @Test
    void shouldSetLastActiveAtOnExit() {
        // Given
        LocalDateTime beforeTransition = LocalDateTime.now();
        
        // When
        context.transitionTo(new SuspendedPromotionState("測試"));
        
        // Then
        LocalDateTime lastActiveAt = context.getTypedProperty("lastActiveAt", LocalDateTime.class);
        assertThat(lastActiveAt).isNotNull();
        assertThat(lastActiveAt).isAfterOrEqualTo(beforeTransition);
    }
    
    @Test
    void shouldCreatePromotionResultWithDefaultValidUntil() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = activeState.evaluate(context, testCustomer, parameters);
        
        // Then
        assertThat(result.getValidUntil()).isNotNull();
        assertThat(result.getValidUntil()).isAfter(LocalDateTime.now().plusDays(29)); // Should be ~30 days
    }
    
    @Test
    void shouldIncludeContextPropertiesInPromotionResult() {
        // Given
        context.setProperty("customProperty", "customValue");
        context.setProperty("maxUsage", 100);
        Map<String, Object> parameters = new HashMap<>();
        
        // When
        PromotionResult result = activeState.evaluate(context, testCustomer, parameters);
        
        // Then
        Map<String, Object> additionalDetails = result.getAdditionalDetails();
        assertThat(additionalDetails).containsKey("contextProperties");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contextProperties = (Map<String, Object>) additionalDetails.get("contextProperties");
        assertThat(contextProperties).containsEntry("customProperty", "customValue");
        assertThat(contextProperties).containsEntry("maxUsage", 100);
    }
}