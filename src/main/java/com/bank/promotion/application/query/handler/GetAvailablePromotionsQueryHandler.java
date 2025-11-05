package com.bank.promotion.application.query.handler;

import com.bank.promotion.application.query.GetAvailablePromotionsQuery;
import com.bank.promotion.application.query.view.AvailablePromotionView;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查詢可用優惠查詢處理器
 */
@Component
public class GetAvailablePromotionsQueryHandler {
    
    /**
     * 處理查詢可用優惠查詢
     */
    public List<AvailablePromotionView> handle(GetAvailablePromotionsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        
        try {
            // TODO: 透過 Repository 查詢可用優惠
            // List<AvailablePromotion> promotions = availablePromotionRepository
            //     .findByCustomerCriteria(query.getCustomerId(), query.getAccountType(), 
            //                           query.getRegion(), query.isActiveOnly());
            
            // 暫時返回模擬資料
            return createMockAvailablePromotions(query);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to query available promotions: " + e.getMessage(), e);
        }
    }
    
    private List<AvailablePromotionView> createMockAvailablePromotions(GetAvailablePromotionsQuery query) {
        List<AvailablePromotionView> mockData = new ArrayList<>();
        
        // 根據帳戶類型提供不同優惠
        if ("VIP".equals(query.getAccountType())) {
            mockData.add(new AvailablePromotionView(
                "promo-vip-001", "VIP專屬理財優惠", "VIP",
                "專為VIP客戶設計的高額理財優惠", 
                BigDecimal.valueOf(5000), BigDecimal.valueOf(10.0),
                LocalDateTime.now(), LocalDateTime.now().plusMonths(3),
                "VIP客戶且年收入超過200萬", "ACTIVE",
                Map.of("minAmount", 100000, "category", "investment")
            ));
        }
        
        mockData.add(new AvailablePromotionView(
            "promo-general-001", "新戶開戶優惠", "GENERAL",
            "新客戶專屬開戶優惠", 
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            LocalDateTime.now(), LocalDateTime.now().plusMonths(1),
            "新客戶開戶", "ACTIVE",
            Map.of("minAmount", 10000, "category", "account")
        ));
        
        // 根據地區提供地區性優惠
        if ("台北".equals(query.getRegion())) {
            mockData.add(new AvailablePromotionView(
                "promo-taipei-001", "台北地區限定優惠", "REGIONAL",
                "台北地區客戶專屬優惠", 
                BigDecimal.valueOf(2000), BigDecimal.valueOf(7.5),
                LocalDateTime.now(), LocalDateTime.now().plusMonths(2),
                "台北地區客戶", "ACTIVE",
                Map.of("region", "台北", "category", "regional")
            ));
        }
        
        return mockData;
    }
}