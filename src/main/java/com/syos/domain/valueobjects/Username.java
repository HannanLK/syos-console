package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidUsernameException;
import java.util.Objects;

public final class Username {
    private final String value;

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        // Normalize input and validate
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be empty");
        }
        String normalized = value.trim().toLowerCase();

        int length = normalized.length();
        if (length < 3 || length > 20) {
            throw new InvalidUsernameException("Username must be 3-20 characters long");
        }
        if (!normalized.matches("[a-z0-9_]+")) {
            throw new InvalidUsernameException("Username may contain letters, numbers, and underscores only");
        }
        return new Username(normalized);
    }

    public String getValue() {
        return value;
    }

}