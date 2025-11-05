package com.bank.promotion.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 客戶資料負載值物件
 * 包含六個輸入欄位的客戶資料
 */
public final class CustomerPayload {
    
    private final String customerId;
    private final String accountType;
    private final BigDecimal annualIncome;
    private final Integer creditScore;
    private final String region;
    private final Integer transactionCount;
    private final BigDecimal accountBalance;
    private final java.util.List<Object> transactionHistory;
    
    public CustomerPayload(String customerId, String accountType, BigDecimal annualIncome, 
                          Integer creditScore, String region, Integer transactionCount) {
        this(customerId, accountType, annualIncome, creditScore, region, transactionCount, null, null);
    }
    
    public CustomerPayload(String customerId, String accountType, BigDecimal annualIncome, 
                          Integer creditScore, String region, Integer transactionCount,
                          BigDecimal accountBalance, java.util.List<Object> transactionHistory) {
        this.customerId = validateCustomerId(customerId);
        this.accountType = validateAccountType(accountType);
        this.annualIncome = validateAnnualIncome(annualIncome);
        this.creditScore = validateCreditScore(creditScore);
        this.region = validateRegion(region);
        this.transactionCount = validateTransactionCount(transactionCount);
        this.accountBalance = accountBalance;
        this.transactionHistory = transactionHistory != null ? 
            java.util.List.copyOf(transactionHistory) : java.util.List.of();
    }
    
    private String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        return customerId.trim();
    }
    
    private String validateAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            throw new IllegalArgumentException("Account type cannot be null or empty");
        }
        return accountType.trim();
    }
    
    private BigDecimal validateAnnualIncome(BigDecimal annualIncome) {
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual income must be non-negative");
        }
        return annualIncome;
    }
    
    private Integer validateCreditScore(Integer creditScore) {
        if (creditScore == null || creditScore < 0 || creditScore > 1000) {
            throw new IllegalArgumentException("Credit score must be between 0 and 1000");
        }
        return creditScore;
    }
    
    private String validateRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be null or empty");
        }
        return region.trim();
    }
    
    private Integer validateTransactionCount(Integer transactionCount) {
        if (transactionCount == null || transactionCount < 0) {
            throw new IllegalArgumentException("Transaction count must be non-negative");
        }
        return transactionCount;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }
    
    public Integer getCreditScore() {
        return creditScore;
    }
    
    public String getRegion() {
        return region;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public BigDecimal getAccountBalance() {
        return accountBalance;
    }
    
    public java.util.List<Object> getTransactionHistory() {
        return transactionHistory;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerPayload that = (CustomerPayload) o;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(accountType, that.accountType) &&
               Objects.equals(annualIncome, that.annualIncome) &&
               Objects.equals(creditScore, that.creditScore) &&
               Objects.equals(region, that.region) &&
               Objects.equals(transactionCount, that.transactionCount) &&
               Objects.equals(accountBalance, that.accountBalance) &&
               Objects.equals(transactionHistory, that.transactionHistory);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, accountType, annualIncome, creditScore, region, transactionCount, accountBalance, transactionHistory);
    }
    
    @Override
    public String toString() {
        return "CustomerPayload{" +
               "customerId='" + customerId + '\'' +
               ", accountType='" + accountType + '\'' +
               ", annualIncome=" + annualIncome +
               ", creditScore=" + creditScore +
               ", region='" + region + '\'' +
               ", transactionCount=" + transactionCount +
               ", accountBalance=" + accountBalance +
               ", transactionHistorySize=" + (transactionHistory != null ? transactionHistory.size() : 0) +
               '}';
    }
}