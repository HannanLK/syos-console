package com.syos.domain.exceptions;

/**
 * Base type for all domain-layer (business) exceptions.
 * These are unchecked as business rule violations are not recoverable
 * by the current call and should be mapped by inbound adapters.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) { super(message); }
    public DomainException(String message, Throwable cause) { super(message, cause); }
    public DomainException(Throwable cause) { super(cause); }
}
