package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a Category ID in the domain.
 * Ensures type safety and prevents primitive obsession.
 */
public class CategoryId {
    private final Long value;

    private CategoryId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        this.value = value;
    }

    public static CategoryId of(Long value) {
        return new CategoryId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "CategoryId{" + value + '}';
    }
}
