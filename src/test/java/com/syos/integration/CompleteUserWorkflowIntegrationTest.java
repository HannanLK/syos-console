package com.syos.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.dto.requests.LoginRequest;
import com.syos.application.dto.requests.RegisterRequest;
import com.syos.application.dto.responses.AuthResponse;
import com.syos.application.dto.responses.UserResponse;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests covering complete user workflows
 * Tests end-to-end scenarios with real implementations
 * 
 * Target: High integration coverage across multiple layers
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Complete User Workflow Integration Tests")
class CompleteUserWorkflowIntegrationTest {
    
    private UserRepository userRepository;
    private LoginUseCase loginUseCase;
    private RegisterCustomerUseCase registerUseCase;
    private ConsoleIO consoleIO;
    
    @BeforeEach
    void setUp() {
        // Use real implementations for integration testing
        userRepository = new InMemoryUserRepository();
        loginUseCase = new LoginUseCase(userRepository);
        registerUseCase = new RegisterCustomerUseCase(userRepository);
        consoleIO = new StandardConsoleIO();
        
        // Initialize with some test data
        initializeTestData();
    }
    
    private void initializeTestData() {
        // Create a pre-existing user for testing
        User existingUser = new User(
            new Username("existinguser"),
            new Password("ExistingPassword123!"),
            new Name("Existing User"),
            new Email("existing@example.com"),
            UserRole.CUSTOMER
        );
        userRepository.save(existingUser);
        
        // Create admin user
        User admin = new User(
            new Username("admin"),
            new Password("AdminPassword123!"),
            new Name("Admin User"),
            new Email("admin@syos.com"),
            UserRole.ADMIN
        );
        userRepository.save(admin);
        
        // Create employee user
        User employee = new User(
            new Username("employee"),
            new Password("EmployeePassword123!"),
            new Name("Employee User"),
            new Email("employee@syos.com"),
            UserRole.EMPLOYEE
        );
        userRepository.save(employee);
    }
    
    @Nested
    @DisplayName("Complete Customer Registration and Login Workflow")
    class CustomerRegistrationAndLoginWorkflow {
        
        @Test
        @DisplayName("Should complete full customer registration and login workflow")
        void shouldCompleteFullCustomerRegistrationAndLoginWorkflow() {
            // Step 1: Register new customer
            RegisterRequest registerRequest = new RegisterRequest(
                "newcustomer",
                "NewCustomerPassword123!",
                "New Customer",
                "newcustomer@example.com"
            );
            
            UserResponse registrationResponse = registerUseCase.execute(registerRequest);
            
            // Verify registration success
            assertNotNull(registrationResponse);
            assertTrue(registrationResponse.isSuccess());
            assertEquals("newcustomer", registrationResponse.getUsername());
            assertEquals(UserRole.CUSTOMER, registrationResponse.getRole());
            assertNull(registrationResponse.getErrorMessage());
            
            // Step 2: Login with new credentials
            LoginRequest loginRequest = new LoginRequest("newcustomer", "NewCustomerPassword123!");
            AuthResponse loginResponse = loginUseCase.execute(loginRequest);
            
            // Verify login success
            assertNotNull(loginResponse);
            assertTrue(loginResponse.isSuccess());
            assertEquals("newcustomer", loginResponse.getUsername());
            assertEquals(UserRole.CUSTOMER, loginResponse.getUserRole());
            assertNotNull(loginResponse.getSessionToken());
            assertNull(loginResponse.getErrorMessage());
            
            // Step 3: Verify user persisted correctly
            assertTrue(userRepository.existsByUsername("newcustomer"));
            User savedUser = userRepository.findByUsername("newcustomer").orElse(null);
            assertNotNull(savedUser);
            assertEquals("newcustomer", savedUser.getUsername().getValue());
            assertTrue(savedUser.isCustomer());
            assertTrue(savedUser.isActive());
        }
        
