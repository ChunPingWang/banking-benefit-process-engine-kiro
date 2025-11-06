package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.application.service.audit.AuditTrail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 稽核控制器
 * 提供稽核軌跡查詢的REST API端點
 */
@RestController
@RequestMapping("/api/v1/audit")
@Validated
@Tag(name = "稽核追蹤", description = "稽核軌跡查詢和合規性報告相關 API")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {
    
    private final AuditService auditService;
    
    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * 查詢稽核軌跡
     * GET /api/v1/audit/trails
     */
    @Operation(
        summary = "查詢稽核軌跡",
        description = "根據請求ID或客戶ID查詢完整的稽核軌跡記錄，支援時間範圍篩選"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "查詢成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "稽核軌跡查詢成功",
                    value = """
                        {
                          "success": true,
                          "data": [
                            {
                              "requestId": "req-123",
                              "operationType": "PROMOTION_EVALUATION",
                              "operationDetails": {"customerId": "CUST001", "result": "VIP優惠"},
                              "executionTimeMs": 150,
                              "status": "SUCCESS",
                              "timestamp": "2024-01-15T10:30:00"
                            }
                          ],
                          "message": "稽核軌跡查詢成功"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "請求參數錯誤"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "權限不足，需要 ADMIN 或 AUDITOR 角色"
        )
    })
    @GetMapping("/trails")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<List<AuditTrail>>> getAuditTrails(
            @Parameter(description = "請求ID，用於查詢特定請求的稽核軌跡") 
            @RequestParam(required = false) String requestId,
            @Parameter(description = "客戶ID，用於查詢特定客戶的稽核軌跡") 
            @RequestParam(required = false) String customerId,
            @Parameter(description = "查詢開始時間 (ISO 8601 格式)", example = "2024-01-01T00:00:00") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "查詢結束時間 (ISO 8601 格式)", example = "2024-01-31T23:59:59") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<AuditTrail> auditTrails;
            
            if (requestId != null && !requestId.trim().isEmpty()) {
                // 根據請求ID查詢
                auditTrails = auditService.getAuditTrails(requestId);
            } else if (customerId != null && !customerId.trim().isEmpty()) {
                // 根據客戶ID查詢
                auditTrails = auditService.getCustomerAuditTrails(customerId, startDate, endDate);
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "必須提供 requestId 或 customerId 參數"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(auditTrails, "稽核軌跡查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 查詢決策步驟追蹤
     * GET /api/v1/audit/decisions
     */
    @GetMapping("/decisions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<List<AuditTrail>>> getDecisionSteps(
            @RequestParam @NotBlank(message = "請求ID不能為空") String requestId) {
        
        try {
            List<AuditTrail> auditTrails = auditService.getAuditTrails(requestId);
            
            // 過濾出決策步驟相關的稽核記錄
            List<AuditTrail> decisionSteps = auditTrails.stream()
                .filter(trail -> "DECISION_STEP".equals(trail.getOperationType()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(decisionSteps, "決策步驟追蹤查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 查詢外部系統呼叫記錄
     * GET /api/v1/audit/external-calls
     */
    @GetMapping("/external-calls")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<List<AuditTrail>>> getExternalSystemCalls(
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<AuditTrail> auditTrails;
            
            if (requestId != null && !requestId.trim().isEmpty()) {
                auditTrails = auditService.getAuditTrails(requestId);
            } else if (customerId != null && !customerId.trim().isEmpty()) {
                auditTrails = auditService.getCustomerAuditTrails(customerId, startDate, endDate);
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "必須提供 requestId 或 customerId 參數"));
            }
            
            // 過濾出外部系統呼叫相關的稽核記錄
            List<AuditTrail> externalCalls = auditTrails.stream()
                .filter(trail -> "EXTERNAL_SYSTEM_CALL".equals(trail.getOperationType()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(externalCalls, "外部系統呼叫記錄查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 查詢資料庫查詢記錄
     * GET /api/v1/audit/database-queries
     */
    @GetMapping("/database-queries")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<List<AuditTrail>>> getDatabaseQueries(
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<AuditTrail> auditTrails;
            
            if (requestId != null && !requestId.trim().isEmpty()) {
                auditTrails = auditService.getAuditTrails(requestId);
            } else if (customerId != null && !customerId.trim().isEmpty()) {
                auditTrails = auditService.getCustomerAuditTrails(customerId, startDate, endDate);
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "必須提供 requestId 或 customerId 參數"));
            }
            
            // 過濾出資料庫查詢相關的稽核記錄
            List<AuditTrail> databaseQueries = auditTrails.stream()
                .filter(trail -> "DATABASE_QUERY".equals(trail.getOperationType()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(databaseQueries, "資料庫查詢記錄查詢成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
    
    /**
     * 生成合規性報告
     * GET /api/v1/audit/compliance-report
     */
    @GetMapping("/compliance-report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<Object>> generateComplianceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String customerId) {
        
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_REQUEST", "開始時間不能晚於結束時間"));
            }
            
            // 實際實作中應該有專門的合規性報告服務
            // ComplianceReportService.generateReport(startDate, endDate, customerId);
            
            return ResponseEntity.ok(ApiResponse.success("合規性報告生成成功"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_REQUEST", "請求參數驗證失敗: " + e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("SYSTEM_ERROR", "系統處理時發生錯誤: " + e.getMessage()));
        }
    }
}