package com.syos.application.usecases.user;

import com.syos.application.dto.commands.CreateUserCommand;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.ports.in.UserManagementPort;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User Management Use Case
 * 
 * Handles user creation and management operations following Clean Architecture principles.
 * Implements UserManagementPort and depends only on domain entities and repository abstractions.
 */
public class UserManagementUseCase implements UserManagementPort {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementUseCase.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection following Dependency Inversion Principle
     */
    public UserManagementUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createCustomer(CreateUserCommand.CreateCustomerCommand command) {
        logger.info("Creating customer: {}", command.getUsername());
        
        try {
            // Validate command
            validateCreateCustomerCommand(command);
            
            // Check uniqueness constraints
            if (userRepository.existsByUsername(command.getUsername())) {
                return UserResponse.failure("Username already exists");
            }
            
            if (userRepository.existsByEmail(command.getEmail())) {
                return UserResponse.failure("Email already registered");
            }

            // Create domain entities
            Username username = Username.of(command.getUsername());
            Email email = Email.of(command.getEmail());
            Password password = Password.hash(command.getPassword());
            
            // Use domain factory method
            User customer = User.createCustomer(username, email, password);

            // Save to repository
            User savedCustomer = userRepository.save(customer);

            logger.info("Customer created successfully: {} (ID: {})", 
                       savedCustomer.getUsername().getValue(), savedCustomer.getId().getValue());

            return UserResponse.success(
                "Customer created successfully",
                savedCustomer.getId().getValue(),
                savedCustomer.getUsername().getValue(),
                savedCustomer.getName().getValue(),
                savedCustomer.getEmail().getValue(),
                savedCustomer.getRole(),
                savedCustomer.getSynexPoints().toString(),
                savedCustomer.isActive(),
                savedCustomer.getCreatedAt()
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Customer creation validation failed: {}", e.getMessage());
            return UserResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating customer: {}", command.getUsername(), e);
            return UserResponse.failure("Failed to create customer");
        }
    }

    @Override
    public UserResponse createEmployee(CreateUserCommand.CreateEmployeeCommand command) {
        logger.info("Creating employee: {} by admin: {}", command.getUsername(), command.getCreatedBy());
        
        try {
            // Validate command
            validateCreateEmployeeCommand(command);
            
            // Verify admin exists and can create employees
            if (!canCreateEmployee(command.getCreatedBy())) {
                return UserResponse.failure("Only active admins can create employees");
            }
            
            // Check uniqueness constraints
            if (userRepository.existsByUsername(command.getUsername())) {
                return UserResponse.failure("Username already exists");
            }
            
            if (userRepository.existsByEmail(command.getEmail())) {
                return UserResponse.failure("Email already registered");
            }

            // Create domain entities
            Name name = Name.of(command.getName());
            Username username = Username.of(command.getUsername());
            Email email = Email.of(command.getEmail());
            Password password = Password.hash(command.getPassword());
            UserID createdBy = UserID.of(command.getCreatedBy());

            // Use domain factory method
            User employee = User.createEmployee(name, username, email, password, createdBy);

            // Save to repository
            User savedEmployee = userRepository.save(employee);

            logger.info("Employee created successfully: {} (ID: {}) by admin: {}", 
                       savedEmployee.getUsername().getValue(), 
                       savedEmployee.getId().getValue(),
                       command.getCreatedBy());

            return UserResponse.success(
                "Employee created successfully",
                savedEmployee.getId().getValue(),
                savedEmployee.getUsername().getValue(),
                savedEmployee.getName().getValue(),
                savedEmployee.getEmail().getValue(),
                savedEmployee.getRole(),
                savedEmployee.getSynexPoints().toString(),
                savedEmployee.isActive(),
                savedEmployee.getCreatedAt()
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Employee creation validation failed: {}", e.getMessage());
            return UserResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating employee: {}", command.getUsername(), e);
            return UserResponse.failure("Failed to create employee");
        }
    }

    @Override
    public UserResponse createAdmin(CreateUserCommand.CreateAdminCommand command) {
        logger.info("Creating admin: {}", command.getUsername());
        
        try {
            // Validate command
            validateCreateAdminCommand(command);
            
            // Check uniqueness constraints
            if (userRepository.existsByUsername(command.getUsername())) {
                return UserResponse.failure("Username already exists");
            }
            
            if (userRepository.existsByEmail(command.getEmail())) {
                return UserResponse.failure("Email already registered");
            }

            // Create domain entities
            Name name = Name.of(command.getName());
            Username username = Username.of(command.getUsername());
            Email email = Email.of(command.getEmail());
            Password password = Password.hash(command.getPassword());
            
            // Use domain factory method
            User admin = User.createAdmin(name, username, email, password);

            // Save to repository
            User savedAdmin = userRepository.save(admin);

            logger.info("Admin created successfully: {} (ID: {})", 
                       savedAdmin.getUsername().getValue(), savedAdmin.getId().getValue());

            return UserResponse.success(
                "Admin created successfully",
                savedAdmin.getId().getValue(),
                savedAdmin.getUsername().getValue(),
                savedAdmin.getName().getValue(),
                savedAdmin.getEmail().getValue(),
                savedAdmin.getRole(),
                savedAdmin.getSynexPoints().toString(),
                savedAdmin.isActive(),
                savedAdmin.getCreatedAt()
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Admin creation validation failed: {}", e.getMessage());
            return UserResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating admin: {}", command.getUsername(), e);
            return UserResponse.failure("Failed to create admin");
        }
    }

    @Override
    public UserResponse deactivateUser(Long userId) {
        logger.info("Deactivating user: {}", userId);
        
        try {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return UserResponse.failure("User not found");
            }

            User user = userOpt.get();
            User deactivatedUser = user.deactivate();
            User savedUser = userRepository.save(deactivatedUser);

            logger.info("User deactivated successfully: {} (ID: {})", 
                       savedUser.getUsername().getValue(), savedUser.getId().getValue());

            return UserResponse.success(
                "User deactivated successfully",
                savedUser.getId().getValue(),
                savedUser.getUsername().getValue(),
                savedUser.getName().getValue(),
                savedUser.getEmail().getValue(),
                savedUser.getRole(),
                savedUser.getSynexPoints().toString(),
                savedUser.isActive(),
                savedUser.getCreatedAt()
            );

        } catch (IllegalStateException e) {
            logger.warn("Cannot deactivate user {}: {}", userId, e.getMessage());
            return UserResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", userId, e);
            return UserResponse.failure("Failed to deactivate user");
        }
    }

    @Override
    public UserResponse updateProfile(Long userId, String name, String email) {
        logger.info("Updating profile for user: {}", userId);
        
        try {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return UserResponse.failure("User not found");
            }

            User user = userOpt.get();
            Name newName = Name.of(name);
            Email newEmail = Email.of(email);
            
            User updatedUser = user.updateProfile(newName, newEmail);
            User savedUser = userRepository.save(updatedUser);

            logger.info("Profile updated successfully for user: {} (ID: {})", 
                       savedUser.getUsername().getValue(), savedUser.getId().getValue());

            return UserResponse.success(
                "Profile updated successfully",
                savedUser.getId().getValue(),
                savedUser.getUsername().getValue(),
                savedUser.getName().getValue(),
                savedUser.getEmail().getValue(),
                savedUser.getRole(),
                savedUser.getSynexPoints().toString(),
                savedUser.isActive(),
                savedUser.getCreatedAt()
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Profile update validation failed for user {}: {}", userId, e.getMessage());
            return UserResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating profile for user: {}", userId, e);
            return UserResponse.failure("Failed to update profile");
        }
    }

    // Validation methods

    private void validateCreateCustomerCommand(CreateUserCommand.CreateCustomerCommand command) {
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private void validateCreateEmployeeCommand(CreateUserCommand.CreateEmployeeCommand command) {
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (command.getCreatedBy() == null || command.getCreatedBy() <= 0) {
            throw new IllegalArgumentException("Valid admin ID is required");
        }
    }

    private void validateCreateAdminCommand(CreateUserCommand.CreateAdminCommand command) {
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin name is required");
        }
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private boolean canCreateEmployee(Long adminId) {
        var adminOpt = userRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            return false;
        }
        User admin = adminOpt.get();
        return admin.canCreateUsers();
    }
}
