package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidUsernameException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Username value object
 */
class UsernameTest {
    
    @Test
    void shouldCreateValidUsername() {
        Username username = Username.of("john_doe");
        assertThat(username.getValue()).isEqualTo("john_doe");
    }
    
    @Test
    void shouldConvertToLowercase() {
        Username username = Username.of("JohnDoe");
        assertThat(username.getValue()).isEqualTo("johndoe");
    }
    
    @Test
    void shouldRejectEmptyUsername() {
        assertThatThrownBy(() -> Username.of(""))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("cannot be empty");
    }
    
    @Test
    void shouldRejectNullUsername() {
        assertThatThrownBy(() -> Username.of(null))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("cannot be empty");
    }
    
    @Test
    void shouldRejectInvalidCharacters() {
        assertThatThrownBy(() -> Username.of("john@doe"))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("letters, numbers, and underscores");
    }
    
    @Test
    void shouldRejectTooShortUsername() {
        assertThatThrownBy(() -> Username.of("ab"))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("3-20 characters");
    }
    
    @Test
    void shouldRejectTooLongUsername() {
        String longUsername = "a".repeat(21);
        assertThatThrownBy(() -> Username.of(longUsername))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("3-20 characters");
    }
    
    @Test
    void shouldAcceptValidUsernames() {
        assertThatCode(() -> Username.of("user123")).doesNotThrowAnyException();
        assertThatCode(() -> Username.of("test_user")).doesNotThrowAnyException();
        assertThatCode(() -> Username.of("ABC")).doesNotThrowAnyException();
        assertThatCode(() -> Username.of("user_123_test")).doesNotThrowAnyException();
    }
}