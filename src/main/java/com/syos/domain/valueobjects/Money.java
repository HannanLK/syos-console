package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Money in LKR. Immutable, non-negative by default with helpers for arithmetic.
 * Enhanced with business logic for product pricing.
 */
public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.scale() > 2) amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Money cannot be negative");
        return new Money(amount);
    }

    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public static Money zero() { 
        return new Money(BigDecimal.ZERO.setScale(2)); 
    }

    public BigDecimal toBigDecimal() { 
        return amount; 
    }

    public Money plus(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money minus(Money other) {
        BigDecimal v = this.amount.subtract(other.amount);
        if (v.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Money result cannot be negative");
        return new Money(v);
    }

    public Money times(int factor) {
        return new Money(this.amount.multiply(new BigDecimal(factor)));
    }

    public Money times(BigDecimal factor) {
        return new Money(this.amount.multiply(factor));
    }

    public Money times(double factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    // Business logic methods for product pricing
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isZeroOrNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Calculate profit margin percentage
     */
    public BigDecimal profitMarginPercent(Money costPrice) {
        if (costPrice.isZero()) {
            throw new IllegalArgumentException("Cost price cannot be zero for margin calculation");
        }
        return this.amount.subtract(costPrice.amount)
                .divide(costPrice.amount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public String toString() {
        return "LKR " + amount.toPlainString();
    }

    public String toDisplayString() {
        return amount.toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }
}
