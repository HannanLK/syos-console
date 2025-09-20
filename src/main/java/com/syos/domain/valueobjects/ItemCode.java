package com.syos.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * ItemCode is a normalized identifier for products. Uppercased, trimmed, 3-20 chars, [A-Z0-9-_].
 */
public final class ItemCode {
    private static final Pattern VALID_CODE = Pattern.compile("^[A-Z0-9_\\-]{3,20}$");
    private final String value;

    // Public constructor for backward compatibility with tests
    public ItemCode(String raw) {
        if (raw == null) throw new IllegalArgumentException("ItemCode cannot be null");
        String v = raw.trim().toUpperCase();
        if (!VALID_CODE.matcher(v).matches()) {
            throw new IllegalArgumentException("ItemCode must be 3-20 chars and contain only A-Z, 0-9, '_' or '-'");
        }
        this.value = v;
    }

    public static ItemCode of(String raw) {
        return new ItemCode(raw);
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
