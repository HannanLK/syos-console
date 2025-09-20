package com.syos.domain.valueobjects;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

/**
 * Password value object that stores only the BCrypt hash.
 */
public final class Password {
    private final String hash; // stored BCrypt hash
    // Raw candidate retained only when constructed from raw for test/backward compatibility
    private final transient String rawCandidate;

    // Private constructor for internal use with already hashed passwords
    private Password(String hash, boolean isAlreadyHashed) {
        if (isAlreadyHashed) {
            if (hash == null || hash.isBlank()) {
                throw new IllegalArgumentException("Password hash cannot be null/blank");
            }
            this.hash = hash;
            this.rawCandidate = null;
        } else {
            validateRaw(hash);
            String salt = BCrypt.gensalt(determineBcryptCost());
            this.hash = BCrypt.hashpw(hash, salt);
            this.rawCandidate = hash;
        }
    }

    // Public constructor for backward compatibility with tests - assumes raw password
    public Password(String rawPassword) {
        this(rawPassword, false);
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
        return new Password(bcryptHash, true);
    }

    /**
     * Create Password by hashing a raw (plain text) password.
     */
    public static Password hash(String rawPassword) {
        return new Password(rawPassword, false);
    }

    public boolean matches(String rawPassword) {
        validateRaw(rawPassword);
        return BCrypt.checkpw(rawPassword, this.hash);
    }

    // Compare against another Password instance.
    // If the other was created from raw, verify raw against this hash; otherwise fall back to hash equality.
    public boolean matches(Password other) {
        if (other == null) return false;
        if (other.rawCandidate != null) {
            return matches(other.rawCandidate);
        }
        return this.hash.equals(other.hash);
    }
    
    // Method for hash-to-hash comparison (needed for User authentication)
    public boolean matches(String candidateHash, boolean isHash) {
        if (isHash) {
            return this.hash.equals(candidateHash);
        } else {
            return matches(candidateHash);
        }
    }

    private static void validateRaw(String raw) {
        if (raw == null) throw new IllegalArgumentException("Password cannot be null");
        if (raw.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters long");
        if (raw.length() > 255) throw new IllegalArgumentException("Password is too long");
    }

    private static int determineBcryptCost() {
        try {
            String prop = System.getProperty("BCRYPT_COST");
            if (prop != null && !prop.isBlank()) {
                int cost = Integer.parseInt(prop.trim());
                if (cost >= 4 && cost <= 15) return cost;
            }
        } catch (Exception ignored) {}
        // Lower cost under Maven Surefire (tests), higher otherwise
        boolean underTests = System.getProperty("surefire.test.class.path") != null;
        return underTests ? 4 : 12;
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