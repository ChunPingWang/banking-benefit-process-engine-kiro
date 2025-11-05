package com.bank.promotion.command.mock;

import com.bank.promotion.domain.command.external.ExternalSystemAdapter;
import com.bank.promotion.domain.command.external.ExternalSystemRequest;
import com.bank.promotion.domain.command.external.ExternalSystemResponse;
import com.bank.promotion.domain.command.external.ExternalSystemException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Mock 外部系統適配器
 * 用於測試環境模擬外部系統行為
 */
@Component
@Profile("test")
public class MockExternalSystemAdapter implements ExternalSystemAdapter {
    
    private final Map<String, ExternalSystemResponse> mockResponses = new HashMap<>();
    private final Map<String, Boolean> systemAvailability = new HashMap<>();
    private boolean defaultAvailable = true;
    private long simulatedDelay = 0;
    
    /**
     * 配置 Mock 回應
     */
    public void configureMockResponse(String endpoint, ExternalSystemResponse response) {
        mockResponses.put(endpoint, response);
    }
    
    /**
     * 配置系統可用性
     */
    public void configureSystemAvailability(String endpoint, boolean available) {
        systemAvailability.put(endpoint, available);
    }
    
    /**
     * 設定預設可用性
     */
    public void setDefaultAvailable(boolean available) {
        this.defaultAvailable = available;
    }
    
    /**
     * 設定模擬延遲時間
     */
    public void setSimulatedDelay(long delayMs) {
        this.simulatedDelay = delayMs;
    }
    
    /**
     * 清除所有 Mock 配置
     */
    public void clearAllMocks() {
        mockResponses.clear();
        systemAvailability.clear();
        defaultAvailable = true;
        simulatedDelay = 0;
    }
    
    @Override
    public ExternalSystemResponse call(ExternalSystemRequest request, long timeout, TimeUnit timeUnit) 
            throws ExternalSystemException {
        
        // 模擬延遲
        if (simulatedDelay > 0) {
            try {
                Thread.sleep(simulatedDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalSystemException("Mock call interrupted", e);
            }
        }
        
        // 檢查超時
        long timeoutMs = timeUnit.toMillis(timeout);
        if (simulatedDelay > timeoutMs) {
            throw new ExternalSystemException("Mock call timeout after " + timeoutMs + "ms");
        }
        
        // 從請求中提取端點資訊
        String endpoint = extractEndpoint(request);
        
        // 檢查系統可用性
        if (!isSystemAvailable(endpoint)) {
            throw new ExternalSystemException("Mock external system unavailable: " + endpoint);
        }
        
        // 返回配置的 Mock 回應
        ExternalSystemResponse mockResponse = mockResponses.get(endpoint);
        if (mockResponse != null) {
            return mockResponse;
        }
        
        // 返回預設成功回應
        return createDefaultSuccessResponse(request);
    }
    
    /**
     * 從請求中提取端點資訊
     */
    private String extractEndpoint(ExternalSystemRequest request) {
        // 嘗試從參數中獲取端點資訊
        Object endpointParam = request.getParameter("endpoint");
        if (endpointParam != null) {
            return endpointParam.toString();
        }
        
        // 使用請求ID作為預設端點
        return request.getRequestId();
    }
    
    /**
     * 檢查系統是否可用
     */
    private boolean isSystemAvailable(String endpoint) {
        return systemAvailability.getOrDefault(endpoint, defaultAvailable);
    }
    
    /**
     * 建立預設成功回應
     */
    private ExternalSystemResponse createDefaultSuccessResponse(ExternalSystemRequest request) {
        Map<String, Object> responseData = new HashMap<>();
        
        // 根據請求參數生成模擬回應
        Object customerId = request.getParameter("customerId");
        if (customerId != null) {
            responseData.put("conditionResult", true);
            responseData.put("discountAmount", 100.0);
            responseData.put("promotionName", "Mock外部系統優惠");
            responseData.put("promotionType", "MOCK_EXTERNAL");
            responseData.put("mockCustomerId", customerId);
        }
        
        // 加入 Mock 標識
        responseData.put("isMockResponse", true);
        responseData.put("mockTimestamp", LocalDateTime.now().toString());
        
        return ExternalSystemResponse.success()
                .addData(responseData)
                .statusCode(200)
                .executionTimeMs(simulatedDelay)
                .responseTime(LocalDateTime.now())
                .build();
    }
    
    @Override
    public boolean isAvailable() {
        return defaultAvailable;
    }
    
    @Override
    public String getAdapterType() {
        return "MOCK";
    }
    
    @Override
    public void close() {
        // Mock 適配器不需要清理資源
    }
    
    /**
     * 建立成功的 Mock 回應
     */
    public static ExternalSystemResponse createMockSuccessResponse(Map<String, Object> data) {
        return ExternalSystemResponse.success()
                .addData(data)
                .addData("isMockResponse", true)
                .statusCode(200)
                .executionTimeMs(10)
                .responseTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 建立失敗的 Mock 回應
     */
    public static ExternalSystemResponse createMockFailureResponse(String errorMessage) {
        return ExternalSystemResponse.failure(errorMessage)
                .addData("isMockResponse", true)
                .statusCode(500)
                .executionTimeMs(10)
                .responseTime(LocalDateTime.now())
                .build();
    }
}