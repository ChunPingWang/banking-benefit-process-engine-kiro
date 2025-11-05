package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.AuditTrailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditTrail entities
 * Provides CRUD operations and custom queries for audit trail management
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, String>, 
                                            JpaSpecificationExecutor<AuditTrailEntity> {

    /**
     * Find audit trails by request ID
     */
    List<AuditTrailEntity> findByRequestId(String requestId);

    /**
     * Find audit trails by request ID ordered by creation time
     */
    List<AuditTrailEntity> findByRequestIdOrderByCreatedAt(String requestId);

    /**
     * Find audit trails by customer ID
     */
    List<AuditTrailEntity> findByCustomerId(String customerId);

    /**
     * Find audit trails by customer ID with pagination
     */
    Page<AuditTrailEntity> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find audit trails by operation type
     */
    List<AuditTrailEntity> findByOperationType(String operationType);

    /**
     * Find audit trails by status
     */
    List<AuditTrailEntity> findByStatus(String status);

    /**
     * Find audit trails by customer ID and operation type
     */
    List<AuditTrailEntity> findByCustomerIdAndOperationType(String customerId, String operationType);

    /**
     * Find audit trails by customer ID and date range
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.customerId = :customerId " +
           "AND at.createdAt BETWEEN :startDate AND :endDate ORDER BY at.createdAt DESC")
    List<AuditTrailEntity> findByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit trails by date range
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY at.createdAt DESC")
    List<AuditTrailEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit trails by date range with pagination
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY at.createdAt DESC")
    Page<AuditTrailEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Find failed audit trails (ERROR status)
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.status = 'ERROR' ORDER BY at.createdAt DESC")
    List<AuditTrailEntity> findFailedAuditTrails();

    /**
     * Find audit trails by operation type and date range
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.operationType = :operationType " +
           "AND at.createdAt BETWEEN :startDate AND :endDate ORDER BY at.createdAt DESC")
    List<AuditTrailEntity> findByOperationTypeAndDateRange(@Param("operationType") String operationType,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find slow operations (execution time > threshold)
     */
    @Query("SELECT at FROM AuditTrailEntity at WHERE at.executionTimeMs > :thresholdMs " +
           "ORDER BY at.executionTimeMs DESC")
    List<AuditTrailEntity> findSlowOperations(@Param("thresholdMs") Integer thresholdMs);

    /**
     * Count audit trails by customer ID and date range
     */
    @Query("SELECT COUNT(at) FROM AuditTrailEntity at WHERE at.customerId = :customerId " +
           "AND at.createdAt BETWEEN :startDate AND :endDate")
    long countByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Count audit trails by operation type and status
     */
    long countByOperationTypeAndStatus(String operationType, String status);

    /**
     * Calculate average execution time by operation type
     */
    @Query("SELECT AVG(at.executionTimeMs) FROM AuditTrailEntity at WHERE at.operationType = :operationType " +
           "AND at.executionTimeMs IS NOT NULL")
    Double calculateAverageExecutionTime(@Param("operationType") String operationType);
}