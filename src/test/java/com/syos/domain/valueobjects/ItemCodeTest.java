package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemCodeTest {

    @Test
    void normalization_validation_andEquality() {
        ItemCode a = ItemCode.of("  coca-001 ");
        ItemCode b = ItemCode.of("COCA-001");
        assertEquals("COCA-001", a.getValue());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals("COCA-001", a.toString());

        assertThrows(IllegalArgumentException.class, () -> ItemCode.of("ab")); // too short
        assertThrows(IllegalArgumentException.class, () -> ItemCode.of("this-code-is-way-too-long-999"));
        assertThrows(IllegalArgumentException.class, () -> ItemCode.of("bad code"));
        assertThrows(IllegalArgumentException.class, () -> ItemCode.of(null));
    }
}
