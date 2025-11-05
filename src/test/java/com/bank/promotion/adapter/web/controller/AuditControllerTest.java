package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.application.service.audit.AuditTrail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
@Import(TestSecurityConfig.class)
class AuditControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuditService auditService;
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetAuditTrailsByRequestIdSuccessfully() throws Exception {
        // Given
        AuditTrail auditTrail = new AuditTrail(
            "req-001", "CUST001", "PROMOTION_EVALUATION",
            Map.of("promotionId", "promo-001"), 150, "SUCCESS", null
        );
        
        when(auditService.getAuditTrails("req-001")).thenReturn(List.of(auditTrail));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("稽核軌跡查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].requestId").value("req-001"))
                .andExpect(jsonPath("$.data[0].operationType").value("PROMOTION_EVALUATION"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetAuditTrailsByCustomerIdSuccessfully() throws Exception {
        // Given
        AuditTrail auditTrail = new AuditTrail(
            "req-001", "CUST001", "PROMOTION_EVALUATION",
            Map.of("promotionId", "promo-001"), 150, "SUCCESS", null
        );
        
        when(auditService.getCustomerAuditTrails(anyString(), any(), any()))
            .thenReturn(List.of(auditTrail));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("customerId", "CUST001")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].customerId").value("CUST001"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetDecisionStepsSuccessfully() throws Exception {
        // Given
        AuditTrail decisionStep = new AuditTrail(
            "req-001", "SYSTEM", "DECISION_STEP",
            Map.of("treeId", "tree-001", "nodeId", "node-001"), 50, "SUCCESS", null
        );
        
        when(auditService.getAuditTrails("req-001")).thenReturn(List.of(decisionStep));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/decisions")
                .param("requestId", "req-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("決策步驟追蹤查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].operationType").value("DECISION_STEP"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetExternalSystemCallsSuccessfully() throws Exception {
        // Given
        AuditTrail externalCall = new AuditTrail(
            "req-001", "CUST001", "EXTERNAL_SYSTEM_CALL",
            Map.of("systemName", "CreditService", "endpoint", "/api/credit-check"), 
            200, "SUCCESS", null
        );
        
        when(auditService.getAuditTrails("req-001")).thenReturn(List.of(externalCall));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/external-calls")
                .param("requestId", "req-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("外部系統呼叫記錄查詢成功"))
                .andExpect(jsonPath("$.data[0].operationType").value("EXTERNAL_SYSTEM_CALL"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGetDatabaseQueriesSuccessfully() throws Exception {
        // Given
        AuditTrail databaseQuery = new AuditTrail(
            "req-001", "SYSTEM", "DATABASE_QUERY",
            Map.of("queryType", "SELECT", "query", "SELECT * FROM customers"), 
            30, "SUCCESS", null
        );
        
        when(auditService.getAuditTrails("req-001")).thenReturn(List.of(databaseQuery));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/database-queries")
                .param("requestId", "req-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("資料庫查詢記錄查詢成功"))
                .andExpect(jsonPath("$.data[0].operationType").value("DATABASE_QUERY"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldGenerateComplianceReportSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/compliance-report")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("合規性報告生成成功"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldReturnBadRequestWhenNoParametersProvided() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("必須提供 requestId 或 customerId 參數"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldReturnBadRequestWhenRequestIdIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/decisions")
                .param("requestId", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldReturnBadRequestWhenDateRangeIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/compliance-report")
                .param("startDate", "2023-12-31T23:59:59")
                .param("endDate", "2023-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("開始時間不能晚於結束時間"));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenInsufficientRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminAccessToAuditTrails() throws Exception {
        // Given
        when(auditService.getAuditTrails("req-001")).thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // Given
        when(auditService.getAuditTrails("req-001"))
            .thenThrow(new RuntimeException("系統錯誤"));
        
        // When & Then
        mockMvc.perform(get("/api/v1/audit/trails")
                .param("requestId", "req-001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"));
    }
}