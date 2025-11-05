package com.bank.promotion.adapter.persistence.service;

import com.bank.promotion.adapter.persistence.entity.PromotionRuleEntity;
import com.bank.promotion.adapter.persistence.exception.EntityNotFoundException;
import com.bank.promotion.adapter.persistence.repository.PromotionRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Cached service for PromotionRule operations
 * Provides caching layer over repository operations
 */
@Service
@Transactional
public class CachedPromotionRuleService {

    private static final Logger logger = LoggerFactory.getLogger(CachedPromotionRuleService.class);

    @Autowired
    private PromotionRuleRepository promotionRuleRepository;

    /**
     * Find promotion rule by ID with caching
     */
    @Cacheable(value = "promotionRules", key = "#id")
    public Optional<PromotionRuleEntity> findById(String id) {
        logger.debug("Loading promotion rule from database: {}", id);
        return promotionRuleRepository.findById(id);
    }

    /**
     * Find active promotion rules with caching
     */
    @Cacheable(value = "promotionRules", key = "'active'")
    public List<PromotionRuleEntity> findActivePromotionRules() {
        logger.debug("Loading active promotion rules from database");
        return promotionRuleRepository.findActivePromotionRules();
    }

    /**
     * Find promotion rules by rule type with caching
     */
    @Cacheable(value = "promotionRules", key = "'ruleType:' + #ruleType")
    public List<PromotionRuleEntity> findByRuleType(String ruleType) {
        logger.debug("Loading promotion rules by rule type from database: {}", ruleType);
        return promotionRuleRepository.findByRuleType(ruleType);
    }

    /**
     * Find promotion rules by rule type and status with caching
     */
    @Cacheable(value = "promotionRules", key = "'ruleType:' + #ruleType + ':status:' + #status")
    public List<PromotionRuleEntity> findByRuleTypeAndStatus(String ruleType, String status) {
        logger.debug("Loading promotion rules by rule type and status from database: {} - {}", ruleType, status);
        return promotionRuleRepository.findByRuleTypeAndStatus(ruleType, status);
    }

    /**
     * Find promotion rules by status with caching
     */
    @Cacheable(value = "promotionRules", key = "'status:' + #status")
    public List<PromotionRuleEntity> findByStatus(String status) {
        logger.debug("Loading promotion rules by status from database: {}", status);
        return promotionRuleRepository.findByStatus(status);
    }

    /**
     * Save promotion rule and update cache
     */
    @CachePut(value = "promotionRules", key = "#promotionRule.id")
    public PromotionRuleEntity save(PromotionRuleEntity promotionRule) {
        logger.debug("Saving promotion rule: {}", promotionRule.getId());
        PromotionRuleEntity saved = promotionRuleRepository.save(promotionRule);
        
        // Evict related caches
        evictStatusCache(promotionRule.getStatus());
        evictRuleTypeCache(promotionRule.getRuleType());
        evictRuleTypeAndStatusCache(promotionRule.getRuleType(), promotionRule.getStatus());
        evictActiveCache();
        
        return saved;
    }

    /**
     * Delete promotion rule and evict from cache
     */
    @CacheEvict(value = "promotionRules", key = "#id")
    public void deleteById(String id) {
        logger.debug("Deleting promotion rule: {}", id);
        
        // Get the entity first to know its properties for cache eviction
        Optional<PromotionRuleEntity> entity = promotionRuleRepository.findById(id);
        
        promotionRuleRepository.deleteById(id);
        
        // Evict related caches
        if (entity.isPresent()) {
            PromotionRuleEntity rule = entity.get();
            evictStatusCache(rule.getStatus());
            evictRuleTypeCache(rule.getRuleType());
            evictRuleTypeAndStatusCache(rule.getRuleType(), rule.getStatus());
            evictActiveCache();
        }
    }

    /**
     * Update promotion rule status and update cache
     */
    @CachePut(value = "promotionRules", key = "#id")
    public PromotionRuleEntity updateStatus(String id, String newStatus) {
        logger.debug("Updating promotion rule status: {} -> {}", id, newStatus);
        
        PromotionRuleEntity entity = promotionRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PromotionRule", id));
        
        String oldStatus = entity.getStatus();
        String ruleType = entity.getRuleType();
        entity.setStatus(newStatus);
        
        PromotionRuleEntity updated = promotionRuleRepository.save(entity);
        
        // Evict related caches
        evictStatusCache(oldStatus);
        evictStatusCache(newStatus);
        evictRuleTypeAndStatusCache(ruleType, oldStatus);
        evictRuleTypeAndStatusCache(ruleType, newStatus);
        evictActiveCache();
        
        return updated;
    }

    /**
     * Evict all promotion rule caches
     */
    @CacheEvict(value = "promotionRules", allEntries = true)
    public void evictAllCaches() {
        logger.debug("Evicting all promotion rule caches");
    }

    /**
     * Evict status-specific cache
     */
    @CacheEvict(value = "promotionRules", key = "'status:' + #status")
    public void evictStatusCache(String status) {
        logger.debug("Evicting promotion rule status cache: {}", status);
    }

    /**
     * Evict rule type-specific cache
     */
    @CacheEvict(value = "promotionRules", key = "'ruleType:' + #ruleType")
    public void evictRuleTypeCache(String ruleType) {
        logger.debug("Evicting promotion rule type cache: {}", ruleType);
    }

    /**
     * Evict rule type and status-specific cache
     */
    @CacheEvict(value = "promotionRules", key = "'ruleType:' + #ruleType + ':status:' + #status")
    public void evictRuleTypeAndStatusCache(String ruleType, String status) {
        logger.debug("Evicting promotion rule type and status cache: {} - {}", ruleType, status);
    }

    /**
     * Evict active promotion rules cache
     */
    @CacheEvict(value = "promotionRules", key = "'active'")
    public void evictActiveCache() {
        logger.debug("Evicting active promotion rules cache");
    }
}