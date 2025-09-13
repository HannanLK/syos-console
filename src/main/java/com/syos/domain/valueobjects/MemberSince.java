package com.syos.domain.valueobjects;

import java.time.LocalDate;
import java.util.Objects;

public final class MemberSince implements Comparable<MemberSince> {
    private final LocalDate value;

    private MemberSince(LocalDate value) { this.value = value; }

    public static MemberSince of(LocalDate value) {
        if (value == null) throw new IllegalArgumentException("MemberSince cannot be null");
        return new MemberSince(value);
    }

    public static MemberSince fromCreatedAt(CreatedAt createdAt) {
        Objects.requireNonNull(createdAt, "createdAt");
        return new MemberSince(createdAt.getValue().toLocalDate());
    }

    public LocalDate getValue() { return value; }

    @Override
    public String toString() { return value.toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberSince that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public int compareTo(MemberSince o) { return this.value.compareTo(o.value); }
}