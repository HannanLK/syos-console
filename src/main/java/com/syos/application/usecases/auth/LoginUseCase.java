package com.syos.application.usecases.auth;

import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Use case: authenticate a user by username and password.
 */
public class LoginUseCase {
    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);
    private final UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Execute method for compatibility with test frameworks using LoginRequest
     * @param request login request containing credentials
     * @return AuthResponse containing result of authentication
     */
    public com.syos.application.dto.responses.AuthResponse execute(com.syos.application.dto.requests.LoginRequest request) {
        // Eagerly validate inputs; tests expect exceptions for null/empty credentials
        if (request == null
                || request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Invalid login input");
        }
        try {
            User user = login(request.getUsername(), request.getPassword());
            // Generate a robust unique session token to avoid collisions within the same millisecond
            String sessionToken = "session_" + java.util.UUID.randomUUID();
            return com.syos.application.dto.responses.AuthResponse.success(
                sessionToken,
                user.getId() != null ? user.getId().getValue() : null,
                user.getUsername().getValue(),
                user.getRole()
            );
        } catch (AuthenticationException e) {
            return com.syos.application.dto.responses.AuthResponse.failure(e.getMessage());
        }
    }

    /**
     * Authenticate user and return User entity if successful
     * @param username the username
     * @param password the raw password
     * @return authenticated User
     * @throws AuthenticationException if authentication fails
     */
    public User login(String username, String password) {
        logger.trace("Login attempt for username: {}", username);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }
        
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }
        
        try {
            String trimmed = username.trim();
            logger.trace("Looking up user with username: {}", trimmed);

            // Check if user exists first using repository (accept raw strings per interface defaults)
            if (!userRepository.existsByUsername(trimmed)) {
                logger.warn("Login failed: Username not found in repository - {}", trimmed);

                // Debug: Print all users if this is development and repository supports it
                try {
                    java.lang.reflect.Method printMethod = userRepository.getClass().getMethod("printAllUsers");
                    printMethod.invoke(userRepository);
                } catch (Exception ignored) {
                    // Method not available - that's OK
                }

                throw new AuthenticationException("Invalid username or password");
            }

            // Find user
            Optional<User> userOpt = userRepository.findByUsername(trimmed);

            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found - {}", trimmed);
                throw new AuthenticationException("Invalid username or password");
            }

            User user = userOpt.get();

            logger.trace("Found user: {} with role: {}", user.getUsername().getValue(), user.getRole());

            // Check password
            if (!user.getPassword().matches(password)) {
                logger.warn("Login failed: Invalid password for user - {}", trimmed);
                throw new AuthenticationException("Invalid username or password");
            }

            // Check if account is active
            if (!user.isActive()) {
                logger.warn("Login failed: Account inactive - {}", trimmed);
                throw new AuthenticationException("Account is inactive. Please contact support.");
            }

            logger.info("Login successful for user: {} with role: {}", 
                user.getUsername().getValue(), user.getRole());

            return user;
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (com.syos.domain.exceptions.InvalidUsernameException e) {
            // Map domain username validation errors to a generic auth failure to avoid leaking rules
            logger.warn("Login failed due to invalid username format: {}", username);
            throw new AuthenticationException("Invalid username or password", e);
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            throw new AuthenticationException("Login failed due to system error", e);
        }
    }
}