package com.bank.promotion.domain.aggregate;

import com.bank.promotion.domain.valueobject.CustomerPayload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 客戶檔案聚合根
 * 管理客戶相關資訊和條件評估邏輯
 */
public class CustomerProfile {
    
    private final String customerId;
    private final CustomerPayload basicInfo;
    private final Map<String, Object> extendedAttributes;
    private final List<TransactionRecord> transactionHistory;
    private final CreditProfile creditProfile;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CustomerProfile(String customerId, CustomerPayload basicInfo) {
        this.customerId = validateCustomerId(customerId);
        this.basicInfo = validateBasicInfo(basicInfo);
        this.extendedAttributes = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
        this.creditProfile = new CreditProfile(basicInfo.getCreditScore());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public CustomerProfile(String customerId, CustomerPayload basicInfo, 
                          Map<String, Object> extendedAttributes,
                          List<TransactionRecord> transactionHistory,
                          CreditProfile creditProfile,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.customerId = validateCustomerId(customerId);
        this.basicInfo = validateBasicInfo(basicInfo);
        this.extendedAttributes = extendedAttributes != null ? new HashMap<>(extendedAttributes) : new HashMap<>();
        this.transactionHistory = transactionHistory != null ? new ArrayList<>(transactionHistory) : new ArrayList<>();
        this.creditProfile = creditProfile != null ? creditProfile : new CreditProfile(basicInfo.getCreditScore());
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    private String validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        return customerId.trim();
    }
    
    private CustomerPayload validateBasicInfo(CustomerPayload basicInfo) {
        if (basicInfo == null) {
            throw new IllegalArgumentException("Basic info cannot be null");
        }
        return basicInfo;
    }
    
    /**
     * 評估客戶是否符合指定條件
     */
    public boolean evaluateCondition(String conditionExpression, Map<String, Object> parameters) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition expression cannot be null or empty");
        }
        
        try {
            return evaluateConditionInternal(conditionExpression, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate condition: " + conditionExpression, e);
        }
    }
    
    private boolean evaluateConditionInternal(String conditionExpression, Map<String, Object> parameters) {
        // 建立評估上下文
        Map<String, Object> context = createEvaluationContext(parameters);
        
        // 簡化的條件評估邏輯 (實際實作會使用 SpEL 或其他表達式引擎)
        return evaluateSimpleCondition(conditionExpression, context);
    }
    
    private Map<String, Object> createEvaluationContext(Map<String, Object> parameters) {
        Map<String, Object> context = new HashMap<>();
        
        // 基本客戶資訊
        context.put("customerId", basicInfo.getCustomerId());
        context.put("accountType", basicInfo.getAccountType());
        context.put("annualIncome", basicInfo.getAnnualIncome());
        context.put("creditScore", basicInfo.getCreditScore());
        context.put("region", basicInfo.getRegion());
        context.put("transactionCount", basicInfo.getTransactionCount());
        
        // 擴展屬性
        context.putAll(extendedAttributes);
        
        // 計算屬性
        context.put("averageTransactionAmount", calculateAverageTransactionAmount());
        context.put("totalTransactionAmount", calculateTotalTransactionAmount());
        context.put("isVipCustomer", isVipCustomer());
        context.put("riskLevel", creditProfile.getRiskLevel());
        
        // 參數
        if (parameters != null) {
            context.putAll(parameters);
        }
        
        return context;
    }
    
    private boolean evaluateSimpleCondition(String expression, Map<String, Object> context) {
        // 簡化的條件評估邏輯
        // 實際實作會使用 SpEL 或其他表達式引擎
        
        if (expression.contains("annualIncome")) {
            return evaluateIncomeCondition(expression, context);
        }
        
        if (expression.contains("creditScore")) {
            return evaluateCreditScoreCondition(expression, context);
        }
        
        if (expression.contains("accountType")) {
            return evaluateAccountTypeCondition(expression, context);
        }
        
        if (expression.contains("isVipCustomer")) {
            return evaluateVipCondition(expression, context);
        }
        
        // 預設返回 true
        return true;
    }
    
    private boolean evaluateIncomeCondition(String expression, Map<String, Object> context) {
        BigDecimal annualIncome = (BigDecimal) context.get("annualIncome");
        
        if (expression.contains(">= 2000000")) {
            return annualIncome.compareTo(BigDecimal.valueOf(2000000)) >= 0;
        }
        
        if (expression.contains(">= 1000000")) {
            return annualIncome.compareTo(BigDecimal.valueOf(1000000)) >= 0;
        }
        
        if (expression.contains(">= 500000")) {
            return annualIncome.compareTo(BigDecimal.valueOf(500000)) >= 0;
        }
        
        return true;
    }
    
