package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.EvaluatePromotionRequest;
import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.application.command.EvaluatePromotionCommand;
import com.bank.promotion.application.service.PromotionApplicationService;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "優惠評估", description = "客戶優惠推薦相關 API")
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
    @Operation(
        summary = "評估客戶優惠",
        description = "根據客戶資料和決策樹配置，評估客戶適合的優惠方案",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "客戶評估請求資料",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EvaluatePromotionRequest.class),
                examples = @ExampleObject(
                    name = "高價值客戶範例",
                    value = """
                        {
                          "customerId": "CUST001",
                          "accountType": "VIP",
                          "annualIncome": 2000000,
                          "creditScore": 750,
                          "region": "台北市",
                          "transactionCount": 50,
                          "accountBalance": 500000,
                          "transactionHistory": [
                            {"amount": 10000, "type": "DEPOSIT", "date": "2024-01-15"},
                            {"amount": 5000, "type": "TRANSFER", "date": "2024-01-20"}
                          ]
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "評估成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "成功回應範例",
                    value = """
                        {
                          "success": true,
                          "data": {
                            "promotionId": "PROMO_VIP_001",
                            "promotionName": "VIP專屬理財優惠",
                            "discountAmount": 1000,
                            "description": "享有專屬理財顧問服務及手續費減免",
                            "validUntil": "2024-12-31T23:59:59"
                          },
                          "message": "優惠評估完成",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "請求資料格式錯誤",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "錯誤回應範例",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "INVALID_REQUEST",
                            "message": "請求資料驗證失敗: 客戶ID不能為空"
                          },
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "系統內部錯誤",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<PromotionResult>> evaluatePromotion(
            @Parameter(description = "客戶優惠評估請求", required = true)
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