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
     * Authenticate user and return User entity if successful
     * @param username the username
     * @param password the raw password
     * @return authenticated User
     * @throws AuthenticationException if authentication fails
     */
    public User login(String username, String password) {
        logger.info("Login attempt for username: {}", username);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }
        
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }
        
        try {
            // Create username value object
            Username u = Username.of(username.trim());
            
            logger.debug("Looking up user with username: {}", u.getValue());
            
            // Check if user exists first
            if (!userRepository.existsByUsername(u.getValue())) {
                logger.warn("Login failed: Username not found in repository - {}", username);
                
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
            Optional<User> userOpt = userRepository.findByUsername(u.getValue());
            
            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found - {}", username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            User user = userOpt.get();
            
            logger.debug("Found user: {} with role: {}", user.getUsername().getValue(), user.getRole());
            
            // Check password
            if (!user.getPassword().matches(password)) {
                logger.warn("Login failed: Invalid password for user - {}", username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Check if account is active
            if (!user.isActive()) {
                logger.warn("Login failed: Account inactive - {}", username);
                throw new AuthenticationException("Account is inactive. Please contact support.");
            }
            
            logger.info("Login successful for user: {} with role: {}", 
                user.getUsername().getValue(), user.getRole());
            
            return user;
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            throw new AuthenticationException("Login failed due to system error", e);
        }
    }
}