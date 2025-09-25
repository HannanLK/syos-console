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
            // Ensure admin (username 1000) exists or create one
            User adminUser = userRepository.findByUsername("1000").orElse(null);
            if (adminUser == null) {
                User newAdmin = User.createAdmin(
                    Name.of("System Admin"),
                    Username.of("1000"),
                    Email.of("admin@syos.lk"),
                    Password.hash("1qaz!QAZ")
                );
                adminUser = userRepository.save(newAdmin);
                logger.info("Created default admin user");
            }

            // Create employee user if not exists
            if (!userRepository.existsByUsername("3033")) {
                User employee = User.createEmployee(
                    Name.of("Test Employee"),
                    Username.of("3033"),
                    Email.of("3033@syos.lk"),
                    Password.hash("1qaz!QAZ"),
                    adminUser != null ? adminUser.getId() : null
                );
                userRepository.save(employee);
                logger.info("Created default employee user");
            }
            
            // Create customer user if not exists
            if (!userRepository.existsByUsername("2303")) {
                User customer = User.createCustomer(
                    Username.of("2303"),
                    Email.of("2303@tsyos.lk"),
                    Password.hash("1qaz!QAZ")
                );
                // Update name to full name
                customer = customer.updateProfile(Name.of("Test Customer"), Email.of("customer@syos.com"));
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