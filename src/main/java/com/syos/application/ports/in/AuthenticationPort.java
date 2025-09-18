package com.syos.application.ports.in;

import com.syos.application.dto.commands.AuthCommand;
import com.syos.application.dto.responses.AuthResponse;

/**
 * Authentication Input Port
 * 
 * Defines the contract for authentication operations.
 * Separated from user management following ISP.
 */
public interface AuthenticationPort {
    
    /**
     * Authenticate user login
     */
    AuthResponse login(AuthCommand.LoginCommand command);
    
    /**
     * Register new customer
     */
    AuthResponse registerCustomer(AuthCommand.RegisterCommand command);
    
    /**
     * Logout current session
     */
    void logout(String sessionId);
    
    /**
     * Validate session
     */
    boolean isValidSession(String sessionId);
}
