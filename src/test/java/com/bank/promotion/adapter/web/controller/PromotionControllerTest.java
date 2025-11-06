package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.EvaluatePromotionRequest;
import com.bank.promotion.application.service.PromotionApplicationService;
import com.bank.promotion.domain.valueobject.PromotionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
@Import(TestSecurityConfig.class)
class PromotionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PromotionApplicationService promotionApplicationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldEvaluatePromotionSuccessfully() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        request.setAccountBalance(BigDecimal.valueOf(500000));
        request.setTransactionHistory(List.of());
        request.setTreeId("default-tree");
        
        PromotionResult mockResult = new PromotionResult(
            "promo-001", "VIP專屬優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            "VIP客戶專屬優惠", LocalDateTime.now().plusMonths(3),
            null, true
        );
        
        when(promotionApplicationService.evaluatePromotion(any())).thenReturn(mockResult);
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("優惠評估完成"))
                .andExpect(jsonPath("$.data.promotionId").value("promo-001"))
                .andExpect(jsonPath("$.data.promotionName").value("VIP專屬優惠"))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnBadRequestWhenCustomerIdIsEmpty() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnBadRequestWhenAnnualIncomeIsNegative() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "CUST001", "VIP", BigDecimal.valueOf(-1000), 
            750, "台北", 50
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnBadRequestWhenCreditScoreIsOutOfRange() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            1500, "台北", 50
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        when(promotionApplicationService.evaluatePromotion(any()))
            .thenThrow(new RuntimeException("系統錯誤"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        // Given
        String invalidJson = "{\"customerId\": \"\"}";
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}