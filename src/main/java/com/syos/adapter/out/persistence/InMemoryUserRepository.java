package com.syos.adapter.out.persistence;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of UserRepository
 * For development and testing purposes
 */
public class InMemoryUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<Long, User> usersById = new HashMap<>();
    private long nextId = 1L;

    public InMemoryUserRepository() {
        initializeDefaultUsers();
    }

    /**
     * Initialize with default users for testing
     */
    protected void initializeDefaultUsers() {
        try {
            // Admin user
            User admin = createDefaultUser(
                1L,
                "admin",
                Password.hash("admin12345"),
                "System Administrator",
                "admin@syos.com",
                UserRole.ADMIN,
                0.0
            );
            saveUserDirectly(admin);
            
            // Employee user
            User employee = createDefaultUser(
                2L,
                "employee",
                Password.hash("employee123"),
                "John Employee",
                "employee@syos.com",
                UserRole.EMPLOYEE,
                0.0
            );
            saveUserDirectly(employee);
            
            // Customer user
            User customer = createDefaultUser(
                3L,
                "customer",
                Password.hash("customer123"),
                "Jane Customer",
                "customer@example.com",
                UserRole.CUSTOMER,
                150.0
            );
            saveUserDirectly(customer);
            
            nextId = 4L;
            logger.info("Initialized default users: admin, employee, customer");
            
        } catch (Exception e) {
            logger.error("Error initializing default users", e);
        }
    }

    private User createDefaultUser(long id, String username, Password hashedPassword, 
                                  String name, String email, UserRole role, double points) {
        LocalDateTime now = LocalDateTime.now();
        
        return User.withId(
            UserID.of(id),
            Username.of(username),
            hashedPassword,
            role,
            Name.of(name),
            Email.of(email),
            SynexPoints.of(BigDecimal.valueOf(points)),
            ActiveStatus.active(),
            CreatedAt.of(now),
            UpdatedAt.of(now),
            null, // createdBy
            MemberSince.of(now)
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("findByUsername called with null or empty username");
            return Optional.empty();
        }
        
        User user = usersByUsername.get(username.toLowerCase());
        if (user != null) {
            logger.debug("Found user by username: {}", username);
        } else {
            logger.debug("User not found by username: {}", username);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("existsByUsername called with null or empty username");
            return false;
        }
        
        boolean exists = usersByUsername.containsKey(username.toLowerCase());
        logger.debug("Username '{}' exists: {}", username, exists);
        return exists;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("existsByEmail called with null or empty email");
            return false;
        }
        
        boolean exists = usersByEmail.containsKey(email.toLowerCase());
        logger.debug("Email '{}' exists: {}", email, exists);
        return exists;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            logger.error("Cannot save null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        User savedUser;
        // If user doesn't have an ID, generate one and create a new user instance with the ID
        if (user.getId() == null) {
            long newId = nextId++;
            savedUser = user.withId(UserID.of(newId));
            saveUserDirectly(savedUser);
            logger.info("Saved new user: {} with ID: {} and role: {}", 
                savedUser.getUsername().getValue(), newId, savedUser.getRole());
        } else {
            savedUser = user;
            saveUserDirectly(savedUser);
            logger.info("Updated existing user: {} with role: {}", 
                savedUser.getUsername().getValue(), savedUser.getRole());
        }
        
        return savedUser;
    }
    
    private void saveUserDirectly(User user) {
        if (user == null || user.getUsername() == null || user.getEmail() == null) {
            logger.error("Cannot save user: user or required fields are null");
            return;
        }
        
        String username = user.getUsername().getValue().toLowerCase();
        String email = user.getEmail().getValue().toLowerCase();
        
        // Remove old mappings if updating existing user
        if (user.getId() != null) {
            User existingUser = usersById.get(user.getId().getValue());
            if (existingUser != null) {
                usersByUsername.remove(existingUser.getUsername().getValue().toLowerCase());
                usersByEmail.remove(existingUser.getEmail().getValue().toLowerCase());
            }
        }
        
        // Add new mappings
        usersByUsername.put(username, user);
        usersByEmail.put(email, user);
        if (user.getId() != null) {
            usersById.put(user.getId().getValue(), user);
        }
        
        logger.debug("User mappings updated - Username: {}, Email: {}, ID: {}", 
            username, email, user.getId() != null ? user.getId().getValue() : "null");
    }
    
    public int getUserCount() {
        return usersById.size();
    }
    
    public void printAllUsers() {
        logger.info("=== All Users in Repository ===");
        logger.info("Total users: {}", usersById.size());
        for (User user : usersById.values()) {
            logger.info("ID: {}, Username: {}, Email: {}, Role: {}", 
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getRole());
        }
        logger.info("============================");
    }
}