package com.syos.domain.valueobjects;

import java.util.Objects;

public final class UserID implements Comparable<UserID> {
    private final long value;

    private UserID(long value) {
        this.value = value;
    }

    public static UserID of(long value) {
        if (value <= 0) throw new IllegalArgumentException("UserID must be positive");
        return new UserID(value);
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserID userID)) return false;
        return value == userID.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(UserID o) {
        return Long.compare(this.value, o.value);
    }
}