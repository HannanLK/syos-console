package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing reorder point for inventory management.
 * Encapsulates business rules around minimum stock levels.
 */
public class ReorderPoint {
    private final Integer value;
    
    public static final int DEFAULT_REORDER_POINT = 50;
    public static final int MINIMUM_REORDER_POINT = 0;
    public static final int MAXIMUM_REORDER_POINT = 10000;

    private ReorderPoint(Integer value) {
        if (value == null || value < MINIMUM_REORDER_POINT || value > MAXIMUM_REORDER_POINT) {
            throw new IllegalArgumentException(
                String.format("Reorder point must be between %d and %d", 
                    MINIMUM_REORDER_POINT, MAXIMUM_REORDER_POINT));
        }
        this.value = value;
    }

    public static ReorderPoint of(Integer value) {
        return new ReorderPoint(value);
    }

    public static ReorderPoint defaultValue() {
        return new ReorderPoint(DEFAULT_REORDER_POINT);
    }

    /**
     * Factory method for high-turnover items (lower reorder point)
     */
    public static ReorderPoint forHighTurnoverItem() {
        return new ReorderPoint(25);
    }

    /**
     * Factory method for low-turnover items (higher reorder point)
     */
    public static ReorderPoint forLowTurnoverItem() {
        return new ReorderPoint(100);
    }

    public Integer getValue() {
        return value;
    }

    public boolean isBreached(int currentStock) {
        return currentStock <= value;
    }

    public boolean isAboveThreshold(int currentStock) {
        return currentStock > value;
    }

    public ReorderPoint increase(int amount) {
        return new ReorderPoint(value + amount);
    }

    public ReorderPoint decrease(int amount) {
        return new ReorderPoint(Math.max(MINIMUM_REORDER_POINT, value - amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReorderPoint that = (ReorderPoint) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ReorderPoint{" + value + '}';
    }
}
