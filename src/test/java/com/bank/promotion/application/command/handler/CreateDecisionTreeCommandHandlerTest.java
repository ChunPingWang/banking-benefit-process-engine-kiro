package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreateDecisionTreeCommandHandlerTest {
    
    private CreateDecisionTreeCommandHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new CreateDecisionTreeCommandHandler();
    }
    
    @Test
    void shouldCreateDecisionTreeSuccessfully() {
        // Given
        CreateDecisionTreeCommand command = new CreateDecisionTreeCommand(
            "測試決策樹", "用於測試的決策樹"
        );
        
        // When
        String treeId = handler.handle(command);
        
        // Then
        assertThat(treeId).isNotNull();
        assertThat(treeId).isNotEmpty();
    }
    
    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Command cannot be null");
    }
    
    @Test
    void shouldHandleExceptionDuringCreation() {
        // Given - 測試空名稱會在命令創建時就拋出異常
        // When & Then
        assertThatThrownBy(() -> new CreateDecisionTreeCommand("", "空名稱應該導致錯誤"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Decision tree name cannot be null or empty");
    }
}