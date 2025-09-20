package com.syos.adapter.in.cli.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for AuthenticationController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerEnhancedTest {
    
    @Mock
    private ConsoleIO consoleIO;
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private LoginUseCase loginUseCase;
    
    @Mock
    private RegisterCustomerUseCase registerCustomerUseCase;
    
    private AuthenticationController authController;
    
    @BeforeEach
    void setUp() {
        authController = new AuthenticationController(
            consoleIO, 
            sessionManager, 
            loginUseCase, 
            registerCustomerUseCase
        );
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create controller with valid dependencies")
        void shouldCreateControllerWithValidDependencies() {
            assertNotNull(authController);
        }
    }
    
    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        
        @Test
        @DisplayName("Should handle successful login")
        void shouldHandleSuccessfulLogin() throws Exception {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            User mockUser = User.createCustomer(
                Username.of("testuser"),
                Email.of("test@example.com"),
                com.syos.domain.valueobjects.Password.hash("password123")
            );
            when(loginUseCase.login("testuser", "password123")).thenReturn(mockUser);
            
            // When
            AuthResponse result = authController.handleLogin();
            
            // Then
            assertTrue(result.isSuccess());
            assertEquals("testuser", result.getUsername());
            assertEquals(UserRole.CUSTOMER, result.getRole());
            verify(sessionManager).createSession(any(UserSession.class));
        }
        
        @Test
        @DisplayName("Should handle login failure")
        void shouldHandleLoginFailure() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("wronguser");
            when(consoleIO.readPassword("Password: ")).thenReturn("wrongpass");
            when(loginUseCase.login("wronguser", "wrongpass"))
                .thenThrow(new AuthenticationException("Invalid credentials"));
            
            // When
            AuthResponse result = authController.handleLogin();
            
            // Then
            assertFalse(result.isSuccess());
            assertEquals("Invalid credentials", result.getMessage());
            verify(sessionManager, never()).createSession(any(UserSession.class));
        }
        
        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("");
            
            // When
            AuthResponse result = authController.handleLogin();
            
            // Then
            assertFalse(result.isSuccess());
            assertEquals("Username cannot be empty", result.getMessage());
        }
        
        @Test
        @DisplayName("Should handle empty password")
        void shouldHandleEmptyPassword() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("");
            
            // When
            AuthResponse result = authController.handleLogin();
            
            // Then
            assertFalse(result.isSuccess());
            assertEquals("Password cannot be empty", result.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {
        
        @Test
        @DisplayName("Should handle successful registration")
        void shouldHandleSuccessfulRegistration() throws Exception {
            // Given
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readLine("Email: ")).thenReturn("test@example.com");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            when(consoleIO.readPassword("Confirm Password: ")).thenReturn("password123");
            
            User mockUser = User.createCustomer(
                Username.of("testuser"),
                Email.of("test@example.com"),
                com.syos.domain.valueobjects.Password.hash("password123")
            );
            when(registerCustomerUseCase.register(any())).thenReturn(mockUser);
            
            // When
            UserResponse result = authController.handleRegistration();
            
            // Then
            assertTrue(result.isSuccess());
            assertEquals("testuser", result.getUsername());
            assertEquals(UserRole.CUSTOMER, result.getRole());
        }
        
        @Test
        @DisplayName("Should handle password mismatch")
        void shouldHandlePasswordMismatch() {
            // Given
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readLine("Email: ")).thenReturn("test@example.com");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            when(consoleIO.readPassword("Confirm Password: ")).thenReturn("different");
            
            // When
            UserResponse result = authController.handleRegistration();
            
            // Then
            assertFalse(result.isSuccess());
            assertEquals("Passwords do not match", result.getMessage());
        }
        
        @Test
        @DisplayName("Should handle empty name")
        void shouldHandleEmptyName() {
            // Given
            when(consoleIO.readLine("Full Name: ")).thenReturn("");
            
            // When
            UserResponse result = authController.handleRegistration();
            
            // Then
            assertFalse(result.isSuccess());
            assertEquals("Name cannot be empty", result.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {
        
        @Test
        @DisplayName("Should handle logout when logged in")
        void shouldHandleLogoutWhenLoggedIn() {
            // Given
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            when(sessionManager.isLoggedIn()).thenReturn(true);
            when(sessionManager.getCurrentSession()).thenReturn(mockSession);
            
            // When
            authController.handleLogout();
            
            // Then
            verify(sessionManager).clearSession();
            verify(consoleIO).printSuccess(contains("Goodbye, testuser"));
        }
        
        @Test
        @DisplayName("Should handle logout when not logged in")
        void shouldHandleLogoutWhenNotLoggedIn() {
            // Given
            when(sessionManager.isLoggedIn()).thenReturn(false);
            
            // When
            authController.handleLogout();
            
            // Then
            verify(sessionManager, never()).clearSession();
            verify(consoleIO).printInfo("You are not currently logged in.");
        }
    }
    
    @Nested
    @DisplayName("Session Info Tests")
    class SessionInfoTests {
        
        @Test
        @DisplayName("Should display session info when logged in")
        void shouldDisplaySessionInfoWhenLoggedIn() {
            // Given
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getName()).thenReturn("Test User");
            when(mockSession.getUsername()).thenReturn("testuser");
            when(mockSession.getRole()).thenReturn(UserRole.CUSTOMER);
            when(sessionManager.isLoggedIn()).thenReturn(true);
            when(sessionManager.getCurrentSession()).thenReturn(mockSession);
            
            // When
            authController.displaySessionInfo();
            
            // Then
            verify(consoleIO).printLine("=== Current Session ===");
            verify(consoleIO).printLine("User: Test User");
            verify(consoleIO).printLine("Username: testuser");
            verify(consoleIO).printLine("Role: CUSTOMER");
        }
        
        @Test
        @DisplayName("Should display no session when not logged in")
        void shouldDisplayNoSessionWhenNotLoggedIn() {
            // Given
            when(sessionManager.isLoggedIn()).thenReturn(false);
            
            // When
            authController.displaySessionInfo();
            
            // Then
            verify(consoleIO).printInfo("No active session");
        }
    }
}
