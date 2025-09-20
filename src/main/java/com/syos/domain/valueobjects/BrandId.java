package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a Brand ID in the domain.
 * Ensures type safety and prevents primitive obsession.
 */
public class BrandId {
    private final Long value;

    // Public constructor for backward compatibility with tests
    public BrandId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Brand ID must be positive");
        }
        this.value = value;
    }

    public static BrandId of(Long value) {
        return new BrandId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrandId brandId = (BrandId) o;
        return Objects.equals(value, brandId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "BrandId{" + value + '}';
    }
}
