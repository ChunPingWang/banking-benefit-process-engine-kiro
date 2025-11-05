package com.bank.promotion.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 交易記錄值物件
 */
public class TransactionRecord {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRecord that = (TransactionRecord) o;
        return Objects.equals(transactionId, that.transactionId) &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(type, that.type) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId, amount, type, timestamp);
    }
    
    @Override
    public String toString() {
        return "TransactionRecord{" +
               "transactionId='" + transactionId + '\'' +
               ", amount=" + amount +
               ", type='" + type + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}