    private boolean evaluateCreditScoreCondition(String expression, Map<String, Object> context) {
        Integer creditScore = (Integer) context.get("creditScore");
        
        if (expression.contains(">= 800")) {
            return creditScore >= 800;
        }
        
        if (expression.contains(">= 700")) {
            return creditScore >= 700;
        }
        
        if (expression.contains(">= 600")) {
            return creditScore >= 600;
        }
        
        return true;
    }
    
    private boolean evaluateAccountTypeCondition(String expression, Map<String, Object> context) {
        String accountType = (String) context.get("accountType");
        
        if (expression.contains("VIP")) {
            return "VIP".equals(accountType);
        }
        
        if (expression.contains("PREMIUM")) {
            return "PREMIUM".equals(accountType);
        }
        
        return true;
    }
    
    private boolean evaluateVipCondition(String expression, Map<String, Object> context) {
        Boolean isVip = (Boolean) context.get("isVipCustomer");
        return Boolean.TRUE.equals(isVip);
    }
    
    /**
     * 添加交易記錄
     */
    public void addTransactionRecord(TransactionRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Transaction record cannot be null");
        }
        
        transactionHistory.add(record);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新擴展屬性
     */
    public void updateExtendedAttribute(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute key cannot be null or empty");
        }
        
        extendedAttributes.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 計算平均交易金額
     */
    public BigDecimal calculateAverageTransactionAmount() {
        if (transactionHistory.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = transactionHistory.stream()
            .map(TransactionRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(transactionHistory.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 計算總交易金額
     */
    public BigDecimal calculateTotalTransactionAmount() {
        return transactionHistory.stream()
            .map(TransactionRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 判斷是否為 VIP 客戶
     */
    public boolean isVipCustomer() {
        return "VIP".equals(basicInfo.getAccountType()) || 
               basicInfo.getAnnualIncome().compareTo(BigDecimal.valueOf(2000000)) >= 0 ||
               basicInfo.getCreditScore() >= 800;
    }
    
    /**
     * 驗證客戶資料
     */
    public ValidationResult validateCustomerData() {
        List<String> errors = new ArrayList<>();
        
        // 驗證基本資訊
        if (basicInfo.getCustomerId() == null || basicInfo.getCustomerId().trim().isEmpty()) {
            errors.add("Customer ID is required");
        }
        
        if (basicInfo.getAnnualIncome().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Annual income cannot be negative");
        }
        
        if (basicInfo.getCreditScore() < 0 || basicInfo.getCreditScore() > 1000) {
            errors.add("Credit score must be between 0 and 1000");
        }
        
        if (basicInfo.getTransactionCount() < 0) {
            errors.add("Transaction count cannot be negative");
        }
        
        // 驗證交易記錄
        for (int i = 0; i < transactionHistory.size(); i++) {
            TransactionRecord record = transactionHistory.get(i);
            if (record.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Transaction amount cannot be negative at index " + i);
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    // Getters
    public String getCustomerId() {
        return customerId;
    }
    
    public CustomerPayload getBasicInfo() {
        return basicInfo;
    }
    
    public Map<String, Object> getExtendedAttributes() {
        return Collections.unmodifiableMap(extendedAttributes);
    }
    
    public List<TransactionRecord> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }
    
    public CreditProfile getCreditProfile() {
        return creditProfile;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerProfile that = (CustomerProfile) o;
        return Objects.equals(customerId, that.customerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
    
    @Override
    public String toString() {
        return "CustomerProfile{" +
               "customerId='" + customerId + '\'' +
               ", basicInfo=" + basicInfo +
               ", extendedAttributesCount=" + extendedAttributes.size() +
               ", transactionHistoryCount=" + transactionHistory.size() +
               ", creditProfile=" + creditProfile +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

/**
 * 交易記錄值物件
 */
class TransactionRecord {
    private final String transactionId;
    private final BigDecimal amount;
    private final String type;
    private final LocalDateTime timestamp;
    
    public TransactionRecord(String transactionId, BigDecimal amount, String type, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getType() {
        return type;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

/**
 * 信用檔案值物件
 */
class CreditProfile {
    private final Integer creditScore;
    private final String riskLevel;
    
    public CreditProfile(Integer creditScore) {
        this.creditScore = creditScore;
        this.riskLevel = calculateRiskLevel(creditScore);
    }
    
    private String calculateRiskLevel(Integer creditScore) {
        if (creditScore >= 800) {
            return "LOW";
        } else if (creditScore >= 600) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
    
    public Integer getCreditScore() {
        return creditScore;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
}