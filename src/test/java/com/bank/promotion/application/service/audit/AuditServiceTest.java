package com.bank.promotion.application.service.audit;

import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AuditServiceTest {
    
    private AuditService auditService;
    
    @BeforeEach
    void setUp() {
        auditService = new AuditService();
    }
    
    @Test
    void shouldRecordPromotionEvaluationSuccessfully() {
        // Given
        String requestId = "req-001";
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        PromotionResult result = new PromotionResult(
            "promo-001", "VIP專屬優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            "VIP客戶專屬優惠", LocalDateTime.now().plusMonths(3),
            null, true
        );
        
        // When
        auditService.recordPromotionEvaluation(requestId, customerPayload, result, 150);
        
        // Then
        List<AuditTrail> auditTrails = auditService.getAuditTrails(requestId);
        assertThat(auditTrails).hasSize(1);
        
        AuditTrail auditTrail = auditTrails.get(0);
        assertThat(auditTrail.getRequestId()).isEqualTo(requestId);
        assertThat(auditTrail.getCustomerId()).isEqualTo("CUST001");
        assertThat(auditTrail.getOperationType()).isEqualTo("PROMOTION_EVALUATION");
        assertThat(auditTrail.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditTrail.getExecutionTimeMs()).isEqualTo(150);
    }
    
    @Test
    void shouldRecordDecisionStepSuccessfully() {
        // Given
        String requestId = "req-001";
        
        // When
        auditService.recordDecisionStep(
            requestId, "tree-001", "node-001", "CONDITION",
            "input data", "output data", 50, "SUCCESS", null
        );
        
        // Then
        List<AuditTrail> auditTrails = auditService.getAuditTrails(requestId);
        assertThat(auditTrails).hasSize(1);
        
        AuditTrail auditTrail = auditTrails.get(0);
        assertThat(auditTrail.getRequestId()).isEqualTo(requestId);
        assertThat(auditTrail.getCustomerId()).isEqualTo("SYSTEM");
        assertThat(auditTrail.getOperationType()).isEqualTo("DECISION_STEP");
        assertThat(auditTrail.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditTrail.getOperationDetails()).containsEntry("treeId", "tree-001");
        assertThat(auditTrail.getOperationDetails()).containsEntry("nodeId", "node-001");
    }
    
    @Test
    void shouldRecordExternalSystemCallSuccessfully() {
        // Given
        String requestId = "req-001";
        
        // When
        auditService.recordExternalSystemCall(
            requestId, "CUST001", "CreditService", "/api/credit/check",
            "request data", "response data", 200, "SUCCESS", null
        );
        
        // Then
        List<AuditTrail> auditTrails = auditService.getAuditTrails(requestId);
        assertThat(auditTrails).hasSize(1);
        
        AuditTrail auditTrail = auditTrails.get(0);
        assertThat(auditTrail.getRequestId()).isEqualTo(requestId);
        assertThat(auditTrail.getCustomerId()).isEqualTo("CUST001");
        assertThat(auditTrail.getOperationType()).isEqualTo("EXTERNAL_SYSTEM_CALL");
        assertThat(auditTrail.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditTrail.getOperationDetails()).containsEntry("systemName", "CreditService");
        assertThat(auditTrail.getOperationDetails()).containsEntry("endpoint", "/api/credit/check");
    }
    
    @Test
    void shouldRecordDatabaseQuerySuccessfully() {
        // Given
        String requestId = "req-001";
        
        // When
        auditService.recordDatabaseQuery(
            requestId, "CUST001", "SELECT", "SELECT * FROM customers WHERE id = ?",
            "customer data", 30, "SUCCESS", null
        );
        
        // Then
        List<AuditTrail> auditTrails = auditService.getAuditTrails(requestId);
        assertThat(auditTrails).hasSize(1);
        
        AuditTrail auditTrail = auditTrails.get(0);
        assertThat(auditTrail.getRequestId()).isEqualTo(requestId);
        assertThat(auditTrail.getCustomerId()).isEqualTo("CUST001");
        assertThat(auditTrail.getOperationType()).isEqualTo("DATABASE_QUERY");
        assertThat(auditTrail.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditTrail.getOperationDetails()).containsEntry("queryType", "SELECT");
    }
    
    @Test
    void shouldGetCustomerAuditTrailsSuccessfully() {
        // Given
        String customerId = "CUST001";
        CustomerPayload customerPayload = new CustomerPayload(
            customerId, "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        PromotionResult result = new PromotionResult(
            "promo-001", "VIP專屬優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            "VIP客戶專屬優惠", LocalDateTime.now().plusMonths(3),
            null, true
        );
        
        auditService.recordPromotionEvaluation("req-001", customerPayload, result, 150);
        auditService.recordPromotionEvaluation("req-002", customerPayload, result, 120);
        
        // When
        List<AuditTrail> customerTrails = auditService.getCustomerAuditTrails(
            customerId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)
        );
        
        // Then
        assertThat(customerTrails).hasSize(2);
        assertThat(customerTrails).allMatch(trail -> customerId.equals(trail.getCustomerId()));
    }
    
    @Test
    void shouldCleanupExpiredAuditDataSuccessfully() {
        // Given
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "VIP", BigDecimal.valueOf(2000000), 
            750, "台北", 50
        );
        PromotionResult result = new PromotionResult(
            "promo-001", "VIP專屬優惠", "VIP",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(5.0),
            "VIP客戶專屬優惠", LocalDateTime.now().plusMonths(3),
            null, true
        );
        
        auditService.recordPromotionEvaluation("req-001", customerPayload, result, 150);
        
        // When
        auditService.cleanupExpiredAuditData(LocalDateTime.now().plusDays(1));
        
        // Then
        List<AuditTrail> auditTrails = auditService.getAuditTrails("req-001");
        assertThat(auditTrails).isEmpty();
    }
    
    @Test
    void shouldThrowExceptionForInvalidParameters() {
        // When & Then
        assertThatThrownBy(() -> auditService.getAuditTrails(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Request ID cannot be null or empty");
        
        assertThatThrownBy(() -> auditService.getCustomerAuditTrails(null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID cannot be null or empty");
        
        assertThatThrownBy(() -> auditService.cleanupExpiredAuditData(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cutoff date cannot be null");
    }
}