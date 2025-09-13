package com.syos.domain.exceptions;

public class InvalidUsernameException extends DomainException {
    public InvalidUsernameException(String message) { super(message); }
    public InvalidUsernameException(String message, Throwable cause) { super(message, cause); }
}
