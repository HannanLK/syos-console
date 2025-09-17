package com.syos.domain.exceptions;

/**
 * Base exception class for domain-level business rule violations.
 * 
 * Domain Layer:
 * - Represents business rule violations
 * - Provides meaningful error messages for business scenarios
 * - Maintains separation from infrastructure concerns
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Factory method for item-related business rule violations
     */
    public static DomainException itemNotFound(String itemCode) {
        return new DomainException("Item with code '" + itemCode + "' not found");
    }
    
    /**
     * Factory method for duplicate item codes
     */
    public static DomainException duplicateItemCode(String itemCode) {
        return new DomainException("Item with code '" + itemCode + "' already exists");
    }
    
    /**
     * Factory method for invalid relationships
     */
    public static DomainException invalidRelationship(String entityType, Long id) {
        return new DomainException(entityType + " with ID " + id + " does not exist or is inactive");
    }
    
    /**
     * Factory method for business rule violations
     */
    public static DomainException businessRuleViolation(String rule) {
        return new DomainException("Business rule violation: " + rule);
    }
}
