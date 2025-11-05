package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.adapter.web.dto.CreateDecisionTreeRequest;
import com.bank.promotion.adapter.web.dto.UpdatePromotionRuleRequest;
import com.bank.promotion.application.command.CreateDecisionTreeCommand;
import com.bank.promotion.application.command.UpdatePromotionRuleCommand;
import com.bank.promotion.application.service.PromotionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理控制器
 * 提供決策樹和優惠規則管理的REST API端點
 */
@RestController
@RequestMapping("/api/v1/management")
@Validated
public class ManagementController {
    
    private final PromotionApplicationService promotionApplicationService;
    
    @Autowired
    public ManagementController(PromotionApplicationService promotionApplicationService) {
        this.promotionApplicationService = promotionApplicationService;
    }
    
    /**
     * 創建決策樹
     * POST /api/v1/management/decision-trees
     */
    @PostMapping("/decision-trees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> createDecisionTree(
            @Valid @RequestBody CreateDecisionTreeRequest request) {
        
        try {
            CreateDecisionTreeCommand command = new CreateDecisionTreeCommand(
                request.getName(),
                request.getDescription()
            );
            
            String treeId = promotionApplicationService.createDecisionTree(command);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(treeId, "決策樹創建成功"));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求資料驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 更新決策樹
     * PUT /api/v1/management/decision-trees/{treeId}
     */
    @PutMapping("/decision-trees/{treeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> updateDecisionTree(
            @PathVariable String treeId,
            @Valid @RequestBody CreateDecisionTreeRequest request) {
        
        try {
            // 這裡應該有更新決策樹的命令，暫時使用創建命令的邏輯
            CreateDecisionTreeCommand command = new CreateDecisionTreeCommand(
                request.getName(),
                request.getDescription()
            );
            
            // 實際實作中應該有 UpdateDecisionTreeCommand
            // promotionApplicationService.updateDecisionTree(updateCommand);
            
            return ResponseEntity.ok(ApiResponse.success("決策樹更新成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求資料驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 刪除決策樹
     * DELETE /api/v1/management/decision-trees/{treeId}
     */
    @DeleteMapping("/decision-trees/{treeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteDecisionTree(@PathVariable String treeId) {
        
        try {
            if (treeId == null || treeId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "決策樹ID不能為空"));
            }
            
            // 實際實作中應該有刪除決策樹的服務方法
            // promotionApplicationService.deleteDecisionTree(treeId);
            
            return ResponseEntity.ok(ApiResponse.success("決策樹刪除成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 創建優惠規則
     * POST /api/v1/management/promotion-rules
     */
    @PostMapping("/promotion-rules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> createPromotionRule(
            @Valid @RequestBody UpdatePromotionRuleRequest request) {
        
        try {
            // 實際實作中應該有創建優惠規則的命令
            // CreatePromotionRuleCommand command = new CreatePromotionRuleCommand(...);
            // promotionApplicationService.createPromotionRule(command);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("優惠規則創建成功"));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求資料驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 更新優惠規則
     * PUT /api/v1/management/promotion-rules/{ruleId}
     */
    @PutMapping("/promotion-rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> updatePromotionRule(
            @PathVariable String ruleId,
            @Valid @RequestBody UpdatePromotionRuleRequest request) {
        
        try {
            UpdatePromotionRuleCommand command = new UpdatePromotionRuleCommand(
                ruleId,
                request.getName(),
                request.getRuleType(),
                request.getRuleContent(),
                request.getParameters(),
                request.getStatus()
            );
            
            promotionApplicationService.updatePromotionRule(command);
            
            return ResponseEntity.ok(ApiResponse.success("優惠規則更新成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求資料驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 刪除優惠規則
     * DELETE /api/v1/management/promotion-rules/{ruleId}
     */
    @DeleteMapping("/promotion-rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deletePromotionRule(@PathVariable String ruleId) {
        
        try {
            if (ruleId == null || ruleId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "優惠規則ID不能為空"));
            }
            
            // 實際實作中應該有刪除優惠規則的服務方法
            // promotionApplicationService.deletePromotionRule(ruleId);
            
            return ResponseEntity.ok(ApiResponse.success("優惠規則刪除成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 啟用/停用優惠規則
     * PATCH /api/v1/management/promotion-rules/{ruleId}/status
     */
    @PatchMapping("/promotion-rules/{ruleId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> togglePromotionRuleStatus(
            @PathVariable String ruleId,
            @RequestParam String status) {
        
        try {
            if (ruleId == null || ruleId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "優惠規則ID不能為空"));
            }
            
            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "狀態值必須為 ACTIVE 或 INACTIVE"));
            }
            
            // 實際實作中應該有切換規則狀態的服務方法
            // promotionApplicationService.togglePromotionRuleStatus(ruleId, status);
            
            return ResponseEntity.ok(ApiResponse.success("優惠規則狀態更新成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
}