        @Test
        @DisplayName("Should prevent duplicate user registration")
        void shouldPreventDuplicateUserRegistration() {
            // Try to register with existing username
            RegisterRequest duplicateRequest = new RegisterRequest(
                "existinguser", // This username already exists
                "NewPassword123!",
                "Duplicate User",
                "duplicate@example.com"
            );
            
            UserResponse response = registerUseCase.execute(duplicateRequest);
            
            // Verify registration failure
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNotNull(response.getErrorMessage());
            assertTrue(response.getErrorMessage().contains("already exists") || 
                      response.getErrorMessage().contains("username"));
        }
    }
    
    @Nested
    @DisplayName("Multi-Role Authentication Workflow")
    class MultiRoleAuthenticationWorkflow {
        
        @Test
        @DisplayName("Should authenticate different user roles correctly")
        void shouldAuthenticateDifferentUserRolesCorrectly() {
            // Test customer login
            LoginRequest customerLogin = new LoginRequest("existinguser", "ExistingPassword123!");
            AuthResponse customerResponse = loginUseCase.execute(customerLogin);
            
            assertTrue(customerResponse.isSuccess());
            assertEquals(UserRole.CUSTOMER, customerResponse.getUserRole());
            
            // Test employee login
            LoginRequest employeeLogin = new LoginRequest("employee", "EmployeePassword123!");
            AuthResponse employeeResponse = loginUseCase.execute(employeeLogin);
            
            assertTrue(employeeResponse.isSuccess());
            assertEquals(UserRole.EMPLOYEE, employeeResponse.getUserRole());
            
            // Test admin login
            LoginRequest adminLogin = new LoginRequest("admin", "AdminPassword123!");
            AuthResponse adminResponse = loginUseCase.execute(adminLogin);
            
            assertTrue(adminResponse.isSuccess());
            assertEquals(UserRole.ADMIN, adminResponse.getUserRole());
            
            // Verify all tokens are unique
            assertNotEquals(customerResponse.getSessionToken(), employeeResponse.getSessionToken());
            assertNotEquals(employeeResponse.getSessionToken(), adminResponse.getSessionToken());
            assertNotEquals(customerResponse.getSessionToken(), adminResponse.getSessionToken());
        }
        
