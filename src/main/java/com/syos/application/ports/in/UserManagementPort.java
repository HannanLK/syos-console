package com.syos.application.ports.in;

import com.syos.application.dto.commands.CreateUserCommand;
import com.syos.application.dto.responses.UserResponse;

/**
 * User Management Input Port
 * 
 * Defines the contract for user management operations.
 * Follows Interface Segregation Principle - only user management concerns.
 */
public interface UserManagementPort {
    
    /**
     * Create a new customer account
     */
    UserResponse createCustomer(CreateUserCommand.CreateCustomerCommand command);
    
    /**
     * Create a new employee account (admin only)
     */
    UserResponse createEmployee(CreateUserCommand.CreateEmployeeCommand command);
    
    /**
     * Create a new admin account (system only)
     */
    UserResponse createAdmin(CreateUserCommand.CreateAdminCommand command);
    
    /**
     * Deactivate user account
     */
    UserResponse deactivateUser(Long userId);
    
    /**
     * Update user profile
     */
    UserResponse updateProfile(Long userId, String name, String email);
}
