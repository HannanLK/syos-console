package com.syos.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import com.syos.domain.exceptions.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for User entity
 * Tests all business logic, validation, security, and edge cases
 * 
 * Target: 100% line coverage for User entity
 */
@DisplayName("User Entity Comprehensive Tests")
class UserEnhancedTest {
    
    private Username validUsername;
    private Password validPassword;
    private Name validName;
    private Email validEmail;
    private User user;
    
    @BeforeEach
    void setUp() {
        validUsername = new Username("testuser");
        validPassword = new Password("SecurePassword123!");
        validName = new Name("Test User");
        validEmail = new Email("test@example.com");
        
        user = new User(validUsername, validPassword, validName, validEmail, UserRole.CUSTOMER);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create user with all valid parameters")
        void shouldCreateUserWithAllValidParameters() {
            assertNotNull(user);
            assertEquals(validUsername, user.getUsername());
            assertEquals(validName, user.getName());
            assertEquals(validEmail, user.getEmail());
            assertEquals(UserRole.CUSTOMER, user.getRole());
            assertTrue(user.isActive());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getMemberSince());
            assertEquals(SynexPoints.ZERO, user.getSynexPoints());
        }
        
        @Test
        @DisplayName("Should create admin user")
        void shouldCreateAdminUser() {
            User admin = new User(
                new Username("admin"),
                validPassword,
                new Name("Admin User"),
                new Email("admin@syos.com"),
                UserRole.ADMIN
            );
            
            assertEquals(UserRole.ADMIN, admin.getRole());
            assertTrue(admin.isAdmin());
        }
        
        @Test
        @DisplayName("Should create employee user")
        void shouldCreateEmployeeUser() {
            User employee = new User(
                new Username("employee"),
                validPassword,
                new Name("Employee User"),
                new Email("employee@syos.com"),
                UserRole.EMPLOYEE
            );
            
            assertEquals(UserRole.EMPLOYEE, employee.getRole());
            assertTrue(employee.isEmployee());
        }
        
        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new User(null, validPassword, validName, validEmail, UserRole.CUSTOMER);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new User(validUsername, null, validName, validEmail, UserRole.CUSTOMER);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new User(validUsername, validPassword, null, validEmail, UserRole.CUSTOMER);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new User(validUsername, validPassword, validName, null, UserRole.CUSTOMER);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when role is null")
        void shouldThrowExceptionWhenRoleIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new User(validUsername, validPassword, validName, validEmail, null);
            });
        }
    }
    
    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        
        @Test
        @DisplayName("Should authenticate with correct password")
        void shouldAuthenticateWithCorrectPassword() {
            assertTrue(user.authenticate(validPassword));
        }
        
        @Test
        @DisplayName("Should not authenticate with incorrect password")
        void shouldNotAuthenticateWithIncorrectPassword() {
            Password wrongPassword = new Password("WrongPassword123!");
            assertFalse(user.authenticate(wrongPassword));
        }
        
        @Test
        @DisplayName("Should not authenticate when user is inactive")
        void shouldNotAuthenticateWhenUserIsInactive() {
            user.deactivate();
            assertFalse(user.authenticate(validPassword));
        }
        
        @Test
        @DisplayName("Should change password with valid old password")
        void shouldChangePasswordWithValidOldPassword() {
            Password newPassword = new Password("NewSecurePassword456!");
            user.changePassword(validPassword, newPassword);
            
            assertFalse(user.authenticate(validPassword));
            assertTrue(user.authenticate(newPassword));
        }
        
        @Test
        @DisplayName("Should throw exception when changing password with invalid old password")
        void shouldThrowExceptionWhenChangingPasswordWithInvalidOldPassword() {
            Password wrongOldPassword = new Password("WrongOldPassword123!");
            Password newPassword = new Password("NewSecurePassword456!");
            
            assertThrows(AuthenticationException.class, () -> {
                user.changePassword(wrongOldPassword, newPassword);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when changing password while inactive")
        void shouldThrowExceptionWhenChangingPasswordWhileInactive() {
            user.deactivate();
            Password newPassword = new Password("NewSecurePassword456!");
            
            assertThrows(IllegalStateException.class, () -> {
                user.changePassword(validPassword, newPassword);
            });
        }
    }
    
    @Nested
    @DisplayName("Role Permission Tests")
    class RolePermissionTests {
        
        @Test
        @DisplayName("Should identify customer role correctly")
        void shouldIdentifyCustomerRoleCorrectly() {
            assertTrue(user.isCustomer());
            assertFalse(user.isEmployee());
            assertFalse(user.isAdmin());
        }
        
        @Test
        @DisplayName("Should identify employee role correctly")
        void shouldIdentifyEmployeeRoleCorrectly() {
            User employee = new User(
                new Username("emp"),
                validPassword,
                validName,
                validEmail,
                UserRole.EMPLOYEE
            );
            
            assertFalse(employee.isCustomer());
            assertTrue(employee.isEmployee());
            assertFalse(employee.isAdmin());
        }
        
        @Test
        @DisplayName("Should identify admin role correctly")
        void shouldIdentifyAdminRoleCorrectly() {
            User admin = new User(
                new Username("admin"),
                validPassword,
                validName,
                validEmail,
                UserRole.ADMIN
            );
            
            assertFalse(admin.isCustomer());
            assertFalse(admin.isEmployee());
            assertTrue(admin.isAdmin());
        }
        
        @Test
        @DisplayName("Should check if user can manage products")
        void shouldCheckIfUserCanManageProducts() {
            // Customers cannot manage products
            assertFalse(user.canManageProducts());
            
            // Employees can manage products
            User employee = new User(
                new Username("emp"),
                validPassword,
                validName,
                validEmail,
                UserRole.EMPLOYEE
            );
            assertTrue(employee.canManageProducts());
            
            // Admins can manage products
            User admin = new User(
                new Username("admin"),
                validPassword,
                validName,
                validEmail,
                UserRole.ADMIN
            );
            assertTrue(admin.canManageProducts());
        }
        
        @Test
        @DisplayName("Should check if user can manage users")
        void shouldCheckIfUserCanManageUsers() {
            // Customers cannot manage users
            assertFalse(user.canManageUsers());
            
            // Employees cannot manage users
            User employee = new User(
                new Username("emp"),
                validPassword,
                validName,
                validEmail,
                UserRole.EMPLOYEE
            );
            assertFalse(employee.canManageUsers());
            
            // Admins can manage users
            User admin = new User(
                new Username("admin"),
                validPassword,
                validName,
                validEmail,
                UserRole.ADMIN
            );
            assertTrue(admin.canManageUsers());
        }
    }
    
    // Additional test methods continue here...
    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesAndSpecialScenarios {
        
        @Test
        @DisplayName("Should handle zero synex points operations")
        void shouldHandleZeroSynexPointsOperations() {
            SynexPoints zeroPoints = SynexPoints.ZERO;
            
            user.addSynexPoints(zeroPoints);
            assertEquals(SynexPoints.ZERO, user.getSynexPoints());
            
            user.redeemSynexPoints(zeroPoints);
            assertEquals(SynexPoints.ZERO, user.getSynexPoints());
        }
    }
}
