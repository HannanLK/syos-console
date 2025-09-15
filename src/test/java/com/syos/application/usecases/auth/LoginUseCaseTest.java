package com.syos.application.usecases.auth;

import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private LoginUseCase loginUseCase;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository);
        
        // Create a test user with hashed password
        testUser = User.withId(
            UserID.of(1L),
            Username.of("testuser"),
            Password.hash("password123"), // This will be hashed
            UserRole.CUSTOMER,
            Name.of("Test User"),
            Email.of("test@example.com"),
            SynexPoints.zero(),
            ActiveStatus.active(),
            CreatedAt.of(LocalDateTime.now()),
            UpdatedAt.of(LocalDateTime.now()),
            null,
            MemberSince.of(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void shouldAuthenticateUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = loginUseCase.login("testuser", "password123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername().getValue()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo(UserRole.CUSTOMER);
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when username is null")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // When & Then
        assertThatThrownBy(() -> loginUseCase.login(null, "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Username cannot be empty");
        
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when username is empty")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("   ", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Username cannot be empty");
        
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", null))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Password cannot be empty");
        
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when password is empty")
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", ""))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Password cannot be empty");
        
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when username does not exist")
    void shouldThrowExceptionWhenUsernameDoesNotExist() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("nonexistent", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid username or password");
        
        verify(userRepository).existsByUsername("nonexistent");
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should throw exception when user exists but findByUsername returns empty")
    void shouldThrowExceptionWhenUserNotFoundAfterExistsCheck() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid username or password");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", "wrongpassword"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid username or password");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user account is inactive")
    void shouldThrowExceptionWhenUserAccountIsInactive() {
        // Given
        User inactiveUser = User.withId(
            UserID.of(1L),
            Username.of("testuser"),
            Password.hash("password123"),
            UserRole.CUSTOMER,
            Name.of("Test User"),
            Email.of("test@example.com"),
            SynexPoints.zero(),
            ActiveStatus.inactive(), // Inactive user
            CreatedAt.of(LocalDateTime.now()),
            UpdatedAt.of(LocalDateTime.now()),
            null,
            MemberSince.of(LocalDateTime.now())
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Account is inactive. Please contact support.");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should normalize username by trimming whitespace")
    void shouldNormalizeUsernameByTrimmingWhitespace() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = loginUseCase.login("  testuser  ", "password123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername().getValue()).isEqualTo("testuser");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> loginUseCase.login("testuser", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Login failed due to system error")
            .hasCauseInstanceOf(RuntimeException.class);
        
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("Should reject null repository in constructor")
    void shouldRejectNullRepositoryInConstructor() {
        // When & Then
        assertThatThrownBy(() -> new LoginUseCase(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should authenticate admin user successfully")
    void shouldAuthenticateAdminUserSuccessfully() {
        // Given
        User adminUser = User.withId(
            UserID.of(2L),
            Username.of("admin"),
            Password.hash("admin123"),
            UserRole.ADMIN,
            Name.of("Admin User"),
            Email.of("admin@syos.com"),
            SynexPoints.zero(),
            ActiveStatus.active(),
            CreatedAt.of(LocalDateTime.now()),
            UpdatedAt.of(LocalDateTime.now()),
            null,
            MemberSince.of(LocalDateTime.now())
        );

        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        User result = loginUseCase.login("admin", "admin123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername().getValue()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should authenticate employee user successfully")
    void shouldAuthenticateEmployeeUserSuccessfully() {
        // Given
        User employeeUser = User.withId(
            UserID.of(3L),
            Username.of("employee"),
            Password.hash("employee123"),
            UserRole.EMPLOYEE,
            Name.of("Employee User"),
            Email.of("employee@syos.com"),
            SynexPoints.zero(),
            ActiveStatus.active(),
            CreatedAt.of(LocalDateTime.now()),
            UpdatedAt.of(LocalDateTime.now()),
            null,
            MemberSince.of(LocalDateTime.now())
        );

        when(userRepository.existsByUsername("employee")).thenReturn(true);
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));

        // When
        User result = loginUseCase.login("employee", "employee123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername().getValue()).isEqualTo("employee");
        assertThat(result.getRole()).isEqualTo(UserRole.EMPLOYEE);
    }
}
