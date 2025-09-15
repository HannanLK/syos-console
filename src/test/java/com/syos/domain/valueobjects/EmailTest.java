package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("Should create valid email")
    void shouldCreateValidEmail() {
        // When
        Email email = Email.of("test@example.com");

        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should create email with various valid formats")
    void shouldCreateEmailWithVariousValidFormats() {
        // Valid email formats
        assertThatNoException().isThrownBy(() -> Email.of("user@domain.com"));
        assertThatNoException().isThrownBy(() -> Email.of("user.name@domain.co.uk"));
        assertThatNoException().isThrownBy(() -> Email.of("user+tag@domain.org"));
        assertThatNoException().isThrownBy(() -> Email.of("user_name@domain-name.com"));
        assertThatNoException().isThrownBy(() -> Email.of("123@domain.com"));
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        // When & Then
        assertThatThrownBy(() -> Email.of(null))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessage("Email cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for empty email")
    void shouldThrowExceptionForEmptyEmail() {
        // When & Then
        assertThatThrownBy(() -> Email.of(""))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    @DisplayName("Should throw exception for blank email")
    void shouldThrowExceptionForBlankEmail() {
        // When & Then
        assertThatThrownBy(() -> Email.of("   "))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    @DisplayName("Should throw exception for email too long")
    void shouldThrowExceptionForEmailTooLong() {
        // Given
        String longEmail = "a".repeat(90) + "@domain.com"; // Over 100 characters

        // When & Then
        assertThatThrownBy(() -> Email.of(longEmail))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessage("Email must be at most 100 characters long");
    }

    @Test
    @DisplayName("Should throw exception for invalid email formats")
    void shouldThrowExceptionForInvalidEmailFormats() {
        // Invalid email formats
        assertThatThrownBy(() -> Email.of("invalid")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("@domain.com")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("user@")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("user@domain")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("user.domain.com")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("user@@domain.com")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> Email.of("user@domain.")).isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("Should trim whitespace")
    void shouldTrimWhitespace() {
        // When
        Email email = Email.of("  test@example.com  ");

        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("test@example.com");
        Email email3 = Email.of("TEST@EXAMPLE.COM");
        Email email4 = Email.of("other@example.com");

        // Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1).isEqualTo(email3); // Case insensitive
        assertThat(email1).isNotEqualTo(email4);
        assertThat(email1).isNotEqualTo(null);
        assertThat(email1).isNotEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@EXAMPLE.COM");

        // Then
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode()); // Case insensitive
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        Email email = Email.of("test@example.com");

        // Then
        assertThat(email.toString()).isEqualTo("test@example.com");
    }
}
