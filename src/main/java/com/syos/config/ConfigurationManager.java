package com.syos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Manager for SYOS Application
 * 
 * Loads and manages application configuration from properties files.
 * Integrates with the logging configuration that was previously unused.
 */
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private final Properties properties;
    
    public ConfigurationManager() {
        this.properties = new Properties();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        try {
            // Load main application properties
            loadPropertiesFile("/config/application.properties");
            
            // Set system properties for logging
            setLoggingSystemProperties();
            
            logger.info("Configuration loaded successfully");
            logger.info("Application Environment: {}", getProperty("app.environment", "production"));
            logger.info("Console Logging Enabled: {}", getProperty("logging.console.enabled", "false"));
            
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            // Set minimal defaults
            properties.setProperty("app.environment", "production");
            properties.setProperty("logging.console.enabled", "false");
        }
    }
    
    private void loadPropertiesFile(String resourcePath) {
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            if (input != null) {
                properties.load(input);
                logger.debug("Loaded properties from: {}", resourcePath);
            } else {
                logger.warn("Properties file not found: {}", resourcePath);
            }
        } catch (IOException e) {
            logger.error("Error loading properties from: " + resourcePath, e);
        }
    }
    
    private void setLoggingSystemProperties() {
        // Set system properties that logback.xml can read
        String logPath = getProperty("logging.path.base", "logs");
        String consoleLogging = getProperty("logging.console.enabled", "false");
        String appEnv = getProperty("app.environment", "production");
        
        System.setProperty("LOG_HOME", logPath);
        System.setProperty("CONSOLE_LOGGING", consoleLogging);
        System.setProperty("APP_ENV", appEnv);
        
        // Create log directory if it doesn't exist
        try {
            java.nio.file.Path logDir = java.nio.file.Paths.get(logPath);
            if (!java.nio.file.Files.exists(logDir)) {
                java.nio.file.Files.createDirectories(logDir);
                logger.info("Created log directory: {}", logDir.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.warn("Could not create log directory: {}", logPath, e);
        }
        
        logger.debug("Set logging system properties - Path: {}, Console: {}, Environment: {}", 
                    logPath, consoleLogging, appEnv);
    }
    
    /**
     * Get property value with default fallback
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get boolean property value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Get integer property value
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    /**
     * Resolve placeholders of form ${VAR:default} using precedence: JVM -D, then ENV, then default.
     */
    private String resolvePlaceholders(String value) {
        if (value == null || value.isBlank()) return value;
        String result = value;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
        java.util.regex.Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String key = matcher.group(1);
            String def = matcher.group(2);
            String replacement = System.getProperty(key);
            if (replacement == null || replacement.isBlank()) {
                replacement = System.getenv(key);
            }
            if (replacement == null) {
                replacement = def != null ? def : "";
            }
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        if (found) {
            matcher.appendTail(sb);
            return sb.toString();
        }
        return result;
    }
    
    /**
     * Check if application is in development mode
     */
    public boolean isDevelopmentMode() {
        return getBooleanProperty("logging.console.enabled", false) ||
               "development".equalsIgnoreCase(getProperty("app.environment", "production"));
    }
    
    /**
     * Get database configuration
     */
    public DatabaseConfig getDatabaseConfig() {
        return new DatabaseConfig(
            resolvePlaceholders(getProperty("datasource.url", "jdbc:postgresql://localhost:5432/syosdb")),
            resolvePlaceholders(getProperty("datasource.username", "postgres")),
            resolvePlaceholders(getProperty("datasource.password", "")),
            resolvePlaceholders(getProperty("datasource.driver", "org.postgresql.Driver"))
        );
    }
    
    /**
     * Database configuration holder
     */
    public static class DatabaseConfig {
        private final String url;
        private final String username;
        private final String password;
        private final String driver;
        
        public DatabaseConfig(String url, String username, String password, String driver) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driver = driver;
        }
        
        public String getUrl() { return url; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getDriver() { return driver; }
    }
}
