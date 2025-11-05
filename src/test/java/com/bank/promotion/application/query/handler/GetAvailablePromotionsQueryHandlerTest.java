package com.bank.promotion.application.query.handler;

import com.bank.promotion.application.query.GetAvailablePromotionsQuery;
import com.bank.promotion.application.query.view.AvailablePromotionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GetAvailablePromotionsQueryHandlerTest {
    
    private GetAvailablePromotionsQueryHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new GetAvailablePromotionsQueryHandler();
    }
    
    @Test
    void shouldReturnVipPromotionsForVipCustomer() {
        // Given
        GetAvailablePromotionsQuery query = new GetAvailablePromotionsQuery(
            "CUST001", "VIP", "台北", true
        );
        
        // When
        List<AvailablePromotionView> result = handler.handle(query);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3); // VIP優惠 + 一般優惠 + 台北地區優惠
        
        // 驗證包含VIP優惠
        boolean hasVipPromotion = result.stream()
            .anyMatch(promo -> "VIP專屬理財優惠".equals(promo.getPromotionName()));
        assertThat(hasVipPromotion).isTrue();
        
        // 驗證包含台北地區優惠
        boolean hasTaipeiPromotion = result.stream()
            .anyMatch(promo -> "台北地區限定優惠".equals(promo.getPromotionName()));
        assertThat(hasTaipeiPromotion).isTrue();
    }
    
    @Test
    void shouldReturnGeneralPromotionsForGeneralCustomer() {
        // Given
        GetAvailablePromotionsQuery query = new GetAvailablePromotionsQuery(
            "CUST002", "一般", "高雄", true
        );
        
        // When
        List<AvailablePromotionView> result = handler.handle(query);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1); // 只有一般優惠
        
        AvailablePromotionView promotion = result.get(0);
        assertThat(promotion.getPromotionName()).isEqualTo("新戶開戶優惠");
        assertThat(promotion.getPromotionType()).isEqualTo("GENERAL");
    }
    
    @Test
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Query cannot be null");
    }
}