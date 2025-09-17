package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing pack size with proper validation.
 * Supports different measurement units and fractional sizes.
 */
public class PackSize {
    private final BigDecimal value;

    private PackSize(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Pack size must be positive");
        }
        this.value = value;
    }

    public static PackSize of(BigDecimal value) {
        return new PackSize(value);
    }

    public static PackSize of(double value) {
        return new PackSize(BigDecimal.valueOf(value));
    }

    public static PackSize of(int value) {
        return new PackSize(BigDecimal.valueOf(value));
    }

    public BigDecimal getValue() {
        return value;
    }

    public double getDoubleValue() {
        return value.doubleValue();
    }

    public boolean isGreaterThan(PackSize other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(PackSize other) {
        return this.value.compareTo(other.value) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackSize packSize = (PackSize) o;
        return Objects.equals(value, packSize.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
