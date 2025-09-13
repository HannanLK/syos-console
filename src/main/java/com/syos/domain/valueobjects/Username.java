package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidUsernameException;
import java.util.Objects;

public final class Username {
    private final String value;

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        if (value == null) throw new InvalidUsernameException("Username cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new InvalidUsernameException("Username cannot be blank");
        if (trimmed.length() < 3) throw new InvalidUsernameException("Username must be at least 3 characters long");
        if (trimmed.length() > 50) throw new InvalidUsernameException("Username must be at most 50 characters long");
        if (!trimmed.matches("[A-Za-z0-9_.-]+")) throw new InvalidUsernameException("Username may contain letters, digits, underscore, dot, and hyphen only");
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