package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.application.query.view.AvailablePromotionView;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import com.bank.promotion.application.service.PromotionApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
@Import(TestSecurityConfig.class)
class QueryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PromotionApplicationService promotionApplicationService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPromotionHistorySuccessfully() throws Exception {
        // Given
        PromotionHistoryView historyView = new PromotionHistoryView(
            "hist-001", "CUST001", "promo-001", "VIP優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0), "COMPLETED",
            LocalDateTime.now(), null
        );
        
        PagedResult<PromotionHistoryView> pagedResult = new PagedResult<>(
            List.of(historyView), 0, 20, 1
        );
        
        when(promotionApplicationService.getPromotionHistory(any())).thenReturn(pagedResult);
        
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("優惠歷史查詢成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value("hist-001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPromotionHistoryWithDateRange() throws Exception {
        // Given
        PagedResult<PromotionHistoryView> pagedResult = new PagedResult<>(
            List.of(), 0, 20, 0
        );
        
        when(promotionApplicationService.getPromotionHistory(any())).thenReturn(pagedResult);
        
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void shouldGetAvailablePromotionsSuccessfully() throws Exception {
        // Given
        AvailablePromotionView promotionView = new AvailablePromotionView(
            "promo-001", "新戶優惠", "NEW_CUSTOMER", "新客戶專屬優惠",
            BigDecimal.valueOf(500), BigDecimal.valueOf(10.0), 
            LocalDateTime.now(), LocalDateTime.now().plusMonths(1), 
            "新客戶且年收入>500000", "ACTIVE", null
        );
        
        when(promotionApplicationService.getAvailablePromotions(any()))
            .thenReturn(List.of(promotionView));
        
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/available")
                .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("可用優惠查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].promotionId").value("promo-001"));
    }
    
    @Test
    void shouldGetAvailablePromotionsWithFilters() throws Exception {
        // Given
        when(promotionApplicationService.getAvailablePromotions(any()))
            .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/available")
                .param("customerId", "CUST001")
                .param("accountType", "VIP")
                .param("region", "台北"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenCustomerIdIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenPageSizeIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001")
                .param("page", "0")
                .param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenPageNumberIsNegative() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001")
                .param("page", "-1")
                .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenAccessingHistoryWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenInsufficientRoleForHistory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // Given
        when(promotionApplicationService.getPromotionHistory(any()))
            .thenThrow(new RuntimeException("系統錯誤"));
        
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"));
    }
}