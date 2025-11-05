package com.bank.promotion.domain.command.external;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 外部系統適配器
 * 透過 HTTP/REST API 與外部系統互動
 */
public class HttpExternalSystemAdapter implements ExternalSystemAdapter {
    
    private final String endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String httpMethod;
    private final Map<String, String> defaultHeaders;
    
    public HttpExternalSystemAdapter(String endpoint, Map<String, Object> parameters) {
        this.endpoint = validateEndpoint(endpoint);
        this.httpMethod = getStringParameter(parameters, "httpMethod", "POST");
        this.defaultHeaders = getDefaultHeaders(parameters);
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    private String validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP endpoint cannot be null or empty");
        }
        
        String normalizedEndpoint = endpoint.trim();
        if (!normalizedEndpoint.startsWith("http://") && !normalizedEndpoint.startsWith("https://")) {
            throw new IllegalArgumentException("HTTP endpoint must start with http:// or https://");
        }
        
        return normalizedEndpoint;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> getDefaultHeaders(Map<String, Object> parameters) {
        Object headersObj = parameters.get("headers");
        if (headersObj instanceof Map) {
            return (Map<String, String>) headersObj;
        }
        return Map.of(
            "Content-Type", "application/json",
            "Accept", "application/json"
        );
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
            // 建立 HTTP 請求
            HttpRequest httpRequest = buildHttpRequest(request, timeout, timeUnit);
            
            // 發送請求
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 處理回應
            return handleHttpResponse(response, executionTime);
            
        } catch (IOException e) {
            throw new ExternalSystemException(
                "HTTP request failed: " + e.getMessage(), e, "HTTP", endpoint, -1
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalSystemException(
                "HTTP request interrupted: " + e.getMessage(), e, "HTTP", endpoint, -1
            );
        } catch (Exception e) {
            throw new ExternalSystemException(
                "Unexpected error during HTTP request: " + e.getMessage(), e, "HTTP", endpoint, -1
            );
        }
    }
    
    /**
     * 建立 HTTP 請求
     */
    private HttpRequest buildHttpRequest(ExternalSystemRequest request, long timeout, TimeUnit timeUnit) 
            throws ExternalSystemException {
        
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.of(timeout, timeUnit.toChronoUnit()));
            
            // 設定標頭
            for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
            
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
            
            // 設定請求方法和內容
            switch (httpMethod.toUpperCase()) {
                case "GET":
                    builder.GET();
                    break;
                case "POST":
                    String jsonBody = objectMapper.writeValueAsString(request.getParameters());
                    builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                    break;
                case "PUT":
                    String putJsonBody = objectMapper.writeValueAsString(request.getParameters());
                    builder.PUT(HttpRequest.BodyPublishers.ofString(putJsonBody));
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                default:
                    throw new ExternalSystemException("Unsupported HTTP method: " + httpMethod);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            throw new ExternalSystemException("Failed to build HTTP request: " + e.getMessage(), e);
        }
    }
    
    /**
     * 處理 HTTP 回應
     */
    private ExternalSystemResponse handleHttpResponse(HttpResponse<String> response, long executionTime) 
            throws ExternalSystemException {
        
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        if (statusCode >= 200 && statusCode < 300) {
            // 成功回應
            try {
                Map<String, Object> responseData = parseResponseBody(responseBody);
                return ExternalSystemResponse.success()
                        .addData(responseData)
                        .statusCode(statusCode)
                        .executionTimeMs(executionTime)
                        .responseTime(LocalDateTime.now())
                        .build();
                        
            } catch (Exception e) {
                throw new ExternalSystemException(
                    "Failed to parse response body: " + e.getMessage(), e, "HTTP", endpoint, statusCode
                );
            }
        } else {
            // 錯誤回應
            String errorMessage = String.format("HTTP request failed with status %d: %s", statusCode, responseBody);
            return ExternalSystemResponse.failure(errorMessage)
                    .statusCode(statusCode)
                    .executionTimeMs(executionTime)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * 解析回應內容
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponseBody(String responseBody) throws IOException {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return Map.of();
        }
        
        try {
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException e) {
            // 如果不是 JSON 格式，將整個回應作為字串返回
            return Map.of("response", responseBody);
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
                    
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 500;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getAdapterType() {
        return "HTTP";
    }
    
    @Override
    public void close() {
        // HttpClient 不需要明確關閉
    }
}