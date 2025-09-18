package com.syos.application.dto.responses;

import com.syos.shared.enums.UserRole;

/**
 * Authentication Response DTO
 * 
 * Response object for authentication operations.
 * Contains session information and user details.
 */
public final class AuthResponse {
    private final boolean success;
    private final String message;
    private final String sessionId;
    private final Long userId;
    private final String username;
    private final UserRole role;
    private final String token; // For future JWT implementation

    private AuthResponse(boolean success, String message, String sessionId, 
                        Long userId, String username, UserRole role, String token) {
        this.success = success;
        this.message = message;
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    /**
     * Factory method for successful authentication
     */
    public static AuthResponse success(String sessionId, Long userId, String username, 
                                     UserRole role) {
        return new AuthResponse(true, "Authentication successful", sessionId, 
                               userId, username, role, null);
    }

    /**
     * Factory method for successful authentication with message
     */
    public static AuthResponse success(String message, String sessionId, Long userId, 
                                     String username, UserRole role) {
        return new AuthResponse(true, message, sessionId, userId, username, role, null);
    }

    /**
     * Factory method for failed authentication
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null, null, null, null, null);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public String getToken() { return token; }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
