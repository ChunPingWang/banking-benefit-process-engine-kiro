package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.application.query.GetAvailablePromotionsQuery;
import com.bank.promotion.application.query.GetPromotionHistoryQuery;
import com.bank.promotion.application.query.view.AvailablePromotionView;
import com.bank.promotion.application.query.view.PagedResult;
import com.bank.promotion.application.query.view.PromotionHistoryView;
import com.bank.promotion.application.service.PromotionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查詢控制器
 * 提供優惠歷史和可用優惠查詢的REST API端點
 */
@RestController
@RequestMapping("/api/v1/promotions")
@Validated
public class QueryController {
    
    private final PromotionApplicationService promotionApplicationService;
    
    @Autowired
    public QueryController(PromotionApplicationService promotionApplicationService) {
        this.promotionApplicationService = promotionApplicationService;
    }
    
    /**
     * 查詢優惠歷史
     * GET /api/v1/promotions/history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<PagedResult<PromotionHistoryView>>> getPromotionHistory(
            @RequestParam @NotBlank(message = "客戶ID不能為空") String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "頁碼必須大於等於0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "頁面大小必須大於0") @Max(value = 100, message = "頁面大小不能超過100") int size) {
        
        try {
            GetPromotionHistoryQuery query = new GetPromotionHistoryQuery(
                customerId, startDate, endDate, page, size
            );
            
            PagedResult<PromotionHistoryView> result = promotionApplicationService.getPromotionHistory(query);
            
            return ResponseEntity.ok(ApiResponse.success(result, "優惠歷史查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 查詢可用優惠
     * GET /api/v1/promotions/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailablePromotionView>>> getAvailablePromotions(
            @RequestParam @NotBlank(message = "客戶ID不能為空") String customerId,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String region) {
        
        try {
            GetAvailablePromotionsQuery query = new GetAvailablePromotionsQuery(
                customerId, accountType, region, true
            );
            
            List<AvailablePromotionView> result = promotionApplicationService.getAvailablePromotions(query);
            
            return ResponseEntity.ok(ApiResponse.success(result, "可用優惠查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
}