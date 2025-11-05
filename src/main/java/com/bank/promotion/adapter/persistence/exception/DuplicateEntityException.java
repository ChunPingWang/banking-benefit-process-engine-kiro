package com.bank.promotion.adapter.persistence.exception;

/**
 * Exception thrown when attempting to create an entity that already exists
 */
public class DuplicateEntityException extends DataAccessException {

    private final String duplicateField;
    private final String duplicateValue;

    public DuplicateEntityException(String entityType, String duplicateField, String duplicateValue) {
        super("CREATE", entityType, 
              String.format("Entity with %s '%s' already exists", duplicateField, duplicateValue));
        this.duplicateField = duplicateField;
        this.duplicateValue = duplicateValue;
    }

    public DuplicateEntityException(String entityType, String duplicateField, String duplicateValue, Throwable cause) {
        super("CREATE", entityType, 
              String.format("Entity with %s '%s' already exists", duplicateField, duplicateValue), cause);
        this.duplicateField = duplicateField;
        this.duplicateValue = duplicateValue;
    }

    public String getDuplicateField() {
        return duplicateField;
    }

    public String getDuplicateValue() {
        return duplicateValue;
    }
}