        @Test
        @DisplayName("Should handle concurrent user operations safely")
        void shouldHandleConcurrentUserOperationsSafely() throws InterruptedException {
            int numberOfThreads = 10;
            Thread[] threads = new Thread[numberOfThreads];
            boolean[] results = new boolean[numberOfThreads];
            
            // Test concurrent registrations with different usernames
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        RegisterRequest request = new RegisterRequest(
                            "concurrent" + index,
                            "ConcurrentPassword123!",
                            "Concurrent User " + index,
                            "concurrent" + index + "@example.com"
                        );
                        
                        UserResponse response = registerUseCase.execute(request);
                        results[index] = response.isSuccess();
                    } catch (Exception e) {
                        results[index] = false;
                    }
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify all registrations were successful
            for (int i = 0; i < numberOfThreads; i++) {
                assertTrue(results[i], "Registration " + i + " should have succeeded");
                assertTrue(userRepository.existsByUsername("concurrent" + i));
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Recovery Workflow")
    class ErrorHandlingAndRecoveryWorkflow {
        
        @Test
        @DisplayName("Should handle invalid input gracefully across use cases")
        void shouldHandleInvalidInputGracefullyAcrossUseCases() {
            // Test invalid registration inputs
            RegisterRequest[] invalidRegistrations = {
                new RegisterRequest(null, "password", "name", "email@example.com"),
                new RegisterRequest("", "password", "name", "email@example.com"),
                new RegisterRequest("user", "", "name", "email@example.com"),
                new RegisterRequest("user", "password", null, "email@example.com"),
                new RegisterRequest("user", "password", "name", "invalid-email")
            };
            
            for (RegisterRequest request : invalidRegistrations) {
                assertThrows(Exception.class, () -> {
                    registerUseCase.execute(request);
                });
            }
            
            // Test invalid login inputs
            LoginRequest[] invalidLogins = {
                new LoginRequest(null, "password"),
                new LoginRequest("", "password"),
                new LoginRequest("user", null),
                new LoginRequest("user", "")
            };
            
            for (LoginRequest request : invalidLogins) {
                assertThrows(Exception.class, () -> {
                    loginUseCase.execute(request);
                });
            }
        }
        
        @Test
        @DisplayName("Should maintain data consistency under error conditions")
        void shouldMaintainDataConsistencyUnderErrorConditions() {
            // Get initial user count
            long initialCount = userRepository.count();
            
            // Attempt multiple failed registrations
            for (int i = 0; i < 5; i++) {
                try {
                    RegisterRequest request = new RegisterRequest(
                        "existinguser", // Duplicate username
                        "SomePassword123!",
                        "Some Name",
                        "some" + i + "@example.com"
                    );
                    registerUseCase.execute(request);
                } catch (Exception e) {
                    // Expected to fail
                }
            }
            
            // Verify user count hasn't changed (no partial registrations)
            assertEquals(initialCount, userRepository.count());
            
            // Verify original user is still intact
            User originalUser = userRepository.findByUsername("existinguser").orElse(null);
            assertNotNull(originalUser);
            assertEquals("existing@example.com", originalUser.getEmail().getValue());
        }
    }
    
    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceAndScalabilityTests {
        
        @Test
        @DisplayName("Should handle large number of users efficiently")
        void shouldHandleLargeNumberOfUsersEfficiently() {
            int numberOfUsers = 10;
            long initialCount = userRepository.count();
            long startTime = System.currentTimeMillis();
            
            // Register many users
            for (int i = 0; i < numberOfUsers; i++) {
                RegisterRequest request = new RegisterRequest(
                    "user" + i,
                    "Password123!",
                    "User " + i,
                    "user" + i + "@example.com"
                );
                
                UserResponse response = registerUseCase.execute(request);
                assertTrue(response.isSuccess());
            }
            
            long registrationTime = System.currentTimeMillis() - startTime;
            
            // Test login performance with many users
            startTime = System.currentTimeMillis();
            
            for (int i = 0; i < Math.min(100, numberOfUsers); i++) {
                LoginRequest request = new LoginRequest("user" + i, "Password123!");
                AuthResponse response = loginUseCase.execute(request);
                assertTrue(response.isSuccess());
            }
            
            long loginTime = System.currentTimeMillis() - startTime;
            
            // Verify reasonable performance (adjust thresholds as needed)
            assertTrue(registrationTime < 30000, "Registration took too long: " + registrationTime + "ms");
            assertTrue(loginTime < 5000, "Logins took too long: " + loginTime + "ms");
            
            // Verify all users were created relative to initial count
            assertEquals(initialCount + numberOfUsers, userRepository.count());
        }
        
        @Test
        @DisplayName("Should maintain performance under concurrent load")
        void shouldMaintainPerformanceUnderConcurrentLoad() throws InterruptedException {
            int numberOfThreads = 50;
            int operationsPerThread = 10;
            Thread[] threads = new Thread[numberOfThreads];
            long[] executionTimes = new long[numberOfThreads];
            
            for (int t = 0; t < numberOfThreads; t++) {
                final int threadIndex = t;
                threads[t] = new Thread(() -> {
                    long threadStartTime = System.currentTimeMillis();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        // Mix of registration and login operations
                        if (i % 2 == 0) {
                            // Registration
                            RegisterRequest request = new RegisterRequest(
                                "thread" + threadIndex + "user" + i,
                                "Password123!",
                                "Thread " + threadIndex + " User " + i,
                                "thread" + threadIndex + "user" + i + "@example.com"
                            );
                            registerUseCase.execute(request);
                        } else {
                            // Login with existing user
                            LoginRequest request = new LoginRequest("existinguser", "ExistingPassword123!");
                            loginUseCase.execute(request);
                        }
                    }
                    
                    executionTimes[threadIndex] = System.currentTimeMillis() - threadStartTime;
                });
            }
            
            long overallStartTime = System.currentTimeMillis();
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for completion
            for (Thread thread : threads) {
                thread.join();
            }
            
            long overallTime = System.currentTimeMillis() - overallStartTime;
            
            // Verify reasonable performance
            assertTrue(overallTime < 60000, "Overall execution took too long: " + overallTime + "ms");
            
            // Verify no thread took excessively long
            for (int i = 0; i < numberOfThreads; i++) {
                assertTrue(executionTimes[i] < 10000, 
                    "Thread " + i + " took too long: " + executionTimes[i] + "ms");
            }
        }
    }
    
