package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.PromotionRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PromotionRule entities
 * Provides CRUD operations and custom queries
 */
@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRuleEntity, String>, 
                                               JpaSpecificationExecutor<PromotionRuleEntity> {

    /**
     * Find promotion rules by status
     */
    List<PromotionRuleEntity> findByStatus(String status);

    /**
     * Find promotion rules by rule type
     */
    List<PromotionRuleEntity> findByRuleType(String ruleType);

    /**
     * Find promotion rules by rule type and status
     */
    List<PromotionRuleEntity> findByRuleTypeAndStatus(String ruleType, String status);

    /**
     * Find promotion rule by name
     */
    Optional<PromotionRuleEntity> findByName(String name);

    /**
     * Find active promotion rules
     */
    @Query("SELECT pr FROM PromotionRuleEntity pr WHERE pr.status = 'ACTIVE'")
    List<PromotionRuleEntity> findActivePromotionRules();

    /**
     * Find promotion rules updated after specific date
     */
    @Query("SELECT pr FROM PromotionRuleEntity pr WHERE pr.updatedAt > :date")
    List<PromotionRuleEntity> findUpdatedAfter(@Param("date") LocalDateTime date);

    /**
     * Find promotion rules by rule type and status ordered by updated date
     */
    List<PromotionRuleEntity> findByRuleTypeAndStatusOrderByUpdatedAtDesc(String ruleType, String status);

    /**
     * Check if promotion rule exists by name
     */
    boolean existsByName(String name);

    /**
     * Count promotion rules by status
     */
    long countByStatus(String status);

    /**
     * Count promotion rules by rule type
     */
    long countByRuleType(String ruleType);
}