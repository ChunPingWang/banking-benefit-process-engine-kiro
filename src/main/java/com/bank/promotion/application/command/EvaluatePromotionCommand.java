package com.bank.promotion.application.command;

import com.bank.promotion.domain.valueobject.CustomerPayload;
import java.util.Objects;

/**
 * 評估優惠命令
 */
public final class EvaluatePromotionCommand {
    
    private final String treeId;
    private final CustomerPayload customerPayload;
    private final String requestId;
    
    public EvaluatePromotionCommand(String treeId, CustomerPayload customerPayload, String requestId) {
        this.treeId = validateTreeId(treeId);
        this.customerPayload = validateCustomerPayload(customerPayload);
        this.requestId = validateRequestId(requestId);
    }
    
    private String validateTreeId(String treeId) {
        if (treeId == null || treeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tree ID cannot be null or empty");
        }
        return treeId.trim();
    }
    
    private CustomerPayload validateCustomerPayload(CustomerPayload customerPayload) {
        if (customerPayload == null) {
            throw new IllegalArgumentException("Customer payload cannot be null");
        }
        return customerPayload;
    }
    
    private String validateRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        return requestId.trim();
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public CustomerPayload getCustomerPayload() {
        return customerPayload;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluatePromotionCommand that = (EvaluatePromotionCommand) o;
        return Objects.equals(treeId, that.treeId) &&
               Objects.equals(customerPayload, that.customerPayload) &&
               Objects.equals(requestId, that.requestId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(treeId, customerPayload, requestId);
    }
    
    @Override
    public String toString() {
        return "EvaluatePromotionCommand{" +
               "treeId='" + treeId + '\'' +
               ", customerPayload=" + customerPayload +
               ", requestId='" + requestId + '\'' +
               '}';
    }
}