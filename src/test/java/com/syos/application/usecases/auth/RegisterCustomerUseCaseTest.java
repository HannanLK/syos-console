package com.syos.application.usecases.auth;

import com.syos.application.exceptions.RegistrationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.exceptions.InvalidEmailException;
import com.syos.domain.exceptions.InvalidUsernameException;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCustomerUseCase")
class RegisterCustomerUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private RegisterCustomerUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterCustomerUseCase(userRepository);
        
        // Default mock behavior for save method - returns user with ID
        // Using lenient() to avoid UnnecessaryStubbing exceptions for tests that don't reach save()
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Simulate repository assigning an ID if not present
            return userToSave.getId() == null ? 
                userToSave.withId(com.syos.domain.valueobjects.UserID.of(1L)) : 
                userToSave;
        });
    }

    @Test
    @DisplayName("Should register new customer successfully")
    void shouldRegisterNewCustomerSuccessfully() {
        // Given
        String username = "newuser";
        String password = "password123";
        String name = "New User";
        String email = "newuser@example.com";

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register(username, password, name, email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername().getValue()).isEqualTo("newuser");
        assertThat(result.getName().getValue()).isEqualTo("New User");
        assertThat(result.getEmail().getValue()).isEqualTo("newuser@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getSynexPoints().getValue()).isEqualByComparingTo("0");

        // Verify password is hashed (not stored in plain text)
        assertThat(result.getPassword().matches(password)).isTrue();
        assertThat(result.getPassword().getHash()).isNotEqualTo(password);

        // Verify repository interactions
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername().getValue()).isEqualTo("newuser");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Should throw exception when username is null")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register(null, "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Username cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when username is empty")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("   ", "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Username cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "password123", null, "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Name cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "password123", "   ", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Name cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "password123", "Name", null))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Email cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when email is empty")
    void shouldThrowExceptionWhenEmailIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "password123", "Name", "   "))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Email cannot be empty");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when password is too short")
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "short", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Password must be at least 8 characters long");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", null, "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Password must be at least 8 characters long");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("existinguser", "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Username already taken");

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("newuser", "password123", "Name", "existing@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Email already registered");

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle invalid username format")
    void shouldHandleInvalidUsernameFormat() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("invalid-username!", "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasCauseInstanceOf(InvalidUsernameException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should handle invalid email format")
    void shouldHandleInvalidEmailFormat() {
        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("username", "password123", "Name", "invalid-email"))
            .isInstanceOf(RegistrationException.class)
            .hasCauseInstanceOf(InvalidEmailException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should normalize username by trimming and lowercasing")
    void shouldNormalizeUsernameByTrimmingAndLowercasing() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register("  NewUser  ", "password123", "Name", "email@example.com");

        // Then
        assertThat(result.getUsername().getValue()).isEqualTo("newuser");

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should trim whitespace from name and email")
    void shouldTrimWhitespaceFromNameAndEmail() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register("newuser", "password123", "  John Doe  ", "  email@example.com  ");

        // Then
        assertThat(result.getName().getValue()).isEqualTo("John Doe");
        assertThat(result.getEmail().getValue()).isEqualTo("email@example.com");

        verify(userRepository).existsByEmail("email@example.com");
    }

    @Test
    @DisplayName("Should handle repository exception during save")
    void shouldHandleRepositoryExceptionDuringSave() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);
        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("newuser", "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Registration failed: Database error")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle repository exception during username check")
    void shouldHandleRepositoryExceptionDuringUsernameCheck() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThatThrownBy(() -> registerUseCase.register("newuser", "password123", "Name", "email@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Registration failed: Database connection error")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject null repository in constructor")
    void shouldRejectNullRepositoryInConstructor() {
        // When & Then
        assertThatThrownBy(() -> new RegisterCustomerUseCase(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should create user with correct timestamps")
    void shouldCreateUserWithCorrectTimestamps() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register("newuser", "password123", "Name", "email@example.com");

        // Then
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getMemberSince()).isNotNull();
        
        // Member since should be same as created at for new customers
        assertThat(result.getMemberSince().getValue()).isEqualTo(result.getCreatedAt().getValue());
    }

    @Test
    @DisplayName("Should accept minimum valid username length")
    void shouldAcceptMinimumValidUsernameLength() {
        // Given
        when(userRepository.existsByUsername("abc")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register("abc", "password123", "Name", "email@example.com");

        // Then
        assertThat(result.getUsername().getValue()).isEqualTo("abc");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should accept maximum valid username length")
    void shouldAcceptMaximumValidUsernameLength() {
        // Given
        String longUsername = "a".repeat(20); // 20 characters
        when(userRepository.existsByUsername(longUsername)).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);

        // When
        User result = registerUseCase.register(longUsername, "password123", "Name", "email@example.com");

        // Then
        assertThat(result.getUsername().getValue()).isEqualTo(longUsername);
        verify(userRepository).save(any(User.class));
    }
}
