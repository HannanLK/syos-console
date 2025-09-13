package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

public final class SynexPoints implements Comparable<SynexPoints> {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private final BigDecimal value; // scale(2)

    private SynexPoints(BigDecimal value) {
        this.value = value.setScale(2);
    }

    public static SynexPoints of(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Synex points cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Synex points cannot be negative");
        return new SynexPoints(value);
    }

    public static SynexPoints zero() {
        return new SynexPoints(ZERO);
    }

    public SynexPoints add(SynexPoints other) {
        Objects.requireNonNull(other, "other");
        return new SynexPoints(this.value.add(other.value));
    }

    public SynexPoints add(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return of(this.value.add(amount));
    }

    public SynexPoints subtract(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        BigDecimal result = this.value.subtract(amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Resulting points cannot be negative");
        return new SynexPoints(result);
    }

    public BigDecimal getValue() { return value; }

    @Override
    public String toString() { return value.toPlainString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SynexPoints that)) return false;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(value.stripTrailingZeros()); }

    @Override
    public int compareTo(SynexPoints o) { return this.value.compareTo(o.value); }
}