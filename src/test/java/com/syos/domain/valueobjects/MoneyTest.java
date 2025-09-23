package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void constructionAndParsing() {
        assertEquals(0, Money.zero().toBigDecimal().compareTo(new BigDecimal("0.00")));
        assertEquals(0, Money.of("123.45").toBigDecimal().compareTo(new BigDecimal("123.45")));
        assertEquals(0, Money.of(10).toBigDecimal().compareTo(new BigDecimal("10.00")));
        assertEquals(0, Money.of(2.5).toBigDecimal().compareTo(new BigDecimal("2.50")));
    }

    @Test
    void arithmetic() {
        Money m1 = Money.of("100.00");
        Money m2 = Money.of("25.50");

        assertEquals(Money.of("125.50"), m1.add(m2));
        assertEquals(Money.of("74.50"), m1.subtract(m2));
        assertEquals(Money.of("200.00"), m1.multiply(2));
        assertEquals(Money.of("50.00"), m1.divide(2));

        assertEquals(Money.of("200.00"), m1.times(2));
        assertEquals(Money.of("50.00"), m1.divide(new BigDecimal("2.0")));
        assertEquals(Money.of("150.00"), m1.plus(Money.of("50")));
        assertEquals(Money.of("50.00"), m1.minus(Money.of("50")));
    }

    @Test
    void comparisonsAndHelpers() {
        Money m1 = Money.of("100.00");
        Money m2 = Money.of("100.00");
        Money m3 = Money.of("99.99");

        assertEquals(0, m1.compareTo(m2));
        assertTrue(m1.isGreaterThan(m3));
        assertTrue(m3.isLessThan(m2));
        assertTrue(Money.zero().isZero());
        assertTrue(m1.isPositive());
        assertEquals("LKR 100.00", m1.toCurrencyString());
        assertTrue(m1.toString().contains("100.00"));
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void profitMarginPercent() {
        Money selling = Money.of("150.00");
        Money cost = Money.of("100.00");
        assertEquals(0, selling.minus(cost).toBigDecimal().compareTo(new BigDecimal("50.00")));
        assertEquals(0, selling.subtract(cost).toBigDecimal().compareTo(new BigDecimal("50.00")));
        assertEquals(0, Money.of("50").toBigDecimal().compareTo(new BigDecimal("50.00")));
        // profit margin percent = (150-100)/100 * 100 = 50.0000
        assertEquals(0, selling.profitMarginPercent(cost).compareTo(new BigDecimal("50.0000")));
    }
}
