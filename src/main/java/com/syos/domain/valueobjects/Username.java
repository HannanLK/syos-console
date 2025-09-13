package com.syos.domain.valueobjects;

import java.util.Objects;

public final class Username {
    private final String value;

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        if (value == null) throw new IllegalArgumentException("Username cannot be null");
        String trimmed = value.trim();
        if (trimmed.length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters long");
        if (trimmed.length() > 50) throw new IllegalArgumentException("Username must be at most 50 characters long");
        return new Username(trimmed);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Username username)) return false;
        return value.equalsIgnoreCase(username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase());
    }
}