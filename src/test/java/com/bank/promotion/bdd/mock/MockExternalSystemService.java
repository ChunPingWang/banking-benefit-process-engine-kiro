package com.bank.promotion.bdd.mock;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 模擬外部系統服務
 * 提供測試用的外部系統回應
 */
@Service
public class MockExternalSystemService {
    
    private final Map<String, CustomerCreditInfo> mockCreditData = new HashMap<>();
    private final Map<String, CustomerTransactionHistory> mockTransactionData = new HashMap<>();
    
    public MockExternalSystemService() {
        initializeMockData();
    }
    
    /**
     * 模擬信用評等系統查詢
     */
    public CustomerCreditInfo getCreditInfo(String customerId) {
        return mockCreditData.getOrDefault(customerId, 
            new CustomerCreditInfo(customerId, "B", BigDecimal.valueOf(500000), false));
    }
    
    /**
     * 模擬交易歷史系統查詢
     */
    public CustomerTransactionHistory getTransactionHistory(String customerId) {
        return mockTransactionData.getOrDefault(customerId,
            new CustomerTransactionHistory(customerId, BigDecimal.valueOf(10000), 5, false));
    }
    
    /**
     * 模擬風險評估系統
     */
    public RiskAssessmentResult assessRisk(String customerId, BigDecimal amount) {
        CustomerCreditInfo credit = getCreditInfo(customerId);
        String riskLevel = credit.getCreditRating().equals("AAA") ? "LOW" : 
                          credit.getCreditRating().equals("AA") ? "MEDIUM" : "HIGH";
        return new RiskAssessmentResult(customerId, riskLevel, true);
    }
    
    /**
     * 初始化測試資料
     */
    private void initializeMockData() {
        // VIP 客戶資料
        mockCreditData.put("CUST001", 
            new CustomerCreditInfo("CUST001", "AAA", BigDecimal.valueOf(2000000), true));
        mockTransactionData.put("CUST001",
            new CustomerTransactionHistory("CUST001", BigDecimal.valueOf(500000), 50, true));
            
        // 一般客戶資料
        mockCreditData.put("CUST002",
            new CustomerCreditInfo("CUST002", "AA", BigDecimal.valueOf(800000), false));
        mockTransactionData.put("CUST002",
            new CustomerTransactionHistory("CUST002", BigDecimal.valueOf(50000), 12, false));
            
        // 低信用客戶資料
        mockCreditData.put("CUST003",
            new CustomerCreditInfo("CUST003", "C", BigDecimal.valueOf(300000), false));
        mockTransactionData.put("CUST003",
            new CustomerTransactionHistory("CUST003", BigDecimal.valueOf(5000), 2, false));
    }
    
    /**
     * 重設測試資料
     */
    public void resetMockData() {
        mockCreditData.clear();
        mockTransactionData.clear();
        initializeMockData();
    }
    
    // 內部類別定義
    public static class CustomerCreditInfo {
        private final String customerId;
        private final String creditRating;
        private final BigDecimal creditLimit;
        private final boolean isVip;
        
        public CustomerCreditInfo(String customerId, String creditRating, 
                                BigDecimal creditLimit, boolean isVip) {
            this.customerId = customerId;
            this.creditRating = creditRating;
            this.creditLimit = creditLimit;
            this.isVip = isVip;
        }
        
        public String getCustomerId() { return customerId; }
        public String getCreditRating() { return creditRating; }
        public BigDecimal getCreditLimit() { return creditLimit; }
        public boolean isVip() { return isVip; }
    }
    
    public static class CustomerTransactionHistory {
        private final String customerId;
        private final BigDecimal monthlyAverage;
        private final int transactionCount;
        private final boolean isHighValue;
        
        public CustomerTransactionHistory(String customerId, BigDecimal monthlyAverage,
                                        int transactionCount, boolean isHighValue) {
            this.customerId = customerId;
            this.monthlyAverage = monthlyAverage;
            this.transactionCount = transactionCount;
            this.isHighValue = isHighValue;
        }
        
        public String getCustomerId() { return customerId; }
        public BigDecimal getMonthlyAverage() { return monthlyAverage; }
        public int getTransactionCount() { return transactionCount; }
        public boolean isHighValue() { return isHighValue; }
    }
    
    public static class RiskAssessmentResult {
        private final String customerId;
        private final String riskLevel;
        private final boolean approved;
        
        public RiskAssessmentResult(String customerId, String riskLevel, boolean approved) {
            this.customerId = customerId;
            this.riskLevel = riskLevel;
            this.approved = approved;
        }
        
        public String getCustomerId() { return customerId; }
        public String getRiskLevel() { return riskLevel; }
        public boolean isApproved() { return approved; }
    }
}