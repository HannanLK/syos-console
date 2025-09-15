package com.syos.integration;

import com.syos.adapter.in.cli.commands.LoginCommand;
import com.syos.adapter.in.cli.commands.RegisterCommand;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Integration test for the enhanced user registration and login flow
 */
public class EnhancedUserFlowIntegrationTest {

    @Mock
    private ConsoleIO mockConsole;
    
    @Mock
    private MenuNavigator mockNavigator;
    
    private UserRepository userRepository;
    private LoginUseCase loginUseCase;
    private RegisterCustomerUseCase registerUseCase;
    private MenuFactory menuFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Use real repository for integration test
        userRepository = new InMemoryUserRepository();
        
        // Initialize use cases
        loginUseCase = new LoginUseCase(userRepository);
        registerUseCase = new RegisterCustomerUseCase(userRepository);
        
        // Initialize menu factory
        menuFactory = new MenuFactory(mockConsole, mockNavigator, loginUseCase, registerUseCase, userRepository);
        
        // Clear any existing sessions
        SessionManager.getInstance().clearSession();
    }

    @Test
    void testEnhancedRegistrationFlow() {
        // Arrange
        when(mockConsole.readLine())
            .thenReturn("John Doe")        // Full Name
            .thenReturn("johndoe")         // Username  
            .thenReturn("john@example.com") // Email
            .thenReturn("");               // Press Enter to continue

        when(mockConsole.readPassword())
            .thenReturn("password123")     // Password
            .thenReturn("password123");    // Confirm Password

        // Create the registration command
        RegisterCommand registerCommand = new RegisterCommand(
            mockConsole, registerUseCase, mockNavigator, menuFactory
        );

        // Act
        registerCommand.execute();

        // Assert
        // Verify that user was successfully registered
        assertTrue(userRepository.findByUsername("johndoe").isPresent());
        
        // Verify that session was automatically created
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals("johndoe", SessionManager.getInstance().getCurrentSession().getUsername());
        assertEquals(UserRole.CUSTOMER, SessionManager.getInstance().getCurrentSession().getRole());
        
        // Verify console interactions
        verify(mockConsole).printSuccess("Registration successful!");
        verify(mockConsole, atLeastOnce()).println(contains("USER PROFILE"));
        verify(mockConsole, atLeastOnce()).println(contains("Good"));  // Time-based greeting
        
        // Verify navigation to customer menu
        verify(mockNavigator).clearMenuStack();
        verify(mockNavigator).pushMenu(any());
    }

    @Test
    void testEnhancedLoginFlow() {
        // Arrange - Pre-register a user
        registerUseCase.register("testuser", "password123", "Test User", "test@example.com");
        
        when(mockConsole.readLine())
            .thenReturn("testuser")        // Username
            .thenReturn("");               // Press Enter to continue

        when(mockConsole.readPassword())
            .thenReturn("password123");    // Password

        // Create the login command
        LoginCommand loginCommand = new LoginCommand(
            mockConsole, loginUseCase, mockNavigator, menuFactory
        );

        // Act
        loginCommand.execute();

        // Assert
        // Verify that session was created
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals("testuser", SessionManager.getInstance().getCurrentSession().getUsername());
        
        // Verify console interactions
        verify(mockConsole).printSuccess("Login successful!");
        verify(mockConsole, atLeastOnce()).println(contains("USER PROFILE"));
        verify(mockConsole, atLeastOnce()).println(contains("Good"));  // Time-based greeting
        
        // Verify navigation to role-specific menu
        verify(mockNavigator).clearMenuStack();
        verify(mockNavigator).pushMenu(any());
    }

    @Test
    void testCustomerMenuStructure() {
        // Arrange
        var customerMenu = menuFactory.createMenuForRole(UserRole.CUSTOMER);
        
        // Act & Assert
        assertNotNull(customerMenu);
        assertEquals("CUSTOMER NAVIGATION MENU", customerMenu.getTitle());
        
        // Verify menu has exactly 4 items
        assertEquals(4, customerMenu.getItems().size());
        
        // Verify menu items are correct
        var menuItems = customerMenu.getItems();
        assertEquals("Browse Products", menuItems.get(0).getLabel());
        assertEquals("View Cart", menuItems.get(1).getLabel());
        assertEquals("Order History", menuItems.get(2).getLabel());
        assertEquals("Logout", menuItems.get(3).getLabel());
    }

    @Test
    void testTimeBasedGreetingLogic() {
        // This test verifies the greeting logic indirectly through the commands
        // In a real implementation, we might extract this to a utility class for easier testing
        
        // Arrange - Pre-register a user
        registerUseCase.register("greetingtest", "password123", "Greeting Test", "greeting@example.com");
        
        when(mockConsole.readLine())
            .thenReturn("greetingtest")
            .thenReturn("");

        when(mockConsole.readPassword())
            .thenReturn("password123");

        LoginCommand loginCommand = new LoginCommand(
            mockConsole, loginUseCase, mockNavigator, menuFactory
        );

        // Act
        loginCommand.execute();

        // Assert - Verify that some greeting was displayed
        verify(mockConsole, atLeastOnce()).println(argThat(s -> 
            s.contains("Good Morning") || s.contains("Good Afternoon") || s.contains("Good Evening")
        ));
    }

    @Test 
    void testRegistrationFailureHandling() {
        // Arrange
        when(mockConsole.readLine())
            .thenReturn("Duplicate User")
            .thenReturn("existinguser")     // Username that will be duplicate
            .thenReturn("duplicate@example.com")
            .thenReturn("");               // Press Enter to continue

        when(mockConsole.readPassword())
            .thenReturn("password123")
            .thenReturn("password123");

        // Pre-register a user with the same username
        registerUseCase.register("existinguser", "password123", "Existing User", "existing@example.com");

        RegisterCommand registerCommand = new RegisterCommand(
            mockConsole, registerUseCase, mockNavigator, menuFactory
        );

        // Act
        registerCommand.execute();

        // Assert
        // Verify error was displayed and session was NOT created for the duplicate
        verify(mockConsole).printError(contains("Registration Error"));
        
        // Session should still be empty (no auto-login on failure)
        assertFalse(SessionManager.getInstance().isLoggedIn());
        
        // Should not navigate anywhere on failure
        verify(mockNavigator, never()).clearMenuStack();
    }
}
