package com.syos.integration;

import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for registration and login flow
 */
public class RegistrationLoginIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationLoginIntegrationTest.class);
    
    private InMemoryUserRepository userRepository;
    private RegisterCustomerUseCase registerUseCase;
    private LoginUseCase loginUseCase;
    
    @BeforeEach
    void setUp() {
        // Create a clean repository without default users for testing
        userRepository = new InMemoryUserRepository() {
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
    void shouldRegisterAndLoginUserSuccessfully() {
        // Given
        String username = "testuser";
        String password = "password123";
        String name = "Test User";
        String email = "test@example.com";
        
        // When registering
        User registeredUser = registerUseCase.register(username, password, name, email);
        
        // Then registration should succeed
        assertNotNull(registeredUser);
        assertEquals(username.toLowerCase(), registeredUser.getUsername().getValue());
        assertEquals(name, registeredUser.getName().getValue());
        assertEquals(email, registeredUser.getEmail().getValue());
        assertNotNull(registeredUser.getId());
        
        // Verify user exists in repository
        assertTrue(userRepository.existsByUsername(username));
        assertTrue(userRepository.existsByEmail(email));
        
        // When logging in
        User loggedInUser = loginUseCase.login(username, password);
        
        // Then login should succeed
        assertNotNull(loggedInUser);
        assertEquals(registeredUser.getId(), loggedInUser.getId());
        assertEquals(registeredUser.getUsername().getValue(), loggedInUser.getUsername().getValue());
        
        System.out.println("✅ Registration and login flow test passed!");
        System.out.println("Registered user: " + registeredUser.getUsername().getValue() + 
                          " with ID: " + registeredUser.getId().getValue());
    }
    
    @Test
    void shouldFailLoginWithWrongPassword() {
        // Given
        String username = "testuser2";
        String password = "password123";
        String wrongPassword = "wrongpassword";
        String name = "Test User 2";
        String email = "test2@example.com";
        
        // Register user
        registerUseCase.register(username, password, name, email);
        
        // When/Then - login with wrong password should fail
        assertThrows(Exception.class, () -> {
            loginUseCase.login(username, wrongPassword);
        });
        
        System.out.println("✅ Wrong password test passed!");
    }
    
    @Test
    void shouldFailLoginWithNonExistentUser() {
        // When/Then - login with non-existent user should fail
        assertThrows(Exception.class, () -> {
            loginUseCase.login("nonexistent", "password123");
        });
        
        System.out.println("✅ Non-existent user test passed!");
    }
}