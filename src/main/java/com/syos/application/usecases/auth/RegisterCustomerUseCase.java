package com.syos.application.usecases.auth;

import com.syos.application.exceptions.RegistrationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.domain.valueobjects.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Use case: register a new customer account.
 */
public class RegisterCustomerUseCase {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCustomerUseCase.class);
    private final UserRepository userRepository;

    public RegisterCustomerUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Register a new customer
     * @param username desired username
     * @param rawPassword raw password
     * @param name full name
     * @param email email address
     * @return newly created User
     * @throws RegistrationException if registration fails
     */
    public User register(String username, String rawPassword, String name, String email) {
        logger.info("Registration attempt for username: {} with email: {}", username, email);
        
        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                throw new RegistrationException("Username cannot be empty");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new RegistrationException("Name cannot be empty");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new RegistrationException("Email cannot be empty");
            }
            if (rawPassword == null || rawPassword.length() < 8) {
                throw new RegistrationException("Password must be at least 8 characters long");
            }
            
            // Validate and create value objects
            Username u = Username.of(username.trim());
            Name n = Name.of(name.trim());
            Email e = Email.of(email.trim());
            
            logger.debug("Created value objects - Username: {}, Name: {}, Email: {}", 
                u.getValue(), n.getValue(), e.getValue());
            
            // Check if username already exists
            if (userRepository.existsByUsername(u.getValue())) {
                logger.warn("Registration failed: Username already exists - {}", username);
                throw new RegistrationException("Username already taken");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(e.getValue())) {
                logger.warn("Registration failed: Email already registered - {}", email);
                throw new RegistrationException("Email already registered");
            }
            
            logger.debug("Username and email availability checked - proceeding with user creation");
            
            // Create new user
            User user = User.registerNew(u, rawPassword, n, e);
            
            logger.debug("User entity created successfully - Username: {}, Role: {}", 
                user.getUsername().getValue(), user.getRole());
            
            // Save to repository
            User savedUser = userRepository.save(user);
            
            logger.info("Registration successful for user: {} with email: {} and role: {}", 
                username, email, savedUser.getRole());
            
            return savedUser;
            
        } catch (RegistrationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            throw new RegistrationException("Registration failed: " + e.getMessage(), e);
        }
    }
}