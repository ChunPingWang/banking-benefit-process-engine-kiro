package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.SystemEventEntity;
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
 * Repository interface for SystemEvent entities
 * Provides CRUD operations and custom queries for system event management
 */
@Repository
public interface SystemEventRepository extends JpaRepository<SystemEventEntity, String>, 
                                             JpaSpecificationExecutor<SystemEventEntity> {

    /**
     * Find system events by event type
     */
    List<SystemEventEntity> findByEventType(String eventType);

    /**
     * Find system events by event category
     */
    List<SystemEventEntity> findByEventCategory(String eventCategory);

    /**
     * Find system events by severity level
     */
    List<SystemEventEntity> findBySeverityLevel(String severityLevel);

    /**
     * Find system events by source component
     */
    List<SystemEventEntity> findBySourceComponent(String sourceComponent);

    /**
     * Find system events by correlation ID
     */
    List<SystemEventEntity> findByCorrelationId(String correlationId);

    /**
     * Find system events by event type and date range
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.eventType = :eventType " +
           "AND se.createdAt BETWEEN :startDate AND :endDate ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findByEventTypeAndDateRange(@Param("eventType") String eventType,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find system events by severity level and date range
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.severityLevel = :severityLevel " +
           "AND se.createdAt BETWEEN :startDate AND :endDate ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findBySeverityLevelAndDateRange(@Param("severityLevel") String severityLevel,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find system events by date range
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find system events by date range with pagination
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY se.createdAt DESC")
    Page<SystemEventEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    /**
     * Find error events (ERROR and CRITICAL severity levels)
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.severityLevel IN ('ERROR', 'CRITICAL') " +
           "ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findErrorEvents();

    /**
     * Find warning events (WARN severity level)
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.severityLevel = 'WARN' " +
           "ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findWarningEvents();

    /**
     * Find system events by source component and date range
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.sourceComponent = :sourceComponent " +
           "AND se.createdAt BETWEEN :startDate AND :endDate ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findBySourceComponentAndDateRange(@Param("sourceComponent") String sourceComponent,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent system events (last 24 hours)
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE se.createdAt >= :twentyFourHoursAgo " +
           "ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findRecentSystemEvents(@Param("twentyFourHoursAgo") LocalDateTime twentyFourHoursAgo);

    /**
     * Count system events by event type and date range
     */
    @Query("SELECT COUNT(se) FROM SystemEventEntity se WHERE se.eventType = :eventType " +
           "AND se.createdAt BETWEEN :startDate AND :endDate")
    long countByEventTypeAndDateRange(@Param("eventType") String eventType,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Count system events by severity level and date range
     */
    @Query("SELECT COUNT(se) FROM SystemEventEntity se WHERE se.severityLevel = :severityLevel " +
           "AND se.createdAt BETWEEN :startDate AND :endDate")
    long countBySeverityLevelAndDateRange(@Param("severityLevel") String severityLevel,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Count system events by source component
     */
    long countBySourceComponent(String sourceComponent);

    /**
     * Find system events by multiple criteria
     */
    @Query("SELECT se FROM SystemEventEntity se WHERE " +
           "(:eventType IS NULL OR se.eventType = :eventType) AND " +
           "(:severityLevel IS NULL OR se.severityLevel = :severityLevel) AND " +
           "(:sourceComponent IS NULL OR se.sourceComponent = :sourceComponent) AND " +
           "se.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY se.createdAt DESC")
    List<SystemEventEntity> findByMultipleCriteria(@Param("eventType") String eventType,
                                                  @Param("severityLevel") String severityLevel,
                                                  @Param("sourceComponent") String sourceComponent,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
}