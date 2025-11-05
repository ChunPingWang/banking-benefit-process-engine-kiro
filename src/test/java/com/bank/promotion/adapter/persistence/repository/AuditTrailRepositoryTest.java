package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.AuditTrailEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AuditTrailRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class AuditTrailRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private com.bank.promotion.adapter.persistence.repository.RequestLogRepository requestLogRepository;

    private AuditTrailEntity testAuditTrail;

    @BeforeEach
    void setUp() {
        testAuditTrail = new AuditTrailEntity(
                "audit-1",
                "request-1",
                "CUST001",
                "PROMOTION_EVALUATION",
                "Customer promotion evaluation started",
                "SUCCESS"
        );
        testAuditTrail.setExecutionTimeMs(150);
    }

    private void createRequestLog(String requestId) {
        com.bank.promotion.adapter.persistence.entity.RequestLogEntity requestLog = 
            new com.bank.promotion.adapter.persistence.entity.RequestLogEntity(
                "req-log-" + requestId, requestId, "/api/test", "POST", "{\"test\":\"data\"}"
            );
        entityManager.persist(requestLog);
    }

    @Test
    void shouldSaveAndFindAuditTrail() {
        // Given
        createRequestLog("request-1");
        entityManager.flush();

        // When
        AuditTrailEntity saved = auditTrailRepository.save(testAuditTrail);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isEqualTo("audit-1");
        assertThat(saved.getRequestId()).isEqualTo("request-1");
        assertThat(saved.getCustomerId()).isEqualTo("CUST001");
        assertThat(saved.getOperationType()).isEqualTo("PROMOTION_EVALUATION");
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindAuditTrailsByRequestId() {
        // Given - Create request logs first
        com.bank.promotion.adapter.persistence.entity.RequestLogEntity requestLog1 = 
            new com.bank.promotion.adapter.persistence.entity.RequestLogEntity(
                "req-log-1", "request-1", "/api/test", "POST", "{\"test\":\"data\"}"
            );
        com.bank.promotion.adapter.persistence.entity.RequestLogEntity requestLog2 = 
            new com.bank.promotion.adapter.persistence.entity.RequestLogEntity(
                "req-log-2", "request-2", "/api/test", "POST", "{\"test\":\"data\"}"
            );
        entityManager.persist(requestLog1);
        entityManager.persist(requestLog2);
        entityManager.flush();

        AuditTrailEntity audit1 = new AuditTrailEntity("audit-1", "request-1", "CUST001", "OPERATION_1", "Details 1", "SUCCESS");
        AuditTrailEntity audit2 = new AuditTrailEntity("audit-2", "request-1", "CUST001", "OPERATION_2", "Details 2", "SUCCESS");
        AuditTrailEntity audit3 = new AuditTrailEntity("audit-3", "request-2", "CUST002", "OPERATION_1", "Details 3", "SUCCESS");
        
        auditTrailRepository.save(audit1);
        auditTrailRepository.save(audit2);
        auditTrailRepository.save(audit3);
        entityManager.flush();

        // When
        List<AuditTrailEntity> auditTrails = auditTrailRepository.findByRequestId("request-1");

        // Then
        assertThat(auditTrails).hasSize(2);
        assertThat(auditTrails).extracting(AuditTrailEntity::getRequestId)
                               .containsOnly("request-1");
    }

    @Test
    void shouldFindAuditTrailsByCustomerId() {
        // Given
        AuditTrailEntity audit1 = new AuditTrailEntity("audit-1", "request-1", "CUST001", "OPERATION_1", "Details 1", "SUCCESS");
        AuditTrailEntity audit2 = new AuditTrailEntity("audit-2", "request-2", "CUST001", "OPERATION_2", "Details 2", "SUCCESS");
        AuditTrailEntity audit3 = new AuditTrailEntity("audit-3", "request-3", "CUST002", "OPERATION_1", "Details 3", "SUCCESS");
        
        auditTrailRepository.save(audit1);
        auditTrailRepository.save(audit2);
        auditTrailRepository.save(audit3);
        entityManager.flush();

        // When
        List<AuditTrailEntity> auditTrails = auditTrailRepository.findByCustomerId("CUST001");

        // Then
        assertThat(auditTrails).hasSize(2);
        assertThat(auditTrails).extracting(AuditTrailEntity::getCustomerId)
                               .containsOnly("CUST001");
    }

    @Test
    void shouldFindAuditTrailsByOperationType() {
        // Given
        AuditTrailEntity audit1 = new AuditTrailEntity("audit-1", "request-1", "CUST001", "PROMOTION_EVALUATION", "Details 1", "SUCCESS");
        AuditTrailEntity audit2 = new AuditTrailEntity("audit-2", "request-2", "CUST002", "PROMOTION_EVALUATION", "Details 2", "SUCCESS");
        AuditTrailEntity audit3 = new AuditTrailEntity("audit-3", "request-3", "CUST003", "RULE_UPDATE", "Details 3", "SUCCESS");
        
        auditTrailRepository.save(audit1);
        auditTrailRepository.save(audit2);
        auditTrailRepository.save(audit3);
        entityManager.flush();

        // When
        List<AuditTrailEntity> auditTrails = auditTrailRepository.findByOperationType("PROMOTION_EVALUATION");

        // Then
        assertThat(auditTrails).hasSize(2);
        assertThat(auditTrails).extracting(AuditTrailEntity::getOperationType)
                               .containsOnly("PROMOTION_EVALUATION");
    }

    @Test
    void shouldFindFailedAuditTrails() {
        // Given
        AuditTrailEntity successAudit = new AuditTrailEntity("audit-1", "request-1", "CUST001", "OPERATION_1", "Details 1", "SUCCESS");
        AuditTrailEntity errorAudit1 = new AuditTrailEntity("audit-2", "request-2", "CUST002", "OPERATION_2", "Details 2", "ERROR");
        AuditTrailEntity errorAudit2 = new AuditTrailEntity("audit-3", "request-3", "CUST003", "OPERATION_3", "Details 3", "ERROR");
        
        auditTrailRepository.save(successAudit);
        auditTrailRepository.save(errorAudit1);
        auditTrailRepository.save(errorAudit2);
        entityManager.flush();

        // When
        List<AuditTrailEntity> failedAudits = auditTrailRepository.findFailedAuditTrails();

        // Then
        assertThat(failedAudits).hasSize(2);
        assertThat(failedAudits).extracting(AuditTrailEntity::getStatus)
                                .containsOnly("ERROR");
    }

    @Test
    void shouldFindAuditTrailsByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        AuditTrailEntity oldAudit = new AuditTrailEntity("audit-1", "request-1", "CUST001", "OPERATION_1", "Details 1", "SUCCESS");
        oldAudit.setCreatedAt(yesterday.minusHours(1));
        
        AuditTrailEntity recentAudit = new AuditTrailEntity("audit-2", "request-2", "CUST002", "OPERATION_2", "Details 2", "SUCCESS");
        recentAudit.setCreatedAt(now);
        
        auditTrailRepository.save(oldAudit);
        auditTrailRepository.save(recentAudit);
        entityManager.flush();

        // When
        List<AuditTrailEntity> auditTrails = auditTrailRepository.findByDateRange(yesterday, tomorrow);

        // Then
        assertThat(auditTrails).hasSize(2);
    }

    @Test
    void shouldFindAuditTrailsByDateRangeWithPagination() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        for (int i = 1; i <= 5; i++) {
            AuditTrailEntity audit = new AuditTrailEntity("audit-" + i, "request-" + i, "CUST00" + i, "OPERATION", "Details " + i, "SUCCESS");
            audit.setCreatedAt(now.minusHours(i));
            auditTrailRepository.save(audit);
        }
        entityManager.flush();

        // When
        Page<AuditTrailEntity> page = auditTrailRepository.findByDateRange(yesterday, tomorrow, PageRequest.of(0, 3));

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldCountAuditTrailsByOperationTypeAndStatus() {
        // Given
        AuditTrailEntity audit1 = new AuditTrailEntity("audit-1", "request-1", "CUST001", "PROMOTION_EVALUATION", "Details 1", "SUCCESS");
        AuditTrailEntity audit2 = new AuditTrailEntity("audit-2", "request-2", "CUST002", "PROMOTION_EVALUATION", "Details 2", "SUCCESS");
        AuditTrailEntity audit3 = new AuditTrailEntity("audit-3", "request-3", "CUST003", "PROMOTION_EVALUATION", "Details 3", "ERROR");
        AuditTrailEntity audit4 = new AuditTrailEntity("audit-4", "request-4", "CUST004", "RULE_UPDATE", "Details 4", "SUCCESS");
        
        auditTrailRepository.save(audit1);
        auditTrailRepository.save(audit2);
        auditTrailRepository.save(audit3);
        auditTrailRepository.save(audit4);
        entityManager.flush();

        // When & Then
        assertThat(auditTrailRepository.countByOperationTypeAndStatus("PROMOTION_EVALUATION", "SUCCESS")).isEqualTo(2);
        assertThat(auditTrailRepository.countByOperationTypeAndStatus("PROMOTION_EVALUATION", "ERROR")).isEqualTo(1);
        assertThat(auditTrailRepository.countByOperationTypeAndStatus("RULE_UPDATE", "SUCCESS")).isEqualTo(1);
    }

    @Test
    void shouldFindSlowOperations() {
        // Given
        AuditTrailEntity fastAudit = new AuditTrailEntity("audit-1", "request-1", "CUST001", "OPERATION_1", "Details 1", "SUCCESS");
        fastAudit.setExecutionTimeMs(50);
        
        AuditTrailEntity slowAudit1 = new AuditTrailEntity("audit-2", "request-2", "CUST002", "OPERATION_2", "Details 2", "SUCCESS");
        slowAudit1.setExecutionTimeMs(1500);
        
        AuditTrailEntity slowAudit2 = new AuditTrailEntity("audit-3", "request-3", "CUST003", "OPERATION_3", "Details 3", "SUCCESS");
        slowAudit2.setExecutionTimeMs(2000);
        
        auditTrailRepository.save(fastAudit);
        auditTrailRepository.save(slowAudit1);
        auditTrailRepository.save(slowAudit2);
        entityManager.flush();

        // When
        List<AuditTrailEntity> slowOperations = auditTrailRepository.findSlowOperations(1000);

        // Then
        assertThat(slowOperations).hasSize(2);
        assertThat(slowOperations).extracting(AuditTrailEntity::getExecutionTimeMs)
                                  .allMatch(time -> time > 1000);
    }
}