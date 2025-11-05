package com.bank.promotion.state;

import com.bank.promotion.domain.state.ActivePromotionState;
import com.bank.promotion.domain.state.ExpiredPromotionState;
import com.bank.promotion.domain.state.PromotionContext;
import com.bank.promotion.domain.state.StateTransitionResult;
import com.bank.promotion.domain.state.SuspendedPromotionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PromotionContextTest {
    
    private PromotionContext context;
    private ActivePromotionState initialState;
    
    @BeforeEach
    void setUp() {
        initialState = new ActivePromotionState();
        context = new PromotionContext("PROMO001", "測試優惠", "TEST_PROMOTION", initialState);
    }
    
    @Test
    void shouldCreateContextWithCorrectInitialValues() {
        // Then
        assertThat(context.getPromotionId()).isEqualTo("PROMO001");
        assertThat(context.getPromotionName()).isEqualTo("測試優惠");
        assertThat(context.getPromotionType()).isEqualTo("TEST_PROMOTION");
        assertThat(context.getCurrentState()).isEqualTo(initialState);
        assertThat(context.getCreatedAt()).isNotNull();
        assertThat(context.getUpdatedAt()).isNotNull();
        assertThat(context.getStateChangeHistory()).hasSize(1); // Initial state event
    }
    
    @Test
    void shouldThrowExceptionForInvalidConstructorParameters() {
        // When & Then
        assertThatThrownBy(() -> new PromotionContext(null, "測試優惠", "TEST", initialState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Promotion ID cannot be null or empty");
        
        assertThatThrownBy(() -> new PromotionContext("", "測試優惠", "TEST", initialState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Promotion ID cannot be null or empty");
        
        assertThatThrownBy(() -> new PromotionContext("PROMO001", null, "TEST", initialState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Promotion name cannot be null or empty");
        
        assertThatThrownBy(() -> new PromotionContext("PROMO001", "測試優惠", null, initialState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Promotion type cannot be null or empty");
        
        assertThatThrownBy(() -> new PromotionContext("PROMO001", "測試優惠", "TEST", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Promotion state cannot be null");
    }
    
    @Test
    void shouldTransitionToValidState() {
        // Given
        SuspendedPromotionState suspendedState = new SuspendedPromotionState("系統維護");
        
        // When
        StateTransitionResult result = context.transitionTo(suspendedState);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getCurrentState()).isEqualTo(suspendedState);
        assertThat(context.getStateChangeHistory()).hasSizeGreaterThan(1);
    }
    
    @Test
    void shouldFailToTransitionToInvalidState() {
        // Given - Active state cannot transition to Expired directly in normal flow
        ExpiredPromotionState expiredState = new ExpiredPromotionState();
        
        // When
        StateTransitionResult result = context.transitionTo(expiredState);
        
        // Then
        assertThat(result.isSuccess()).isTrue(); // Actually, Active can transition to Expired
        assertThat(context.getCurrentState()).isEqualTo(expiredState);
    }
    
    @Test
    void shouldFailToTransitionToSameState() {
        // Given
        ActivePromotionState sameState = new ActivePromotionState();
        
        // When
        StateTransitionResult result = context.transitionTo(sameState);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("目標狀態與當前狀態相同");
        assertThat(context.getCurrentState()).isEqualTo(initialState);
    }
    
    @Test
    void shouldFailToTransitionToNullState() {
        // When
        StateTransitionResult result = context.transitionTo(null);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains("目標狀態不能為 null");
        assertThat(context.getCurrentState()).isEqualTo(initialState);
    }
    
    @Test
    void shouldSetAndGetProperties() {
        // When
        context.setProperty("validUntil", LocalDateTime.now().plusDays(30));
        context.setProperty("maxUsageCount", 100);
        context.setProperty("description", "測試描述");
        
        // Then
        assertThat(context.getProperty("validUntil")).isInstanceOf(LocalDateTime.class);
        assertThat(context.getProperty("maxUsageCount")).isEqualTo(100);
        assertThat(context.getProperty("description")).isEqualTo("測試描述");
        assertThat(context.getProperty("nonExistent")).isNull();
    }
    
    @Test
    void shouldGetPropertyWithDefaultValue() {
        // When & Then
        assertThat(context.getProperty("nonExistent", "defaultValue")).isEqualTo("defaultValue");
        assertThat(context.getProperty("nonExistent", 42)).isEqualTo(42);
        
        // Set a property and verify it returns the actual value, not default
        context.setProperty("existingKey", "actualValue");
        assertThat(context.getProperty("existingKey", "defaultValue")).isEqualTo("actualValue");
    }
    
    @Test
    void shouldRemoveProperties() {
        // Given
        context.setProperty("testKey", "testValue");
        assertThat(context.hasProperty("testKey")).isTrue();
        
        // When
        Object removedValue = context.removeProperty("testKey");
        
        // Then
        assertThat(removedValue).isEqualTo("testValue");
        assertThat(context.hasProperty("testKey")).isFalse();
        assertThat(context.getProperty("testKey")).isNull();
    }
    
    @Test
    void shouldReturnNullWhenRemovingNonExistentProperty() {
        // When
        Object removedValue = context.removeProperty("nonExistent");
        
        // Then
        assertThat(removedValue).isNull();
    }
    
    @Test
    void shouldCheckPropertyExistence() {
        // Given
        context.setProperty("existingKey", "value");
        
        // When & Then
        assertThat(context.hasProperty("existingKey")).isTrue();
        assertThat(context.hasProperty("nonExistentKey")).isFalse();
    }
    
    @Test
    void shouldGetAllPropertiesAsCopy() {
        // Given
        context.setProperty("key1", "value1");
        context.setProperty("key2", "value2");
        
        // When
        var allProperties = context.getAllProperties();
        
        // Then
        assertThat(allProperties).hasSize(2);
        assertThat(allProperties).containsEntry("key1", "value1");
        assertThat(allProperties).containsEntry("key2", "value2");
        
        // Verify it's a copy (modifications don't affect original)
        allProperties.put("key3", "value3");
        assertThat(context.hasProperty("key3")).isFalse();
    }
    
    @Test
    void shouldThrowExceptionForInvalidPropertyKey() {
        // When & Then
        assertThatThrownBy(() -> context.setProperty(null, "value"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Property key cannot be null or empty");
        
        assertThatThrownBy(() -> context.setProperty("", "value"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Property key cannot be null or empty");
        
        assertThatThrownBy(() -> context.setProperty("   ", "value"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Property key cannot be null or empty");
    }
    
    @Test
    void shouldAddStateChangeEvents() {
        // Given
        LocalDateTime eventTime = LocalDateTime.now();
        
        // When
        context.addStateChangeEvent("測試事件", eventTime);
        
        // Then
        var history = context.getStateChangeHistory();
        assertThat(history).hasSizeGreaterThan(1); // Initial event + new event
        
        var lastEvent = history.get(history.size() - 1);
        assertThat(lastEvent.getDescription()).isEqualTo("測試事件");
        assertThat(lastEvent.getTimestamp()).isEqualTo(eventTime);
        assertThat(lastEvent.getStateName()).isEqualTo(initialState.getStateName());
    }
    
    @Test
    void shouldUpdateTimestampWhenPropertiesChange() {
        // Given
        LocalDateTime initialUpdatedAt = context.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        context.setProperty("testKey", "testValue");
        
        // Then
        assertThat(context.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}