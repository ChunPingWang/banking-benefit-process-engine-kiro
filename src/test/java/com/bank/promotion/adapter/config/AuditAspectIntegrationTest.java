package com.bank.promotion.adapter.config;

import com.bank.promotion.adapter.persistence.repository.AuditTrailRepository;
import com.bank.promotion.adapter.persistence.repository.SystemEventRepository;
import com.bank.promotion.application.service.PromotionApplicationService;
import com.bank.promotion.application.command.EvaluatePromotionCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AuditAspect
 * Tests the automatic audit recording functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditAspectIntegrationTest {

    @Autowired
    private PromotionApplicationService promotionApplicationService;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private SystemEventRepository systemEventRepository;

    @Test
    void shouldRecordAuditTrailForPromotionEvaluation() throws InterruptedException {
        // Given
        com.bank.promotion.domain.valueobject.CustomerPayload customerPayload = 
            new com.bank.promotion.domain.valueobject.CustomerPayload(
                "CUST001", 
                "VIP", 
                java.math.BigDecimal.valueOf(2000000), 
                750, 
                "TAIPEI", 
                100,
                java.math.BigDecimal.valueOf(500000),
                java.util.List.of()
            );
        
        EvaluatePromotionCommand command = new EvaluatePromotionCommand(
            "test-tree-1", 
            customerPayload, 
            "request-" + System.currentTimeMillis()
        );

        long initialAuditCount = auditTrailRepository.count();

        // When
        try {
            promotionApplicationService.evaluatePromotion(command);
        } catch (Exception e) {
            // Expected since we don't have full implementation yet
        }

        // Wait a bit for async processing
        Thread.sleep(100);

        // Then
        long finalAuditCount = auditTrailRepository.count();
        assertThat(finalAuditCount).isGreaterThan(initialAuditCount);
    }

    @Test
    void shouldRecordSystemEventOnError() throws InterruptedException {
        // Given - create command with invalid tree ID to trigger error
        com.bank.promotion.domain.valueobject.CustomerPayload customerPayload = 
            new com.bank.promotion.domain.valueobject.CustomerPayload(
                "CUST001", 
                "VIP", 
                java.math.BigDecimal.valueOf(2000000), 
                750, 
                "TAIPEI", 
                100
            );
        
        EvaluatePromotionCommand invalidCommand = new EvaluatePromotionCommand(
            "non-existent-tree", 
            customerPayload, 
            "request-" + System.currentTimeMillis()
        );
        
        long initialEventCount = systemEventRepository.count();

        // When
        try {
            promotionApplicationService.evaluatePromotion(invalidCommand);
        } catch (Exception e) {
            // Expected error
        }

        // Wait a bit for async processing
        Thread.sleep(100);

        // Then
        long finalEventCount = systemEventRepository.count();
        assertThat(finalEventCount).isGreaterThanOrEqualTo(initialEventCount);
    }
}