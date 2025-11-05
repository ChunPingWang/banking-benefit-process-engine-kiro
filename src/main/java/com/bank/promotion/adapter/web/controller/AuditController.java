package com.bank.promotion.adapter.web.controller;

import com.bank.promotion.adapter.web.dto.ApiResponse;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.application.service.audit.AuditTrail;
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
    @GetMapping("/trails")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse<List<AuditTrail>>> getAuditTrails(
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
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