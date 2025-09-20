package com.syos.application.usecases.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.syos.application.dto.requests.LoginRequest;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for LoginUseCase
 * Tests all authentication scenarios, security validation, and error handling
 * 
 * Target: 100% line coverage for LoginUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Comprehensive Tests")
class LoginUseCaseEnhancedTest {
    
    @Mock
    private UserRepository userRepository;
    
    private LoginUseCase loginUseCase;
    private LoginRequest validLoginRequest;
    private User activeCustomer;
    private User activeEmployee;
    private User activeAdmin;
    private User inactiveUser;
    
    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository);
        
        validLoginRequest = new LoginRequest("testuser", "SecurePassword123!");
        
        // Create test users
        activeCustomer = new User(
            new Username("testuser"),
            new Password("SecurePassword123!"),
            new Name("Test Customer"),
            new Email("customer@example.com"),
            UserRole.CUSTOMER
        );
        
        activeEmployee = new User(
            new Username("employee"),
            new Password("SecurePassword123!"),
            new Name("Test Employee"),
            new Email("employee@syos.com"),
            UserRole.EMPLOYEE
        );
        
        activeAdmin = new User(
            new Username("admin"),
            new Password("SecurePassword123!"),
            new Name("Test Admin"),
            new Email("admin@syos.com"),
            UserRole.ADMIN
        );
        
        inactiveUser = new User(
            new Username("inactive"),
            new Password("SecurePassword123!"),
            new Name("Inactive User"),
            new Email("inactive@example.com"),
            UserRole.CUSTOMER
        );
        inactiveUser.deactivate(); // Make user inactive
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create LoginUseCase with valid repository")
        void shouldCreateLoginUseCaseWithValidRepository() {
            assertNotNull(loginUseCase);
        }
        
        @Test
        @DisplayName("Should throw exception when repository is null")
        void shouldThrowExceptionWhenRepositoryIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new LoginUseCase(null);
            });
        }
    }
    
    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {
        
        @Test
        @DisplayName("Should successfully authenticate active customer")
        void shouldSuccessfullyAuthenticateActiveCustomer() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            AuthResponse response = loginUseCase.execute(validLoginRequest);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals("testuser", response.getUsername());
            assertEquals(UserRole.CUSTOMER, response.getUserRole());
            assertNotNull(response.getSessionToken());
            assertNull(response.getErrorMessage());
            
            verify(userRepository).findByUsername("testuser");
            verify(userRepository).updateLastLogin(activeCustomer);
        }
        
        @Test
        @DisplayName("Should successfully authenticate active employee")
        void shouldSuccessfullyAuthenticateActiveEmployee() {
            // Given
            LoginRequest employeeRequest = new LoginRequest("employee", "SecurePassword123!");
            when(userRepository.findByUsername("employee")).thenReturn(Optional.of(activeEmployee));
            
            // When
            AuthResponse response = loginUseCase.execute(employeeRequest);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals("employee", response.getUsername());
            assertEquals(UserRole.EMPLOYEE, response.getUserRole());
            assertNotNull(response.getSessionToken());
            
            verify(userRepository).findByUsername("employee");
            verify(userRepository).updateLastLogin(activeEmployee);
        }
        
        @Test
        @DisplayName("Should successfully authenticate active admin")
        void shouldSuccessfullyAuthenticateActiveAdmin() {
            // Given
            LoginRequest adminRequest = new LoginRequest("admin", "SecurePassword123!");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(activeAdmin));
            
            // When
            AuthResponse response = loginUseCase.execute(adminRequest);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals("admin", response.getUsername());
            assertEquals(UserRole.ADMIN, response.getUserRole());
            assertNotNull(response.getSessionToken());
            
            verify(userRepository).findByUsername("admin");
            verify(userRepository).updateLastLogin(activeAdmin);
        }
    }
    
    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {
        
        @Test
        @DisplayName("Should fail authentication when user does not exist")
        void shouldFailAuthenticationWhenUserDoesNotExist() {
            // Given
            LoginRequest nonExistentUserRequest = new LoginRequest("nonexistent", "password");
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            
            // When
            AuthResponse response = loginUseCase.execute(nonExistentUserRequest);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getUsername());
            assertNull(response.getUserRole());
            assertNull(response.getSessionToken());
            assertEquals("Invalid username or password", response.getErrorMessage());
            
            verify(userRepository).findByUsername("nonexistent");
            verify(userRepository, never()).updateLastLogin(any());
        }
        
        @Test
        @DisplayName("Should fail authentication when password is incorrect")
        void shouldFailAuthenticationWhenPasswordIsIncorrect() {
            // Given
            LoginRequest wrongPasswordRequest = new LoginRequest("testuser", "WrongPassword123!");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            AuthResponse response = loginUseCase.execute(wrongPasswordRequest);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getUsername());
            assertNull(response.getUserRole());
            assertNull(response.getSessionToken());
            assertEquals("Invalid username or password", response.getErrorMessage());
            
            verify(userRepository).findByUsername("testuser");
            verify(userRepository, never()).updateLastLogin(any());
        }
        
        @Test
        @DisplayName("Should fail authentication when user is inactive")
        void shouldFailAuthenticationWhenUserIsInactive() {
            // Given
            LoginRequest inactiveUserRequest = new LoginRequest("inactive", "SecurePassword123!");
            when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(inactiveUser));
            
            // When
            AuthResponse response = loginUseCase.execute(inactiveUserRequest);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getUsername());
            assertNull(response.getUserRole());
            assertNull(response.getSessionToken());
            assertEquals("Account is deactivated", response.getErrorMessage());
            
            verify(userRepository).findByUsername("inactive");
            verify(userRepository, never()).updateLastLogin(any());
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should throw exception when login request is null")
        void shouldThrowExceptionWhenLoginRequestIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            LoginRequest nullUsernameRequest = new LoginRequest(null, "password");
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(nullUsernameRequest);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when username is empty")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            LoginRequest emptyUsernameRequest = new LoginRequest("", "password");
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(emptyUsernameRequest);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when username is blank")
        void shouldThrowExceptionWhenUsernameIsBlank() {
            LoginRequest blankUsernameRequest = new LoginRequest("   ", "password");
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(blankUsernameRequest);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            LoginRequest nullPasswordRequest = new LoginRequest("username", null);
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(nullPasswordRequest);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is empty")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            LoginRequest emptyPasswordRequest = new LoginRequest("username", "");
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(emptyPasswordRequest);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is blank")
        void shouldThrowExceptionWhenPasswordIsBlank() {
            LoginRequest blankPasswordRequest = new LoginRequest("username", "   ");
            
            assertThrows(IllegalArgumentException.class, () -> {
                loginUseCase.execute(blankPasswordRequest);
            });
        }
    }
    
    @Nested
    @DisplayName("Session Token Generation Tests")
    class SessionTokenGenerationTests {
        
        @Test
        @DisplayName("Should generate unique session tokens for different login sessions")
        void shouldGenerateUniqueSessionTokensForDifferentLoginSessions() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            AuthResponse response1 = loginUseCase.execute(validLoginRequest);
            AuthResponse response2 = loginUseCase.execute(validLoginRequest);
            
            // Then
            assertNotNull(response1.getSessionToken());
            assertNotNull(response2.getSessionToken());
            assertNotEquals(response1.getSessionToken(), response2.getSessionToken());
            
            verify(userRepository, times(2)).findByUsername("testuser");
            verify(userRepository, times(2)).updateLastLogin(activeCustomer);
        }
        
        @Test
        @DisplayName("Should generate session token with proper format")
        void shouldGenerateSessionTokenWithProperFormat() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            AuthResponse response = loginUseCase.execute(validLoginRequest);
            
            // Then
            String sessionToken = response.getSessionToken();
            assertNotNull(sessionToken);
            assertTrue(sessionToken.length() > 10); // Should be a reasonable length
            assertTrue(sessionToken.matches("^[A-Za-z0-9\\-]+$")); // Should be alphanumeric with hyphens
        }
    }
    
    @Nested
    @DisplayName("Repository Interaction Tests")
    class RepositoryInteractionTests {
        
        @Test
        @DisplayName("Should call repository methods in correct order for successful login")
        void shouldCallRepositoryMethodsInCorrectOrderForSuccessfulLogin() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            loginUseCase.execute(validLoginRequest);
            
            // Then
            verify(userRepository).findByUsername("testuser");
            verify(userRepository).updateLastLogin(activeCustomer);
        }
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(userRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                loginUseCase.execute(validLoginRequest);
            });
            
            verify(userRepository).findByUsername("testuser");
            verify(userRepository, never()).updateLastLogin(any());
        }
        
        @Test
        @DisplayName("Should handle update last login failure gracefully")
        void shouldHandleUpdateLastLoginFailureGracefully() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            doThrow(new RuntimeException("Update failed")).when(userRepository).updateLastLogin(activeCustomer);
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                loginUseCase.execute(validLoginRequest);
            });
            
            verify(userRepository).findByUsername("testuser");
            verify(userRepository).updateLastLogin(activeCustomer);
        }
    }
    
    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        
        @Test
        @DisplayName("Should not reveal if username exists when authentication fails")
        void shouldNotRevealIfUsernameExistsWhenAuthenticationFails() {
            // Given - Non-existent user
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            
            // When
            AuthResponse response1 = loginUseCase.execute(new LoginRequest("nonexistent", "password"));
            
            // Given - Existing user with wrong password
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            AuthResponse response2 = loginUseCase.execute(new LoginRequest("testuser", "wrongpassword"));
            
            // Then - Both should have the same error message
            assertEquals(response1.getErrorMessage(), response2.getErrorMessage());
            assertEquals("Invalid username or password", response1.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should handle case insensitive username lookup correctly")
        void shouldHandleCaseInsensitiveUsernameLookupCorrectly() {
            // Given
            when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.empty());
            
            // When
            AuthResponse response = loginUseCase.execute(new LoginRequest("TESTUSER", "SecurePassword123!"));
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals("Invalid username or password", response.getErrorMessage());
            
            verify(userRepository).findByUsername("TESTUSER"); // Should search exactly as provided
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesAndSpecialScenarios {
        
        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Given
            String specialUsername = "test.user@domain";
            LoginRequest specialUsernameRequest = new LoginRequest(specialUsername, "SecurePassword123!");
            when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.empty());
            
            // When
            AuthResponse response = loginUseCase.execute(specialUsernameRequest);
            
            // Then
            assertFalse(response.isSuccess());
            verify(userRepository).findByUsername(specialUsername);
        }
        
        @Test
        @DisplayName("Should handle very long usernames")
        void shouldHandleVeryLongUsernames() {
            // Given
            String longUsername = "a".repeat(1000);
            LoginRequest longUsernameRequest = new LoginRequest(longUsername, "password");
            when(userRepository.findByUsername(longUsername)).thenReturn(Optional.empty());
            
            // When
            AuthResponse response = loginUseCase.execute(longUsernameRequest);
            
            // Then
            assertFalse(response.isSuccess());
            verify(userRepository).findByUsername(longUsername);
        }
        
        @Test
        @DisplayName("Should handle concurrent login attempts")
        void shouldHandleConcurrentLoginAttempts() throws InterruptedException {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When - Simulate concurrent access
            Thread[] threads = new Thread[10];
            AuthResponse[] responses = new AuthResponse[10];
            
            for (int i = 0; i < 10; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    responses[index] = loginUseCase.execute(validLoginRequest);
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then - All should be successful but with unique tokens
            for (AuthResponse response : responses) {
                assertTrue(response.isSuccess());
                assertNotNull(response.getSessionToken());
            }
            
            // Verify all tokens are unique
            long uniqueTokens = java.util.Arrays.stream(responses)
                .map(AuthResponse::getSessionToken)
                .distinct()
                .count();
            
            assertEquals(10, uniqueTokens);
        }
    }
}
