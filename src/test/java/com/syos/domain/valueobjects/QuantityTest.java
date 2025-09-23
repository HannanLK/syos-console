package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void of_acceptsZeroAndPositive_andRejectsNegative() {
        assertEquals(new BigDecimal("0"), Quantity.of(BigDecimal.ZERO).toBigDecimal());
        assertEquals(new BigDecimal("2.50"), Quantity.of(new BigDecimal("2.50")).toBigDecimal());
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(new BigDecimal("-0.01")));
    }

    @Test
    void arithmeticAndComparisons() {
        Quantity q1 = Quantity.of(new BigDecimal("5"));
        Quantity q2 = Quantity.of(new BigDecimal("2.5"));

        assertTrue(q1.isGreaterThan(q2));
        assertTrue(q2.isLessThan(q1));
        assertEquals(0, q1.plus(q2).compareTo(Quantity.of(new BigDecimal("7.5"))));
        assertEquals(0, q1.add(q2).compareTo(Quantity.of(new BigDecimal("7.5"))));

        assertEquals(0, q1.minus(q2).compareTo(Quantity.of(new BigDecimal("2.5"))));
        assertEquals(0, q1.subtract(q2).compareTo(Quantity.of(new BigDecimal("2.5"))));

        assertThrows(IllegalArgumentException.class, () -> q2.minus(q1));
    }

    @Test
    void equalityAndToString() {
        Quantity a = Quantity.of(new BigDecimal("1.000"));
        Quantity b = Quantity.of(new BigDecimal("1"));
        assertEquals(a, b);
        assertEquals("1", a.toString());
        assertEquals(a.hashCode(), b.hashCode());
    }
}
