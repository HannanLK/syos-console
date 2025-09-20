package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money in LKR. Immutable, non-negative by default with helpers for arithmetic.
 * Enhanced with business logic for product pricing.
 */
public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    // For backward compatibility with tests expecting public constructor
    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        // Preserve provided precision; do not force scale here
        this.amount = amount;
    }
    
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("Amount cannot be null");
        }
        // Normalize to two decimal places for factory method usage
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        return new Money(normalized);
    }

    public static Money of(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return of(BigDecimal.valueOf(amount));
    }

    public static Money of(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return of(BigDecimal.valueOf(amount));
    }

    public static Money of(String amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount string cannot be null");
        }
        try {
            return of(new BigDecimal(amount));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid amount format: " + amount);
        }
    }

    public static Money zero() { 
        return new Money(BigDecimal.ZERO); 
    }

    // Public constant for backward compatibility
    public static final Money ZERO = zero();

    public BigDecimal toBigDecimal() { 
        return amount; 
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    // Arithmetic operations with proper null checks
    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot add null money");
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot subtract null money");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money result cannot be negative");
        }
        return new Money(result);
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Cannot multiply by negative factor");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor cannot be null");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot multiply by negative factor");
        }
        return new Money(this.amount.multiply(factor));
    }

    public Money multiply(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Cannot multiply by negative factor");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    public Money divide(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        if (divisor < 0) {
            throw new IllegalArgumentException("Cannot divide by negative number");
        }
        return new Money(this.amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        if (divisor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot divide by negative number");
        }
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_UP));
    }

    // Legacy aliases for backward compatibility
    public Money plus(Money other) {
        return add(other);
    }

    public Money minus(Money other) {
        return subtract(other);
    }

    public Money times(int factor) {
        return multiply(factor);
    }

    public Money times(BigDecimal factor) {
        return multiply(factor);
    }

    public Money times(double factor) {
        return multiply(factor);
    }

    // Comparison methods with null checks
    public boolean isLessThan(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null money");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null money");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null money");
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    // Alias for backward compatibility
    public boolean isGreaterThanOrEqual(Money other) {
        return isGreaterThanOrEqualTo(other);
    }

    public boolean isLessThanOrEqualTo(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null money");
        }
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isZeroOrNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    // String formatting methods
    public String toCurrencyString() {
        return "LKR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public String toDisplayString() {
        // Display without unnecessary trailing zeros (e.g., 150.0 -> 150)
        return amount.stripTrailingZeros().toPlainString();
    }

    /**
     * Calculate profit margin percentage
     */
    public BigDecimal profitMarginPercent(Money costPrice) {
        if (costPrice.isZero()) {
            throw new IllegalArgumentException("Cost price cannot be zero for margin calculation");
        }
        return this.amount.subtract(costPrice.amount)
                .divide(costPrice.amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public int compareTo(Money o) {
        if (o == null) {
            throw new IllegalArgumentException("Cannot compare with null money");
        }
        return this.amount.compareTo(o.amount);
    }

    @Override
    public String toString() {
        // Use fixed 2-decimal scale for readability in logs/tests
        String amt = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return "Money{amount=" + amt + ", currency=LKR}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        // Normalize scale to ensure mathematically equal amounts have equal hashes
        return amount.stripTrailingZeros().hashCode();
    }
}
