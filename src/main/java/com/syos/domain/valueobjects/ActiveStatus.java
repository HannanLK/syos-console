package com.syos.domain.valueobjects;

import java.util.Objects;

public final class ActiveStatus {
    private final boolean value;

    private ActiveStatus(boolean value) { this.value = value; }

    public static ActiveStatus active() { return new ActiveStatus(true); }
    public static ActiveStatus inactive() { return new ActiveStatus(false); }
    public static ActiveStatus of(boolean value) { return new ActiveStatus(value); }

    public boolean isActive() { return value; }

    @Override
    public String toString() { return Boolean.toString(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveStatus that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}