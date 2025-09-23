package com.syos.application.usecases.auth;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import com.syos.domain.exceptions.DomainException;
import com.syos.infrastructure.security.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use case for creating new employee accounts
 * 
 * Addresses Requirements:
 * - Admin can create employee accounts
 * - Validate employee data before creation
 * - Ensure unique username/email
 * - Hash passwords securely
 */
public class CreateEmployeeUseCase {
    private static final Logger logger = LoggerFactory.getLogger(CreateEmployeeUseCase.class);
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CreateEmployeeUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Execute the create employee use case
     */
    public CreateEmployeeResponse execute(CreateEmployeeRequest request) {
        // Log safely without dereferencing possibly null request
        String uname = (request == null) ? "<null>" : String.valueOf(request.getUsername());
        logger.info("Creating new employee: {}", uname);
        
        try {
            validateRequest(request);
            
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DomainException("Username '" + request.getUsername() + "' already exists");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DomainException("Email '" + request.getEmail() + "' already exists");
            }

            // Create domain entity
            // Hash password using encoder to support broader policy (tests allow >=6 chars)
            String hashed = passwordEncoder.encode(request.getPassword());
            User employee = User.createEmployee(
                Name.of(request.getName()),
                Username.of(request.getUsername()),
                Email.of(request.getEmail()),
                Password.fromHash(hashed),
                UserID.of(request.getCreatedBy())
            );

            // Save to repository
            User savedEmployee = userRepository.save(employee);

            logger.info("Employee created successfully: {} (ID: {})", 
                savedEmployee.getUsername().getValue(), savedEmployee.getId().getValue());
            
            return CreateEmployeeResponse.success(savedEmployee.getId().getValue());
            
        } catch (IllegalArgumentException | DomainException e) {
            logger.warn("Employee creation failed: {}", e.getMessage());
            return CreateEmployeeResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating employee", e);
            return CreateEmployeeResponse.failure("Failed to create employee: " + e.getMessage());
        }
    }

    private void validateRequest(CreateEmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        if (request.getCreatedBy() == null || request.getCreatedBy() <= 0) {
            throw new IllegalArgumentException("Valid creator ID is required");
        }
    }

    /**
     * Request DTO for creating an employee
     */
    public static class CreateEmployeeRequest {
        private String name;
        private String username;
        private String email;
        private String password;
        private Long createdBy;

        public CreateEmployeeRequest() {}

        // Builder-style setters
        public CreateEmployeeRequest name(String name) { this.name = name; return this; }
        public CreateEmployeeRequest username(String username) { this.username = username; return this; }
        public CreateEmployeeRequest email(String email) { this.email = email; return this; }
        public CreateEmployeeRequest password(String password) { this.password = password; return this; }
        public CreateEmployeeRequest createdBy(Long createdBy) { this.createdBy = createdBy; return this; }

        // Getters
        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Long getCreatedBy() { return createdBy; }
    }

    /**
     * Response DTO for create employee operation
     */
    public static class CreateEmployeeResponse {
        private final boolean success;
        private final Long userId;
        private final String message;

        private CreateEmployeeResponse(boolean success, Long userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }

        public static CreateEmployeeResponse success(Long userId) {
            return new CreateEmployeeResponse(true, userId, "Employee created successfully");
        }

        public static CreateEmployeeResponse failure(String message) {
            return new CreateEmployeeResponse(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public Long getUserId() { return userId; }
        public String getMessage() { return message; }
    }
}
