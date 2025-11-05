package com.bank.promotion.adapter.persistence.service;

import com.bank.promotion.adapter.persistence.entity.PromotionHistoryEntity;
import com.bank.promotion.adapter.persistence.repository.PromotionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Async service for PromotionHistory operations
 * Provides non-blocking operations for promotion history recording
 */
@Service
public class AsyncPromotionHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPromotionHistoryService.class);

    @Autowired
    private PromotionHistoryRepository promotionHistoryRepository;

    /**
     * Asynchronously save promotion history
     */
    @Async
    @Transactional
    public CompletableFuture<PromotionHistoryEntity> saveAsync(PromotionHistoryEntity promotionHistory) {
        try {
            logger.debug("Asynchronously saving promotion history for customer: {}", 
                        promotionHistory.getCustomerId());
            
            PromotionHistoryEntity saved = promotionHistoryRepository.save(promotionHistory);
            
            logger.debug("Successfully saved promotion history: {}", saved.getId());
            return CompletableFuture.completedFuture(saved);
            
        } catch (Exception e) {
            logger.error("Failed to save promotion history asynchronously for customer: {}", 
                        promotionHistory.getCustomerId(), e);
            
            CompletableFuture<PromotionHistoryEntity> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously save multiple promotion history records
     */
    @Async
    @Transactional
    public CompletableFuture<Void> saveAllAsync(Iterable<PromotionHistoryEntity> promotionHistories) {
        try {
            logger.debug("Asynchronously saving multiple promotion history records");
            
            promotionHistoryRepository.saveAll(promotionHistories);
            
            logger.debug("Successfully saved multiple promotion history records");
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            logger.error("Failed to save multiple promotion history records asynchronously", e);
            
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously record promotion evaluation result
     */
    @Async
    @Transactional
    public CompletableFuture<Void> recordPromotionEvaluationAsync(String customerId, 
                                                                 String promotionId, 
                                                                 String promotionResult) {
        try {
            logger.debug("Asynchronously recording promotion evaluation for customer: {}", customerId);
            
            PromotionHistoryEntity history = new PromotionHistoryEntity();
            history.setId(java.util.UUID.randomUUID().toString());
            history.setCustomerId(customerId);
            history.setPromotionId(promotionId);
            history.setPromotionResult(promotionResult);
            
            promotionHistoryRepository.save(history);
            
            logger.debug("Successfully recorded promotion evaluation for customer: {}", customerId);
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            logger.error("Failed to record promotion evaluation asynchronously for customer: {}", 
                        customerId, e);
            
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}