package com.bank.promotion.adapter.web.integration;

import com.bank.promotion.adapter.web.dto.EvaluatePromotionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PromotionApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldEvaluatePromotionEndToEnd() throws Exception {
        // Given
        EvaluatePromotionRequest request = new EvaluatePromotionRequest(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        request.setAccountBalance(BigDecimal.valueOf(500000));
        request.setTransactionHistory(List.of());
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void shouldGetAvailablePromotionsEndToEnd() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/available")
                .param("customerId", "CUST001")
                .param("accountType", "VIP")
                .param("region", "台北"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPromotionHistoryEndToEnd() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetAuditTrailsEndToEnd() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndManageDecisionTreeEndToEnd() throws Exception {
        // Given - Create decision tree request
        String createRequest = """
            {
                "name": "測試決策樹",
                "description": "整合測試用決策樹"
            }
            """;
        
        // When & Then - Create decision tree
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldUpdatePromotionRuleEndToEnd() throws Exception {
        // Given
        String updateRequest = """
            {
                "name": "VIP客戶規則",
                "ruleType": "SPEL",
                "ruleContent": "#{creditScore > 700 && annualIncome > 1000000}",
                "parameters": {
                    "creditThreshold": 700,
                    "incomeThreshold": 1000000
                },
                "status": "ACTIVE"
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/api/v1/management/promotion-rules/rule-001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void shouldHandleValidationErrorsCorrectly() throws Exception {
        // Given - Invalid request with empty customer ID
        EvaluatePromotionRequest invalidRequest = new EvaluatePromotionRequest(
            "", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/promotions/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    void shouldHandleSecurityCorrectly() throws Exception {
        // When & Then - Access protected endpoint without authentication
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001"))
                .andExpect(status().isUnauthorized());
        
        // When & Then - Access audit endpoint without proper role
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldEnforceRoleBasedAccess() throws Exception {
        // When & Then - User role trying to access admin endpoint
        mockMvc.perform(get("/api/v1/promotions/history")
                .param("customerId", "CUST001"))
                .andExpect(status().isForbidden());
        
        // When & Then - User role trying to access audit endpoint
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isForbidden());
    }
}