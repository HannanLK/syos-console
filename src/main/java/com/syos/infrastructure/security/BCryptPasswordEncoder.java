package com.syos.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt implementation of PasswordEncoder
 */
public class BCryptPasswordEncoder implements PasswordEncoder {
    private final int strength;

    public BCryptPasswordEncoder() {
        this(12);
    }

    public BCryptPasswordEncoder(int strength) {
        if (strength < 4 || strength > 31) {
            throw new IllegalArgumentException("BCrypt strength must be between 4 and 31");
        }
        this.strength = strength;
    }

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Raw password cannot be null/blank");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(strength));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
    
    // Legacy method names for compatibility
    public String hash(String rawPassword) {
        return encode(rawPassword);
    }
}