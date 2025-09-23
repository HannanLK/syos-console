package com.syos.domain.valueobjects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordAdvancedTest {

    @AfterEach
    void clearProp() {
        System.clearProperty("BCRYPT_COST");
    }

    @Test
    void matches_overloads_coverRawAndHashVariants() {
        Password a = Password.hash("SuperSecret123");
        Password bRaw = new Password("SuperSecret123"); // raw constructor retains candidate
        assertTrue(a.matches(bRaw)); // raw candidate branch

        Password bHash = Password.fromHash(a.getHash());
        assertTrue(a.matches(bHash)); // hash equality branch

        assertTrue(a.matches(a.getHash(), true)); // matches against hash flag
        assertFalse(a.matches("wrongPass", false)); // raw mismatch (ensure >=8 length to pass validation)
    }

    @Test
    void bcrypt_cost_property_paths_doNotThrow_andStillMatch() {
        // valid custom cost
        System.setProperty("BCRYPT_COST", "6");
        Password p1 = Password.hash("Cost6Pass!!");
        assertTrue(p1.matches("Cost6Pass!!"));

        // invalid cost -> falls back to default path
        System.setProperty("BCRYPT_COST", "30");
        Password p2 = Password.hash("DefaultCostPass!!");
        assertTrue(p2.matches("DefaultCostPass!!"));

        // malformed value also falls back
        System.setProperty("BCRYPT_COST", "abc");
        Password p3 = Password.hash("AnotherPass!!");
        assertTrue(p3.matches("AnotherPass!!"));
    }

    @Test
    void toString_and_equalityContracts() {
        Password p = Password.hash("T0pSecret!!");
        assertEquals("[PROTECTED]", p.toString());
        assertEquals(Password.fromHash(p.getHash()), p);
        assertEquals(Password.fromHash(p.getHash()).hashCode(), p.hashCode());
    }
}
