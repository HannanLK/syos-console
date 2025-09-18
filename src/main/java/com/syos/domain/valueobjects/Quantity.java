package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quantity supports fractional values (e.g., KG, L) and integer units. Non-negative.
 */
public final class Quantity implements Comparable<Quantity> {
    private final BigDecimal value;

    private Quantity(BigDecimal value) {
        this.value = value;
    }

    public static Quantity of(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        return new Quantity(value);
    }

    public static Quantity zero() { return new Quantity(BigDecimal.ZERO); }

    // Backward-compatibility helpers expected by some domain entities
    public BigDecimal getValue() { return value; }
    public BigDecimal toBigDecimal() { return value; }

    public boolean isZero() { return value.compareTo(BigDecimal.ZERO) == 0; }
    public boolean isNegative() { return value.compareTo(BigDecimal.ZERO) < 0; }
    public boolean isZeroOrNegative() { return value.compareTo(BigDecimal.ZERO) <= 0; }
    public boolean isGreaterThan(Quantity other) { return this.compareTo(other) > 0; }
    public boolean isLessThan(Quantity other) { return this.compareTo(other) < 0; }

    public Quantity min(Quantity other) { return new Quantity(value.min(other.value)); }

    public Quantity plus(Quantity other) { return new Quantity(value.add(other.value)); }
    public Quantity add(Quantity other) { return plus(other); }

    public Quantity minus(Quantity other) {
        BigDecimal v = value.subtract(other.value);
        if (v.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Quantity result cannot be negative");
        return new Quantity(v);
    }
    public Quantity subtract(Quantity other) { return minus(other); }

    @Override
    public int compareTo(Quantity o) { return value.compareTo(o.value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity quantity)) return false;
        return value.compareTo(quantity.value) == 0;
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return value.toPlainString(); }
}
