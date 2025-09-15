package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password Value Object")
class PasswordTest {

    @Test
    @DisplayName("Should hash password correctly")
    void shouldHashPasswordCorrectly() {
        // Given
        String rawPassword = "password123";

        // When
        Password password = Password.hash(rawPassword);

        // Then
        assertThat(password.getHash()).isNotEqualTo(rawPassword);
        assertThat(password.getHash()).startsWith("$2");
        assertThat(password.matches(rawPassword)).isTrue();
    }

    @Test
    @DisplayName("Should create password from existing hash")
    void shouldCreatePasswordFromExistingHash() {
        // Given
        String existingHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS";

        // When
        Password password = Password.fromHash(existingHash);

        // Then
        assertThat(password.getHash()).isEqualTo(existingHash);
    }

    @Test
    @DisplayName("Should validate password matches correctly")
    void shouldValidatePasswordMatchesCorrectly() {
        // Given
        String rawPassword = "password123";
        Password password = Password.hash(rawPassword);

        // Then
        assertThat(password.matches("password123")).isTrue();
        assertThat(password.matches("wrongpassword")).isFalse();
        assertThat(password.matches("PASSWORD123")).isFalse(); // Case sensitive
    }

    @Test
    @DisplayName("Should throw exception for null raw password")
    void shouldThrowExceptionForNullRawPassword() {
        // When & Then
        assertThatThrownBy(() -> Password.hash(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for too short password")
    void shouldThrowExceptionForTooShortPassword() {
        // When & Then
        assertThatThrownBy(() -> Password.hash("short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should throw exception for too long password")
    void shouldThrowExceptionForTooLongPassword() {
        // Given
        String tooLongPassword = "a".repeat(256);

        // When & Then
        assertThatThrownBy(() -> Password.hash(tooLongPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password is too long");
    }

    @Test
    @DisplayName("Should throw exception for null hash")
    void shouldThrowExceptionForNullHash() {
        // When & Then
        assertThatThrownBy(() -> Password.fromHash(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password hash cannot be null/blank");
    }

    @Test
    @DisplayName("Should throw exception for blank hash")
    void shouldThrowExceptionForBlankHash() {
        // When & Then
        assertThatThrownBy(() -> Password.fromHash("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password hash cannot be null/blank");
    }

    @Test
    @DisplayName("Should throw exception for invalid hash format")
    void shouldThrowExceptionForInvalidHashFormat() {
        // When & Then
        assertThatThrownBy(() -> Password.fromHash("invalid-hash-format"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid BCrypt hash format");
    }

    @Test
    @DisplayName("Should accept valid BCrypt hash formats")
    void shouldAcceptValidBCryptHashFormats() {
        // Valid BCrypt formats
        assertThatNoException().isThrownBy(() -> Password.fromHash("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS"));
        assertThatNoException().isThrownBy(() -> Password.fromHash("$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS"));
        assertThatNoException().isThrownBy(() -> Password.fromHash("$2y$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS"));
    }

    @Test
    @DisplayName("Should accept minimum length password")
    void shouldAcceptMinimumLengthPassword() {
        // Given
        String minPassword = "12345678"; // 8 characters

        // When & Then
        assertThatNoException().isThrownBy(() -> Password.hash(minPassword));
    }

    @Test
    @DisplayName("Should accept maximum length password")
    void shouldAcceptMaximumLengthPassword() {
        // Given
        String maxPassword = "a".repeat(255); // 255 characters

        // When & Then
        assertThatNoException().isThrownBy(() -> Password.hash(maxPassword));
    }

    @Test
    @DisplayName("Should throw exception when matching with null password")
    void shouldThrowExceptionWhenMatchingWithNullPassword() {
        // Given
        Password password = Password.hash("password123");

        // When & Then
        assertThatThrownBy(() -> password.matches(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when matching with too short password")
    void shouldThrowExceptionWhenMatchingWithTooShortPassword() {
        // Given
        Password password = Password.hash("password123");

        // When & Then
        assertThatThrownBy(() -> password.matches("short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        String hash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS";
        Password password1 = Password.fromHash(hash);
        Password password2 = Password.fromHash(hash);
        Password password3 = Password.hash("different123");

        // Then
        assertThat(password1).isEqualTo(password2);
        assertThat(password1).isNotEqualTo(password3);
        assertThat(password1).isNotEqualTo(null);
        assertThat(password1).isNotEqualTo("password");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        String hash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj7.4kh9CdIS";
        Password password1 = Password.fromHash(hash);
        Password password2 = Password.fromHash(hash);

        // Then
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("Should not expose password in toString")
    void shouldNotExposePasswordInToString() {
        // Given
        Password password = Password.hash("password123");

        // Then
        assertThat(password.toString()).isEqualTo("[PROTECTED]");
        assertThat(password.toString()).doesNotContain("password123");
        assertThat(password.toString()).doesNotContain(password.getHash());
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String rawPassword = "password123";

        // When
        Password password1 = Password.hash(rawPassword);
        Password password2 = Password.hash(rawPassword);

        // Then
        assertThat(password1.getHash()).isNotEqualTo(password2.getHash());
        assertThat(password1.matches(rawPassword)).isTrue();
        assertThat(password2.matches(rawPassword)).isTrue();
    }
}
