package com.bank.promotion.application.query.handler;

import com.bank.promotion.application.query.GetPromotionHistoryQuery;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GetPromotionHistoryQueryHandlerTest {
    
    private GetPromotionHistoryQueryHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new GetPromotionHistoryQueryHandler();
    }
    
    @Test
    void shouldReturnPromotionHistorySuccessfully() {
        // Given
        GetPromotionHistoryQuery query = new GetPromotionHistoryQuery(
            "CUST001", 
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            0, 10
        );
        
        // When
        PagedResult<PromotionHistoryView> result = handler.handle(query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isGreaterThan(0);
        
        // 驗證返回的歷史記錄
        PromotionHistoryView firstHistory = result.getContent().get(0);
        assertThat(firstHistory.getCustomerId()).isEqualTo("CUST001");
        assertThat(firstHistory.getPromotionName()).isNotEmpty();
    }
    
    @Test
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Query cannot be null");
    }
    
    @Test
    void shouldReturnEmptyResultForSecondPage() {
        // Given
        GetPromotionHistoryQuery query = new GetPromotionHistoryQuery(
            "CUST001", null, null, 1, 10
        );
        
        // When
        PagedResult<PromotionHistoryView> result = handler.handle(query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.isHasNext()).isFalse();
    }
}