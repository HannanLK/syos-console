package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.dto.requests.LoginRequest;
import com.syos.application.dto.requests.RegisterRequest;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Authentication Controller
 * Handles user authentication and registration operations
 */
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    private final ConsoleIO consoleIO;
    private final SessionManager sessionManager;
    private final LoginUseCase loginUseCase;
    private final RegisterCustomerUseCase registerCustomerUseCase;

    public AuthenticationController(ConsoleIO consoleIO, 
                                   SessionManager sessionManager,
                                   LoginUseCase loginUseCase,
                                   RegisterCustomerUseCase registerCustomerUseCase) {
        this.consoleIO = consoleIO;
        this.sessionManager = sessionManager;
        this.loginUseCase = loginUseCase;
        this.registerCustomerUseCase = registerCustomerUseCase;
    }

    /**
     * Handle user login process
     */
    public AuthResponse handleLogin() {
        try {
            consoleIO.printLine("=== User Login ===");
            
            String username = consoleIO.readLine("Username: ");
            if (username == null || username.trim().isEmpty()) {
                return AuthResponse.failure("Username cannot be empty");
            }
            
            String password = consoleIO.readPassword("Password: ");
            if (password == null || password.isEmpty()) {
                return AuthResponse.failure("Password cannot be empty");
            }
            
            // Create login request
            LoginRequest request = new LoginRequest(username.trim(), password);
            
            // Authenticate user
            User user = loginUseCase.login(request.getUsername(), request.getPassword());
            
            // Create session
            UserSession session = new UserSession(user);
            sessionManager.createSession(session);
            
            // Generate session ID
            String sessionId = UUID.randomUUID().toString();
            
            consoleIO.printSuccess("Login successful! Welcome, " + user.getName().getValue());
            
            return AuthResponse.success(
                sessionId,
                user.getId() != null ? user.getId().getValue() : null,
                user.getUsername().getValue(),
                user.getRole()
            );
            
        } catch (AuthenticationException e) {
            consoleIO.printError("Login failed: " + e.getMessage());
            return AuthResponse.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            consoleIO.printError("Login failed due to system error");
            return AuthResponse.failure("System error during login");
        }
    }

    /**
     * Handle user registration process
     */
    public UserResponse handleRegistration() {
        try {
            consoleIO.printLine("=== Customer Registration ===");
            
            String name = consoleIO.readLine("Full Name: ");
            if (name == null || name.trim().isEmpty()) {
                return UserResponse.failure("Name cannot be empty");
            }
            
            String username = consoleIO.readLine("Username: ");
            if (username == null || username.trim().isEmpty()) {
                return UserResponse.failure("Username cannot be empty");
            }
            
            String email = consoleIO.readLine("Email: ");
            if (email == null || email.trim().isEmpty()) {
                return UserResponse.failure("Email cannot be empty");
            }
            
            String password = consoleIO.readPassword("Password: ");
            if (password == null || password.isEmpty()) {
                return UserResponse.failure("Password cannot be empty");
            }
            
            String confirmPassword = consoleIO.readPassword("Confirm Password: ");
            if (!password.equals(confirmPassword)) {
                return UserResponse.failure("Passwords do not match");
            }
            
            // Create registration request
            RegisterRequest request = RegisterRequest.fromLegacy(
                name.trim(), 
                username.trim(), 
                email.trim(), 
                password
            );
            
            // Register user
            User user = registerCustomerUseCase.register(request);
            
            consoleIO.printSuccess("Registration successful! You can now login with your credentials.");
            
            return UserResponse.success(
                user.getId() != null ? user.getId().getValue() : null,
                user.getUsername().getValue(),
                user.getName().getValue(),
                user.getEmail().getValue(),
                user.getRole(),
                "Registration successful"
            );
            
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            consoleIO.printError("Registration failed: " + e.getMessage());
            return UserResponse.failure(e.getMessage());
        }
    }

    /**
     * Handle logout process
     */
    public void handleLogout() {
        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getCurrentSession().getUsername();
            sessionManager.clearSession();
            consoleIO.printSuccess("Logged out successfully. Goodbye, " + username + "!");
        } else {
            consoleIO.printInfo("You are not currently logged in.");
        }
    }

    /**
     * Display current session information
     */
    public void displaySessionInfo() {
        if (sessionManager.isLoggedIn()) {
            UserSession session = sessionManager.getCurrentSession();
            consoleIO.printLine("=== Current Session ===");
            consoleIO.printLine("User: " + session.getName());
            consoleIO.printLine("Username: " + session.getUsername());
            consoleIO.printLine("Role: " + session.getRole());
            consoleIO.printLine("SYNEX Points: " + session.getSynexPoints());
            consoleIO.printLine("Login Time: " + session.getLoginTime());
        } else {
            consoleIO.printInfo("No active session");
        }
    }
}
