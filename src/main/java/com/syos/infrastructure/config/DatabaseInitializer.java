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
            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.createWithRole(
                    Username.of("admin"),
                    "admin12345",
                    Name.of("System Administrator"),
                    Email.of("admin@syos.com"),
                    UserRole.ADMIN,
                    null
                );
                userRepository.save(admin);
                logger.info("Created default admin user");
            }
            
            // Create employee user if not exists
            if (!userRepository.existsByUsername("employee")) {
                User employee = User.createWithRole(
                    Username.of("employee"),
                    "employee123",
                    Name.of("John Employee"),
                    Email.of("employee@syos.com"),
                    UserRole.EMPLOYEE,
                    null
                );
                userRepository.save(employee);
                logger.info("Created default employee user");
            }
            
            // Create customer user if not exists
            if (!userRepository.existsByUsername("customer")) {
                User customer = User.createWithRole(
                    Username.of("customer"),
                    "customer123",
                    Name.of("Jane Customer"),
                    Email.of("customer@example.com"),
                    UserRole.CUSTOMER,
                    null
                );
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