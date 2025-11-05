package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.RequestLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RequestLog entities
 * Provides CRUD operations and custom queries for audit purposes
 */
@Repository
public interface RequestLogRepository extends JpaRepository<RequestLogEntity, String>, 
                                            JpaSpecificationExecutor<RequestLogEntity> {

    /**
     * Find request log by request ID
     */
    Optional<RequestLogEntity> findByRequestId(String requestId);

    /**
     * Find request logs by API endpoint
     */
    List<RequestLogEntity> findByApiEndpoint(String apiEndpoint);

    /**
     * Find request logs by API endpoint with pagination
     */
    Page<RequestLogEntity> findByApiEndpoint(String apiEndpoint, Pageable pageable);

    /**
     * Find request logs by HTTP method
     */
    List<RequestLogEntity> findByHttpMethod(String httpMethod);

    /**
     * Find request logs by response status
     */
    List<RequestLogEntity> findByResponseStatus(Integer responseStatus);

    /**
     * Find request logs by date range
     */
    @Query("SELECT rl FROM RequestLogEntity rl WHERE rl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY rl.createdAt DESC")
    List<RequestLogEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find request logs by date range with pagination
     */
    @Query("SELECT rl FROM RequestLogEntity rl WHERE rl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY rl.createdAt DESC")
    Page<RequestLogEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Find request logs by endpoint and date range
     */
    @Query("SELECT rl FROM RequestLogEntity rl WHERE rl.apiEndpoint = :endpoint " +
           "AND rl.createdAt BETWEEN :startDate AND :endDate ORDER BY rl.createdAt DESC")
    List<RequestLogEntity> findByEndpointAndDateRange(@Param("endpoint") String endpoint,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find failed requests (4xx and 5xx status codes)
     */
    @Query("SELECT rl FROM RequestLogEntity rl WHERE rl.responseStatus >= 400 " +
           "ORDER BY rl.createdAt DESC")
    List<RequestLogEntity> findFailedRequests();

    /**
     * Find slow requests (processing time > threshold)
     */
    @Query("SELECT rl FROM RequestLogEntity rl WHERE rl.processingTimeMs > :thresholdMs " +
           "ORDER BY rl.processingTimeMs DESC")
    List<RequestLogEntity> findSlowRequests(@Param("thresholdMs") Integer thresholdMs);

    /**
     * Find request logs with audit trails
     */
    @Query("SELECT DISTINCT rl FROM RequestLogEntity rl LEFT JOIN FETCH rl.auditTrails WHERE rl.requestId = :requestId")
    Optional<RequestLogEntity> findByRequestIdWithAuditTrails(@Param("requestId") String requestId);

    /**
     * Find request logs with decision steps
     */
    @Query("SELECT DISTINCT rl FROM RequestLogEntity rl LEFT JOIN FETCH rl.decisionSteps WHERE rl.requestId = :requestId")
    Optional<RequestLogEntity> findByRequestIdWithDecisionSteps(@Param("requestId") String requestId);

    /**
     * Count requests by endpoint and date range
     */
    @Query("SELECT COUNT(rl) FROM RequestLogEntity rl WHERE rl.apiEndpoint = :endpoint " +
           "AND rl.createdAt BETWEEN :startDate AND :endDate")
    long countByEndpointAndDateRange(@Param("endpoint") String endpoint,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate average processing time by endpoint
     */
    @Query("SELECT AVG(rl.processingTimeMs) FROM RequestLogEntity rl WHERE rl.apiEndpoint = :endpoint " +
           "AND rl.processingTimeMs IS NOT NULL")
    Double calculateAverageProcessingTime(@Param("endpoint") String endpoint);
}