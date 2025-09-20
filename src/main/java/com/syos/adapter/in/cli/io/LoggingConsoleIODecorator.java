package com.syos.adapter.in.cli.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for ConsoleIO that adds logging capabilities
 * Implements Decorator Pattern (Pattern #13)
 * 
 * Design Patterns:
 * - Decorator Pattern: Adds logging behavior to ConsoleIO
 * - Proxy Pattern: Acts as proxy for the underlying ConsoleIO
 * 
 * Clean Architecture: Interface Adapters Layer
 */
public class LoggingConsoleIODecorator implements ConsoleIO {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConsoleIODecorator.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("audit");
    
    private final ConsoleIO wrappedConsoleIO;
    private final boolean logUserInputs;
    private final boolean logOutputs;
    
    public LoggingConsoleIODecorator(ConsoleIO consoleIO) {
        this(consoleIO, false, true); // Default: don't log inputs (security), log outputs
    }
    
    public LoggingConsoleIODecorator(ConsoleIO consoleIO, boolean logUserInputs, boolean logOutputs) {
        if (consoleIO == null) {
            throw new IllegalArgumentException("ConsoleIO cannot be null");
        }
        this.wrappedConsoleIO = consoleIO;
        this.logUserInputs = logUserInputs;
        this.logOutputs = logOutputs;
        
        logger.debug("LoggingConsoleIODecorator created - logInputs: {}, logOutputs: {}", 
                    logUserInputs, logOutputs);
    }
    
    @Override
    public void println(String message) {
        if (logOutputs) {
            auditLogger.info("OUTPUT: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.println(message);
    }
    
    @Override
    public void println() {
        if (logOutputs) {
            auditLogger.info("OUTPUT: [empty line]");
        }
        wrappedConsoleIO.println();
    }
    
    @Override
    public void print(String message) {
        if (logOutputs) {
            auditLogger.info("OUTPUT: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.print(message);
    }
    
    @Override
    public String readLine(String prompt) {
        if (logOutputs && prompt != null) {
            auditLogger.info("PROMPT: {}", sanitizeForLogging(prompt));
        }
        
        String input = wrappedConsoleIO.readLine(prompt);
        
        if (logUserInputs && input != null) {
            // For security, only log non-sensitive inputs
            if (!isSensitivePrompt(prompt)) {
                auditLogger.info("INPUT: {}", sanitizeForLogging(input));
            } else {
                auditLogger.info("INPUT: [SENSITIVE DATA HIDDEN]");
            }
        }
        
        return input;
    }
    
    @Override
    public String readLine() {
        String input = wrappedConsoleIO.readLine();
        
        if (logUserInputs && input != null) {
            auditLogger.info("INPUT: {}", sanitizeForLogging(input));
        }
        
        return input;
    }
    
    @Override
    public String readPassword() {
        String password = wrappedConsoleIO.readPassword();
        
        // Never log actual passwords, only the fact that password input occurred
        if (logUserInputs) {
            auditLogger.info("PASSWORD_INPUT: [PASSWORD LENGTH: {}]",
                    password != null ? password.length() : 0);
        }
        
        return password;
    }
    
    @Override
    public void printSuccess(String message) {
        if (logOutputs) {
            auditLogger.info("SUCCESS: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.printSuccess(message);
    }
    
    @Override
    public void printError(String message) {
        if (logOutputs) {
            auditLogger.warn("ERROR: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.printError(message);
    }
    
    @Override
    public void printWarning(String message) {
        if (logOutputs) {
            auditLogger.warn("WARNING: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.printWarning(message);
    }
    
    @Override
    public void printInfo(String message) {
        if (logOutputs) {
            auditLogger.info("INFO: {}", sanitizeForLogging(message));
        }
        wrappedConsoleIO.printInfo(message);
    }
    
    @Override
    public void printf(String format, Object... args) {
        String formatted;
        try {
            formatted = String.format(format, args);
        } catch (Exception e) {
            formatted = format;
        }
        if (logOutputs) {
            auditLogger.info("OUTPUT: {}", sanitizeForLogging(formatted));
        }
        wrappedConsoleIO.printf(format, args);
    }
    
    @Override
    public void clear() {
        if (logOutputs) {
            auditLogger.info("CONSOLE: Screen cleared");
        }
        wrappedConsoleIO.clear();
    }
    
    /**
     * Sanitize message for logging by removing potential sensitive information
     */
    private String sanitizeForLogging(String message) {
        if (message == null) {
            return "[null]";
        }
        
        // Remove potential sensitive information patterns
        String sanitized = message
            .replaceAll("(?i)password[\\s:=]*[^\\s]+", "password: [HIDDEN]")
            .replaceAll("(?i)token[\\s:=]*[^\\s]+", "token: [HIDDEN]")
            .replaceAll("(?i)secret[\\s:=]*[^\\s]+", "secret: [HIDDEN]")
            .replaceAll("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\b", "[CARD_NUMBER_HIDDEN]")
            .replaceAll("\\b\\d{3}-\\d{2}-\\d{4}\\b", "[SSN_HIDDEN]");
        
        // Truncate very long messages
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 497) + "...";
        }
        
        return sanitized;
    }
    
    /**
     * Check if prompt is asking for sensitive information
     */
    private boolean isSensitivePrompt(String prompt) {
        if (prompt == null) {
            return false;
        }
        
        String lowerPrompt = prompt.toLowerCase();
        return lowerPrompt.contains("password") ||
               lowerPrompt.contains("pin") ||
               lowerPrompt.contains("secret") ||
               lowerPrompt.contains("token") ||
               lowerPrompt.contains("credit") ||
               lowerPrompt.contains("ssn");
    }
    
    /**
     * Get the wrapped ConsoleIO instance
     * Useful for unwrapping decorators if needed
     */
    public ConsoleIO getWrappedConsoleIO() {
        return wrappedConsoleIO;
    }
    
    /**
     * Check if this decorator is logging user inputs
     */
    public boolean isLoggingUserInputs() {
        return logUserInputs;
    }
    
    /**
     * Check if this decorator is logging outputs
     */
    public boolean isLoggingOutputs() {
        return logOutputs;
    }
    
    /**
     * Create a new decorator with different logging settings
     */
    public LoggingConsoleIODecorator withLogging(boolean logInputs, boolean logOutputs) {
        return new LoggingConsoleIODecorator(this.wrappedConsoleIO, logInputs, logOutputs);
    }
}
