package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidEmailException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final String value;

    // Public constructor for backward compatibility with tests
    public Email(String value) {
        this.value = validate(value);
    }

    private static String validate(String value) {
        if (value == null) throw new InvalidEmailException("Email cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new InvalidEmailException("Email cannot be blank");
        if (trimmed.length() > 100) throw new InvalidEmailException("Email must be at most 100 characters long");
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Invalid email format");
        }
        return trimmed;
    }

    public static Email of(String value) {
        return new Email(validate(value));
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return value.equalsIgnoreCase(email.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value.toLowerCase()); }
}