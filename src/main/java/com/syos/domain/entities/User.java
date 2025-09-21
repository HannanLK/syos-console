package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import com.syos.domain.exceptions.AuthenticationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Pure Domain Entity - User Aggregate Root
 * 
 * No framework dependencies, pure business logic only.
 * Follows DDD principles with clear business invariants.
 */
public class User {
    private final UserID id;
    private final Username username;
    private Password password;
    private final UserRole role;
    private Name name;
    private Email email;
    private SynexPoints synexPoints;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final UserID createdBy;

    // Private constructor enforces factory methods
    private User(UserID id, Username username, Password password, UserRole role,
                 Name name, Email email, SynexPoints synexPoints, boolean active,
                 LocalDateTime createdAt, LocalDateTime updatedAt, UserID createdBy) {
        this.id = id;
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (email == null) throw new IllegalArgumentException("Email cannot be null");
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.synexPoints = synexPoints != null ? synexPoints : SynexPoints.zero();
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.createdBy = createdBy;
    }
    
    // Public constructor for backward compatibility with tests
    public User(Username username, Password password, Name name, Email email, UserRole role) {
        this(null, username, password, role, name, email, SynexPoints.zero(), true, 
             LocalDateTime.now(), LocalDateTime.now(), null);
    }

    /**
     * Factory method: Create new customer
     * Encapsulates business rules for customer creation
     */
    public static User createCustomer(Username username, Email email, Password password) {
        validateCustomerCreationRules(username, email, password);
        
        return new User(
            null, // ID will be assigned by repository
            username,
            password,
            UserRole.CUSTOMER,
            Name.of("New Customer"), // Default name, can be updated later
            email,
            SynexPoints.zero(),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );
    }

    /**
     * Factory method: Create employee
     * Only admin can create employees - business rule enforced
     */
    public static User createEmployee(Name name, Username username, Email email, 
                                   Password password, UserID createdByAdmin) {
        Objects.requireNonNull(createdByAdmin, "Employee must be created by admin");
        validateEmployeeCreationRules(name, username, email, password);
        
        return new User(
            null,
            username,
            password,
            UserRole.EMPLOYEE,
            name,
            email,
            SynexPoints.zero(), // Employees don't accumulate points
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            createdByAdmin
        );
    }

    /**
     * Factory method: Create admin
     * System-level operation with special validation
     */
    public static User createAdmin(Name name, Username username, Email email, Password password) {
        validateAdminCreationRules(name, username, email, password);
        
        return new User(
            null,
            username,
            password,
            UserRole.ADMIN,
            name,
            email,
            SynexPoints.zero(), // Admins don't accumulate points
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null // System created
        );
    }

    /**
     * Reconstitute from persistence (used by repositories)
     * No validation since data is already persisted
     */
    public static User reconstitute(UserID id, Username username, Password password,
                                   UserRole role, Name name, Email email,
                                   SynexPoints synexPoints, boolean active,
                                   LocalDateTime createdAt, LocalDateTime updatedAt,
                                   UserID createdBy) {
        return new User(id, username, password, role, name, email,
                       synexPoints, active, createdAt, updatedAt, createdBy);
    }

    /**
     * Compatibility factory for tests: create user with explicit role and raw password
     */
    public static User createWithRole(Username username, String rawPassword, Name name, Email email,
                                      UserRole role, UserID createdBy) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(rawPassword, "Password is required");
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(role, "Role is required");

