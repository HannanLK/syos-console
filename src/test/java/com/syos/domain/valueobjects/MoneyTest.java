package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object")
class MoneyTest {

    @Test
    @DisplayName("should create money with scale normalization and non-negative validation")
    void creationAndNormalization() {
        Money m = Money.of(new BigDecimal("100.1234"));
        assertThat(m.getAmount().scale()).isLessThanOrEqualTo(2);

        assertThatThrownBy(() -> Money.of((BigDecimal) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("arithmetic operations: plus, minus, times and multiply alias")
    void arithmeticOperations() {
        Money a = Money.of(new BigDecimal("150.00"));
        Money b = Money.of(new BigDecimal("50.00"));

        assertThat(a.plus(b).getAmount()).isEqualByComparingTo("200.00");
        assertThat(a.minus(b).getAmount()).isEqualByComparingTo("100.00");
        assertThat(a.times(2).getAmount()).isEqualByComparingTo("300.00");
        assertThat(a.times(new BigDecimal("1.10")).getAmount()).isEqualByComparingTo("165.0000");
        assertThat(a.multiply(new BigDecimal("2"))).isEqualTo(a.times(new BigDecimal("2")));

        Money c = Money.of(new BigDecimal("0")).plus(Money.of(new BigDecimal("0")));
        assertThat(c.isZero()).isTrue();

        assertThatThrownBy(() -> b.minus(a))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("comparisons and helpers")
    void comparisons() {
        Money low = Money.of(new BigDecimal("10.00"));
        Money high = Money.of(new BigDecimal("20.00"));

        assertThat(low.isLessThan(high)).isTrue();
        assertThat(high.isGreaterThan(low)).isTrue();
        assertThat(high.isGreaterThanOrEqual(low)).isTrue();
        assertThat(low.compareTo(high)).isLessThan(0);
        assertThat(low.equals(Money.of(new BigDecimal("10.0")))).isTrue();
        assertThat(low.hashCode()).isEqualTo(Money.of(new BigDecimal("10.00")).hashCode());
        assertThat(low.toString()).contains("LKR");
        assertThat(Money.of(new BigDecimal("150.00")).toDisplayString()).isEqualTo("150");
    }

    @Test
    @DisplayName("profit margin percent calculation")
    void profitMargin() {
        Money sell = Money.of(new BigDecimal("150.00"));
        Money cost = Money.of(new BigDecimal("100.00"));
        assertThat(sell.profitMarginPercent(cost)).isEqualByComparingTo("50.0000");
        assertThatThrownBy(() -> sell.profitMarginPercent(Money.zero()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
