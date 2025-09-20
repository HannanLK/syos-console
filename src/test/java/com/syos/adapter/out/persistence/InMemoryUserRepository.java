package com.syos.adapter.out.persistence;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of UserRepository for testing purposes.
 * Provides thread-safe operations for testing user management functionality.
 */
class InMemoryUserRepositoryTestDouble implements UserRepository {
    
    private final Map<UserID, User> users = new HashMap<>();
    private final Map<Username, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private long nextId = 1L;
    
    public InMemoryUserRepositoryTestDouble() {
        // Initialize with default users for testing
        initializeDefaultUsers();
    }
    
    protected void initializeDefaultUsers() {
        // Create default admin
        User admin = User.createAdmin(
            Name.of("System Admin"),
            Username.of("admin"),
            Email.of("admin@syos.com"),
            Password.hash("admin123")
        );
        UserID adminId = new UserID(nextId++);
        saveWithId(admin, adminId);
        // Retrieve persisted admin with ID for reference
        User persistedAdmin = users.get(adminId);
        
        // Create default employee
        User employee = User.createEmployee(
            Name.of("Store Employee"),
            Username.of("employee"),
            Email.of("employee@syos.com"),
            Password.hash("emp12345"),
            persistedAdmin.getId()
        );
        saveWithId(employee, new UserID(nextId++));
        
        // Create default customer
        User customer = User.createCustomer(
            Username.of("customer"),
            Email.of("customer@syos.com"),
            Password.hash("cust1234")
        );
        saveWithId(customer, new UserID(nextId++));
    }
    
    private void saveWithId(User user, UserID id) {
        User userWithId = User.reconstitute(
            id,
            user.getUsername(),
            user.getPassword(),
            user.getRole(),
            user.getName(),
            user.getEmail(),
            user.getSynexPoints(),
            user.isActive(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getCreatedBy()
        );
        users.put(id, userWithId);
        usersByUsername.put(userWithId.getUsername(), userWithId);
        usersByEmail.put(userWithId.getEmail().getValue().toLowerCase(), userWithId);
    }
    
    @Override
    public synchronized Optional<User> findById(UserID userId) {
        return Optional.ofNullable(users.get(userId));
    }
    
    @Override
    public synchronized Optional<User> findByUsername(Username username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username));
    }
    
    @Override
    public synchronized boolean existsByUsername(Username username) {
        if (username == null) {
            return false;
        }
        return usersByUsername.containsKey(username);
    }
    
    @Override
    public synchronized boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return usersByEmail.containsKey(email.toLowerCase());
    }
    
    // Added explicit String-based overloads to avoid ambiguity with interface defaults
    public synchronized Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return findByUsername(Username.of(username));
    }
    
    public synchronized boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return existsByUsername(Username.of(username));
    }
    
    @Override
    public synchronized User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            // Assign new ID for new users
            UserID newId = new UserID(nextId++);
            User newUser = User.reconstitute(
                newId,
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.getName(),
                user.getEmail(),
                user.getSynexPoints(),
                user.isActive(),
                user.getCreatedAt(),
                LocalDateTime.now(), // Updated at
                user.getCreatedBy()
            );
            users.put(newId, newUser);
            usersByUsername.put(newUser.getUsername(), newUser);
            usersByEmail.put(newUser.getEmail().getValue(), newUser);
            return newUser;
        } else {
            // Update existing user
            users.put(user.getId(), user);
            usersByUsername.put(user.getUsername(), user);
            usersByEmail.put(user.getEmail().getValue(), user);
            return user;
        }
    }
    
    @Override
    public synchronized void delete(User user) {
        users.remove(user.getId());
        usersByUsername.remove(user.getUsername());
        usersByEmail.remove(user.getEmail().getValue());
    }
    
    @Override
    public synchronized void deleteById(UserID userId) {
        User user = users.get(userId);
        if (user != null) {
            delete(user);
        }
    }
    
    @Override
    public synchronized List<User> findAll() {
        return users.values().stream().collect(Collectors.toList());
    }
    
    @Override
    public synchronized boolean existsById(UserID userId) {
        return users.containsKey(userId);
    }
    
    @Override
    public synchronized long count() {
        return users.size();
    }
    
    @Override
    public synchronized void deleteAll() {
        users.clear();
        usersByUsername.clear();
        usersByEmail.clear();
        nextId = 1L;
    }
    
    // Additional methods for test compatibility
    
    /**
     * Get user count for testing
     */
    public synchronized int getUserCount() {
        return users.size();
    }
    
    /**
     * Print all users for debugging
     */
    public synchronized void printAllUsers() {
        System.out.println("\n=== Current Users in Repository ===");
        for (User user : users.values()) {
            System.out.printf("ID: %s, Username: %s, Role: %s, Email: %s, Active: %s%n",
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getRole(),
                user.getEmail().getValue(),
                user.isActive()
            );
        }
        System.out.println("=================================\n");
    }
    
    /**
     * Clear all data for testing purposes
     */
    public synchronized void clear() {
        deleteAll();
        initializeDefaultUsers();
    }
}
