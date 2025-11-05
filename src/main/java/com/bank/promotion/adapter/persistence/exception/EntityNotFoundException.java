package com.bank.promotion.adapter.persistence.exception;

/**
 * Exception thrown when a requested entity is not found in the database
 */
public class EntityNotFoundException extends DataAccessException {

    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super("FIND", entityType, String.format("Entity with ID '%s' not found", entityId));
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityType, String entityId, String additionalInfo) {
        super("FIND", entityType, String.format("Entity with ID '%s' not found. %s", entityId, additionalInfo));
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }
}