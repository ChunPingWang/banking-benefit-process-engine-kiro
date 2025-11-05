package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.CreateDecisionTreeRequest;
import com.bank.promotion.adapter.web.dto.UpdatePromotionRuleRequest;
import com.bank.promotion.application.service.PromotionApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagementController.class)
@Import(TestSecurityConfig.class)
class ManagementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PromotionApplicationService promotionApplicationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateDecisionTreeSuccessfully() throws Exception {
        // Given
        CreateDecisionTreeRequest request = new CreateDecisionTreeRequest(
            "測試決策樹", "測試用決策樹描述"
        );
        
        when(promotionApplicationService.createDecisionTree(any())).thenReturn("tree-001");
        
        // When & Then
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("決策樹創建成功"))
                .andExpect(jsonPath("$.data").value("tree-001"));
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldUpdatePromotionRuleSuccessfully() throws Exception {
        // Given
        UpdatePromotionRuleRequest request = new UpdatePromotionRuleRequest(
            "VIP客戶規則", "SPEL", "#{creditScore > 700}", 
            Map.of("threshold", 700), "ACTIVE"
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/management/promotion-rules/rule-001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("優惠規則更新成功"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteDecisionTreeSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/management/decision-trees/tree-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("決策樹刪除成功"));
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldTogglePromotionRuleStatusSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/management/promotion-rules/rule-001/status")
                .with(csrf())
                .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("優惠規則狀態更新成功"));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        // Given
        CreateDecisionTreeRequest request = new CreateDecisionTreeRequest(
            "測試決策樹", "測試用決策樹描述"
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenInsufficientRole() throws Exception {
        // Given
        CreateDecisionTreeRequest request = new CreateDecisionTreeRequest(
            "測試決策樹", "測試用決策樹描述"
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenDecisionTreeNameIsEmpty() throws Exception {
        // Given
        CreateDecisionTreeRequest request = new CreateDecisionTreeRequest(
            "", "測試用決策樹描述"
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenRuleTypeIsInvalid() throws Exception {
        // Given
        UpdatePromotionRuleRequest request = new UpdatePromotionRuleRequest(
            "測試規則", "INVALID_TYPE", "test rule", 
            Map.of(), "ACTIVE"
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/management/promotion-rules/rule-001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenStatusIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/management/promotion-rules/rule-001/status")
                .with(csrf())
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // Given
        CreateDecisionTreeRequest request = new CreateDecisionTreeRequest(
            "測試決策樹", "測試用決策樹描述"
        );
        
        when(promotionApplicationService.createDecisionTree(any()))
            .thenThrow(new RuntimeException("系統錯誤"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/management/decision-trees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"));
    }
}