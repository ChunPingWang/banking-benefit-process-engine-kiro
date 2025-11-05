package com.bank.promotion.application.service;

import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.application.command.handler.CreateDecisionTreeCommandHandler;
import com.bank.promotion.application.command.handler.EvaluatePromotionCommandHandler;
import com.bank.promotion.application.command.handler.UpdatePromotionRuleCommandHandler;
import com.bank.promotion.application.query.GetAvailablePromotionsQuery;
import com.bank.promotion.application.query.GetPromotionHistoryQuery;
import com.bank.promotion.application.query.handler.GetAvailablePromotionsQueryHandler;
import com.bank.promotion.application.query.handler.GetPromotionHistoryQueryHandler;
import com.bank.promotion.application.query.view.AvailablePromotionView;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionApplicationServiceTest {
    
    @Mock
    private CreateDecisionTreeCommandHandler createDecisionTreeHandler;
    
    @Mock
    private UpdatePromotionRuleCommandHandler updatePromotionRuleHandler;
    
    @Mock
    private EvaluatePromotionCommandHandler evaluatePromotionHandler;
    
    @Mock
    private GetPromotionHistoryQueryHandler getPromotionHistoryHandler;
    
    @Mock
    private GetAvailablePromotionsQueryHandler getAvailablePromotionsHandler;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private PerformanceMonitoringService performanceMonitoringService;
    
    private PromotionApplicationService applicationService;
    
    @BeforeEach
    void setUp() {
        applicationService = new PromotionApplicationService(
            createDecisionTreeHandler,
            updatePromotionRuleHandler,
            evaluatePromotionHandler,
            getPromotionHistoryHandler,
            getAvailablePromotionsHandler,
            auditService,
            performanceMonitoringService
        );
    }
    
    @Test
    void shouldCreateDecisionTreeSuccessfully() {
        // Given
        CreateDecisionTreeCommand command = new CreateDecisionTreeCommand(
            "測試決策樹", "測試用決策樹"
        );
        String expectedTreeId = "tree-001";
        
        when(createDecisionTreeHandler.handle(command)).thenReturn(expectedTreeId);
        
        // When
        String result = applicationService.createDecisionTree(command);
        
        // Then
        assertThat(result).isEqualTo(expectedTreeId);
        verify(createDecisionTreeHandler).handle(command);
        verify(performanceMonitoringService).recordOperationTime(eq("CREATE_DECISION_TREE"), anyLong());
    }
    
    @Test
    void shouldEvaluatePromotionWithAuditTrail() {
        // Given
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        EvaluatePromotionCommand command = new EvaluatePromotionCommand(
            "tree-001", customerPayload, "req-001"
        );
        
        PromotionResult expectedResult = new PromotionResult(
            "promo-001", "VIP專屬優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            "VIP客戶專屬優惠", LocalDateTime.now().plusMonths(3),
            null, true
        );
        
        when(evaluatePromotionHandler.handle(command)).thenReturn(expectedResult);
        
        // When
        PromotionResult result = applicationService.evaluatePromotion(command);
        
        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(evaluatePromotionHandler).handle(command);
        verify(auditService).recordPromotionEvaluation(
            eq("req-001"), eq(customerPayload), eq(expectedResult), anyLong()
        );
        verify(performanceMonitoringService).recordOperationTime(eq("EVALUATE_PROMOTION"), anyLong());
    }
    
    @Test
    void shouldHandleEvaluationErrorWithAuditTrail() {
        // Given
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        EvaluatePromotionCommand command = new EvaluatePromotionCommand(
            "tree-001", customerPayload, "req-001"
        );
        
        RuntimeException expectedException = new RuntimeException("決策樹執行失敗");
        when(evaluatePromotionHandler.handle(command)).thenThrow(expectedException);
        
        // When & Then
        assertThatThrownBy(() -> applicationService.evaluatePromotion(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to evaluate promotion");
        
        verify(auditService).recordPromotionEvaluation(
            eq("req-001"), eq(customerPayload), any(PromotionResult.class), anyLong()
        );
        verify(performanceMonitoringService).recordOperationError(
            eq("EVALUATE_PROMOTION"), anyLong(), any(RuntimeException.class)
        );
    }
    
    @Test
    void shouldGetPromotionHistorySuccessfully() {
        // Given
        GetPromotionHistoryQuery query = new GetPromotionHistoryQuery(
            "CUST001", LocalDateTime.now().minusDays(30), LocalDateTime.now(), 0, 10
        );
        
        PagedResult<PromotionHistoryView> expectedResult = new PagedResult<>(
            List.of(), 0, 10, 0
        );
        
        when(getPromotionHistoryHandler.handle(query)).thenReturn(expectedResult);
        
        // When
        PagedResult<PromotionHistoryView> result = applicationService.getPromotionHistory(query);
        
        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(getPromotionHistoryHandler).handle(query);
        verify(performanceMonitoringService).recordOperationTime(eq("GET_PROMOTION_HISTORY"), anyLong());
    }
    
    @Test
    void shouldGetAvailablePromotionsSuccessfully() {
        // Given
        GetAvailablePromotionsQuery query = new GetAvailablePromotionsQuery(
            "CUST001", "VIP", "台北", true
        );
        
        List<AvailablePromotionView> expectedResult = List.of();
        when(getAvailablePromotionsHandler.handle(query)).thenReturn(expectedResult);
        
        // When
        List<AvailablePromotionView> result = applicationService.getAvailablePromotions(query);
        
        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(getAvailablePromotionsHandler).handle(query);
        verify(performanceMonitoringService).recordOperationTime(eq("GET_AVAILABLE_PROMOTIONS"), anyLong());
    }
    
    @Test
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> applicationService.createDecisionTree(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Command cannot be null");
        
        assertThatThrownBy(() -> applicationService.evaluatePromotion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Command cannot be null");
        
        assertThatThrownBy(() -> applicationService.getPromotionHistory(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Query cannot be null");
        
        assertThatThrownBy(() -> applicationService.getAvailablePromotions(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Query cannot be null");
    }
}