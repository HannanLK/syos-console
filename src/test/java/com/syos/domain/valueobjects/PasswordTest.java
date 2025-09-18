package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    void hashAndMatches_workForValidPasswords() {
        Password p = Password.hash("veryStrongPass");
        assertNotNull(p.getHash());
        assertTrue(p.matches("veryStrongPass"));
        assertFalse(p.matches("wrongPass"));
    }

    @Test
    void fromHash_rejectsInvalidHash() {
        assertThrows(IllegalArgumentException.class, () -> Password.fromHash("not-bcrypt"));
    }

    @Test
    void equalsAndHashCode_useHash() {
        Password p1 = Password.hash("anotherStrongPass");
        Password p2 = Password.fromHash(p1.getHash());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void validateRaw_enforcesMinLength() {
        assertThrows(IllegalArgumentException.class, () -> Password.hash("short"));
    }
}
