package com.bank.promotion.adapter.persistence.exception;

/**
 * Custom exception for data access layer operations
 * Wraps underlying database exceptions with business context
 */
public class DataAccessException extends RuntimeException {

    private final String operation;
    private final String entityType;

    public DataAccessException(String message) {
        super(message);
        this.operation = null;
        this.entityType = null;
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.entityType = null;
    }

    public DataAccessException(String operation, String entityType, String message) {
        super(String.format("Data access error in operation '%s' for entity '%s': %s", 
                          operation, entityType, message));
        this.operation = operation;
        this.entityType = entityType;
    }

    public DataAccessException(String operation, String entityType, String message, Throwable cause) {
        super(String.format("Data access error in operation '%s' for entity '%s': %s", 
                          operation, entityType, message), cause);
        this.operation = operation;
        this.entityType = entityType;
    }

    public String getOperation() {
        return operation;
    }

    public String getEntityType() {
        return entityType;
    }
}