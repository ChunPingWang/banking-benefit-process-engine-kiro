package com.bank.promotion.adapter.config;

import com.bank.promotion.adapter.persistence.entity.AuditTrailEntity;
import com.bank.promotion.adapter.persistence.entity.SystemEventEntity;
import com.bank.promotion.adapter.persistence.repository.AuditTrailRepository;
import com.bank.promotion.adapter.persistence.repository.SystemEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AOP Aspect for automatic audit trail recording
 * Intercepts method calls and records audit information
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private SystemEventRepository systemEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Intercepts promotion evaluation methods to record audit trail
     */
    @Around("execution(* com.bank.promotion.application.service.PromotionApplicationService.evaluatePromotion(..))")
    public Object auditPromotionEvaluation(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        try {
            // Extract customer ID from method arguments
            Object[] args = joinPoint.getArgs();
            String customerId = extractCustomerId(args);
            
            // Record start of operation
            recordAuditTrail(requestId, customerId, "PROMOTION_EVALUATION_START", 
                           "Started promotion evaluation", "SUCCESS", null, null);
            
            // Execute the actual method
            Object result = joinPoint.proceed();
            
            // Record successful completion
            long executionTime = System.currentTimeMillis() - startTime;
            recordAuditTrail(requestId, customerId, "PROMOTION_EVALUATION_COMPLETE", 
                           "Completed promotion evaluation successfully", "SUCCESS", 
                           (int) executionTime, null);
            
            return result;
            
        } catch (Exception e) {
            // Record error
            long executionTime = System.currentTimeMillis() - startTime;
            String customerId = extractCustomerId(joinPoint.getArgs());
            recordAuditTrail(requestId, customerId, "PROMOTION_EVALUATION_ERROR", 
                           "Error during promotion evaluation", "ERROR", 
                           (int) executionTime, e.getMessage());
            
            // Record system event for error
            recordSystemEvent("PROMOTION_EVALUATION_ERROR", "ERROR", 
                            "Error during promotion evaluation: " + e.getMessage(), 
                            "ERROR", "PromotionApplicationService", requestId);
            
            throw e;
        }
    }

    /**
     * Intercepts decision tree execution methods
     */
    @Around("execution(* com.bank.promotion.domain.aggregate.PromotionDecisionTree.evaluate(..))")
    public Object auditDecisionTreeExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            recordSystemEvent("DECISION_TREE_EXECUTION", "BUSINESS_LOGIC", 
                            "Decision tree executed successfully", 
                            "INFO", "PromotionDecisionTree", requestId);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordSystemEvent("DECISION_TREE_ERROR", "ERROR", 
                            "Decision tree execution failed: " + e.getMessage(), 
                            "ERROR", "PromotionDecisionTree", requestId);
            throw e;
        }
    }

    /**
     * Asynchronously record audit trail
     */
    @Async
    public void recordAuditTrail(String requestId, String customerId, String operationType, 
                                String operationDetails, String status, Integer executionTimeMs, 
                                String errorMessage) {
        try {
            AuditTrailEntity auditTrail = new AuditTrailEntity();
            auditTrail.setId(UUID.randomUUID().toString());
            auditTrail.setRequestId(requestId);
            auditTrail.setCustomerId(customerId != null ? customerId : "UNKNOWN");
            auditTrail.setOperationType(operationType);
            auditTrail.setOperationDetails(operationDetails);
            auditTrail.setStatus(status);
            auditTrail.setExecutionTimeMs(executionTimeMs);
            auditTrail.setErrorMessage(errorMessage);
            
            auditTrailRepository.save(auditTrail);
            
        } catch (Exception e) {
            logger.error("Failed to record audit trail: {}", e.getMessage(), e);
        }
    }

    /**
     * Asynchronously record system event
     */
    @Async
    public void recordSystemEvent(String eventType, String eventCategory, String eventDetails, 
                                 String severityLevel, String sourceComponent, String correlationId) {
        try {
            SystemEventEntity systemEvent = new SystemEventEntity();
            systemEvent.setId(UUID.randomUUID().toString());
            systemEvent.setEventType(eventType);
            systemEvent.setEventCategory(eventCategory);
            systemEvent.setEventDetails(eventDetails);
            systemEvent.setSeverityLevel(severityLevel);
            systemEvent.setSourceComponent(sourceComponent);
            systemEvent.setCorrelationId(correlationId);
            
            systemEventRepository.save(systemEvent);
            
        } catch (Exception e) {
            logger.error("Failed to record system event: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract customer ID from method arguments
     */
    private String extractCustomerId(Object[] args) {
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg != null) {
                    // Try to extract customer ID from various argument types
                    try {
                        if (arg.getClass().getSimpleName().contains("Customer")) {
                            // Use reflection to get customer ID field
                            return arg.toString(); // Simplified for now
                        }
                    } catch (Exception e) {
                        logger.debug("Could not extract customer ID from argument: {}", arg.getClass());
                    }
                }
            }
        }
        return "UNKNOWN";
    }
}