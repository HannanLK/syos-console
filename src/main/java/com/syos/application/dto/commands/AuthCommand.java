package com.syos.application.dto.commands;

/**
 * Authentication Commands
 * 
 * Command objects for authentication operations.
 * Immutable DTOs that carry authentication data across application boundaries.
 */
public final class AuthCommand {
    
    /**
     * Command for user login
     */
    public static final class LoginCommand {
        private final String username;
        private final String password;
        
        public LoginCommand(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        @Override
        public String toString() {
            return "LoginCommand{username='" + username + "'}";
        }
    }
    
    /**
     * Command for customer registration
     */
    public static final class RegisterCommand {
        private final String username;
        private final String email;
        private final String password;
        private final String name;
        
        // Constructor expected by tests: (name, username, email, password)
        public RegisterCommand(String name, String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.name = name;
        }
        
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getName() { return name; }
        
        @Override
        public String toString() {
            return "RegisterCommand{username='" + username + "', email='" + email + 
                   "', name='" + name + "'}";
        }
    }
}
