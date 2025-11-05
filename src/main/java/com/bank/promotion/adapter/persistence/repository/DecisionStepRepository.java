package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.DecisionStepEntity;
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
 * Repository interface for DecisionStep entities
 * Provides CRUD operations and custom queries for decision step tracking
 */
@Repository
public interface DecisionStepRepository extends JpaRepository<DecisionStepEntity, String>, 
                                              JpaSpecificationExecutor<DecisionStepEntity> {

    /**
     * Find decision steps by request ID
     */
    List<DecisionStepEntity> findByRequestId(String requestId);

    /**
     * Find decision steps by request ID ordered by step order
     */
    List<DecisionStepEntity> findByRequestIdOrderByStepOrder(String requestId);

    /**
     * Find decision steps by tree ID
     */
    List<DecisionStepEntity> findByTreeId(String treeId);

    /**
     * Find decision steps by tree ID and node ID
     */
    List<DecisionStepEntity> findByTreeIdAndNodeId(String treeId, String nodeId);

    /**
     * Find decision steps by node type
     */
    List<DecisionStepEntity> findByNodeType(String nodeType);

    /**
     * Find decision steps by status
     */
    List<DecisionStepEntity> findByStatus(String status);

    /**
     * Find decision steps by tree ID and date range
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.treeId = :treeId " +
           "AND ds.createdAt BETWEEN :startDate AND :endDate ORDER BY ds.createdAt DESC")
    List<DecisionStepEntity> findByTreeIdAndDateRange(@Param("treeId") String treeId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find decision steps by date range
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ds.createdAt DESC")
    List<DecisionStepEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find decision steps by date range with pagination
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ds.createdAt DESC")
    Page<DecisionStepEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    /**
     * Find failed decision steps (ERROR status)
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.status = 'ERROR' ORDER BY ds.createdAt DESC")
    List<DecisionStepEntity> findFailedDecisionSteps();

    /**
     * Find slow decision steps (execution time > threshold)
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.executionTimeMs > :thresholdMs " +
           "ORDER BY ds.executionTimeMs DESC")
    List<DecisionStepEntity> findSlowDecisionSteps(@Param("thresholdMs") Integer thresholdMs);

    /**
     * Find decision steps by node type and date range
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.nodeType = :nodeType " +
           "AND ds.createdAt BETWEEN :startDate AND :endDate ORDER BY ds.createdAt DESC")
    List<DecisionStepEntity> findByNodeTypeAndDateRange(@Param("nodeType") String nodeType,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Count decision steps by request ID
     */
    long countByRequestId(String requestId);

    /**
     * Count decision steps by tree ID and status
     */
    long countByTreeIdAndStatus(String treeId, String status);

    /**
     * Count decision steps by node type and status
     */
    long countByNodeTypeAndStatus(String nodeType, String status);

    /**
     * Calculate average execution time by node type
     */
    @Query("SELECT AVG(ds.executionTimeMs) FROM DecisionStepEntity ds WHERE ds.nodeType = :nodeType " +
           "AND ds.executionTimeMs IS NOT NULL")
    Double calculateAverageExecutionTimeByNodeType(@Param("nodeType") String nodeType);

    /**
     * Calculate average execution time by tree ID
     */
    @Query("SELECT AVG(ds.executionTimeMs) FROM DecisionStepEntity ds WHERE ds.treeId = :treeId " +
           "AND ds.executionTimeMs IS NOT NULL")
    Double calculateAverageExecutionTimeByTreeId(@Param("treeId") String treeId);

    /**
     * Find decision path for a request (all steps ordered by step order)
     */
    @Query("SELECT ds FROM DecisionStepEntity ds WHERE ds.requestId = :requestId " +
           "ORDER BY ds.stepOrder ASC")
    List<DecisionStepEntity> findDecisionPathByRequestId(@Param("requestId") String requestId);
}