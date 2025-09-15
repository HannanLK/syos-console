package com.syos.adapter.out.persistence;

import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryUserRepository")
class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;
    private User testUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        
        testUser = User.createWithRole(
            Username.of("testuser"),
            "password123",
            Name.of("Test User"),
            Email.of("test@example.com"),
            UserRole.CUSTOMER,
            null
        );
    }

    @Test
    @DisplayName("Should save and find user by username")
    void shouldSaveAndFindUserByUsername() {
        // When
        User savedUser = repository.save(testUser);
        Optional<User> found = repository.findByUsername("testuser");

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getUsername().getValue()).isEqualTo("testuser");
        assertThat(found.get().getName().getValue()).isEqualTo("Test User");
        assertThat(found.get().getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(found.get().getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Should return empty optional when user not found")
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        // When
        Optional<User> found = repository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // Given
        repository.save(testUser);

        // When & Then
        assertThat(repository.existsByUsername("testuser")).isTrue();
        assertThat(repository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        repository.save(testUser);

        // When & Then
        assertThat(repository.existsByEmail("test@example.com")).isTrue();
        assertThat(repository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should be case insensitive for username lookups")
    void shouldBeCaseInsensitiveForUsernameLookups() {
        // Given
        repository.save(testUser);

        // When & Then
        assertThat(repository.existsByUsername("TESTUSER")).isTrue();
        assertThat(repository.existsByUsername("TestUser")).isTrue();
        assertThat(repository.findByUsername("TESTUSER")).isPresent();
    }

    @Test
    @DisplayName("Should be case insensitive for email lookups")
    void shouldBeCaseInsensitiveForEmailLookups() {
        // Given
        repository.save(testUser);

        // When & Then
        assertThat(repository.existsByEmail("TEST@EXAMPLE.COM")).isTrue();
        assertThat(repository.existsByEmail("Test@Example.Com")).isTrue();
    }

    @Test
    @DisplayName("Should assign ID when saving user without ID")
    void shouldAssignIdWhenSavingUserWithoutId() {
        // Given
        User userWithoutId = User.createWithRole(
            Username.of("newuser"),
            "password123",
            Name.of("New User"),
            Email.of("new@example.com"),
            UserRole.CUSTOMER,
            null
        );

        // When
        User savedUser = repository.save(userWithoutId);
        Optional<User> found = repository.findByUsername("newuser");

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId().getValue()).isGreaterThan(0L);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("Should update existing user when saving with existing ID")
    void shouldUpdateExistingUserWhenSavingWithExistingId() {
        // Given
        repository.save(testUser);
        Optional<User> saved = repository.findByUsername("testuser");
        assertThat(saved).isPresent();

        User updatedUser = User.withId(
            saved.get().getId(),
            Username.of("testuser"),
            saved.get().getPassword(),
            UserRole.EMPLOYEE, // Changed role
            Name.of("Updated Name"), // Changed name
            saved.get().getEmail(),
            saved.get().getSynexPoints(),
            saved.get().getActiveStatus(),
            saved.get().getCreatedAt(),
            UpdatedAt.of(LocalDateTime.now()),
            saved.get().getCreatedBy(),
            saved.get().getMemberSince()
        );

        // When
        repository.save(updatedUser);
        Optional<User> found = repository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.get().getId());
        assertThat(found.get().getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(found.get().getName().getValue()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should handle multiple users")
    void shouldHandleMultipleUsers() {
        // Given
        User user1 = User.createWithRole(
            Username.of("user1"),
            "password123",
            Name.of("User One"),
            Email.of("user1@example.com"),
            UserRole.CUSTOMER,
            null
        );

        User user2 = User.createWithRole(
            Username.of("user2"),
            "password123",
            Name.of("User Two"),
            Email.of("user2@example.com"),
            UserRole.EMPLOYEE,
            null
        );

        User user3 = User.createWithRole(
            Username.of("admin"),
            "admin123",
            Name.of("Admin User"),
            Email.of("admin@example.com"),
            UserRole.ADMIN,
            null
        );

        // When
        repository.save(user1);
        repository.save(user2);
        repository.save(user3);

        // Then
        assertThat(repository.existsByUsername("user1")).isTrue();
        assertThat(repository.existsByUsername("user2")).isTrue();
        assertThat(repository.existsByUsername("admin")).isTrue();

        assertThat(repository.existsByEmail("user1@example.com")).isTrue();
        assertThat(repository.existsByEmail("user2@example.com")).isTrue();
        assertThat(repository.existsByEmail("admin@example.com")).isTrue();

        Optional<User> foundUser1 = repository.findByUsername("user1");
        Optional<User> foundUser2 = repository.findByUsername("user2");
        Optional<User> foundAdmin = repository.findByUsername("admin");

        assertThat(foundUser1).isPresent();
        assertThat(foundUser2).isPresent();
        assertThat(foundAdmin).isPresent();

        assertThat(foundUser1.get().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(foundUser2.get().getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(foundAdmin.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should not allow duplicate usernames")
    void shouldNotAllowDuplicateUsernames() {
        // Given
        repository.save(testUser);

        User duplicateUser = User.createWithRole(
            Username.of("testuser"),
            "differentpassword",
            Name.of("Different User"),
            Email.of("different@example.com"),
            UserRole.EMPLOYEE,
            null
        );

        // When
        repository.save(duplicateUser);
        Optional<User> found = repository.findByUsername("testuser");

        // Then - should update existing user, not create duplicate
        assertThat(found).isPresent();
        assertThat(found.get().getName().getValue()).isEqualTo("Different User");
        assertThat(found.get().getRole()).isEqualTo(UserRole.EMPLOYEE);
    }

    @Test
    @DisplayName("Should assign unique sequential IDs")
    void shouldAssignUniqueSequentialIds() {
        // Given
        User user1 = User.createWithRole(
            Username.of("user1"),
            "password123",
            Name.of("User One"),
            Email.of("user1@example.com"),
            UserRole.CUSTOMER,
            null
        );

        User user2 = User.createWithRole(
            Username.of("user2"),
            "password123",
            Name.of("User Two"),
            Email.of("user2@example.com"),
            UserRole.CUSTOMER,
            null
        );

        // When
        repository.save(user1);
        repository.save(user2);

        Optional<User> found1 = repository.findByUsername("user1");
        Optional<User> found2 = repository.findByUsername("user2");

        // Then
        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getId().getValue()).isLessThan(found2.get().getId().getValue());
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInputGracefully() {
        // When & Then
        assertThatThrownBy(() -> repository.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User cannot be null");

        assertThat(repository.findByUsername(null)).isEmpty();
        assertThat(repository.existsByUsername(null)).isFalse();
        assertThat(repository.existsByEmail(null)).isFalse();
    }

    @Test
    @DisplayName("Should handle empty string input gracefully")
    void shouldHandleEmptyStringInputGracefully() {
        // When & Then
        assertThat(repository.findByUsername("")).isEmpty();
        assertThat(repository.existsByUsername("")).isFalse();
        assertThat(repository.existsByEmail("")).isFalse();
    }
}
