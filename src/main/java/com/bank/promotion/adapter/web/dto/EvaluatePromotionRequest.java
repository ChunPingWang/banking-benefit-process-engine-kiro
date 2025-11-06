package com.bank.promotion.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 優惠評估請求DTO
 */
@Schema(description = "客戶優惠評估請求資料")
public class EvaluatePromotionRequest {
    
    @Schema(description = "客戶唯一識別碼", example = "CUST001", required = true)
    @NotBlank(message = "客戶ID不能為空")
    @JsonProperty("customerId")
    private String customerId;
    
    @Schema(description = "帳戶類型", example = "VIP", allowableValues = {"一般", "VIP", "白金", "鑽石"}, required = true)
    @NotBlank(message = "帳戶類型不能為空")
    @JsonProperty("accountType")
    private String accountType;
    
    @Schema(description = "年收入 (新台幣)", example = "2000000", required = true)
    @NotNull(message = "年收入不能為空")
    @DecimalMin(value = "0", message = "年收入必須為非負數")
    @JsonProperty("annualIncome")
    private BigDecimal annualIncome;
    
    @Schema(description = "信用評分 (0-1000)", example = "750", minimum = "0", maximum = "1000", required = true)
    @NotNull(message = "信用評分不能為空")
    @Min(value = 0, message = "信用評分必須在0-1000之間")
    @Max(value = 1000, message = "信用評分必須在0-1000之間")
    @JsonProperty("creditScore")
    private Integer creditScore;
    
    @Schema(description = "客戶所在地區", example = "台北市", required = true)
    @NotBlank(message = "地區不能為空")
    @JsonProperty("region")
    private String region;
    
    @Schema(description = "近期交易次數", example = "50", minimum = "0", required = true)
    @NotNull(message = "交易次數不能為空")
    @Min(value = 0, message = "交易次數必須為非負數")
    @JsonProperty("transactionCount")
    private Integer transactionCount;
    
    @Schema(description = "帳戶餘額 (新台幣)", example = "500000", minimum = "0")
    @DecimalMin(value = "0", message = "帳戶餘額必須為非負數")
    @JsonProperty("accountBalance")
    private BigDecimal accountBalance;
    
    @Schema(description = "交易歷史記錄", example = "[{\"amount\": 10000, \"type\": \"DEPOSIT\", \"date\": \"2024-01-15\"}]")
    @JsonProperty("transactionHistory")
    private List<Object> transactionHistory;
    
    @Schema(description = "決策樹ID (可選，預設使用系統預設決策樹)", example = "default-tree")
    @JsonProperty("treeId")
    private String treeId;
    
    public EvaluatePromotionRequest() {
    }
    
    public EvaluatePromotionRequest(String customerId, String accountType, BigDecimal annualIncome,
                                   Integer creditScore, String region, Integer transactionCount) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.annualIncome = annualIncome;
        this.creditScore = creditScore;
        this.region = region;
        this.transactionCount = transactionCount;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }
    
    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }
    
    public Integer getCreditScore() {
        return creditScore;
    }
    
    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public BigDecimal getAccountBalance() {
        return accountBalance;
    }
    
    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }
    
    public List<Object> getTransactionHistory() {
        return transactionHistory;
    }
    
    public void setTransactionHistory(List<Object> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluatePromotionRequest that = (EvaluatePromotionRequest) o;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(accountType, that.accountType) &&
               Objects.equals(annualIncome, that.annualIncome) &&
               Objects.equals(creditScore, that.creditScore) &&
               Objects.equals(region, that.region) &&
               Objects.equals(transactionCount, that.transactionCount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, accountType, annualIncome, creditScore, region, transactionCount);
    }
    
    @Override
    public String toString() {
        return "EvaluatePromotionRequest{" +
               "customerId='" + customerId + '\'' +
               ", accountType='" + accountType + '\'' +
               ", annualIncome=" + annualIncome +
               ", creditScore=" + creditScore +
               ", region='" + region + '\'' +
               ", transactionCount=" + transactionCount +
               ", accountBalance=" + accountBalance +
               ", treeId='" + treeId + '\'' +
               '}';
    }
}