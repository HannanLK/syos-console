package com.syos.application.usecases.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for LoginUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Tests")
class LoginUseCaseEnhancedTest {
    
    @Mock
    private UserRepository userRepository;
    
    private LoginUseCase loginUseCase;
    private User activeCustomer;
    private User inactiveUser;
    
    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository);
        
        // Create test users using factory methods
        activeCustomer = User.createCustomer(
            Username.of("testuser"),
            Email.of("customer@example.com"),
            com.syos.domain.valueobjects.Password.hash("SecurePassword123!")
        );
        
        inactiveUser = User.createCustomer(
            Username.of("inactive"),
            Email.of("inactive@example.com"),
            com.syos.domain.valueobjects.Password.hash("SecurePassword123!")
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
            assertThrows(NullPointerException.class, () -> {
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
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            User result = loginUseCase.login("testuser", "SecurePassword123!");
            
            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername().getValue());
            assertEquals(UserRole.CUSTOMER, result.getRole());
            
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).findByUsername("testuser");
        }
    }
    
    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {
        
        @Test
        @DisplayName("Should fail authentication when user does not exist")
        void shouldFailAuthenticationWhenUserDoesNotExist() {
            // Given
            when(userRepository.existsByUsername("nonexistent")).thenReturn(false);
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("nonexistent", "password");
            });
            
            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository).existsByUsername("nonexistent");
            verify(userRepository, never()).findByUsername(anyString());
        }
        
        @Test
        @DisplayName("Should fail authentication when password is incorrect")
        void shouldFailAuthenticationWhenPasswordIsIncorrect() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("testuser", "WrongPassword123!");
            });
            
            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).findByUsername("testuser");
        }
        
        @Test
        @DisplayName("Should fail authentication when user is inactive")
        void shouldFailAuthenticationWhenUserIsInactive() {
            // Given
            when(userRepository.existsByUsername("inactive")).thenReturn(true);
            when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(inactiveUser));
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("inactive", "SecurePassword123!");
            });
            
            assertEquals("Account is inactive. Please contact support.", exception.getMessage());
            verify(userRepository).existsByUsername("inactive");
            verify(userRepository).findByUsername("inactive");
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login(null, "password");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when username is empty")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("", "password");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("username", null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is empty")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("username", "");
            });
        }
    }
    
    @Nested
    @DisplayName("Repository Interaction Tests")
    class RepositoryInteractionTests {
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("testuser", "password");
            });
            
            assertEquals("Login failed due to system error", exception.getMessage());
            verify(userRepository).existsByUsername("testuser");
        }
        
        @Test
        @DisplayName("Should handle missing user gracefully")
        void shouldHandleMissingUserGracefully() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login("testuser", "password");
            });
            
            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).findByUsername("testuser");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle username with whitespace")
        void shouldHandleUsernameWithWhitespace() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeCustomer));
            
            // When
            User result = loginUseCase.login("  testuser  ", "SecurePassword123!");
            
            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername().getValue());
            
            verify(userRepository).existsByUsername("testuser"); // Should be trimmed
        }
        
        @Test
        @DisplayName("Should handle special characters in username")
        void shouldHandleSpecialCharactersInUsername() {
            // Given
            String specialUsername = "test.user@domain";
            when(userRepository.existsByUsername(specialUsername)).thenReturn(false);
            
            // When & Then
            AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
                loginUseCase.login(specialUsername, "password");
            });
            
            assertEquals("Invalid username or password", exception.getMessage());
            verify(userRepository).existsByUsername(specialUsername);
        }
    }
}
