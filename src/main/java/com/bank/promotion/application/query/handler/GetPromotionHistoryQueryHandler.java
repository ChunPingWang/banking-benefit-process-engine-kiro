package com.bank.promotion.application.query.handler;

import com.bank.promotion.application.query.GetPromotionHistoryQuery;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查詢優惠歷史查詢處理器
 */
@Component
public class GetPromotionHistoryQueryHandler {
    
    /**
     * 處理查詢優惠歷史查詢
     */
    public PagedResult<PromotionHistoryView> handle(GetPromotionHistoryQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        
        try {
            // TODO: 透過 Repository 查詢優惠歷史
            // List<PromotionHistory> histories = promotionHistoryRepository
            //     .findByCustomerIdAndDateRange(query.getCustomerId(), 
            //                                  query.getStartDate(), query.getEndDate(),
            //                                  PageRequest.of(query.getPage(), query.getSize()));
            
            // 暫時返回模擬資料
            List<PromotionHistoryView> mockData = createMockPromotionHistory(query.getCustomerId());
            
            // 模擬分頁
            int start = query.getPage() * query.getSize();
            int end = Math.min(start + query.getSize(), mockData.size());
            List<PromotionHistoryView> pageContent = start < mockData.size() ? 
                mockData.subList(start, end) : new ArrayList<>();
            
            return new PagedResult<>(pageContent, query.getPage(), query.getSize(), mockData.size());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to query promotion history: " + e.getMessage(), e);
        }
    }
    
    private List<PromotionHistoryView> createMockPromotionHistory(String customerId) {
        List<PromotionHistoryView> mockData = new ArrayList<>();
        
        mockData.add(new PromotionHistoryView(
            "hist-001", customerId, "promo-001", "VIP專屬理財優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0), "COMPLETED",
            LocalDateTime.now().minusDays(1), Map.of("channel", "online")
        ));
        
        mockData.add(new PromotionHistoryView(
            "hist-002", customerId, "promo-002", "新戶開戶優惠", "GENERAL",
            BigDecimal.valueOf(500), BigDecimal.valueOf(2.5), "COMPLETED",
            LocalDateTime.now().minusDays(7), Map.of("channel", "branch")
        ));
        
        return mockData;
    }
}