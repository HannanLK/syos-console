package com.syos.test;

import com.syos.domain.valueobjects.Username;
import com.syos.domain.valueobjects.Password;
import com.syos.domain.valueobjects.Email;
import com.syos.domain.valueobjects.Name;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple compilation test to verify all components work together
 */
@DisplayName("Compilation Test")
public class CompilationTest {

    @Test
    @DisplayName("Should compile and create basic value objects")
    void shouldCompileAndCreateBasicValueObjects() {
        // Test value objects
        Username username = Username.of("testuser");
        Password password = Password.hash("password123");
        Email email = Email.of("test@example.com");
        Name name = Name.of("Test User");
        
        assertThat(username.getValue()).isEqualTo("testuser");
        assertThat(password.matches("password123")).isTrue();
        assertThat(email.getValue()).isEqualTo("test@example.com");
        assertThat(name.getValue()).isEqualTo("Test User");
        
        // Test enum
        UserRole role = UserRole.CUSTOMER;
        assertThat(role).isEqualTo(UserRole.CUSTOMER);
    }
}
