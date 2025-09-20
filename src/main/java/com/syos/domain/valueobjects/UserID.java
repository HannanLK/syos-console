package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * User ID Value Object
 *
 * Represents unique identifier for users in the domain.
 * Immutable and self-validating.
 */
public final class UserID {
    private final Long value;

    /**
     * Public constructor for backward compatibility
     */
    public UserID(Long value) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.value = value;
    }

    private UserID(Long value, boolean validated) {
        this.value = value; // Used only by factory methods with pre-validation
    }

    /**
     * Create UserID from Long value
     */
    public static UserID of(Long value) {
        return new UserID(value);
    }

    /**
     * Generate new unique ID placeholder (null until persisted)
     */
    public static UserID generate() {
        return new UserID(null, true); // Will be assigned by repository
    }

    public Long getValue() {
        return value;
    }

    public boolean isAssigned() {
        return value != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserID userId = (UserID) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UserID{" + value + "}";
    }
}
