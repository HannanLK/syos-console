package com.syos.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

public final class CreatedAt implements Comparable<CreatedAt> {
    private final LocalDateTime value;

    private CreatedAt(LocalDateTime value) { this.value = value; }

    public static CreatedAt now() { return new CreatedAt(LocalDateTime.now()); }
    public static CreatedAt of(LocalDateTime value) {
        if (value == null) throw new IllegalArgumentException("CreatedAt cannot be null");
        return new CreatedAt(value);
    }

    public LocalDateTime getValue() { return value; }

    @Override
    public String toString() { return value.toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreatedAt createdAt)) return false;
        return Objects.equals(value, createdAt.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public int compareTo(CreatedAt o) { return this.value.compareTo(o.value); }
}