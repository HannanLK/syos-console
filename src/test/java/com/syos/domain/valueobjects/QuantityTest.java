package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void factory_and_getters_and_toString() {
        Quantity q = Quantity.of(new BigDecimal("5.50"));
        assertEquals(new BigDecimal("5.50"), q.getValue());
        assertEquals("5.50", q.toString());
        assertFalse(q.isZero());
        assertFalse(q.isNegative());
        assertFalse(q.isZeroOrNegative());
    }

    @Test
    void comparisons_and_minimum() {
        Quantity a = Quantity.of(new BigDecimal("2"));
        Quantity b = Quantity.of(new BigDecimal("3"));
        assertTrue(b.isGreaterThan(a));
        assertTrue(a.isLessThan(b));
        assertEquals(a, a.min(b));
        assertEquals(0, a.compareTo(Quantity.of(new BigDecimal("2"))));
    }

    @Test
    void arithmetic_add_and_subtract_and_guards() {
        Quantity a = Quantity.of(new BigDecimal("4"));
        Quantity b = Quantity.of(new BigDecimal("1.5"));
        assertEquals(Quantity.of(new BigDecimal("5.5")), a.add(b));
        assertEquals(Quantity.of(new BigDecimal("2.5")), a.subtract(b));

        // subtract below zero throws
        assertThrows(IllegalArgumentException.class, () -> b.subtract(a));
        // negative value rejected
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(new BigDecimal("-0.01")));
    }

    @Test
    void zero_factory_and_equals_hashCode() {
        Quantity z1 = Quantity.zero();
        Quantity z2 = Quantity.of(BigDecimal.ZERO);
        assertEquals(z1, z2);
        assertEquals(z1.hashCode(), z2.hashCode());
        assertTrue(z1.isZero());
        assertTrue(z1.isZeroOrNegative());
    }
}
