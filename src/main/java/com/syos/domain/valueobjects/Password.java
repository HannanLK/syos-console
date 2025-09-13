package com.syos.domain.valueobjects;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

/**
 * Password value object that stores only the BCrypt hash.
 */
public final class Password {
    private final String hash; // stored BCrypt hash

    private Password(String hash) {
        this.hash = hash;
    }

    /**
     * Create Password from an existing BCrypt hash (e.g., loaded from DB).
     */
    public static Password fromHash(String bcryptHash) {
        if (bcryptHash == null || bcryptHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null/blank");
        }
        if (!bcryptHash.startsWith("$2a$") && !bcryptHash.startsWith("$2b$") && !bcryptHash.startsWith("$2y$")) {
            throw new IllegalArgumentException("Invalid BCrypt hash format");
        }
        return new Password(bcryptHash);
    }

    /**
     * Create Password by hashing a raw (plain text) password.
     */
    public static Password hash(String rawPassword) {
        validateRaw(rawPassword);
        String salt = BCrypt.gensalt(12);
        String hashed = BCrypt.hashpw(rawPassword, salt);
        return new Password(hashed);
    }

    public boolean matches(String rawPassword) {
        validateRaw(rawPassword);
        return BCrypt.checkpw(rawPassword, this.hash);
    }

    private static void validateRaw(String raw) {
        if (raw == null) throw new IllegalArgumentException("Password cannot be null");
        if (raw.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters long");
        if (raw.length() > 255) throw new IllegalArgumentException("Password is too long");
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password password)) return false;
        return Objects.equals(hash, password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}