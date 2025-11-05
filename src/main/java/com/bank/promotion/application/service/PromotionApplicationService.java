package com.bank.promotion.application.service;

import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.application.command.UpdatePromotionRuleCommand;
import com.bank.promotion.application.command.handler.CreateDecisionTreeCommandHandler;
import com.bank.promotion.application.command.handler.EvaluatePromotionCommandHandler;
import com.bank.promotion.application.command.handler.UpdatePromotionRuleCommandHandler;
import com.bank.promotion.application.query.GetAvailablePromotionsQuery;
import com.bank.promotion.application.query.GetPromotionHistoryQuery;
import com.bank.promotion.application.query.handler.GetAvailablePromotionsQueryHandler;
import com.bank.promotion.application.query.handler.GetPromotionHistoryQueryHandler;
import com.bank.promotion.application.query.view.AvailablePromotionView;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 優惠應用服務
 * 協調命令和查詢處理器，提供統一的應用層介面
 */
@Service
@Transactional
public class PromotionApplicationService {
    
    private final CreateDecisionTreeCommandHandler createDecisionTreeHandler;
    private final UpdatePromotionRuleCommandHandler updatePromotionRuleHandler;
    private final EvaluatePromotionCommandHandler evaluatePromotionHandler;
    private final GetPromotionHistoryQueryHandler getPromotionHistoryHandler;
    private final GetAvailablePromotionsQueryHandler getAvailablePromotionsHandler;
    private final AuditService auditService;
    private final PerformanceMonitoringService performanceMonitoringService;
    
    @Autowired
    public PromotionApplicationService(
            CreateDecisionTreeCommandHandler createDecisionTreeHandler,
            UpdatePromotionRuleCommandHandler updatePromotionRuleHandler,
            EvaluatePromotionCommandHandler evaluatePromotionHandler,
            GetPromotionHistoryQueryHandler getPromotionHistoryHandler,
            GetAvailablePromotionsQueryHandler getAvailablePromotionsHandler,
            AuditService auditService,
            PerformanceMonitoringService performanceMonitoringService) {
        this.createDecisionTreeHandler = createDecisionTreeHandler;
        this.updatePromotionRuleHandler = updatePromotionRuleHandler;
        this.evaluatePromotionHandler = evaluatePromotionHandler;
        this.getPromotionHistoryHandler = getPromotionHistoryHandler;
        this.getAvailablePromotionsHandler = getAvailablePromotionsHandler;
        this.auditService = auditService;
        this.performanceMonitoringService = performanceMonitoringService;
    }
    
    /**
     * 創建決策樹
     */
    public String createDecisionTree(CreateDecisionTreeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            String treeId = createDecisionTreeHandler.handle(command);
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationTime("CREATE_DECISION_TREE", executionTime);
            
            return treeId;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationError("CREATE_DECISION_TREE", executionTime, e);
            throw new RuntimeException("Failed to create decision tree", e);
        }
    }
    
    /**
     * 更新優惠規則
     */
    public void updatePromotionRule(UpdatePromotionRuleCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            updatePromotionRuleHandler.handle(command);
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationTime("UPDATE_PROMOTION_RULE", executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationError("UPDATE_PROMOTION_RULE", executionTime, e);
            throw new RuntimeException("Failed to update promotion rule", e);
        }
    }
    
    /**
     * 評估優惠
     */
    public PromotionResult evaluatePromotion(EvaluatePromotionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            PromotionResult result = evaluatePromotionHandler.handle(command);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 記錄稽核軌跡
            auditService.recordPromotionEvaluation(
                command.getRequestId(),
                command.getCustomerPayload(),
                result,
                executionTime
            );
            
            performanceMonitoringService.recordOperationTime("EVALUATE_PROMOTION", executionTime);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 記錄錯誤的稽核軌跡
            auditService.recordPromotionEvaluation(
                command.getRequestId(),
                command.getCustomerPayload(),
                createErrorPromotionResult(e),
                executionTime
            );
            
            performanceMonitoringService.recordOperationError("EVALUATE_PROMOTION", executionTime, e);
            throw new RuntimeException("Failed to evaluate promotion", e);
        }
    }
    
    /**
     * 查詢優惠歷史
     */
    @Transactional(readOnly = true)
    public PagedResult<PromotionHistoryView> getPromotionHistory(GetPromotionHistoryQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            PagedResult<PromotionHistoryView> result = getPromotionHistoryHandler.handle(query);
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationTime("GET_PROMOTION_HISTORY", executionTime);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationError("GET_PROMOTION_HISTORY", executionTime, e);
            throw new RuntimeException("Failed to get promotion history", e);
        }
    }
    
    /**
     * 查詢可用優惠
     */
    @Transactional(readOnly = true)
    public List<AvailablePromotionView> getAvailablePromotions(GetAvailablePromotionsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<AvailablePromotionView> result = getAvailablePromotionsHandler.handle(query);
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationTime("GET_AVAILABLE_PROMOTIONS", executionTime);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperationError("GET_AVAILABLE_PROMOTIONS", executionTime, e);
            throw new RuntimeException("Failed to get available promotions", e);
        }
    }
    
    private PromotionResult createErrorPromotionResult(Exception e) {
        return new PromotionResult(
            "ERROR",
            "系統錯誤",
            "ERROR",
            null,
            null,
            "系統處理時發生錯誤: " + e.getMessage(),
            null,
            null,
            false
        );
    }
}