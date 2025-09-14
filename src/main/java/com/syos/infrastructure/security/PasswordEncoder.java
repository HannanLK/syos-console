package com.syos.infrastructure.security;

/**
 * Interface for password encoding
 */
public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}