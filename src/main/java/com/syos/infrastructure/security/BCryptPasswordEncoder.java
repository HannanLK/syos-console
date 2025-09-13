package com.syos.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoder {
    private final int strength;

    public BCryptPasswordEncoder() {
        this(12);
    }

    public BCryptPasswordEncoder(int strength) {
        if (strength < 4 || strength > 31) throw new IllegalArgumentException("BCrypt strength must be between 4 and 31");
        this.strength = strength;
    }

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Raw password cannot be null/blank");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(strength));
    }

    public boolean matches(String rawPassword, String bcryptHash) {
        if (rawPassword == null || bcryptHash == null || bcryptHash.isBlank()) return false;
        return BCrypt.checkpw(rawPassword, bcryptHash);
    }
}
