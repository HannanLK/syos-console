package com.syos.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Database configuration for PostgreSQL with better error handling and validation
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static EntityManagerFactory entityManagerFactory;
    private static boolean connectionTested = false;
    
    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/syosdb";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "apiit-LV6";
    private static final String DB_DRIVER = "org.postgresql.Driver";
    
    /**
     * Get or create EntityManagerFactory with comprehensive error handling
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            try {
                logger.info("Initializing database connection to: {}", DB_URL);
                
                // First validate database connectivity at JDBC level
                validateDatabaseConnection();
                
                // Create JPA properties
                Map<String, String> properties = createJpaProperties();
                
                // Create EntityManagerFactory
                entityManagerFactory = Persistence.createEntityManagerFactory("syos-persistence-unit", properties);
                
                logger.info("EntityManagerFactory created successfully");
                
                // Test JPA-level connection
                testJpaConnection();
                connectionTested = true;
                
                logger.info("Database connection fully initialized and tested");
                
            } catch (Exception e) {
                logger.error("Failed to initialize database connection: {}", e.getMessage(), e);
                
                // Enhanced error reporting
                if (e.getMessage().contains("database \"syosdb\" does not exist")) {
                    logger.error("Database 'syosdb' does not exist. Please:");
                    logger.error("1. Run the database-setup.sql script as superuser");
                    logger.error("2. Or manually create database: CREATE DATABASE syosdb;");
                    logger.error("3. Then run Flyway migrations: mvn flyway:migrate");
                } else if (e.getMessage().contains("Connection refused")) {
                    logger.error("PostgreSQL server is not running or not accessible at localhost:5432");
                    logger.error("Please ensure PostgreSQL is running and accessible");
                } else if (e.getMessage().contains("authentication failed")) {
                    logger.error("Authentication failed for user '{}'", DB_USERNAME);
                    logger.error("Please check username and password in configuration");
                }
                
                throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
            }
        }
        return entityManagerFactory;
    }
    
    /**
     * Validate database connection at JDBC level before creating JPA factory
     */
    private static void validateDatabaseConnection() throws SQLException {
        logger.debug("Validating JDBC connection...");
        
        try {
            // Load PostgreSQL driver
            Class.forName(DB_DRIVER);
            
            // Test basic connectivity
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                if (conn.isValid(5)) { // 5 second timeout
                    logger.debug("JDBC connection validation successful");
                } else {
                    throw new SQLException("JDBC connection validation failed");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
    }
    
    /**
     * Create JPA properties with production-ready settings
     */
    private static Map<String, String> createJpaProperties() {
        Map<String, String> properties = new HashMap<>();
        
        // Basic connection properties
        properties.put("jakarta.persistence.jdbc.url", DB_URL);
        properties.put("jakarta.persistence.jdbc.user", DB_USERNAME);
        properties.put("jakarta.persistence.jdbc.password", DB_PASSWORD);
        properties.put("jakarta.persistence.jdbc.driver", DB_DRIVER);
        
        // Hibernate dialect and schema management
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate"); // Only validate, don't create/update
        properties.put("hibernate.default_schema", "public");
        
        // SQL logging (development)
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "false");
        properties.put("hibernate.highlight_sql", "false");
        
        // Performance optimization
        properties.put("hibernate.jdbc.batch_size", "25");
        properties.put("hibernate.jdbc.fetch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        properties.put("hibernate.id.new_generator_mappings", "true");
        
        // Connection pool settings (HikariCP)
        properties.put("hibernate.connection.provider_class", "org.hibernate.hikari.internal.HikariCPConnectionProvider");
        properties.put("hibernate.hikari.minimumIdle", "5");
        properties.put("hibernate.hikari.maximumPoolSize", "20");
        properties.put("hibernate.hikari.idleTimeout", "300000");
        properties.put("hibernate.hikari.connectionTimeout", "20000");
        properties.put("hibernate.hikari.maxLifetime", "1200000");
        properties.put("hibernate.hikari.poolName", "SYOSHikariPool");
        
        // Transaction and session management
        properties.put("hibernate.connection.autocommit", "false");
        properties.put("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_HOLD");
        
        // Timezone and encoding
        properties.put("hibernate.jdbc.time_zone", "Asia/Colombo");
        
        // Validation
        properties.put("jakarta.persistence.validation.mode", "AUTO");
        properties.put("hibernate.check_nullability", "true");
        
        return properties;
    }
    
    /**
     * Test JPA-level connection with actual entity operations
     */
    private static void testJpaConnection() {
        logger.debug("Testing JPA connection...");
        
        try (var em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            
            // Test basic database connectivity
            Object result = em.createNativeQuery("SELECT 1 as test_connection").getSingleResult();
            logger.debug("Basic connectivity test result: {}", result);
            
            // Test schema access
            Object schemaTest = em.createNativeQuery(
                "SELECT schemaname FROM pg_tables WHERE tablename = 'users' LIMIT 1"
            ).getSingleResult();
            logger.debug("Schema access test result: {}", schemaTest);
            
            em.getTransaction().commit();
            logger.debug("JPA connection test completed successfully");
            
        } catch (Exception e) {
            logger.error("JPA connection test failed", e);
            throw new RuntimeException("JPA connection test failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if database connection has been tested and validated
     */
    public static boolean isConnectionTested() {
        return connectionTested && entityManagerFactory != null && entityManagerFactory.isOpen();
    }
    
    /**
     * Get database connection URL for informational purposes
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }
    
    /**
     * Close EntityManagerFactory and clean up resources
     */
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            try {
                entityManagerFactory.close();
                connectionTested = false;
                logger.info("Database connection closed successfully");
            } catch (Exception e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
    
    /**
     * Force re-initialization of database connection (for testing)
     */
    public static void reset() {
        closeEntityManagerFactory();
        entityManagerFactory = null;
        connectionTested = false;
        logger.info("Database configuration reset");
    }
}