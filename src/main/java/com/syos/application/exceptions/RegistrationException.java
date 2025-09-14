package com.syos.application.exceptions;

/**
 * Exception thrown when registration fails
 */
public class RegistrationException extends ApplicationException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}