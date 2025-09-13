package com.syos.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

public final class UpdatedAt implements Comparable<UpdatedAt> {
    private final LocalDateTime value;

    private UpdatedAt(LocalDateTime value) { this.value = value; }

    public static UpdatedAt now() { return new UpdatedAt(LocalDateTime.now()); }
    public static UpdatedAt of(LocalDateTime value) {
        if (value == null) throw new IllegalArgumentException("UpdatedAt cannot be null");
        return new UpdatedAt(value);
    }

    public LocalDateTime getValue() { return value; }

    public UpdatedAt touch() { return now(); }

    @Override
    public String toString() { return value.toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdatedAt that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public int compareTo(UpdatedAt o) { return this.value.compareTo(o.value); }
}