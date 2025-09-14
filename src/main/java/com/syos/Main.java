package com.syos;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Main entry point for SYOS Console Application
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting SYOS Console Application");
        // Ensure log directory exists based on LOG_HOME or default 'logs'
        try {
            String logHome = System.getProperty("LOG_HOME", "logs");
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(logHome));
            logger.info("Log directory: {}", java.nio.file.Paths.get(logHome).toAbsolutePath());
        } catch (Exception ex) {
            logger.warn("Could not ensure log directory exists", ex);
        }
        
        try {
            // Initialize infrastructure
            ConsoleIO console = new StandardConsoleIO();
            UserRepository userRepository = new InMemoryUserRepository();
            
            // Initialize use cases
            LoginUseCase loginUseCase = new LoginUseCase(userRepository);
            RegisterCustomerUseCase registerUseCase = new RegisterCustomerUseCase(userRepository);
            
            // Initialize menu system
            MenuNavigator navigator = new MenuNavigator(console);
            MenuFactory menuFactory = new MenuFactory(console, navigator, loginUseCase, registerUseCase);
            
            // Display welcome banner
            displayWelcomeBanner(console);
            
            // Log initial information (no console output)
            logInitialInfo();
            
            // Start application with the main menu
            navigator.start(menuFactory.createMainMenu());
            
            logger.info("SYOS Console Application terminated normally");
            
        } catch (Exception e) {
            logger.error("Fatal application error", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void displayWelcomeBanner(ConsoleIO console) {
        try {
            InputStream inputStream = Main.class.getResourceAsStream("/static/banner.txt");
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    console.println(line);
                }
                reader.close();
            } else {
                // Fallback banner if file not found
                displayFallbackBanner(console);
            }
        } catch (IOException e) {
            logger.error("Error reading banner file", e);
            displayFallbackBanner(console);
        }
    }

    private static void displayFallbackBanner(ConsoleIO console) {
        console.println("\n╔════════════════════════════════════════════════════════╗");
        console.println("║                                                        ║");
        console.println("║          WELCOME TO SYNEX OUTLET STORE                ║");
        console.println("║            77 Hortan Pl, Colombo 07                   ║");
        console.println("║                                                        ║");
        console.println("║            A System by Hannanlk                       ║");
        console.println("║                                                        ║");
        console.println("╚════════════════════════════════════════════════════════╝");
    }

    private static void logInitialInfo() {
        // Log system readiness and initialization details instead of printing to console
        logger.info("System ready. Initializing components...");
        logger.info("User repository initialized: InMemoryUserRepository");
        logger.info("Menu system initialized");
        // If/when database initialization is added, log success here instead of printing to console.
        // Example: logger.info("Database initialized successfully");
    }
}