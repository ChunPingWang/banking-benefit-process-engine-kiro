package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.EvaluatePromotionRequest;
import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.application.service.PromotionApplicationService;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * 優惠評估控制器
 * 提供優惠推薦相關的REST API端點
 */
@RestController
@RequestMapping("/api/v1/promotions")
@Validated
public class PromotionController {
    
    private final PromotionApplicationService promotionApplicationService;
    
    @Autowired
    public PromotionController(PromotionApplicationService promotionApplicationService) {
        this.promotionApplicationService = promotionApplicationService;
    }
    
    /**
     * 評估客戶優惠資格
     * POST /api/v1/promotions/evaluate
     */
    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<PromotionResult>> evaluatePromotion(
            @Valid @RequestBody EvaluatePromotionRequest request) {
        
        try {
            // 生成請求ID用於追蹤
            String requestId = UUID.randomUUID().toString();
            
            // 轉換請求資料為領域物件
            CustomerPayload customerPayload = new CustomerPayload(
                request.getCustomerId(),
                request.getAccountType(),
                request.getAnnualIncome(),
                request.getCreditScore(),
                request.getRegion(),
                request.getTransactionCount(),
                request.getAccountBalance(),
                request.getTransactionHistory()
            );
            
            // 建立評估命令
            EvaluatePromotionCommand command = new EvaluatePromotionCommand(
                request.getTreeId() != null ? request.getTreeId() : "default-tree",
                customerPayload,
                requestId
            );
            
            // 執行優惠評估
            PromotionResult result = promotionApplicationService.evaluatePromotion(command);
            
            // 返回成功回應
            return ResponseEntity.ok(ApiResponse.success(result, "優惠評估完成"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求資料驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    

}