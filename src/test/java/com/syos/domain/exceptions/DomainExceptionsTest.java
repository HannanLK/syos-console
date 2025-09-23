package com.syos.domain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionsTest {

    @Test
    void domainException_factoriesProduceMeaningfulMessages() {
        DomainException ex1 = DomainException.itemNotFound("A1");
        assertTrue(ex1.getMessage().contains("A1"));

        DomainException ex2 = DomainException.duplicateItemCode("B2");
        assertTrue(ex2.getMessage().toLowerCase().contains("already"));

        DomainException ex3 = DomainException.invalidRelationship("Brand", 10L);
        assertTrue(ex3.getMessage().contains("Brand") && ex3.getMessage().contains("10"));

        DomainException ex4 = DomainException.businessRuleViolation("No negative prices");
        assertTrue(ex4.getMessage().contains("No negative prices"));
    }

    @Test
    void specificExceptions_extendDomainException() {
        InvalidEmailException email = new InvalidEmailException("bad");
        assertTrue(email instanceof DomainException);
        assertEquals("bad", email.getMessage());

        InvalidUsernameException user = new InvalidUsernameException("invalid");
        assertTrue(user instanceof DomainException);

        TransferNotAllowedException transfer = new TransferNotAllowedException("not allowed");
        assertTrue(transfer instanceof DomainException);

        InsufficientStockException stock = new InsufficientStockException("low");
        assertTrue(stock instanceof DomainException);

        InvalidQuantityException qty = new InvalidQuantityException("bad qty");
        assertTrue(qty instanceof DomainException);
    }

    @Test
    void authenticationException_isRuntime() {
        AuthenticationException ex = new AuthenticationException("auth failed");
        assertTrue(ex instanceof RuntimeException);
        assertEquals("auth failed", ex.getMessage());
        AuthenticationException ex2 = new AuthenticationException("wrapped", new RuntimeException("x"));
        assertEquals("wrapped", ex2.getMessage());
        assertNotNull(ex2.getCause());
    }
}
