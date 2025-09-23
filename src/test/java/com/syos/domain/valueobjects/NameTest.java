package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameTest {

    @Test
    void valid_and_trimming_and_toString() {
        Name n = Name.of("  John Doe  ");
        assertEquals("John Doe", n.getValue());
        assertEquals("John Doe", n.toString());
        assertEquals(n, Name.of("John Doe"));
        assertEquals(n.hashCode(), Name.of("John Doe").hashCode());
    }

    @Test
    void min_and_max_length_enforced() {
        assertThrows(IllegalArgumentException.class, () -> Name.of("J"));
        String longName = "a".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> Name.of(longName));
        assertThrows(IllegalArgumentException.class, () -> Name.of(null));
    }
}
