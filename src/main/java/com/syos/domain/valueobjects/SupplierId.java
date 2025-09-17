package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a Supplier ID in the domain.
 * Ensures type safety and prevents primitive obsession.
 */
public class SupplierId {
    private final Long value;

    private SupplierId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Supplier ID must be positive");
        }
        this.value = value;
    }

    public static SupplierId of(Long value) {
        return new SupplierId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierId that = (SupplierId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SupplierId{" + value + '}';
    }
}
