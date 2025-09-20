package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Synex Points Value Object
 * 
 * Represents loyalty points that customers earn from purchases.
 * Immutable with business rules embedded.
 */
public final class SynexPoints implements Comparable<SynexPoints> {
    private static final BigDecimal ZERO_VALUE = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal POINTS_RATE = new BigDecimal("0.01"); // 1% of purchase
    private final BigDecimal value; // scale(2)
    
    // Public constant for backward compatibility with tests
    public static final SynexPoints ZERO = zero();

    private SynexPoints(BigDecimal value) {
        this.value = value.setScale(2);
    }

    public static SynexPoints of(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Synex points cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Synex points cannot be negative");
        return new SynexPoints(value);
    }

    public static SynexPoints zero() {
        return new SynexPoints(ZERO_VALUE);
    }

    /**
     * Calculate points from purchase amount
     * Business rule: 1% of purchase amount
     */
    public static SynexPoints fromPurchase(Money purchaseAmount) {
        Objects.requireNonNull(purchaseAmount, "Purchase amount cannot be null");
        BigDecimal pointsValue = purchaseAmount.toBigDecimal().multiply(POINTS_RATE);
        return new SynexPoints(pointsValue);
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

    /**
     * Subtract points (for redemption)
     */
    public SynexPoints subtract(SynexPoints other) {
        Objects.requireNonNull(other, "Other points cannot be null");
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Insufficient points for redemption");
        return new SynexPoints(result);
    }

    /**
     * Check if sufficient points for redemption
     */
    public boolean isGreaterThanOrEqual(SynexPoints other) {
        Objects.requireNonNull(other, "Other points cannot be null");
        return this.value.compareTo(other.value) >= 0;
    }

    /**
     * Check if zero points
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal getValue() { return value; }

    @Override
    public String toString() { return value.toPlainString() + " points"; }

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