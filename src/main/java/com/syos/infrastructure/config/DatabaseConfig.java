package com.syos.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Database configuration for PostgreSQL
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static EntityManagerFactory entityManagerFactory;
    
    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/syosdb";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "apiit-LV6";
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            try {
                logger.info("Initializing database connection...");
                
                Map<String, String> properties = new HashMap<>();
                properties.put("jakarta.persistence.jdbc.url", DB_URL);
                properties.put("jakarta.persistence.jdbc.user", DB_USERNAME);
                properties.put("jakarta.persistence.jdbc.password", DB_PASSWORD);
                properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
                
                // Hibernate specific properties
                properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                properties.put("hibernate.hbm2ddl.auto", "update"); // Creates tables automatically
                properties.put("hibernate.show_sql", "false"); // Set to true for SQL debugging
                properties.put("hibernate.format_sql", "true");
                
                entityManagerFactory = Persistence.createEntityManagerFactory("syos-persistence-unit", properties);
                
                logger.info("Database connection initialized successfully");
                
                // Test the connection
                testConnection();
                
            } catch (Exception e) {
                logger.error("Failed to initialize database connection", e);
                throw new RuntimeException("Database initialization failed", e);
            }
        }
        return entityManagerFactory;
    }
    
    private static void testConnection() {
        try (var em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            // Simple test query
            em.createNativeQuery("SELECT 1").getSingleResult();
            em.getTransaction().commit();
            logger.info("Database connection test successful");
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            throw new RuntimeException("Database connection test failed", e);
        }
    }
    
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            logger.info("Database connection closed");
        }
    }
}