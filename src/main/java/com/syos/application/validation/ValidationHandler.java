package com.syos.application.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of Responsibility implementation for input validation
 * Implements Chain of Responsibility Pattern (Pattern #14)
 * 
 * Design Patterns:
 * - Chain of Responsibility: Sequential validation processing
 * - Composite Pattern: Combines multiple validators
 * 
 * Clean Architecture: Application Layer
 */
public abstract class ValidationHandler {
    
    protected ValidationHandler nextHandler;
    
    /**
     * Set the next handler in the chain
     */
    public ValidationHandler setNext(ValidationHandler handler) {
        this.nextHandler = handler;
        return handler;
    }
    
    /**
     * Handle validation request
     */
    public ValidationResult handle(ValidationRequest request) {
        ValidationResult result = doValidation(request);
        
        // If validation failed, return immediately
        if (!result.isValid()) {
            return result;
        }
        
        // If validation passed and there's a next handler, continue the chain
        if (nextHandler != null) {
            return nextHandler.handle(request);
        }
        
        // All validations passed
        return ValidationResult.success();
    }
    
    /**
     * Perform the actual validation logic
     * Subclasses must implement this method
     */
    protected abstract ValidationResult doValidation(ValidationRequest request);
    
    /**
     * Get the name of this validator for debugging
     */
    protected abstract String getValidatorName();
    
    /**
     * Validation request containing data to validate
     */
    public static class ValidationRequest {
        private final String fieldName;
        private final Object value;
        private final String fieldType;
        private final Object context;
        
        public ValidationRequest(String fieldName, Object value, String fieldType) {
            this(fieldName, value, fieldType, null);
        }
        
        public ValidationRequest(String fieldName, Object value, String fieldType, Object context) {
            this.fieldName = fieldName;
            this.value = value;
            this.fieldType = fieldType;
            this.context = context;
        }
        
        public String getFieldName() { return fieldName; }
        public Object getValue() { return value; }
        public String getFieldType() { return fieldType; }
        public Object getContext() { return context; }
        
        public String getStringValue() {
            return value != null ? value.toString() : null;
        }
        
        public boolean hasValue() {
            return value != null;
        }
    }
    
    /**
     * Validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errorMessages;
        private final String validatorName;
        
        private ValidationResult(boolean valid, List<String> errorMessages, String validatorName) {
            this.valid = valid;
            this.errorMessages = errorMessages != null ? new ArrayList<>(errorMessages) : new ArrayList<>();
            this.validatorName = validatorName;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }
        
        public static ValidationResult failure(String errorMessage, String validatorName) {
            List<String> errors = new ArrayList<>();
            errors.add(errorMessage);
            return new ValidationResult(false, errors, validatorName);
        }
        
        public static ValidationResult failure(List<String> errorMessages, String validatorName) {
            return new ValidationResult(false, errorMessages, validatorName);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrorMessages() { return new ArrayList<>(errorMessages); }
        public String getValidatorName() { return validatorName; }
        
        public String getFirstErrorMessage() {
            return errorMessages.isEmpty() ? null : errorMessages.get(0);
        }
        
        public String getAllErrorMessages() {
            return String.join("; ", errorMessages);
        }
    }
}