        Password password = Password.hash(rawPassword);
        LocalDateTime now = LocalDateTime.now();
        return new User(null, username, password, role, name, email,
                role == UserRole.CUSTOMER ? SynexPoints.zero() : SynexPoints.zero(),
                true, now, now, createdBy);
    }

    /**
     * Compatibility reconstitution method expected by some tests.
     * Maps value objects to the current domain fields.
     */
    public static User withId(UserID id,
                              Username username,
                              Password password,
                              UserRole role,
                              Name name,
                              Email email,
                              SynexPoints synexPoints,
                              ActiveStatus activeStatus,
                              CreatedAt createdAt,
                              UpdatedAt updatedAt,
                              UserID createdBy,
                              MemberSince memberSince) {
        boolean active = activeStatus != null ? activeStatus.isActive() : true;
        LocalDateTime created = createdAt != null ? createdAt.getValue()
                : (memberSince != null ? memberSince.getValue() : LocalDateTime.now());
        LocalDateTime updated = updatedAt != null ? updatedAt.getValue() : created;
        return new User(id, username, password, role, name, email,
                synexPoints != null ? synexPoints : SynexPoints.zero(), active, created, updated, createdBy);
    }

    // Overload to accept LocalDateTime createdAt for backward test compatibility
    public static User withId(UserID id,
                              Username username,
                              Password password,
                              UserRole role,
                              Name name,
                              Email email,
                              SynexPoints synexPoints,
                              ActiveStatus activeStatus,
                              LocalDateTime createdAt,
                              UpdatedAt updatedAt,
                              UserID createdBy,
                              MemberSince memberSince) {
        boolean active = activeStatus != null ? activeStatus.isActive() : true;
        LocalDateTime created = createdAt != null ? createdAt
                : (memberSince != null ? memberSince.getValue() : LocalDateTime.now());
        LocalDateTime updated = updatedAt != null ? updatedAt.getValue() : created;
        return new User(id, username, password, role, name, email,
                synexPoints != null ? synexPoints : SynexPoints.zero(), active, created, updated, createdBy);
    }

    /**
     * Additional getters for backward compatibility with tests expecting value objects
     */
    public ActiveStatus getActiveStatus() { return ActiveStatus.of(active); }
    public MemberSince getMemberSince() { return MemberSince.of(createdAt); }

    // Business Methods

    /**
     * Can this user manage products?
     * Business rule: Only admins and employees can manage products
     */
    public boolean canManageProducts() {
        return active && (role == UserRole.ADMIN || role == UserRole.EMPLOYEE);
    }

    /**
     * Can this user create other users?
     * Business rule: Only active admins can create users
     */
    public boolean canCreateUsers() {
        return active && role == UserRole.ADMIN;
    }
    
    /**
     * Can this user manage other users?
     * Business rule: Only active admins can manage users
     */
    public boolean canManageUsers() {
        return active && role == UserRole.ADMIN;
    }
    
    /**
     * Role checking methods for backward compatibility
     */
    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }
    
    public boolean isEmployee() {
        return role == UserRole.EMPLOYEE;
    }
    
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    /**
     * Authenticate user with password
     */
    public boolean authenticate(Password password) {
        return active && this.password.matches(password);
    }
    
    /**
     * Alternative authenticate method for raw string password
     */
    public boolean authenticate(String rawPassword) {
        return active && this.password.matches(rawPassword);
    }
    
    /**
     * Deactivate user account
     * Business rule: Cannot deactivate system admin
     */
    public User deactivate() {
        if (role == UserRole.ADMIN && "admin".equals(username.getValue())) {
            throw new IllegalStateException("Cannot deactivate system admin");
        }
        // Mutate in place for test compatibility
        this.active = false;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
    
    /**
     * Reactivate user account
     */
    public User reactivate() {
        // Allow reactivation for any non-null user
        this.active = true;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
    
    /**
     * Add Synex Points - for backward compatibility with tests
     */
    public void addSynexPoints(SynexPoints points) {
        Objects.requireNonNull(points, "Points cannot be null");
        this.synexPoints = this.synexPoints.add(points);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Redeem Synex Points - for backward compatibility with tests
     */
    public void redeemSynexPoints(SynexPoints points) {
        Objects.requireNonNull(points, "Points cannot be null");
        this.synexPoints = this.synexPoints.subtract(points);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Can this user accumulate Synex Points?
     * Business rule: Only customers accumulate points
     */
    public boolean canAccumulatePoints() {
        return active && role == UserRole.CUSTOMER;
    }

    /**
     * Award Synex Points for purchase
     * Business rule: 1% of purchase amount as points
     */
    public User awardSynexPoints(Money purchaseAmount) {
        if (!canAccumulatePoints()) {
            throw new IllegalStateException("User cannot accumulate Synex Points");
        }
        
        SynexPoints pointsToAward = SynexPoints.fromPurchase(purchaseAmount);
        SynexPoints newPoints = synexPoints.add(pointsToAward);
        
        return new User(id, username, password, role, name, email,
                       newPoints, active, createdAt, LocalDateTime.now(), createdBy);
    }

    /**
     * Update profile information
     * Business rule: Cannot change role or username
     */
    public User updateProfile(Name newName, Email newEmail) {
        Objects.requireNonNull(newName, "Name cannot be null");
        Objects.requireNonNull(newEmail, "Email cannot be null");
        
        this.name = newName;
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Change password
     * Business rule: New password must be different and meet criteria
     */
    public User changePassword(Password newPassword) {
        Objects.requireNonNull(newPassword, "New password cannot be null");
        
        if (password.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        return new User(id, username, newPassword, role, name, email,
                       synexPoints, active, createdAt, LocalDateTime.now(), createdBy);
    }
    
    /**
     * Change password with old password verification - for backward compatibility with tests
     */
    public void changePassword(Password oldPassword, Password newPassword) {
        Objects.requireNonNull(oldPassword, "Old password cannot be null");
        Objects.requireNonNull(newPassword, "New password cannot be null");
        
        if (!this.password.matches(oldPassword)) {
            throw new AuthenticationException("Invalid old password");
        }
        
        if (!active) {
            throw new IllegalStateException("Cannot change password for inactive user");
        }
        
        // Mutate in place for test compatibility
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    // Validation Methods

    private static void validateCustomerCreationRules(Username username, Email email, Password password) {
        Objects.requireNonNull(username, "Username is required for customer");
        Objects.requireNonNull(email, "Email is required for customer");
        Objects.requireNonNull(password, "Password is required for customer");
        
        // Additional business rules can be added here
        // Username length and format are validated in Username value object.
        if (username.getValue().length() < 2) {
            throw new IllegalArgumentException("Username must be at least 2 characters");
        }
    }

    private static void validateEmployeeCreationRules(Name name, Username username, Email email, Password password) {
        Objects.requireNonNull(name, "Name is required for employee");
        Objects.requireNonNull(username, "Username is required for employee");
        Objects.requireNonNull(email, "Email is required for employee");
        Objects.requireNonNull(password, "Password is required for employee");
        
        // Employee-specific business rules
        if (name.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty");
        }
    }

    private static void validateAdminCreationRules(Name name, Username username, Email email, Password password) {
        Objects.requireNonNull(name, "Name is required for admin");
        Objects.requireNonNull(username, "Username is required for admin");
        Objects.requireNonNull(email, "Email is required for admin");
        Objects.requireNonNull(password, "Password is required for admin");
        
        // Admin-specific business rules
        // Password strength is validated when creating the Password value object.
        // Additional admin-specific rules can be added here if needed.
    }

    // Getters (immutable)
    public UserID getId() { return id; }
    public Username getUsername() { return username; }
    public Password getPassword() { return password; }
    public UserRole getRole() { return role; }
    public Name getName() { return name; }
    public Email getEmail() { return email; }
    public SynexPoints getSynexPoints() { return synexPoints; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public UserID getCreatedBy() { return createdBy; }

    // Equality based on business identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username=" + username +
                ", role=" + role +
                ", name=" + name +
                ", active=" + active +
                '}';
    }
}
