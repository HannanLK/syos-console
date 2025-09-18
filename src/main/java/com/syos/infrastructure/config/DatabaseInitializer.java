package com.syos.infrastructure.config;

import com.syos.application.ports.out.UserRepository;
import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to initialize default users in the database
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final UserRepository userRepository;

    public DatabaseInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void initializeDefaultUsers() {
        logger.info("Initializing default users in database...");
        
        try {
            // Ensure admin exists or create one
            User adminUser = userRepository.findByUsername("admin").orElse(null);
            if (adminUser == null) {
                User newAdmin = User.createAdmin(
                    Name.of("System Administrator"),
                    Username.of("admin"),
                    Email.of("admin@syos.com"),
                    Password.hash("admin12345")
                );
                adminUser = userRepository.save(newAdmin);
                logger.info("Created default admin user");
            }

            // Create employee user if not exists
            if (!userRepository.existsByUsername("employee")) {
                User employee = User.createEmployee(
                    Name.of("John Employee"),
                    Username.of("employee"),
                    Email.of("employee@syos.com"),
                    Password.hash("employee123"),
                    adminUser != null ? adminUser.getId() : null
                );
                userRepository.save(employee);
                logger.info("Created default employee user");
            }
            
            // Create customer user if not exists
            if (!userRepository.existsByUsername("customer")) {
                User customer = User.createCustomer(
                    Username.of("customer"),
                    Email.of("customer@example.com"),
                    Password.hash("customer123")
                );
                // Update name to full name
                customer = customer.updateProfile(Name.of("Jane Customer"), Email.of("customer@example.com"));
                userRepository.save(customer);
                logger.info("Created default customer user");
            }
            
            logger.info("Default users initialization completed");
            
        } catch (Exception e) {
            logger.error("Error initializing default users", e);
            throw new RuntimeException("Failed to initialize default users", e);
        }
    }
}