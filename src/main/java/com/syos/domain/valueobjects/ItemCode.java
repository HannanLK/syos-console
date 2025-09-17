package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * ItemCode is a normalized identifier for products. Uppercased, trimmed, 3-20 chars, [A-Z0-9-_].
 */
public final class ItemCode {
    private final String value;

    private ItemCode(String value) {
        this.value = value;
    }

    public static ItemCode of(String raw) {
        if (raw == null) throw new IllegalArgumentException("ItemCode cannot be null");
        String v = raw.trim().toUpperCase();
        if (v.length() < 3 || v.length() > 20) throw new IllegalArgumentException("ItemCode must be 3-20 chars");
        if (!v.matches("[A-Z0-9_-]+")) throw new IllegalArgumentException("ItemCode must be alphanumeric, dash or underscore");
        return new ItemCode(v);
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemCode itemCode)) return false;
        return value.equals(itemCode.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}
