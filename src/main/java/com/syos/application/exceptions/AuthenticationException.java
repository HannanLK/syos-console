package com.syos.application.exceptions;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}