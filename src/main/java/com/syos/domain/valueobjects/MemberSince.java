package com.syos.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value object for member since date
 */
public final class MemberSince {
    private final LocalDateTime value;

    private MemberSince(LocalDateTime value) {
        this.value = Objects.requireNonNull(value, "Member since date cannot be null");
    }

    public static MemberSince of(LocalDateTime dateTime) {
        return new MemberSince(dateTime);
    }

    public static MemberSince fromCreatedAt(CreatedAt createdAt) {
        return new MemberSince(createdAt.getValue());
    }

    public static MemberSince now() {
        return new MemberSince(LocalDateTime.now());
    }

    public LocalDateTime getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberSince that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}