package com.syos.adapter.in.cli.controllers;

import org.junit.jupiter.api.AfterEach;
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
import com.syos.application.dto.requests.LoginRequest;
import com.syos.application.dto.requests.RegisterRequest;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.valueobjects.Username;
import com.syos.shared.enums.UserRole;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AuthenticationController
 * Tests all user interaction scenarios, input validation, and session management
 * 
 * Target: 100% line coverage for AuthenticationController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Comprehensive Tests")
class AuthenticationControllerEnhancedTest {
    
    @Mock
    private ConsoleIO consoleIO;
    
    @Mock
    private LoginUseCase loginUseCase;
    
    @Mock
    private RegisterCustomerUseCase registerUseCase;
    
    @Mock
    private SessionManager sessionManager;
    
    private AuthenticationController authController;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        authController = new AuthenticationController(
            consoleIO, 
            sessionManager, 
            loginUseCase, 
            registerUseCase
        );
        
        // Capture system output for verification
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create controller with valid dependencies")
        void shouldCreateControllerWithValidDependencies() {
            assertNotNull(authController);
        }
        
        @Test
        @DisplayName("Should throw exception when ConsoleIO is null")
        void shouldThrowExceptionWhenConsoleIOIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new AuthenticationController(null, sessionManager, loginUseCase, registerUseCase);
            });
        }
    }
    
    @Nested
    @DisplayName("Login Process Tests")
    class LoginProcessTests {
        
        @Test
        @DisplayName("Should handle successful login flow")
        void shouldHandleSuccessfulLoginFlow() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            AuthResponse successResponse = new AuthResponse(
                true, 
                "testuser", 
                UserRole.CUSTOMER, 
                "session-token-123", 
                null
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(successResponse);
            
            UserSession userSession = new UserSession(new Username("testuser"), UserRole.CUSTOMER, "session-token-123");
            when(sessionManager.createSession("testuser", UserRole.CUSTOMER, "session-token-123"))
                .thenReturn(userSession);
            
            // When
            boolean result = authController.handleLogin();
            
            // Then
            assertTrue(result);
            verify(consoleIO).readLine("Username: ");
            verify(consoleIO).readPassword("Password: ");
            verify(loginUseCase).execute(any(LoginRequest.class));
            verify(sessionManager).createSession("testuser", UserRole.CUSTOMER, "session-token-123");
            verify(consoleIO).printSuccess(contains("Welcome, testuser"));
        }
        
        @Test
        @DisplayName("Should handle failed login flow")
        void shouldHandleFailedLoginFlow() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("wronguser");
            when(consoleIO.readPassword("Password: ")).thenReturn("wrongpass");
            
            AuthResponse failureResponse = new AuthResponse(
                false, 
                null, 
                null, 
                null, 
                "Invalid credentials"
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(failureResponse);
            
            // When
            boolean result = authController.handleLogin();
            
            // Then
            assertFalse(result);
            verify(consoleIO).readLine("Username: ");
            verify(consoleIO).readPassword("Password: ");
            verify(loginUseCase).execute(any(LoginRequest.class));
            verify(sessionManager, never()).createSession(anyString(), any(), anyString());
            verify(consoleIO).printError("Login failed: Invalid credentials");
        }
        
        @Test
        @DisplayName("Should handle empty username input")
        void shouldHandleEmptyUsernameInput() {
            // Given
            when(consoleIO.readLine("Username: "))
                .thenReturn("") // Empty first time
                .thenReturn("   ") // Blank second time
                .thenReturn("validuser"); // Valid third time
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            AuthResponse successResponse = new AuthResponse(
                true, 
                "validuser", 
                UserRole.CUSTOMER, 
                "session-token-123", 
                null
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(successResponse);
            
            UserSession userSession = new UserSession(new Username("validuser"), UserRole.CUSTOMER, "session-token-123");
            when(sessionManager.createSession("validuser", UserRole.CUSTOMER, "session-token-123"))
                .thenReturn(userSession);
            
            // When
            boolean result = authController.handleLogin();
            
            // Then
            assertTrue(result);
            verify(consoleIO, times(3)).readLine("Username: ");
            verify(consoleIO, times(2)).printWarning("Username cannot be empty. Please try again.");
        }
    }
    
    @Nested
    @DisplayName("Registration Process Tests")
    class RegistrationProcessTests {
        
        @Test
        @DisplayName("Should handle successful registration flow")
        void shouldHandleSuccessfulRegistrationFlow() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("newuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("NewPassword123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("New User");
            when(consoleIO.readLine("Email: ")).thenReturn("newuser@example.com");
            
            UserResponse successResponse = new UserResponse(
                true,
                "newuser",
                UserRole.CUSTOMER,
                "New User",
                "newuser@example.com",
                null
            );
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(successResponse);
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertTrue(result);
            verify(consoleIO).readLine("Username: ");
            verify(consoleIO).readPassword("Password: ");
            verify(consoleIO).readLine("Full Name: ");
            verify(consoleIO).readLine("Email: ");
            verify(registerUseCase).execute(any(RegisterRequest.class));
            verify(consoleIO).printSuccess(contains("Registration successful"));
        }
        
        @Test
        @DisplayName("Should handle failed registration flow")
        void shouldHandleFailedRegistrationFlow() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("existinguser");
            when(consoleIO.readPassword("Password: ")).thenReturn("Password123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("Existing User");
            when(consoleIO.readLine("Email: ")).thenReturn("existing@example.com");
            
            UserResponse failureResponse = new UserResponse(
                false,
                null,
                null,
                null,
                null,
                "Username already exists"
            );
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(failureResponse);
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertFalse(result);
            verify(registerUseCase).execute(any(RegisterRequest.class));
            verify(consoleIO).printError("Registration failed: Username already exists");
        }
        
        @Test
        @DisplayName("Should validate email format during registration")
        void shouldValidateEmailFormatDuringRegistration() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("Password123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Email: "))
                .thenReturn("invalid-email") // Invalid first time
                .thenReturn("still-invalid@") // Invalid second time
                .thenReturn("valid@example.com"); // Valid third time
            
            UserResponse successResponse = new UserResponse(
                true,
                "testuser",
                UserRole.CUSTOMER,
                "Test User",
                "valid@example.com",
                null
            );
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(successResponse);
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertTrue(result);
            verify(consoleIO, times(3)).readLine("Email: ");
            verify(consoleIO, times(2)).printWarning("Please enter a valid email address.");
        }
        
        @Test
        @DisplayName("Should handle registration use case exceptions")
        void shouldHandleRegistrationUseCaseExceptions() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("Password123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Email: ")).thenReturn("test@example.com");
            
            when(registerUseCase.execute(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertFalse(result);
            verify(consoleIO).printError(contains("Registration error"));
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should validate password strength during registration")
        void shouldValidatePasswordStrengthDuringRegistration() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: "))
                .thenReturn("weak") // Too weak first time
                .thenReturn("password") // No uppercase/numbers second time
                .thenReturn("StrongPassword123!"); // Strong third time
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Email: ")).thenReturn("test@example.com");
            
            UserResponse successResponse = new UserResponse(
                true,
                "testuser",
                UserRole.CUSTOMER,
                "Test User",
                "test@example.com",
                null
            );
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(successResponse);
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertTrue(result);
            verify(consoleIO, times(3)).readPassword("Password: ");
            verify(consoleIO, times(2)).printWarning(contains("Password must be"));
        }
        
        @Test
        @DisplayName("Should validate username format")
        void shouldValidateUsernameFormat() {
            // Given
            when(consoleIO.readLine("Username: "))
                .thenReturn("ab") // Too short first time
                .thenReturn("user with spaces") // Contains spaces second time
                .thenReturn("validuser"); // Valid third time
            when(consoleIO.readPassword("Password: ")).thenReturn("ValidPassword123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("Valid User");
            when(consoleIO.readLine("Email: ")).thenReturn("valid@example.com");
            
            UserResponse successResponse = new UserResponse(
                true,
                "validuser",
                UserRole.CUSTOMER,
                "Valid User",
                "valid@example.com",
                null
            );
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(successResponse);
            
            // When
            boolean result = authController.handleRegistration();
            
            // Then
            assertTrue(result);
            verify(consoleIO, times(3)).readLine("Username: ");
            verify(consoleIO, times(2)).printWarning(contains("Username must"));
        }
    }
    
    @Nested
    @DisplayName("Session Management Integration Tests")
    class SessionManagementIntegrationTests {
        
        @Test
        @DisplayName("Should integrate with session manager correctly")
        void shouldIntegrateWithSessionManagerCorrectly() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("sessionuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            AuthResponse successResponse = new AuthResponse(
                true, 
                "sessionuser", 
                UserRole.EMPLOYEE, 
                "session-token-456", 
                null
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(successResponse);
            
            UserSession mockSession = new UserSession(new Username("sessionuser"), UserRole.EMPLOYEE, "session-token-456");
            when(sessionManager.createSession("sessionuser", UserRole.EMPLOYEE, "session-token-456"))
                .thenReturn(mockSession);
            
            // When
            boolean result = authController.handleLogin();
            
            // Then
            assertTrue(result);
            verify(sessionManager).createSession("sessionuser", UserRole.EMPLOYEE, "session-token-456");
            
            // Verify session details
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            
            verify(sessionManager).createSession(usernameCaptor.capture(), roleCaptor.capture(), tokenCaptor.capture());
            
            assertEquals("sessionuser", usernameCaptor.getValue());
            assertEquals(UserRole.EMPLOYEE, roleCaptor.getValue());
            assertEquals("session-token-456", tokenCaptor.getValue());
        }
        
        @Test
        @DisplayName("Should handle session creation failure gracefully")
        void shouldHandleSessionCreationFailureGracefully() {
            // Given
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            AuthResponse successResponse = new AuthResponse(
                true, 
                "testuser", 
                UserRole.CUSTOMER, 
                "session-token-123", 
                null
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(successResponse);
            when(sessionManager.createSession("testuser", UserRole.CUSTOMER, "session-token-123"))
                .thenThrow(new RuntimeException("Session creation failed"));
            
            // When
            boolean result = authController.handleLogin();
            
            // Then
            assertFalse(result);
            verify(consoleIO).printError(contains("Login error"));
        }
    }
    
    @Nested
    @DisplayName("User Interface Interaction Tests")
    class UserInterfaceInteractionTests {
        
        @Test
        @DisplayName("Should provide clear error messages for different failure scenarios")
        void shouldProvideClearErrorMessagesForDifferentFailureScenarios() {
            // Test 1: Login failure
            when(consoleIO.readLine("Username: ")).thenReturn("testuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("wrongpass");
            
            AuthResponse loginFailure = new AuthResponse(false, null, null, null, "Invalid credentials");
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(loginFailure);
            
            assertFalse(authController.handleLogin());
            verify(consoleIO).printError("Login failed: Invalid credentials");
            
            // Reset mocks for next test
            reset(consoleIO, loginUseCase, registerUseCase);
            
            // Test 2: Registration failure
            when(consoleIO.readLine("Username: ")).thenReturn("existinguser");
            when(consoleIO.readPassword("Password: ")).thenReturn("ValidPass123!");
            when(consoleIO.readLine("Full Name: ")).thenReturn("Test User");
            when(consoleIO.readLine("Email: ")).thenReturn("test@example.com");
            
            UserResponse registrationFailure = new UserResponse(false, null, null, null, null, "Username already taken");
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(registrationFailure);
            
            assertFalse(authController.handleRegistration());
            verify(consoleIO).printError("Registration failed: Username already taken");
        }
        
        @Test
        @DisplayName("Should provide appropriate success messages")
        void shouldProvideAppropriateSuccessMessages() {
            // Test successful login message
            when(consoleIO.readLine("Username: ")).thenReturn("successuser");
            when(consoleIO.readPassword("Password: ")).thenReturn("password123");
            
            AuthResponse successResponse = new AuthResponse(
                true, 
                "successuser", 
                UserRole.ADMIN, 
                "session-token-789", 
                null
            );
            when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(successResponse);
            
            UserSession userSession = new UserSession(new Username("successuser"), UserRole.ADMIN, "session-token-789");
            when(sessionManager.createSession("successuser", UserRole.ADMIN, "session-token-789"))
                .thenReturn(userSession);
            
            assertTrue(authController.handleLogin());
            
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(consoleIO).printSuccess(messageCaptor.capture());
            
            String successMessage = messageCaptor.getValue();
            assertTrue(successMessage.contains("Welcome"));
            assertTrue(successMessage.contains("successuser"));
            assertTrue(successMessage.contains("ADMIN"));
        }
        
        @Test
        @DisplayName("Should handle user interruption gracefully")
        void shouldHandleUserInterruptionGracefully() {
            // Test interruption during login
            when(consoleIO.readLine("Username: ")).thenThrow(new RuntimeException("User interrupted"));
            
            assertFalse(authController.handleLogin());
            verify(consoleIO).printError(contains("Login error"));
            
            // Test interruption during registration
            reset(consoleIO);
            when(consoleIO.readLine("Username: ")).thenThrow(new RuntimeException("User interrupted"));
            
            assertFalse(authController.handleRegistration());
            verify(consoleIO).printError(contains("Registration error"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {
        
        @Test
        @DisplayName("Should handle very long input gracefully")
        void shouldHandleVeryLongInputGracefully() {
            // Create very long strings
            String longUsername = "a".repeat(1000);
            String longPassword = "P@ssw0rd" + "a".repeat(1000);
            String longName = "Very ".repeat(200) + "Long Name";
            String longEmail = "very" + "long".repeat(100) + "@example.com";
            
            when(consoleIO.readLine("Username: ")).thenReturn(longUsername);
            when(consoleIO.readPassword("Password: ")).thenReturn(longPassword);
            when(consoleIO.readLine("Full Name: ")).thenReturn(longName);
            when(consoleIO.readLine("Email: ")).thenReturn(longEmail);
            
            UserResponse response = new UserResponse(true, longUsername, UserRole.CUSTOMER, longName, longEmail, null);
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(response);
            
            // Should handle without crashing
            assertTrue(authController.handleRegistration());
            
            // Verify the use case received the long inputs
            ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
            verify(registerUseCase).execute(requestCaptor.capture());
            
            RegisterRequest capturedRequest = requestCaptor.getValue();
            assertEquals(longUsername, capturedRequest.getUsername());
            assertEquals(longName, capturedRequest.getName());
        }
        
        @Test
        @DisplayName("Should handle special characters in input")
        void shouldHandleSpecialCharactersInInput() {
            // Test with various special characters
            String specialUsername = "user@domain.com";
            String specialPassword = "P@$$w0rd!#%";
            String specialName = "José María Ñ";
            String specialEmail = "josé@domain.cóm";
            
            when(consoleIO.readLine("Username: ")).thenReturn(specialUsername);
            when(consoleIO.readPassword("Password: ")).thenReturn(specialPassword);
            when(consoleIO.readLine("Full Name: ")).thenReturn(specialName);
            when(consoleIO.readLine("Email: ")).thenReturn(specialEmail);
            
            UserResponse response = new UserResponse(true, specialUsername, UserRole.CUSTOMER, specialName, specialEmail, null);
            when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(response);
            
            assertTrue(authController.handleRegistration());
            
            ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
            verify(registerUseCase).execute(requestCaptor.capture());
            
            RegisterRequest capturedRequest = requestCaptor.getValue();
            assertEquals(specialUsername, capturedRequest.getUsername());
            assertEquals(specialName, capturedRequest.getName());
        }
    }
}
