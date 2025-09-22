package com.syos;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.out.persistence.JpaUserRepository;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.ports.out.*;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.infrastructure.config.DatabaseConfig;
import com.syos.infrastructure.config.DatabaseInitializer;
import com.syos.infrastructure.persistence.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Enhanced Main entry point for SYOS Console Application
 * Now includes complete product management workflow
 */
public class Main {
    // Ensure logging system properties are set BEFORE any logger is initialized
    static {
        // Set logging configuration explicitly
        System.setProperty("logging.config", "classpath:logging/logback.xml");
        System.setProperty("LOG_HOME", "D:/4th_final/sem1/clean_cod/syos/syos-console/logs");
        // Set environment-based console logging
        String environment = System.getProperty("APP_ENV", "production");
        if ("development".equals(environment)) {
            System.setProperty("CONSOLE_LOGGING", "true");
        } else {
            System.setProperty("CONSOLE_LOGGING", "false");
        }
        
        // Suppress all Hibernate debug logging in production
        if (!"development".equals(environment)) {
            System.setProperty("hibernate.show_sql", "false");
            System.setProperty("hibernate.format_sql", "false");
            System.setProperty("hibernate.use_sql_comments", "false");
            System.setProperty("org.hibernate.SQL", "WARN");
            System.setProperty("org.hibernate.orm.connections.pooling", "WARN");
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final boolean USE_DATABASE = true; // Set to false for in-memory mode

    // Repository fields
    private static UserRepository userRepository = null;
    private static ItemMasterFileRepository itemRepository = null;
    private static BrandRepository brandRepository = null;
    private static CategoryRepository categoryRepository = null;
    private static SupplierRepository supplierRepository = null;
    private static BatchRepository batchRepository = null;
    private static WarehouseStockRepository warehouseStockRepository = null;
    private static ShelfStockRepository shelfStockRepository = null;
    private static WebInventoryRepository webInventoryRepository = null;

    public static void main(String[] args) {
        // Set logging configuration explicitly
        System.setProperty("logging.config", "classpath:logging/logback.xml");
        System.setProperty("LOG_HOME", "D:/4th_final/sem1/clean_cod/syos/syos-console/logs");
        
        // Determine environment from system property or default to production
        String environment = System.getProperty("APP_ENV", "production");
        
        // Set environment-based console logging
        if ("development".equals(environment)) {
            System.setProperty("CONSOLE_LOGGING", "true");
        } else {
            System.setProperty("CONSOLE_LOGGING", "false");
        }
        
        // Suppress all Hibernate debug logging in production
        if (!"development".equals(environment)) {
            System.setProperty("hibernate.show_sql", "false");
            System.setProperty("hibernate.format_sql", "false");
            System.setProperty("hibernate.use_sql_comments", "false");
            System.setProperty("org.hibernate.SQL", "WARN");
            System.setProperty("org.hibernate.orm.connections.pooling", "WARN");
        }

        logger.info("Starting SYOS Console Application with Enhanced Product Management - Environment: {}", environment);
        
        // Ensure the log directory exists
        try {
            Path dir = Paths.get("D:/4th_final/sem1/clean_cod/syos/syos-console/logs");
            java.nio.file.Files.createDirectories(dir);
            logger.info("Log directory: {}", dir.toAbsolutePath());
        } catch (Exception ex) {
            logger.warn("Could not ensure log directory exists", ex);
        }
        
        EntityManagerFactory emf = null;
        
        try {
            // Initialize infrastructure
            ConsoleIO baseConsoleIO = new StandardConsoleIO();
            
            // Wrap with logging decorator in development mode
            ConsoleIO console = "development".equals(environment) 
                ? new com.syos.adapter.in.cli.io.LoggingConsoleIODecorator(baseConsoleIO, false, true)
                : baseConsoleIO;
                
            // Initialize Event Bus for domain events
            com.syos.application.services.EventBus eventBus = 
                com.syos.application.services.EventBus.getInstance();
            logger.info("Event Bus initialized for domain event publishing");
            
            if (USE_DATABASE) {
                logger.info("Initializing PostgreSQL database connection...");
                try {
                    emf = DatabaseConfig.getEntityManagerFactory();
                    EntityManager em = emf.createEntityManager();
                    
                    // Initialize all repositories
                    userRepository = new JpaUserRepository(emf); // JpaUserRepository manages its own EMs
                    itemRepository = new JpaItemMasterFileRepository(em);
                    brandRepository = new JpaBrandRepository(em);
                    categoryRepository = new JpaCategoryRepository(em);
                    supplierRepository = new JpaSupplierRepository(em);
                    batchRepository = new JpaBatchRepository(emf);
                    // Use JPA warehouse repository so initial stock persists and is visible in Warehouse view
                    warehouseStockRepository = new com.syos.infrastructure.persistence.repositories.JpaWarehouseStockRepository(emf);
                    shelfStockRepository = createInMemoryShelfStockRepository(); // Placeholder
                    webInventoryRepository = createInMemoryWebInventoryRepository(); // Placeholder
                    
                    // Initialize default users and reference data in database
                    DatabaseInitializer initializer = new DatabaseInitializer(userRepository);
                    initializer.initializeDefaultUsers();
                    
                    console.printSuccess("🗄️ Connected to PostgreSQL database - data will persist permanently!");
                    logger.info("PostgreSQL repositories initialized successfully");
                    
                } catch (Exception e) {
                    logger.error("Failed to connect to database. Application cannot proceed without a persistent store.", e);
                    console.printError("ERROR: Could not connect to the PostgreSQL database. Please ensure the DB is running and configuration is correct (" + DatabaseConfig.getDatabaseUrl() + ").");
                    console.printError("Application will now exit to prevent data loss. Fix the DB connection and restart.");
                    // Terminate to avoid misleading 'success' on in-memory fallback
                    if (emf != null && emf.isOpen()) {
                        DatabaseConfig.closeEntityManagerFactory();
                    }
                    System.exit(2);
                }
            } else {
                logger.info("Using in-memory repository (development mode)");
                console.printWarning("⚠️ Using in-memory storage - data will be lost on restart!");
                initializeInMemoryRepositories();
            }
            
            // Validate repositories are initialized
            if (userRepository == null) {
                throw new IllegalStateException("User repository not initialized");
            }
            
            // Initialize session manager
            SessionManager sessionManager = SessionManager.getInstance();
            
            // Initialize use cases
            LoginUseCase loginUseCase = new LoginUseCase(userRepository);
            RegisterCustomerUseCase registerUseCase = new RegisterCustomerUseCase(userRepository);
            
            // Initialize complete product management use case
            CompleteProductManagementUseCase productManagementUseCase = new CompleteProductManagementUseCase(
                itemRepository,
                brandRepository,
                categoryRepository,
                supplierRepository,
                batchRepository,
                warehouseStockRepository,
                shelfStockRepository,
                webInventoryRepository
            );
            
            // Initialize menu system with product management
            MenuNavigator navigator = new MenuNavigator(console);

            // Add Product command dependencies
            com.syos.application.usecases.inventory.AddProductUseCase addProductUseCase =
                new com.syos.application.usecases.inventory.AddProductUseCase(
                    itemRepository, brandRepository, categoryRepository, supplierRepository);

            // Reporting repositories (read-only)
            com.syos.application.ports.out.TransactionReportRepository transactionReportRepository = new com.syos.infrastructure.persistence.repositories.JpaTransactionReportRepository(emf);
            com.syos.application.ports.out.BillReportRepository billReportRepository = new com.syos.infrastructure.persistence.repositories.JpaBillReportRepository(emf);

            // Initialize promotions and discount service
            com.syos.infrastructure.persistence.repositories.JpaPromotionRepository promoRepo = new com.syos.infrastructure.persistence.repositories.JpaPromotionRepository(emf);
            com.syos.application.services.DiscountService discountService = new com.syos.application.services.DiscountService(promoRepo);
            com.syos.infrastructure.persistence.repositories.JpaPOSRepository posRepository = new com.syos.infrastructure.persistence.repositories.JpaPOSRepository(emf);

            MenuFactory menuFactory = new MenuFactory(
                console,
                navigator,
                loginUseCase,
                registerUseCase,
                userRepository,
                addProductUseCase,
                brandRepository,
                categoryRepository,
                supplierRepository,
                sessionManager,
                itemRepository,
                webInventoryRepository,
                warehouseStockRepository,
                shelfStockRepository,
                productManagementUseCase,
                batchRepository,
                transactionReportRepository,
                billReportRepository,
                discountService,
                posRepository,
                promoRepo
            );
            
            // Display welcome banner
            displayWelcomeBanner(console);
            
            // Show repository type to user
            if (userRepository instanceof JpaUserRepository) {
                console.println("🗄️ Connected to PostgreSQL database - data will persist permanently!");
            } else {
                console.println("⚠️ Using in-memory storage - data will be lost on restart!");
            }
            console.println();
            
            // Log initial information
            logInitialInfo(userRepository);
            
            // Start application with the main menu
            navigator.start(menuFactory.createMainMenu());
            
            logger.info("SYOS Console Application terminated normally");
            
        } catch (Exception e) {
            logger.error("Fatal application error", e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up database connection
            if (emf != null && emf.isOpen()) {
                DatabaseConfig.closeEntityManagerFactory();
                logger.info("Database connection closed");
            }
        }
    }

    private static void initializeInMemoryRepositories() {
        logger.info("Initializing in-memory repositories...");
        
        // Initialize all in-memory repositories
        userRepository = new InMemoryUserRepository();
        itemRepository = new com.syos.adapter.out.persistence.memory.InMemoryItemMasterFileRepository();
        brandRepository = new com.syos.adapter.out.persistence.memory.InMemoryBrandRepository();
        categoryRepository = new com.syos.adapter.out.persistence.memory.InMemoryCategoryRepository();
        supplierRepository = new com.syos.adapter.out.persistence.memory.InMemorySupplierRepository();
        batchRepository = new com.syos.adapter.out.persistence.memory.InMemoryBatchRepository();
        warehouseStockRepository = new com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository();
        shelfStockRepository = new com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository();
        webInventoryRepository = new com.syos.adapter.out.persistence.memory.InMemoryWebInventoryRepository();
        
        logger.info("In-memory repositories initialized successfully");
    }

    private static ShelfStockRepository createInMemoryShelfStockRepository() {
        // Placeholder - would need actual implementation
        logger.warn("Using placeholder for ShelfStockRepository");
        return new com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository();
    }

    private static WebInventoryRepository createInMemoryWebInventoryRepository() {
        // Placeholder - would need actual implementation  
        logger.warn("Using placeholder for WebInventoryRepository");
        return new com.syos.adapter.out.persistence.memory.InMemoryWebInventoryRepository();
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
        console.println("║         🚀 Enhanced Product Management                 ║");
        console.println("║                                                        ║");
        console.println("╚════════════════════════════════════════════════════════╝");
    }

    private static void logInitialInfo(UserRepository userRepository) {
        // Log system readiness and initialization details
        logger.info("System ready. Enhanced SYOS Console Application initialized.");
        logger.info("User repository initialized: {}", userRepository.getClass().getSimpleName());
        logger.info("Product management system initialized");
        logger.info("Menu system with enhanced product workflow initialized");
        
        if (userRepository instanceof JpaUserRepository) {
            logger.info("Database persistence enabled - data will be saved to PostgreSQL");
        } else {
            logger.info("In-memory persistence - data will be lost on application restart");
        }

    }

    /**
     * Enhanced Menu Factory that includes product management
     */
}
