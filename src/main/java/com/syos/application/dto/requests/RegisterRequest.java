package com.syos.application.dto.requests;

/**
 * DTO for registration requests
 */
public class RegisterRequest {
    private final String username;
    private final String password;
    private final String email;
    private final String name;

    // Preferred constructor used by integration tests (username, password, name, email)
    public RegisterRequest(String username, String password, String name, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    // Factory for legacy ordering (name, username, email, password)
    public static RegisterRequest fromLegacy(String name, String username, String email, String password) {
        return new RegisterRequest(username, password, name, email);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}