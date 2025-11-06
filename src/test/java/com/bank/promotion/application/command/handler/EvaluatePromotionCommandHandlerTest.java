package com.bank.promotion.application.command.handler;

import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EvaluatePromotionCommandHandlerTest {
    
    @Mock
    private AuditService auditService;
    
    private EvaluatePromotionCommandHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new EvaluatePromotionCommandHandler(auditService);
    }
    
    @Test
    void shouldEvaluatePromotionSuccessfully() {
        // Given
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        EvaluatePromotionCommand command = new EvaluatePromotionCommand(
            "tree-001", customerPayload, "req-001"
        );
        
        // When
        PromotionResult result = handler.handle(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getPromotionId()).isEqualTo("promo-vip-001");
        assertThat(result.getPromotionName()).isEqualTo("VIP專屬優惠");
    }
    
    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Command cannot be null");
    }
}