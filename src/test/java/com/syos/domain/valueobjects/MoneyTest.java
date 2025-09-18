package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void arithmeticOperations_andComparisons() {
        Money a = Money.of(new BigDecimal("100.00"));
        Money b = Money.of(new BigDecimal("25.50"));

        assertEquals(Money.of(new BigDecimal("125.50")), a.plus(b));
        assertEquals(Money.of(new BigDecimal("74.50")), a.minus(b));
        assertTrue(a.isGreaterThan(b));
        assertTrue(a.isGreaterThanOrEqual(b));
        assertTrue(Money.zero().isZero());
    }

    @Test
    void times_worksForDifferentOverloads() {
        Money a = Money.of(10.0);
        assertEquals(Money.of(30.0), a.times(3));
        assertEquals(Money.of(25.0), a.times(2.5));
        assertEquals(Money.of(new BigDecimal("15.00")), a.times(new BigDecimal("1.5")));
    }

    @Test
    void negativeMoneyNotAllowed_andMinusNotBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("-1")));
        Money a = Money.of(5.0);
        assertThrows(IllegalArgumentException.class, () -> a.minus(Money.of(10.0)));
    }

    @Test
    void profitMarginPercent_calculatesProperly() {
        Money cost = Money.of(new BigDecimal("100.00"));
        Money selling = Money.of(new BigDecimal("130.00"));
        BigDecimal margin = selling.profitMarginPercent(cost);
        assertEquals(new BigDecimal("30.0000"), margin);
    }
}
