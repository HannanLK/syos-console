package com.syos;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.out.persistence.JpaUserRepository;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.infrastructure.config.DatabaseConfig;
import com.syos.infrastructure.config.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point for SYOS Console Application
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final boolean USE_DATABASE = true; // Set to false for in-memory mode

    public static void main(String[] args) {
        logger.info("Starting SYOS Console Application");
        
        // Ensure the log directory exists based on LOG_HOME or default 'logs'
        try {
            String logHome = System.getProperty("LOG_HOME", "logs");
            Path dir = Paths.get(logHome);
            java.nio.file.Files.createDirectories(dir);
            logger.info("Log directory: {}", dir.toAbsolutePath());
        } catch (Exception ex) {
            logger.warn("Could not ensure log directory exists", ex);
        }
        
        UserRepository userRepository = null;
        EntityManagerFactory emf = null;
        
        try {
            // Initialize infrastructure
            ConsoleIO console = new StandardConsoleIO();
            
            if (USE_DATABASE) {
                logger.info("Initializing PostgreSQL database connection...");
                try {
                    emf = DatabaseConfig.getEntityManagerFactory();
                    userRepository = new JpaUserRepository(emf);
                    
                    // Initialize default users in database
                    DatabaseInitializer initializer = new DatabaseInitializer(userRepository);
                    initializer.initializeDefaultUsers();
                    
                    logger.info("PostgreSQL repository initialized successfully");
                } catch (Exception e) {
                    logger.warn("Failed to connect to database, falling back to in-memory storage", e);
                    console.printWarning("Database connection failed. Using in-memory storage for this session.");
                    console.printWarning("Data will not persist after application restart.");
                    userRepository = new InMemoryUserRepository();
                }
            } else {
                logger.info("Using in-memory repository (development mode)");
                userRepository = new InMemoryUserRepository();
            }
            
            // Debug: Print initial repository state
            logger.info("Repository initialized: {}", userRepository.getClass().getSimpleName());
            
            // Initialize use cases
            LoginUseCase loginUseCase = new LoginUseCase(userRepository);
            RegisterCustomerUseCase registerUseCase = new RegisterCustomerUseCase(userRepository);
            
            // Initialize menu system
            MenuNavigator navigator = new MenuNavigator(console);
            MenuFactory menuFactory = new MenuFactory(console, navigator, loginUseCase, registerUseCase, userRepository);
            
            // Display welcome banner
            displayWelcomeBanner(console);
            
            // Show repository type to user
            if (userRepository instanceof JpaUserRepository) {
                console.println("Connected to PostgreSQL database - data will persist permanently!");
            } else {
                console.println("⚠️ Using in-memory storage - data will be lost on restart!");
            }
            console.println();
            
            // Log initial information (no console output)
            logInitialInfo(userRepository);
            
            // Start application with the main menu
            navigator.start(menuFactory.createMainMenu());
            
            logger.info("SYOS Console Application terminated normally");
            
        } catch (Exception e) {
            logger.error("Fatal application error", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        } finally {
            // Clean up database connection
            if (emf != null && emf.isOpen()) {
                DatabaseConfig.closeEntityManagerFactory();
                logger.info("Database connection closed");
            }
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

    private static void logInitialInfo(UserRepository userRepository) {
        // Log system readiness and initialization details instead of printing to console
        logger.info("System ready. Initializing components...");
        logger.info("User repository initialized: {}", userRepository.getClass().getSimpleName());
        logger.info("Menu system initialized");
        
        if (userRepository instanceof JpaUserRepository) {
            logger.info("Database persistence enabled - data will be saved to PostgreSQL");
        } else {
            logger.info("In-memory persistence - data will be lost on application restart");
        }
    }
}