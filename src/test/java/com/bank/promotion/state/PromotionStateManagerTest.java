package com.bank.promotion.state;

import com.bank.promotion.domain.state.ActivePromotionState;
import com.bank.promotion.domain.state.ExpiredPromotionState;
import com.bank.promotion.domain.state.PromotionContext;
import com.bank.promotion.domain.state.PromotionStateManager;
import com.bank.promotion.domain.state.StateTransitionResult;
import com.bank.promotion.domain.state.SuspendedPromotionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class PromotionStateManagerTest {
    
    private PromotionStateManager stateManager;
    private PromotionContext context;
    
    @BeforeEach
    void setUp() {
        stateManager = new PromotionStateManager();
        context = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", new ActivePromotionState());
    }
    
    @Test
    void shouldCreateActiveState() {
        // When
        var state = stateManager.createState("ACTIVE");
        
        // Then
        assertThat(state).isInstanceOf(ActivePromotionState.class);
        assertThat(state.getStateName()).isEqualTo("ACTIVE");
    }
    
    @Test
    void shouldCreateSuspendedState() {
        // When
        var state = stateManager.createState("SUSPENDED");
        
        // Then
        assertThat(state).isInstanceOf(SuspendedPromotionState.class);
        assertThat(state.getStateName()).isEqualTo("SUSPENDED");
    }
    
    @Test
    void shouldCreateSuspendedStateWithReason() {
        // When
        var state = stateManager.createState("SUSPENDED", "系統維護");
        
        // Then
        assertThat(state).isInstanceOf(SuspendedPromotionState.class);
        SuspendedPromotionState suspendedState = (SuspendedPromotionState) state;
        assertThat(suspendedState.getSuspensionReason()).isEqualTo("系統維護");
    }
    
    @Test
    void shouldCreateExpiredState() {
        // When
        var state = stateManager.createState("EXPIRED");
        
        // Then
        assertThat(state).isInstanceOf(ExpiredPromotionState.class);
        assertThat(state.getStateName()).isEqualTo("EXPIRED");
    }
    
    @Test
    void shouldBeCaseInsensitive() {
        // When & Then
        assertThat(stateManager.createState("active")).isInstanceOf(ActivePromotionState.class);
        assertThat(stateManager.createState("Suspended")).isInstanceOf(SuspendedPromotionState.class);
        assertThat(stateManager.createState("EXPIRED")).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldThrowExceptionForUnknownStateType() {
        // When & Then
        assertThatThrownBy(() -> stateManager.createState("UNKNOWN_STATE"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown state type: UNKNOWN_STATE");
    }
    
    @Test
    void shouldThrowExceptionForNullOrEmptyStateName() {
        // When & Then
        assertThatThrownBy(() -> stateManager.createState(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("State name cannot be null or empty");
        
        assertThatThrownBy(() -> stateManager.createState(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("State name cannot be null or empty");
        
        assertThatThrownBy(() -> stateManager.createState("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("State name cannot be null or empty");
    }
    
    @Test
    void shouldCreateDefaultInitialState() {
        // When
        var state = stateManager.createDefaultInitialState();
        
        // Then
        assertThat(state).isInstanceOf(ActivePromotionState.class);
    }
    
    @Test
    void shouldValidateTransitions() {
        // Given
        var activeState = new ActivePromotionState();
        var suspendedState = new SuspendedPromotionState();
        var expiredState = new ExpiredPromotionState();
        
        // When & Then
        assertThat(stateManager.isValidTransition(activeState, suspendedState)).isTrue();
        assertThat(stateManager.isValidTransition(activeState, expiredState)).isTrue();
        assertThat(stateManager.isValidTransition(suspendedState, activeState)).isTrue();
        assertThat(stateManager.isValidTransition(expiredState, activeState)).isFalse();
        
        // Null checks
        assertThat(stateManager.isValidTransition(null, activeState)).isFalse();
        assertThat(stateManager.isValidTransition(activeState, null)).isFalse();
    }
    
    @Test
    void shouldCheckSupportedStates() {
        // When & Then
        assertThat(stateManager.isStateSupported("ACTIVE")).isTrue();
        assertThat(stateManager.isStateSupported("SUSPENDED")).isTrue();
        assertThat(stateManager.isStateSupported("EXPIRED")).isTrue();
        assertThat(stateManager.isStateSupported("active")).isTrue(); // Case insensitive
        
        assertThat(stateManager.isStateSupported("UNKNOWN")).isFalse();
        assertThat(stateManager.isStateSupported(null)).isFalse();
        assertThat(stateManager.isStateSupported("")).isFalse();
    }
    
    @Test
    void shouldReturnSupportedStateNames() {
        // When
        var supportedStates = stateManager.getSupportedStateNames();
        
        // Then
        assertThat(supportedStates).hasSize(3);
        assertThat(supportedStates).contains("ACTIVE", "SUSPENDED", "EXPIRED");
    }
    
    @Test
    void shouldTransitionToState() {
        // When
        StateTransitionResult result = stateManager.transitionTo(context, "SUSPENDED");
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(SuspendedPromotionState.class);
    }
    
    @Test
    void shouldTransitionToStateWithParameter() {
        // When
        StateTransitionResult result = stateManager.transitionTo(context, "SUSPENDED", "系統維護");
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(SuspendedPromotionState.class);
        
        SuspendedPromotionState suspendedState = (SuspendedPromotionState) context.getCurrentState();
        assertThat(suspendedState.getSuspensionReason()).isEqualTo("系統維護");
    }
    
    @Test
    void shouldFailToTransitionWithNullContext() {
        // When
        StateTransitionResult result = stateManager.transitionTo(null, "SUSPENDED");
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("優惠上下文不能為 null");
    }
    
    @Test
    void shouldFailToTransitionToUnsupportedState() {
        // When
        StateTransitionResult result = stateManager.transitionTo(context, "UNKNOWN_STATE");
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("不支援的狀態類型: UNKNOWN_STATE");
    }
    
    @Test
    void shouldActivatePromotion() {
        // Given
        context.transitionTo(new SuspendedPromotionState("測試"));
        
        // When
        StateTransitionResult result = stateManager.activatePromotion(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ActivePromotionState.class);
    }
    
    @Test
    void shouldSuspendPromotion() {
        // When
        StateTransitionResult result = stateManager.suspendPromotion(context, "系統維護");
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(SuspendedPromotionState.class);
    }
    
    @Test
    void shouldExpirePromotion() {
        // When
        StateTransitionResult result = stateManager.expirePromotion(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldFailOperationsWithNullContext() {
        // When & Then
        assertThat(stateManager.activatePromotion(null).isFailure()).isTrue();
        assertThat(stateManager.suspendPromotion(null, "reason").isFailure()).isTrue();
        assertThat(stateManager.expirePromotion(null).isFailure()).isTrue();
    }
    
    @Test
    void shouldCheckAndHandleExpiration() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().minusDays(1));
        
        // When
        boolean handled = stateManager.checkAndHandleExpiration(context);
        
        // Then
        assertThat(handled).isTrue();
        assertThat(context.getCurrentState()).isInstanceOf(ExpiredPromotionState.class);
    }
    
    @Test
    void shouldNotHandleExpirationWhenAlreadyExpired() {
        // Given
        context.transitionTo(new ExpiredPromotionState());
        
        // When
        boolean handled = stateManager.checkAndHandleExpiration(context);
        
        // Then
        assertThat(handled).isFalse();
    }
    
    @Test
    void shouldNotHandleExpirationWhenStillValid() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        
        // When
        boolean handled = stateManager.checkAndHandleExpiration(context);
        
        // Then
        assertThat(handled).isFalse();
        assertThat(context.getCurrentState()).isInstanceOf(ActivePromotionState.class);
    }
    
    @Test
    void shouldReturnFalseForNullContextInExpiration() {
        // When
        boolean handled = stateManager.checkAndHandleExpiration(null);
        
        // Then
        assertThat(handled).isFalse();
    }
    
    @Test
    void shouldGetStateStatistics() {
        // Given
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        context.setProperty("activatedAt", LocalDateTime.now().minusHours(2));
        
        // When
        Map<String, Object> statistics = stateManager.getStateStatistics(context);
        
        // Then
        assertThat(statistics).isNotEmpty();
        assertThat(statistics).containsEntry("currentState", "ACTIVE");
        assertThat(statistics).containsEntry("isActive", true);
        assertThat(statistics).containsEntry("isTerminal", false);
        assertThat(statistics).containsEntry("isValid", true);
        assertThat(statistics).containsKey("stateChangeCount");
        assertThat(statistics).containsKey("createdAt");
        assertThat(statistics).containsKey("updatedAt");
        assertThat(statistics).containsKey("activatedAt");
    }
    
    @Test
    void shouldReturnEmptyStatisticsForNullContext() {
        // When
        Map<String, Object> statistics = stateManager.getStateStatistics(null);
        
        // Then
        assertThat(statistics).isEmpty();
    }
    
    @Test
    void shouldIncludeSuspensionStatisticsWhenApplicable() {
        // Given
        context.transitionTo(new SuspendedPromotionState("測試"));
        context.setProperty("suspensionDurationHours", 24L);
        
        // When
        Map<String, Object> statistics = stateManager.getStateStatistics(context);
        
        // Then
        assertThat(statistics).containsEntry("currentState", "SUSPENDED");
        assertThat(statistics).containsKey("suspendedAt");
        assertThat(statistics).containsEntry("suspensionDurationHours", 24L);
    }
    
    @Test
    void shouldIncludeExpirationStatisticsWhenApplicable() {
        // Given
        context.transitionTo(new ExpiredPromotionState());
        context.setProperty("lifetimeDays", 30L);
        
        // When
        Map<String, Object> statistics = stateManager.getStateStatistics(context);
        
        // Then
        assertThat(statistics).containsEntry("currentState", "EXPIRED");
        assertThat(statistics).containsKey("expiredAt");
        assertThat(statistics).containsEntry("lifetimeDays", 30L);
    }
}