package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SynexPointsTest {

    @Test
    void fromPurchase_isOnePercentOfMoney() {
        SynexPoints p = SynexPoints.fromPurchase(Money.of(new BigDecimal("1000.00")));
        assertEquals(new BigDecimal("10.00"), p.getValue());
    }

    @Test
    void addAndSubtract_behaveCorrectly() {
        SynexPoints a = SynexPoints.of(new BigDecimal("5.00"));
        SynexPoints b = SynexPoints.of(new BigDecimal("2.50"));
        assertEquals(SynexPoints.of(new BigDecimal("7.50")), a.add(b));
        assertEquals(SynexPoints.of(new BigDecimal("2.50")), a.subtract(b));
        assertTrue(a.isGreaterThanOrEqual(b));
    }

    @Test
    void subtract_throwsOnNegativeResult() {
        SynexPoints a = SynexPoints.of(new BigDecimal("1.00"));
        SynexPoints b = SynexPoints.of(new BigDecimal("2.00"));
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }
}
