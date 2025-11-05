package com.bank.promotion.domain.command.external;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SOAP 外部系統適配器
 * 透過 SOAP Web Service 與外部系統互動
 * 
 * 注意：這是一個基礎實作，實際使用時需要根據具體的 SOAP 服務進行客製化
 */
public class SoapExternalSystemAdapter implements ExternalSystemAdapter {
    
    private final String endpoint;
    private final String soapAction;
    private final String namespace;
    
    public SoapExternalSystemAdapter(String endpoint, Map<String, Object> parameters) {
        this.endpoint = validateEndpoint(endpoint);
        this.soapAction = getStringParameter(parameters, "soapAction", "");
        this.namespace = getStringParameter(parameters, "namespace", "http://tempuri.org/");
    }
    
    private String validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("SOAP endpoint cannot be null or empty");
        }
        return endpoint.trim();
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
            // 建立 SOAP 請求
            String soapRequest = buildSoapRequest(request);
            
            // 發送 SOAP 請求 (這裡使用模擬實作)
            String soapResponse = sendSoapRequest(soapRequest, timeout, timeUnit);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 解析 SOAP 回應
            Map<String, Object> responseData = parseSoapResponse(soapResponse);
            
            return ExternalSystemResponse.success()
                    .addData(responseData)
                    .statusCode(200)
                    .executionTimeMs(executionTime)
                    .responseTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            throw new ExternalSystemException(
                "SOAP request failed: " + e.getMessage(), e, "SOAP", endpoint, -1
            );
        }
    }
    
    /**
     * 建立 SOAP 請求
     */
    private String buildSoapRequest(ExternalSystemRequest request) {
        StringBuilder soapEnvelope = new StringBuilder();
        
        soapEnvelope.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        soapEnvelope.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        soapEnvelope.append("xmlns:tns=\"").append(namespace).append("\">\n");
        soapEnvelope.append("  <soap:Header/>\n");
        soapEnvelope.append("  <soap:Body>\n");
        soapEnvelope.append("    <tns:PromotionRequest>\n");
        
        // 加入請求參數
        for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
            soapEnvelope.append("      <tns:").append(entry.getKey()).append(">");
            soapEnvelope.append(escapeXml(entry.getValue().toString()));
            soapEnvelope.append("</tns:").append(entry.getKey()).append(">\n");
        }
        
        soapEnvelope.append("    </tns:PromotionRequest>\n");
        soapEnvelope.append("  </soap:Body>\n");
        soapEnvelope.append("</soap:Envelope>");
        
        return soapEnvelope.toString();
    }
    
    /**
     * 發送 SOAP 請求 (模擬實作)
     */
    private String sendSoapRequest(String soapRequest, long timeout, TimeUnit timeUnit) 
            throws ExternalSystemException {
        
        // 這裡是模擬實作，實際使用時需要整合真正的 SOAP 客戶端
        // 例如使用 JAX-WS、Apache CXF 或 Spring WS
        
        // 模擬回應
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "  <soap:Body>\n" +
               "    <PromotionResponse>\n" +
               "      <conditionResult>true</conditionResult>\n" +
               "      <discountAmount>100.00</discountAmount>\n" +
               "      <promotionName>SOAP優惠</promotionName>\n" +
               "    </PromotionResponse>\n" +
               "  </soap:Body>\n" +
               "</soap:Envelope>";
    }
    
    /**
     * 解析 SOAP 回應 (簡化實作)
     */
    private Map<String, Object> parseSoapResponse(String soapResponse) {
        // 這裡是簡化的解析實作，實際使用時需要使用 XML 解析器
        // 例如 JAXB、DOM 或 SAX
        
        Map<String, Object> responseData = new java.util.HashMap<>();
        
        // 簡單的字串解析 (僅用於示範)
        if (soapResponse.contains("<conditionResult>true</conditionResult>")) {
            responseData.put("conditionResult", true);
        } else if (soapResponse.contains("<conditionResult>false</conditionResult>")) {
            responseData.put("conditionResult", false);
        }
        
        // 提取折扣金額
        String discountPattern = "<discountAmount>";
        int startIndex = soapResponse.indexOf(discountPattern);
        if (startIndex != -1) {
            startIndex += discountPattern.length();
            int endIndex = soapResponse.indexOf("</discountAmount>", startIndex);
            if (endIndex != -1) {
                String discountValue = soapResponse.substring(startIndex, endIndex);
                try {
                    responseData.put("discountAmount", Double.parseDouble(discountValue));
                } catch (NumberFormatException e) {
                    responseData.put("discountAmount", 0.0);
                }
            }
        }
        
        // 提取優惠名稱
        String namePattern = "<promotionName>";
        startIndex = soapResponse.indexOf(namePattern);
        if (startIndex != -1) {
            startIndex += namePattern.length();
            int endIndex = soapResponse.indexOf("</promotionName>", startIndex);
            if (endIndex != -1) {
                String promotionName = soapResponse.substring(startIndex, endIndex);
                responseData.put("promotionName", promotionName);
            }
        }
        
        return responseData;
    }
    
    /**
     * XML 字元轉義
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 簡單的可用性檢查 (實際實作可能需要發送 SOAP 探測請求)
            return endpoint != null && !endpoint.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getAdapterType() {
        return "SOAP";
    }
    
    @Override
    public void close() {
        // SOAP 客戶端資源清理 (如果需要的話)
    }
}