package com.bank.promotion.domain.valueobject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 驗證結果值物件
 * 包含驗證是否成功和錯誤訊息列表
 */
public final class ValidationResult {
    
    private final boolean isValid;
    private final List<String> errors;
    
    public ValidationResult(boolean isValid, List<String> errors) {
        this.isValid = isValid;
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }
    
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors);
    }
    
    public static ValidationResult failure(String error) {
        return new ValidationResult(false, List.of(error));
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public String getErrorMessage() {
        return String.join("; ", errors);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return isValid == that.isValid && Objects.equals(errors, that.errors);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(isValid, errors);
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
               "isValid=" + isValid +
               ", errors=" + errors +
               '}';
    }
}