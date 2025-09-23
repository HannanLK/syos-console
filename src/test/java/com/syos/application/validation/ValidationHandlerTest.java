package com.syos.application.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationHandlerTest {

    static class NotNullValidator extends ValidationHandler {
        @Override
        protected ValidationResult doValidation(ValidationRequest request) {
            if (request.getValue() == null) {
                return ValidationResult.failure(request.getFieldName() + " must not be null", getValidatorName());
            }
            return ValidationResult.success();
        }

        @Override
        protected String getValidatorName() {
            return "NotNullValidator";
        }
    }

    static class MinLengthValidator extends ValidationHandler {
        private final int min;
        MinLengthValidator(int min) { this.min = min; }

        @Override
        protected ValidationResult doValidation(ValidationRequest request) {
            String s = request.getStringValue();
            if (s == null || s.length() < min) {
                return ValidationResult.failure(
                        request.getFieldName() + " must be at least " + min + " chars",
                        getValidatorName());
            }
            return ValidationResult.success();
        }

        @Override
        protected String getValidatorName() {
            return "MinLengthValidator(" + min + ")";
        }
    }

    @Test
    void chain_allValid_returnsSuccess() {
        ValidationHandler chain = new NotNullValidator();
        chain.setNext(new MinLengthValidator(3));

        ValidationHandler.ValidationResult result = chain.handle(
                new ValidationHandler.ValidationRequest("username", "john", "string"));

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorMessages().size());
        assertNull(result.getValidatorName());
    }

    @Test
    void chain_stopsOnFirstFailure_returnsFailureDetails() {
        ValidationHandler chain = new NotNullValidator();
        chain.setNext(new MinLengthValidator(5));

        ValidationHandler.ValidationResult result = chain.handle(
                new ValidationHandler.ValidationRequest("password", "1234", "string"));

        assertFalse(result.isValid());
        assertEquals("MinLengthValidator(5)", result.getValidatorName());
        assertEquals(1, result.getErrorMessages().size());
        assertTrue(result.getFirstErrorMessage().contains("at least 5"));
        assertTrue(result.getAllErrorMessages().contains("at least 5"));
    }

    @Test
    void firstValidatorFails_nextNotInvoked() {
        ValidationHandler chain = new NotNullValidator();
        chain.setNext(new MinLengthValidator(1));

        ValidationHandler.ValidationResult result = chain.handle(
                new ValidationHandler.ValidationRequest("field", null, "string"));

        assertFalse(result.isValid());
        assertEquals("NotNullValidator", result.getValidatorName());
    }
}
