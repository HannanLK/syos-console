package com.syos.application.dto.responses;

import com.syos.shared.enums.UserRole;

import java.time.LocalDateTime;

/**
 * User Response DTO
 * 
 * Response object for user operations.
 * Immutable data transfer object that carries user data across application boundaries.
 */
public final class UserResponse {
    private final boolean success;
    private final String message;
    private final Long userId;
    private final String username;
    private final String name;
    private final String email;
    private final UserRole role;
    private final String synexPoints;
    private final boolean active;
    private final LocalDateTime createdAt;

    private UserResponse(boolean success, String message, Long userId, String username, 
                        String name, String email, UserRole role, String synexPoints,
                        boolean active, LocalDateTime createdAt) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.synexPoints = synexPoints;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Factory method for successful response
     */
    public static UserResponse success(Long userId, String username, String name, 
                                     String email, UserRole role, String synexPoints,
                                     boolean active, LocalDateTime createdAt) {
        return new UserResponse(true, "Operation successful", userId, username, name, 
                               email, role, synexPoints, active, createdAt);
    }

    /**
     * Factory method for successful response with message
     */
    public static UserResponse success(String message, Long userId, String username, 
                                     String name, String email, UserRole role, 
                                     String synexPoints, boolean active, LocalDateTime createdAt) {
        return new UserResponse(true, message, userId, username, name, email, role, 
                               synexPoints, active, createdAt);
    }

    /**
     * Factory method for simple successful response
     */
    public static UserResponse success(Long userId, String username, String name, 
                                     String email, UserRole role, String message) {
        return new UserResponse(true, message, userId, username, name, email, role, 
                               null, true, LocalDateTime.now());
    }

    /**
     * Factory method for failure response
     */
    public static UserResponse failure(String message) {
        return new UserResponse(false, message, null, null, null, null, null, null, false, null);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getSynexPoints() { return synexPoints; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Additional getter for testing compatibility
    public String getErrorMessage() { return success ? null : message; }

    @Override
    public String toString() {
        return "UserResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
