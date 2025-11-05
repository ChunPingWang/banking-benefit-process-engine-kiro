package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.PromotionHistoryEntity;
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
 * Repository interface for PromotionHistory entities
 * Provides CRUD operations and custom queries
 */
@Repository
public interface PromotionHistoryRepository extends JpaRepository<PromotionHistoryEntity, String>, 
                                                  JpaSpecificationExecutor<PromotionHistoryEntity> {

    /**
     * Find promotion history by customer ID
     */
    List<PromotionHistoryEntity> findByCustomerId(String customerId);

    /**
     * Find promotion history by customer ID with pagination
     */
    Page<PromotionHistoryEntity> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find promotion history by customer ID ordered by execution date descending
     */
    List<PromotionHistoryEntity> findByCustomerIdOrderByExecutedAtDesc(String customerId);

    /**
     * Find promotion history by promotion ID
     */
    List<PromotionHistoryEntity> findByPromotionId(String promotionId);

    /**
     * Find promotion history by customer ID and date range
     */
    @Query("SELECT ph FROM PromotionHistoryEntity ph WHERE ph.customerId = :customerId " +
           "AND ph.executedAt BETWEEN :startDate AND :endDate ORDER BY ph.executedAt DESC")
    List<PromotionHistoryEntity> findByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Find promotion history by date range
     */
    @Query("SELECT ph FROM PromotionHistoryEntity ph WHERE ph.executedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ph.executedAt DESC")
    List<PromotionHistoryEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find promotion history by date range with pagination
     */
    @Query("SELECT ph FROM PromotionHistoryEntity ph WHERE ph.executedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ph.executedAt DESC")
    Page<PromotionHistoryEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    /**
     * Count promotion history by customer ID
     */
    long countByCustomerId(String customerId);

    /**
     * Count promotion history by customer ID and date range
     */
    @Query("SELECT COUNT(ph) FROM PromotionHistoryEntity ph WHERE ph.customerId = :customerId " +
           "AND ph.executedAt BETWEEN :startDate AND :endDate")
    long countByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent promotion history (last 30 days)
     */
    @Query("SELECT ph FROM PromotionHistoryEntity ph WHERE ph.executedAt >= :thirtyDaysAgo " +
           "ORDER BY ph.executedAt DESC")
    List<PromotionHistoryEntity> findRecentPromotionHistory(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}