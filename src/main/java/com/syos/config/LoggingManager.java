package com.syos.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Logging Manager for SYOS Application
 * 
 * Properly initializes the logging system using the configuration
 * from application.properties, making the logging configuration
 * actually work instead of being unused.
 */
public class LoggingManager {
    private static boolean initialized = false;
    
    /**
     * Initialize logging system with configuration
     */
    public static void initialize(ConfigurationManager config) {
        if (initialized) {
            return;
        }
        
        try {
            // Configure logback programmatically
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset(); // Clear any default configuration
                
                // Load logback configuration
                InputStream configStream = LoggingManager.class.getResourceAsStream("/logback.xml");
                if (configStream != null) {
                    configurator.doConfigure(configStream);
                    configStream.close();
                } else {
                    // Fallback to programmatic configuration
                    configureProgrammatically(context, config);
                }
                
            } catch (JoranException e) {
                // Fallback to programmatic configuration
                System.err.println("Error configuring logback from XML, using programmatic config: " + e.getMessage());
                configureProgrammatically(context, config);
            }
            
            initialized = true;
            
            // Log initialization success
            Logger logger = LoggerFactory.getLogger(LoggingManager.class);
            logger.info("Logging system initialized successfully");
            
            if (config.isDevelopmentMode()) {
                logger.info("🔧 Development Mode: Console and file logging enabled");
            } else {
                logger.info("📝 Production Mode: File logging only");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Programmatic logging configuration as fallback
     */
    private static void configureProgrammatically(LoggerContext context, ConfigurationManager config) {
        try {
            ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            
            // Create file appender
            ch.qos.logback.core.rolling.RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
                new ch.qos.logback.core.rolling.RollingFileAppender<>();
            fileAppender.setContext(context);
            fileAppender.setName("FILE");
            
            String logPath = config.getProperty("logging.path.base", "logs");
            fileAppender.setFile(logPath + "/syos.log");
            
            // Set up rolling policy
            ch.qos.logback.core.rolling.TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = 
                new ch.qos.logback.core.rolling.TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(fileAppender);
            rollingPolicy.setFileNamePattern(logPath + "/syos.%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(30);
            
            ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP<ch.qos.logback.classic.spi.ILoggingEvent> triggeringPolicy = 
                new ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP<>();
            triggeringPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("50MB"));
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
            
            rollingPolicy.start();
            fileAppender.setRollingPolicy(rollingPolicy);
            
            // Set up encoder
            ch.qos.logback.classic.encoder.PatternLayoutEncoder encoder = new ch.qos.logback.classic.encoder.PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            encoder.start();
            fileAppender.setEncoder(encoder);
            
            fileAppender.start();
            rootLogger.addAppender(fileAppender);
            
            // Add console appender in development mode
            if (config.isDevelopmentMode()) {
                ch.qos.logback.core.ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
                    new ch.qos.logback.core.ConsoleAppender<>();
                consoleAppender.setContext(context);
                consoleAppender.setName("CONSOLE");
                
                ch.qos.logback.classic.encoder.PatternLayoutEncoder consoleEncoder = new ch.qos.logback.classic.encoder.PatternLayoutEncoder();
                consoleEncoder.setContext(context);
                consoleEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n");
                consoleEncoder.start();
                consoleAppender.setEncoder(consoleEncoder);
                
                consoleAppender.start();
                rootLogger.addAppender(consoleAppender);
            }
            
        } catch (Exception e) {
            System.err.println("Error in programmatic logging configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get current logging status
     */
    public static String getLoggingStatus() {
        if (!initialized) {
            return "Not initialized";
        }
        
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        
        StringBuilder status = new StringBuilder();
        status.append("Logging Status: Initialized\n");
        status.append("Root Level: ").append(rootLogger.getLevel()).append("\n");
        status.append("Appenders: ");
        
        rootLogger.iteratorForAppenders().forEachRemaining(appender -> {
            status.append(appender.getName()).append(" ");
        });
        
        return status.toString();
    }
    
    /**
     * Check if logging is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
