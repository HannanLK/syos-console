package com.syos.domain.valueobjects;

import java.util.Objects;

public final class Name {
    private final String value;

    // Public constructor for backward compatibility with tests
    public Name(String value) {
        this.value = validate(value);
    }

    private static String validate(String value) {
        if (value == null) throw new IllegalArgumentException("Name cannot be null");
        String trimmed = value.trim();
        if (trimmed.length() < 2) throw new IllegalArgumentException("Name must be at least 2 characters long");
        if (trimmed.length() > 100) throw new IllegalArgumentException("Name must be at most 100 characters long");
        return trimmed;
    }

    public static Name of(String value) {
        return new Name(validate(value));
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Name name)) return false;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}