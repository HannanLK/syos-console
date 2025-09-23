package com.syos.domain.exceptions;

import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Username;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionConstructorsTest {

    @Test
    void domainException_withCause_preservesMessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        DomainException ex = new DomainException("wrapper", cause);
        assertEquals("wrapper", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void invalidEmailException_isDomainException_andMessage() {
        InvalidEmailException ex = new InvalidEmailException("bad email");
        assertTrue(ex instanceof DomainException);
        assertEquals("bad email", ex.getMessage());
    }

    @Test
    void invalidUsernameException_isDomainException_andThrownByValidator() {
        InvalidUsernameException ex = new InvalidUsernameException("bad username");
        assertTrue(ex instanceof DomainException);
        assertEquals("bad username", ex.getMessage());

        // Also verify thrown by Username validator path
        assertThrows(InvalidUsernameException.class, () -> Username.of(" "));
    }

    @Test
    void invalidEmailException_thrownByEmailValidator() {
        assertThrows(InvalidEmailException.class, () -> Email.of("no-at-symbol"));
    }
}