    @Nested
    @DisplayName("Business Logic Integration Tests")
    class BusinessLogicIntegrationTests {
        
        @Test
        @DisplayName("Should handle complete user lifecycle correctly")
        void shouldHandleCompleteUserLifecycleCorrectly() {
            // 1. Register user
            RegisterRequest registerRequest = new RegisterRequest(
                "lifecycle",
                "LifecyclePassword123!",
                "Lifecycle User",
                "lifecycle@example.com"
            );
            
            UserResponse registrationResponse = registerUseCase.execute(registerRequest);
            assertTrue(registrationResponse.isSuccess());
            
            // 2. Login user
            LoginRequest loginRequest = new LoginRequest("lifecycle", "LifecyclePassword123!");
            AuthResponse loginResponse = loginUseCase.execute(loginRequest);
            assertTrue(loginResponse.isSuccess());
            
            // 3. Verify user exists and is active
            User user = userRepository.findByUsername("lifecycle").orElse(null);
            assertNotNull(user);
            assertTrue(user.isActive());
            assertTrue(user.isCustomer());
            
            // 4. Test password authentication
            assertTrue(user.authenticate(new Password("LifecyclePassword123!")));
            assertFalse(user.authenticate(new Password("WrongPassword123!")));
            
            // 5. Test synex points (should start at zero)
            assertEquals(SynexPoints.ZERO, user.getSynexPoints());
            
            // 6. Add synex points
            user.addSynexPoints(SynexPoints.of(new java.math.BigDecimal("100")));
            assertEquals(SynexPoints.of(new java.math.BigDecimal("100")), user.getSynexPoints());
            
            // 7. Redeem some points
            user.redeemSynexPoints(SynexPoints.of(new java.math.BigDecimal("25")));
            assertEquals(SynexPoints.of(new java.math.BigDecimal("75")), user.getSynexPoints());
            
            // 8. Update user profile
            user.updateProfile(new Name("Updated Lifecycle User"), new Email("updated@example.com"));
            assertEquals("Updated Lifecycle User", user.getName().getValue());
            assertEquals("updated@example.com", user.getEmail().getValue());
        }
        
        @Test
        @DisplayName("Should enforce business rules consistently across operations")
        void shouldEnforceBusinessRulesConsistentlyAcrossOperations() {
            // Test username uniqueness across multiple operations
            RegisterRequest request1 = new RegisterRequest(
                "uniquetest",
                "Password123!",
                "First User",
                "first@example.com"
            );
            
            RegisterRequest request2 = new RegisterRequest(
                "uniquetest", // Same username
                "Password456!",
                "Second User",
                "second@example.com"
            );
            
            // First registration should succeed
            UserResponse response1 = registerUseCase.execute(request1);
            assertTrue(response1.isSuccess());
            
            // Second registration should fail
            UserResponse response2 = registerUseCase.execute(request2);
            assertFalse(response2.isSuccess());
            
            // Test email uniqueness
            RegisterRequest request3 = new RegisterRequest(
                "uniquetest2",
                "Password789!",
                "Third User",
                "first@example.com" // Same email as first user
            );
            
            UserResponse response3 = registerUseCase.execute(request3);
            assertFalse(response3.isSuccess());
        }
    }
}
