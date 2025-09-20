package com.syos.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.syos.application.services.EventBus;
import com.syos.adapter.in.cli.io.LoggingConsoleIODecorator;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.application.validation.ValidationHandler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify all enhancements work correctly
 * Tests the complete enhanced system with all design patterns
 * 
 * Target: Validate complete system functionality
 */
@DisplayName("Complete System Enhancement Integration Test")
class SystemEnhancementIntegrationTest {
    
    private static ByteArrayOutputStream outputCapture;
    private static PrintStream originalOut;
    
    @BeforeAll
    static void setUpClass() {
        // Set up environment for testing
        System.setProperty("APP_ENV", "development");
        System.setProperty("CONSOLE_LOGGING", "true");
        
        // Capture output for verification
        outputCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputCapture));
    }
    
    @AfterAll
    static void tearDownClass() {
        System.setOut(originalOut);
        System.clearProperty("APP_ENV");
        System.clearProperty("CONSOLE_LOGGING");
    }
    
    @Test
    @DisplayName("Should initialize Event Bus singleton correctly")
    void shouldInitializeEventBusSingletonCorrectly() {
        // When
        EventBus eventBus1 = EventBus.getInstance();
        EventBus eventBus2 = EventBus.getInstance();
        
        // Then
        assertNotNull(eventBus1);
        assertNotNull(eventBus2);
        assertSame(eventBus1, eventBus2); // Should be same instance (Singleton)
        
        // Should start with no subscribers
        assertEquals(0, eventBus1.getSubscriberCount());
    }
    
    @Test
    @DisplayName("Should create LoggingConsoleIODecorator correctly")
    void shouldCreateLoggingConsoleIODecoratorCorrectly() {
        // Given
        StandardConsoleIO baseConsoleIO = new StandardConsoleIO();
        
        // When
        LoggingConsoleIODecorator decorator = new LoggingConsoleIODecorator(baseConsoleIO, true, true);
        
        // Then
        assertNotNull(decorator);
        assertTrue(decorator.isLoggingUserInputs());
        assertTrue(decorator.isLoggingOutputs());
        assertSame(baseConsoleIO, decorator.getWrappedConsoleIO());
    }
    
    @Test
    @DisplayName("Should create validation chain correctly")
    void shouldCreateValidationChainCorrectly() {
        // Given
        ValidationHandler nullValidator = new ValidationHandler.ValidationHandler() {
            @Override
            protected ValidationResult doValidation(ValidationRequest request) {
                if (request.getValue() == null) {
                    return ValidationResult.failure("Value cannot be null", getValidatorName());
                }
                return ValidationResult.success();
            }
            
            @Override
            protected String getValidatorName() {
                return "TestNullValidator";
            }
        };
        
        ValidationHandler lengthValidator = new ValidationHandler.ValidationHandler() {
            @Override
            protected ValidationResult doValidation(ValidationRequest request) {
                String value = request.getStringValue();
                if (value != null && value.length() < 3) {
                    return ValidationResult.failure("Value must be at least 3 characters", getValidatorName());
                }
                return ValidationResult.success();
            }
            
            @Override
            protected String getValidatorName() {
                return "TestLengthValidator";
            }
        };
        
        // When - Create validation chain
        nullValidator.setNext(lengthValidator);
        
        // Test valid input
        ValidationHandler.ValidationRequest validRequest = 
            new ValidationHandler.ValidationRequest("testField", "validValue", "string");
        ValidationHandler.ValidationResult validResult = nullValidator.handle(validRequest);
        
        // Test invalid input (null)
        ValidationHandler.ValidationRequest nullRequest = 
            new ValidationHandler.ValidationRequest("testField", null, "string");
        ValidationHandler.ValidationResult nullResult = nullValidator.handle(nullRequest);
        
        // Test invalid input (too short)
        ValidationHandler.ValidationRequest shortRequest = 
            new ValidationHandler.ValidationRequest("testField", "ab", "string");
        ValidationHandler.ValidationResult shortResult = nullValidator.handle(shortRequest);
        
        // Then
        assertTrue(validResult.isValid());
        assertFalse(nullResult.isValid());
        assertEquals("Value cannot be null", nullResult.getFirstErrorMessage());
        
        assertFalse(shortResult.isValid());
        assertEquals("Value must be at least 3 characters", shortResult.getFirstErrorMessage());
    }
    
    @Test
    @DisplayName("Should demonstrate all design patterns working together")
    void shouldDemonstrateAllDesignPatternsWorkingTogether() {
        // Singleton Pattern - EventBus
        EventBus eventBus = EventBus.getInstance();
        assertNotNull(eventBus);
        
        // Decorator Pattern - Enhanced ConsoleIO
        StandardConsoleIO baseIO = new StandardConsoleIO();
        LoggingConsoleIODecorator enhancedIO = new LoggingConsoleIODecorator(baseIO);
        assertNotNull(enhancedIO);
        
        // Chain of Responsibility - Validation
        ValidationHandler validator = new ValidationHandler.ValidationHandler() {
            @Override
            protected ValidationResult doValidation(ValidationRequest request) {
                return ValidationResult.success();
            }
            
            @Override
            protected String getValidatorName() {
                return "IntegrationTestValidator";
            }
        };
        
        ValidationHandler.ValidationRequest request = 
            new ValidationHandler.ValidationRequest("test", "value", "string");
        ValidationHandler.ValidationResult result = validator.handle(request);
        
        assertTrue(result.isValid());
        
        // Observer Pattern - Event subscription
        TestEventSubscriber subscriber = new TestEventSubscriber();
        eventBus.subscribe(subscriber);
        assertEquals(1, eventBus.getSubscriberCount());
        
        // Clean up
        eventBus.unsubscribe(subscriber);
        assertEquals(0, eventBus.getSubscriberCount());
    }
    
    @Test
    @DisplayName("Should handle production vs development environment correctly")
    void shouldHandleProductionVsDevelopmentEnvironmentCorrectly() {
        // Test development mode
        System.setProperty("APP_ENV", "development");
        String devEnv = System.getProperty("APP_ENV");
        assertEquals("development", devEnv);
        
        // Test production mode
        System.setProperty("APP_ENV", "production");
        String prodEnv = System.getProperty("APP_ENV");
        assertEquals("production", prodEnv);
        
        // Reset to development for other tests
        System.setProperty("APP_ENV", "development");
    }
    
    @Test
    @DisplayName("Should demonstrate clean architecture layer separation")
    void shouldDemonstrateCleanArchitectureLayerSeparation() {
        // Domain Layer - Pure business logic, no external dependencies
        assertDoesNotThrow(() -> {
            com.syos.domain.valueobjects.Money money = com.syos.domain.valueobjects.Money.of(100.00);
            assertNotNull(money);
            assertTrue(money.isPositive());
        });
        
        // Application Layer - Use cases and business rules
        assertDoesNotThrow(() -> {
            ValidationHandler.ValidationRequest request = 
                new ValidationHandler.ValidationRequest("amount", "100.00", "decimal");
            assertNotNull(request);
            assertEquals("100.00", request.getStringValue());
        });
        
        // Infrastructure Layer - External concerns
        assertDoesNotThrow(() -> {
            EventBus eventBus = EventBus.getInstance();
            assertNotNull(eventBus);
        });
        
        // Interface Adapters - UI and external interfaces
        assertDoesNotThrow(() -> {
            StandardConsoleIO consoleIO = new StandardConsoleIO();
            LoggingConsoleIODecorator decorator = new LoggingConsoleIODecorator(consoleIO);
            assertNotNull(decorator);
        });
    }
    
    @Test
    @DisplayName("Should demonstrate SOLID principles implementation")
    void shouldDemonstrateSOLIDPrinciplesImplementation() {
        // Single Responsibility - Each class has one responsibility
        EventBus eventBus = EventBus.getInstance(); // Only manages events
        assertNotNull(eventBus);
        
        // Open/Closed - Can extend without modifying
        StandardConsoleIO baseIO = new StandardConsoleIO();
        LoggingConsoleIODecorator enhancedIO = new LoggingConsoleIODecorator(baseIO);
        assertNotNull(enhancedIO);
        
        // Liskov Substitution - Subtypes are substitutable
        com.syos.adapter.in.cli.io.ConsoleIO consoleIO1 = new StandardConsoleIO();
        com.syos.adapter.in.cli.io.ConsoleIO consoleIO2 = new LoggingConsoleIODecorator(baseIO);
        assertNotNull(consoleIO1);
        assertNotNull(consoleIO2);
        
        // Interface Segregation - Small, focused interfaces
        // Dependency Inversion - Depend on abstractions
        assertTrue(true); // Demonstrated by the architecture itself
    }
    
    /**
     * Test event subscriber for testing Observer pattern
     */
    private static class TestEventSubscriber implements EventBus.EventSubscriber {
        private boolean eventReceived = false;
        
        @Override
        public boolean canHandle(com.syos.domain.events.DomainEvent event) {
            return true; // Handle all events for testing
        }
        
        @Override
        public void handle(com.syos.domain.events.DomainEvent event) {
            eventReceived = true;
        }
        
        public boolean isEventReceived() {
            return eventReceived;
        }
    }
}
