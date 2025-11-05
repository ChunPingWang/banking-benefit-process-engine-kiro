package com.bank.promotion.domain.command.external;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 資料庫外部系統適配器
 * 透過資料庫查詢與外部資料來源互動
 * 
 * 注意：這是一個基礎實作，實際使用時需要注入真正的 DataSource 或 JdbcTemplate
 */
public class DatabaseExternalSystemAdapter implements ExternalSystemAdapter {
    
    private final String connectionString;
    private final String queryTemplate;
    private final String databaseType;
    
    public DatabaseExternalSystemAdapter(String connectionString, Map<String, Object> parameters) {
        this.connectionString = validateConnectionString(connectionString);
        this.queryTemplate = getStringParameter(parameters, "queryTemplate", "");
        this.databaseType = getStringParameter(parameters, "databaseType", "postgresql");
        
        if (queryTemplate.isEmpty()) {
            throw new IllegalArgumentException("Database query template cannot be empty");
        }
    }
    
    private String validateConnectionString(String connectionString) {
        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Database connection string cannot be null or empty");
        }
        return connectionString.trim();
    }
    
    private String getStringParameter(Map<String, Object> parameters, String key, String defaultValue) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    @Override
    public ExternalSystemResponse call(ExternalSystemRequest request, long timeout, TimeUnit timeUnit) 
            throws ExternalSystemException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 建立查詢語句
            String query = buildQuery(request);
            
            // 執行查詢 (這裡使用模擬實作)
            Map<String, Object> queryResult = executeQuery(query, request.getParameters(), timeout, timeUnit);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return ExternalSystemResponse.success()
                    .addData(queryResult)
                    .statusCode(200)
                    .executionTimeMs(executionTime)
                    .responseTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            throw new ExternalSystemException(
                "Database query failed: " + e.getMessage(), e, "DATABASE", connectionString, -1
            );
        }
    }
    
    /**
     * 建立查詢語句
     */
    private String buildQuery(ExternalSystemRequest request) {
        String query = queryTemplate;
        
        // 替換查詢模板中的參數佔位符
        for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
            String placeholder = "#{" + entry.getKey() + "}";
            String value = entry.getValue().toString();
            
            // 簡單的 SQL 注入防護 (實際使用時應該使用 PreparedStatement)
            value = sanitizeSqlValue(value);
            
            query = query.replace(placeholder, value);
        }
        
        return query;
    }
    
    /**
     * 執行資料庫查詢 (模擬實作)
     */
    private Map<String, Object> executeQuery(String query, Map<String, Object> parameters, 
                                           long timeout, TimeUnit timeUnit) throws ExternalSystemException {
        
        // 這裡是模擬實作，實際使用時需要整合真正的資料庫連線
        // 例如使用 Spring JdbcTemplate、MyBatis 或 JPA
        
        Map<String, Object> result = new HashMap<>();
        
        // 模擬不同類型的查詢結果
        if (query.toLowerCase().contains("customer_score")) {
            // 模擬客戶評分查詢
            Object customerId = parameters.get("customerId");
            if (customerId != null) {
                result.put("customerScore", 750);
                result.put("riskLevel", "LOW");
                result.put("conditionResult", true);
            }
        } else if (query.toLowerCase().contains("promotion_amount")) {
            // 模擬優惠金額計算查詢
            Object accountBalance = parameters.get("accountBalance");
            if (accountBalance instanceof Number) {
                double balance = ((Number) accountBalance).doubleValue();
                double discountAmount = balance * 0.05; // 5% 折扣
                result.put("discountAmount", discountAmount);
                result.put("promotionName", "資料庫計算優惠");
                result.put("promotionType", "DATABASE_CALCULATED");
            }
        } else if (query.toLowerCase().contains("account_history")) {
            // 模擬帳戶歷史查詢
            result.put("transactionCount", 25);
            result.put("averageBalance", 50000.0);
            result.put("loyaltyScore", 85);
            result.put("conditionResult", true);
        } else {
            // 預設查詢結果
            result.put("queryExecuted", true);
            result.put("resultCount", 1);
            result.put("conditionResult", true);
        }
        
        // 加入查詢元資料
        result.put("queryExecutionTime", System.currentTimeMillis());
        result.put("databaseType", databaseType);
        result.put("executedQuery", query);
        
        return result;
    }
    
    /**
     * SQL 值清理 (基礎的 SQL 注入防護)
     */
    private String sanitizeSqlValue(String value) {
        if (value == null) {
            return "NULL";
        }
        
        // 移除潛在的 SQL 注入字元
        String sanitized = value.replace("'", "''")  // 轉義單引號
                               .replace(";", "")      // 移除分號
                               .replace("--", "")     // 移除註解
                               .replace("/*", "")     // 移除多行註解開始
                               .replace("*/", "");    // 移除多行註解結束
        
        // 如果是字串值，加上單引號
        if (!isNumeric(sanitized)) {
            return "'" + sanitized + "'";
        }
        
        return sanitized;
    }
    
    /**
     * 檢查字串是否為數字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 簡單的可用性檢查 (實際實作可能需要測試資料庫連線)
            return connectionString != null && !connectionString.isEmpty() && 
                   queryTemplate != null && !queryTemplate.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getAdapterType() {
        return "DATABASE";
    }
    
    @Override
    public void close() {
        // 資料庫連線資源清理 (如果需要的話)
    }
}