package com.syos.application.usecases.auth;

import com.syos.application.dto.commands.AuthCommand;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.ports.in.AuthenticationPort;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.Password;
import com.syos.domain.valueobjects.Username;
import com.syos.infrastructure.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Authentication Use Case
 * 
 * Handles user authentication operations following Clean Architecture principles.
 * Implements the AuthenticationPort and depends only on domain entities and repository abstractions.
 */
public class AuthenticationUseCase implements AuthenticationPort {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUseCase.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection following Dependency Inversion Principle
     */
    public AuthenticationUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(AuthCommand.LoginCommand command) {
        logger.info("Login attempt for username: {}", command.getUsername());
        
        try {
            // Input validation
            if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
                return AuthResponse.failure("Username is required");
            }
            
            if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
                return AuthResponse.failure("Password is required");
            }

            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(command.getUsername());
            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found - {}", command.getUsername());
                return AuthResponse.failure("Invalid username or password");
            }

            User user = userOpt.get();
            
            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed: User account deactivated - {}", command.getUsername());
                return AuthResponse.failure("Account is deactivated");
            }

            // Verify password
            boolean passwordValid = passwordEncoder.matches(
                command.getPassword(), 
                user.getPassword().getHash()
            );
            
            if (!passwordValid) {
                logger.warn("Login failed: Invalid password - {}", command.getUsername());
                return AuthResponse.failure("Invalid username or password");
            }

            // Generate session ID
            String sessionId = generateSessionId();
            
            logger.info("Login successful for user: {} ({})", user.getUsername().getValue(), user.getRole());
            
            return AuthResponse.success(
                "Login successful", 
                sessionId, 
                user.getId().getValue(), 
                user.getUsername().getValue(), 
                user.getRole()
            );

        } catch (Exception e) {
            logger.error("Error during login for username: {}", command.getUsername(), e);
            return AuthResponse.failure("An error occurred during login");
        }
    }

    @Override
    public AuthResponse registerCustomer(AuthCommand.RegisterCommand command) {
        logger.info("Customer registration attempt for username: {}", command.getUsername());
        
        try {
            // Input validation
            validateRegistrationCommand(command);

            // Check if username already exists
            if (userRepository.existsByUsername(command.getUsername())) {
                return AuthResponse.failure("Username already exists");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(command.getEmail())) {
                return AuthResponse.failure("Email already registered");
            }

            // Create domain entities using value objects
            Username username = Username.of(command.getUsername());
            com.syos.domain.valueobjects.Email email = com.syos.domain.valueobjects.Email.of(command.getEmail());
            Password password = Password.hash(command.getPassword());

            // Use domain factory method
            User newCustomer = User.createCustomer(username, email, password);
            
            // Update name if provided
            if (command.getName() != null && !command.getName().trim().isEmpty()) {
                newCustomer = newCustomer.updateProfile(
                    com.syos.domain.valueobjects.Name.of(command.getName()), 
                    email
                );
            }

            // Save user
            User savedUser = userRepository.save(newCustomer);

            // Generate session for immediate login
            String sessionId = generateSessionId();

            logger.info("Customer registration successful: {} (ID: {})", 
                       savedUser.getUsername().getValue(), savedUser.getId().getValue());

            return AuthResponse.success(
                "Registration successful", 
                sessionId, 
                savedUser.getId().getValue(), 
                savedUser.getUsername().getValue(), 
                savedUser.getRole()
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Registration validation failed: {}", e.getMessage());
            return AuthResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error during registration for username: {}", command.getUsername(), e);
            return AuthResponse.failure("An error occurred during registration");
        }
    }

    @Override
    public void logout(String sessionId) {
        logger.info("Logout for session: {}", sessionId);
        // In a real implementation, this would invalidate the session
        // For now, we'll handle session management in the SessionManager
    }

    @Override
    public boolean isValidSession(String sessionId) {
        // In a real implementation, this would validate against stored sessions
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    /**
     * Validate registration command
     */
    private void validateRegistrationCommand(AuthCommand.RegisterCommand command) {
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Business rules validation
        if (command.getUsername().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (command.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        if (!isValidEmail(command.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
