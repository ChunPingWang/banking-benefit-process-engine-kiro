package com.bank.promotion.adapter.persistence.service;

import com.bank.promotion.adapter.persistence.entity.DecisionTreeEntity;
import com.bank.promotion.adapter.persistence.exception.EntityNotFoundException;
import com.bank.promotion.adapter.persistence.repository.DecisionTreeRepository;
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
 * Cached service for DecisionTree operations
 * Provides caching layer over repository operations
 */
@Service
@Transactional
public class CachedDecisionTreeService {

    private static final Logger logger = LoggerFactory.getLogger(CachedDecisionTreeService.class);

    @Autowired
    private DecisionTreeRepository decisionTreeRepository;

    /**
     * Find decision tree by ID with caching
     */
    @Cacheable(value = "decisionTrees", key = "#id")
    public Optional<DecisionTreeEntity> findById(String id) {
        logger.debug("Loading decision tree from database: {}", id);
        return decisionTreeRepository.findById(id);
    }

    /**
     * Find decision tree by ID with nodes, cached
     */
    @Cacheable(value = "decisionTrees", key = "'withNodes:' + #id")
    public Optional<DecisionTreeEntity> findByIdWithNodes(String id) {
        logger.debug("Loading decision tree with nodes from database: {}", id);
        return decisionTreeRepository.findByIdWithNodes(id);
    }

    /**
     * Find active decision trees with caching
     */
    @Cacheable(value = "decisionTrees", key = "'active'")
    public List<DecisionTreeEntity> findActiveDecisionTrees() {
        logger.debug("Loading active decision trees from database");
        return decisionTreeRepository.findActiveDecisionTrees();
    }

    /**
     * Find decision trees by status with caching
     */
    @Cacheable(value = "decisionTrees", key = "'status:' + #status")
    public List<DecisionTreeEntity> findByStatus(String status) {
        logger.debug("Loading decision trees by status from database: {}", status);
        return decisionTreeRepository.findByStatus(status);
    }

    /**
     * Save decision tree and update cache
     */
    @CachePut(value = "decisionTrees", key = "#decisionTree.id")
    public DecisionTreeEntity save(DecisionTreeEntity decisionTree) {
        logger.debug("Saving decision tree: {}", decisionTree.getId());
        DecisionTreeEntity saved = decisionTreeRepository.save(decisionTree);
        
        // Evict related caches
        evictStatusCache(decisionTree.getStatus());
        evictActiveCache();
        
        return saved;
    }

    /**
     * Delete decision tree and evict from cache
     */
    @CacheEvict(value = "decisionTrees", key = "#id")
    public void deleteById(String id) {
        logger.debug("Deleting decision tree: {}", id);
        
        // Get the entity first to know its status for cache eviction
        Optional<DecisionTreeEntity> entity = decisionTreeRepository.findById(id);
        
        decisionTreeRepository.deleteById(id);
        
        // Evict related caches
        if (entity.isPresent()) {
            evictStatusCache(entity.get().getStatus());
            evictActiveCache();
        }
    }

    /**
     * Update decision tree status and update cache
     */
    @CachePut(value = "decisionTrees", key = "#id")
    public DecisionTreeEntity updateStatus(String id, String newStatus) {
        logger.debug("Updating decision tree status: {} -> {}", id, newStatus);
        
        DecisionTreeEntity entity = decisionTreeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DecisionTree", id));
        
        String oldStatus = entity.getStatus();
        entity.setStatus(newStatus);
        
        DecisionTreeEntity updated = decisionTreeRepository.save(entity);
        
        // Evict related caches
        evictStatusCache(oldStatus);
        evictStatusCache(newStatus);
        evictActiveCache();
        
        return updated;
    }

    /**
     * Evict all decision tree caches
     */
    @CacheEvict(value = "decisionTrees", allEntries = true)
    public void evictAllCaches() {
        logger.debug("Evicting all decision tree caches");
    }

    /**
     * Evict status-specific cache
     */
    @CacheEvict(value = "decisionTrees", key = "'status:' + #status")
    public void evictStatusCache(String status) {
        logger.debug("Evicting decision tree status cache: {}", status);
    }

    /**
     * Evict active decision trees cache
     */
    @CacheEvict(value = "decisionTrees", key = "'active'")
    public void evictActiveCache() {
        logger.debug("Evicting active decision trees cache");
    }
}