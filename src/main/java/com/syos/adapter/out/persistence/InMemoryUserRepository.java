package com.syos.adapter.out.persistence;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.security.BCryptPasswordEncoder;
import com.syos.shared.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of UserRepository
 * For development and testing purposes
 */
public class InMemoryUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, User> usersById = new HashMap<>();

    public InMemoryUserRepository() {
        initializeDefaultUsers();
    }

    /**
     * Initialize with default users for testing
     */
    private void initializeDefaultUsers() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        try {
            // Admin user
            User admin = createUser(
                "admin-001",
                "admin",
                encoder.encode("admin123"),
                "System Administrator",
                "admin@syos.com",
                UserRole.ADMIN,
                0.0
            );
            saveUser(admin);
            
            // Employee user
            User employee = createUser(
                "emp-001",
                "employee",
                encoder.encode("emp123"),
                "John Employee",
                "employee@syos.com",
                UserRole.EMPLOYEE,
                0.0
            );
            saveUser(employee);
            
            // Customer user
            User customer = createUser(
                "cust-001",
                "customer",
                encoder.encode("cust123"),
                "Jane Customer",
                "customer@example.com",
                UserRole.CUSTOMER,
                150.0
            );
            saveUser(customer);
            
            logger.info("Initialized default users: admin, employee, customer");
            
        } catch (Exception e) {
            logger.error("Error initializing default users", e);
        }
    }

    private User createUser(String id, String username, String hashedPassword, 
                           String name, String email, UserRole role, double points) {
        // Using reflection to create User with ID for default users
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Get User constructor via reflection
            var constructor = User.class.getDeclaredConstructor(
                UserID.class, Username.class, Password.class, UserRole.class,
                Name.class, Email.class, SynexPoints.class, ActiveStatus.class,
                CreatedAt.class, UpdatedAt.class, UserID.class, MemberSince.class
            );
            constructor.setAccessible(true);
            
            return constructor.newInstance(
                UserID.of(id),
                Username.of(username),
                Password.fromHash(hashedPassword),
                role,
                Name.of(name),
                Email.of(email),
                SynexPoints.of(points),
                ActiveStatus.active(),
                CreatedAt.of(now),
                UpdatedAt.of(now),
                null, // createdBy
                MemberSince.of(now)
            );
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username.toLowerCase()));
    }

    @Override
    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username.toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email.toLowerCase());
    }

    @Override
    public void save(User user) {
        // Generate ID if null (for new users)
        if (user.getId() == null) {
            // Create new user with generated ID using reflection
            try {
                String newId = UUID.randomUUID().toString();
                LocalDateTime now = LocalDateTime.now();
                
                var constructor = User.class.getDeclaredConstructor(
                    UserID.class, Username.class, Password.class, UserRole.class,
                    Name.class, Email.class, SynexPoints.class, ActiveStatus.class,
                    CreatedAt.class, UpdatedAt.class, UserID.class, MemberSince.class
                );
                constructor.setAccessible(true);
                
                user = constructor.newInstance(
                    UserID.of(newId),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getName(),
                    user.getEmail(),
                    user.getSynexPoints(),
                    user.getActiveStatus(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getCreatedBy(),
                    user.getMemberSince()
                );
            } catch (Exception e) {
                logger.error("Error saving user with generated ID", e);
                throw new RuntimeException("Failed to save user", e);
            }
        }
        
        saveUser(user);
        logger.info("Saved user: {} with role: {}", 
            user.getUsername().getValue(), user.getRole());
    }
    
    private void saveUser(User user) {
        String username = user.getUsername().getValue().toLowerCase();
        String email = user.getEmail().getValue().toLowerCase();
        String id = user.getId().getValue();
        
        usersByUsername.put(username, user);
        usersByEmail.put(email, user);
        usersById.put(id, user);
    }
    
    public int getUserCount() {
        return usersById.size();
    }
}