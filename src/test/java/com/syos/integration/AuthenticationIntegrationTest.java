package com.syos.integration;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.exceptions.RegistrationException;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationIntegrationTest.class);

    private UserRepository userRepository;
    private RegisterCustomerUseCase registerUseCase;
    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        // Create a clean repository without default users for testing
        userRepository = new InMemoryUserRepository() {
            // Override to skip default user initialization for clean tests
            @Override
            protected void initializeDefaultUsers() {
                // Skip default user initialization for integration tests
                logger.info("Skipping default user initialization for integration tests");
            }
        };
        registerUseCase = new RegisterCustomerUseCase(userRepository);
        loginUseCase = new LoginUseCase(userRepository);
    }

    @Test
    @DisplayName("Should complete full registration and login flow")
    void shouldCompleteFullRegistrationAndLoginFlow() {
        // Registration
        String username = "testuser";
        String password = "password123";
        String name = "Test User";
        String email = "test@example.com";

        // When - Register
        User registeredUser = registerUseCase.register(username, password, name, email);

        // Then - Registration should succeed
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername().getValue()).isEqualTo("testuser");
        assertThat(registeredUser.getName().getValue()).isEqualTo("Test User");
        assertThat(registeredUser.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(registeredUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(registeredUser.isActive()).isTrue();
        assertThat(registeredUser.getId()).isNotNull();

        // When - Login with correct credentials
        User loggedInUser = loginUseCase.login(username, password);

        // Then - Login should succeed
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getUsername().getValue()).isEqualTo("testuser");
        assertThat(loggedInUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(loggedInUser.getId()).isEqualTo(registeredUser.getId());
    }

    @Test
    @DisplayName("Should fail login with incorrect password after registration")
    void shouldFailLoginWithIncorrectPasswordAfterRegistration() {
        // Given - Register user
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When & Then - Login with wrong password should fail
        assertThatThrownBy(() -> loginUseCase.login("testuser", "wrongpassword"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid username or password");
    }

    @Test
    @DisplayName("Should fail login with non-existent username")
    void shouldFailLoginWithNonExistentUsername() {
        // When & Then - Login with non-existent user should fail
        assertThatThrownBy(() -> loginUseCase.login("nonexistent", "password123"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("Invalid username or password");
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void shouldPreventDuplicateUsernameRegistration() {
        // Given - Register first user
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When & Then - Attempt to register with same username should fail
        assertThatThrownBy(() -> registerUseCase.register("testuser", "different123", "Different User", "different@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Username already taken");
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void shouldPreventDuplicateEmailRegistration() {
        // Given - Register first user
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When & Then - Attempt to register with same email should fail
        assertThatThrownBy(() -> registerUseCase.register("differentuser", "password123", "Different User", "test@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Email already registered");
    }

    @Test
    @DisplayName("Should handle case insensitive username operations")
    void shouldHandleCaseInsensitiveUsernameOperations() {
        // Given - Register user with lowercase username
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When & Then - Login with different case should work
        User loggedInUser = loginUseCase.login("TESTUSER", "password123");
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getUsername().getValue()).isEqualTo("testuser"); // Stored as lowercase

        // Registration with different case should fail (duplicate)
        assertThatThrownBy(() -> registerUseCase.register("TESTUSER", "password123", "Another User", "another@example.com"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Username already taken");
    }

    @Test
    @DisplayName("Should handle case insensitive email operations")
    void shouldHandleCaseInsensitiveEmailOperations() {
        // Given - Register user with lowercase email
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When & Then - Registration with different case email should fail (duplicate)
        assertThatThrownBy(() -> registerUseCase.register("anotheruser", "password123", "Another User", "TEST@EXAMPLE.COM"))
            .isInstanceOf(RegistrationException.class)
            .hasMessage("Email already registered");
    }

    @Test
    @DisplayName("Should handle multiple user registrations and logins")
    void shouldHandleMultipleUserRegistrationsAndLogins() {
        // Given - Register multiple users
        User customer = registerUseCase.register("customer", "password123", "Customer User", "customer@example.com");
        User employee = registerUseCase.register("employee", "password456", "Employee User", "employee@example.com");
        User admin = registerUseCase.register("admin", "password789", "Admin User", "admin@example.com");

        // Then - All should be registered successfully
        assertThat(customer.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(employee.getRole()).isEqualTo(UserRole.CUSTOMER); // RegisterCustomerUseCase always creates customers
        assertThat(admin.getRole()).isEqualTo(UserRole.CUSTOMER);

        // When - Login with each user
        User loggedCustomer = loginUseCase.login("customer", "password123");
        User loggedEmployee = loginUseCase.login("employee", "password456");
        User loggedAdmin = loginUseCase.login("admin", "password789");

        // Then - All should login successfully
        assertThat(loggedCustomer.getUsername().getValue()).isEqualTo("customer");
        assertThat(loggedEmployee.getUsername().getValue()).isEqualTo("employee");
        assertThat(loggedAdmin.getUsername().getValue()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should handle whitespace trimming in registration and login")
    void shouldHandleWhitespaceTrimmingInRegistrationAndLogin() {
        // Given - Register with whitespace
        User registeredUser = registerUseCase.register("  testuser  ", "password123", "  Test User  ", "  test@example.com  ");

        // Then - Should trim whitespace
        assertThat(registeredUser.getUsername().getValue()).isEqualTo("testuser");
        assertThat(registeredUser.getName().getValue()).isEqualTo("Test User");
        assertThat(registeredUser.getEmail().getValue()).isEqualTo("test@example.com");

        // When - Login with whitespace
        User loggedInUser = loginUseCase.login("  testuser  ", "password123");

        // Then - Should work correctly
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getUsername().getValue()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should maintain user data consistency between registration and login")
    void shouldMaintainUserDataConsistencyBetweenRegistrationAndLogin() {
        // Given - Register user
        User registeredUser = registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When - Login
        User loggedInUser = loginUseCase.login("testuser", "password123");

        // Then - All data should be consistent  
        assertThat(loggedInUser.getId()).isEqualTo(registeredUser.getId());
        assertThat(loggedInUser.getUsername()).isEqualTo(registeredUser.getUsername());
        assertThat(loggedInUser.getName()).isEqualTo(registeredUser.getName());
        assertThat(loggedInUser.getEmail()).isEqualTo(registeredUser.getEmail());
        assertThat(loggedInUser.getRole()).isEqualTo(registeredUser.getRole());
        assertThat(loggedInUser.getSynexPoints()).isEqualTo(registeredUser.getSynexPoints());
        assertThat(loggedInUser.isActive()).isEqualTo(registeredUser.isActive());
        assertThat(loggedInUser.getCreatedAt()).isEqualTo(registeredUser.getCreatedAt());
        assertThat(loggedInUser.getMemberSince()).isEqualTo(registeredUser.getMemberSince());
        // Note: createdBy can be null for self-registered users
    }

    @Test
    @DisplayName("Should handle repository persistence correctly")
    void shouldHandleRepositoryPersistenceCorrectly() {
        // Given - Register user
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");

        // When - Check repository state
        boolean usernameExists = userRepository.existsByUsername("testuser");
        boolean emailExists = userRepository.existsByEmail("test@example.com");
        var foundUser = userRepository.findByUsername("testuser");

        // Then - Repository should reflect the changes
        assertThat(usernameExists).isTrue();
        assertThat(emailExists).isTrue();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername().getValue()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate business rules during integration")
    void shouldValidateBusinessRulesDuringIntegration() {
        // Registration validation
        assertThatThrownBy(() -> registerUseCase.register("", "password123", "Test User", "test@example.com"))
            .isInstanceOf(RegistrationException.class);
        
        assertThatThrownBy(() -> registerUseCase.register("testuser", "short", "Test User", "test@example.com"))
            .isInstanceOf(RegistrationException.class);
        
        assertThatThrownBy(() -> registerUseCase.register("testuser", "password123", "", "test@example.com"))
            .isInstanceOf(RegistrationException.class);
        
        assertThatThrownBy(() -> registerUseCase.register("testuser", "password123", "Test User", "invalid-email"))
            .isInstanceOf(RegistrationException.class);

        // Login validation
        assertThatThrownBy(() -> loginUseCase.login("", "password123"))
            .isInstanceOf(AuthenticationException.class);
        
        assertThatThrownBy(() -> loginUseCase.login("testuser", ""))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Should handle password security correctly")
    void shouldHandlePasswordSecurityCorrectly() {
        // Given - Register user
        String rawPassword = "password123";
        User registeredUser = registerUseCase.register("testuser", rawPassword, "Test User", "test@example.com");

        // Then - Password should be hashed, not stored in plain text
        assertThat(registeredUser.getPassword().getHash()).isNotEqualTo(rawPassword);
        assertThat(registeredUser.getPassword().getHash()).startsWith("$2");
        
        // But should still match the original password
        assertThat(registeredUser.getPassword().matches(rawPassword)).isTrue();
        
        // Login should work with original password
        User loggedInUser = loginUseCase.login("testuser", rawPassword);
        assertThat(loggedInUser).isNotNull();
        
        // But not with wrong password
        assertThatThrownBy(() -> loginUseCase.login("testuser", "wrongpassword"))
            .isInstanceOf(AuthenticationException.class);
    }
}
