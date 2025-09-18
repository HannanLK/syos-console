package com.syos.infrastructure.security;

/**
 * Password Encoder Interface
 * 
 * Abstraction for password encoding operations.
 * Allows different implementations (BCrypt, Argon2, etc.) following Strategy pattern.
 */
public interface PasswordEncoder {
    
    /**
     * Encode a raw password
     */
    String encode(String rawPassword);
    
    /**
     * Check if raw password matches encoded password
     */
    boolean matches(String rawPassword, String encodedPassword);
}
