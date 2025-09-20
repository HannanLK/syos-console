package com.syos.config;

import com.syos.adapter.in.cli.controllers.*;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.adapter.out.persistence.memory.*;
import com.syos.application.ports.in.AuthenticationPort;
import com.syos.application.ports.in.UserManagementPort;
import com.syos.application.ports.out.*;
import com.syos.application.usecases.auth.AuthenticationUseCase;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.application.usecases.user.UserManagementUseCase;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.auth.CreateEmployeeUseCase;
import com.syos.infrastructure.security.BCryptPasswordEncoder;
import com.syos.infrastructure.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service Container - Dependency Injection Container
 * 
 * Implements Factory and Dependency Injection patterns.
 * Follows Single Responsibility Principle - only handles object creation and wiring.
 * Follows Open/Closed Principle - easily extensible for new services.
 */
public class ServiceContainer {
    private static final Logger logger = LoggerFactory.getLogger(ServiceContainer.class);
    
    // Core Infrastructure
    private final ConfigurationManager config;
    private final PasswordEncoder passwordEncoder;
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    
    // Repositories
    private final UserRepository userRepository;
    private final ItemMasterFileRepository itemRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    
    // Application Services (Ports)
    private final AuthenticationPort authenticationService;
    private final UserManagementPort userManagementService;
    private final AddProductUseCase addProductUseCase;
    
    // Controllers
    private final AuthenticationController authController;
    private final AdminController adminController;
    private final EmployeeController employeeController;
    private final CustomerController customerController;

    public ServiceContainer(ConfigurationManager config) {
        this.config = config;
        logger.info("Initializing Service Container...");
        
        // Initialize infrastructure components
        this.passwordEncoder = createPasswordEncoder();
        this.console = createConsole();
        this.sessionManager = createSessionManager();
        
        // Initialize repositories
        this.userRepository = createUserRepository();
        this.itemRepository = createItemRepository();
        this.brandRepository = createBrandRepository();
        this.categoryRepository = createCategoryRepository();
        this.supplierRepository = createSupplierRepository();
        
        // Initialize application services
        this.authenticationService = createAuthenticationService();
        this.userManagementService = createUserManagementService();
        this.addProductUseCase = createAddProductUseCase();
        
        // Initialize controllers
        this.authController = createAuthController();
        this.adminController = createAdminController();
        this.employeeController = createEmployeeController();
        this.customerController = createCustomerController();
        
        logger.info("Service Container initialized successfully");
    }

    // Factory methods for Infrastructure

    private PasswordEncoder createPasswordEncoder() {
        int strength = config.getIntProperty("security.password.bcrypt.strength", 12);
        return new BCryptPasswordEncoder(strength);
    }

    private ConsoleIO createConsole() {
        return new StandardConsoleIO();
    }

    private SessionManager createSessionManager() {
        return SessionManager.getInstance();
    }

    // Factory methods for Repositories

    private UserRepository createUserRepository() {
        // Strategy pattern: could be switched to JPA implementation
        String repositoryType = config.getProperty("repository.type", "memory");
        
        switch (repositoryType.toLowerCase()) {
            case "database":
            case "jpa":
                // Would create JPA repository here
                logger.warn("Database repository not implemented yet, using in-memory");
                return new InMemoryUserRepository();
            case "memory":
            default:
                return new InMemoryUserRepository();
        }
    }

    private ItemMasterFileRepository createItemRepository() {
        return new InMemoryItemMasterFileRepository();
    }

    private BrandRepository createBrandRepository() {
        return new InMemoryBrandRepository();
    }

    private CategoryRepository createCategoryRepository() {
        return new InMemoryCategoryRepository();
    }

    private SupplierRepository createSupplierRepository() {
        return new InMemorySupplierRepository();
    }

    // Factory methods for Application Services

    private AuthenticationPort createAuthenticationService() {
        return new AuthenticationUseCase(userRepository, passwordEncoder);
    }

    private UserManagementPort createUserManagementService() {
        return new UserManagementUseCase(userRepository, passwordEncoder);
    }

    private AddProductUseCase createAddProductUseCase() {
        return new AddProductUseCase(
            itemRepository, 
            brandRepository, 
            categoryRepository, 
            supplierRepository
        );
    }

    private com.syos.application.usecases.auth.LoginUseCase createLoginUseCase() {
        return new com.syos.application.usecases.auth.LoginUseCase(userRepository);
    }

    private com.syos.application.usecases.auth.RegisterCustomerUseCase createRegisterCustomerUseCase() {
        return new com.syos.application.usecases.auth.RegisterCustomerUseCase(userRepository);
    }

    // Factory methods for Controllers

    private AuthenticationController createAuthController() {
        return new AuthenticationController(
            console,
            sessionManager,
            createLoginUseCase(),
            createRegisterCustomerUseCase()
        );
    }

    private AdminController createAdminController() {
        return new AdminController(
            console,
            sessionManager,
            addProductUseCase,
            new CreateEmployeeUseCase(userRepository),
            userRepository,
            brandRepository,
            categoryRepository,
            supplierRepository,
            createEmployeeController()
        );
    }

    private EmployeeController createEmployeeController() {
        return new EmployeeController(
            console,
            sessionManager,
            addProductUseCase,
            brandRepository,
            categoryRepository,
            supplierRepository
        );
    }

    private CustomerController createCustomerController() {
        return new CustomerController();
    }

    // Public accessors following Interface Segregation Principle

    public ConsoleIO getConsole() {
        return console;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public AuthenticationController getAuthController() {
        return authController;
    }

    public AdminController getAdminController() {
        return adminController;
    }

    public EmployeeController getEmployeeController() {
        return employeeController;
    }

    public CustomerController getCustomerController() {
        return customerController;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    // Configuration access
    public ConfigurationManager getConfig() {
        return config;
    }

    /**
     * Initialize default system data
     */
    public void initializeDefaultData() {
        logger.info("Initializing default system data...");
        
        try {
            // Create default admin if not exists
            if (!userRepository.existsByUsername("admin")) {
                var createAdminCommand = new com.syos.application.dto.commands.CreateUserCommand.CreateAdminCommand(
                    "System Administrator",
                    "admin",
                    "admin@syos.com",
                    "admin123"
                );
                
                var response = userManagementService.createAdmin(createAdminCommand);
                if (response.isSuccess()) {
                    logger.info("Default admin user created successfully");
                } else {
                    logger.error("Failed to create default admin: {}", response.getMessage());
                }
            }
            
            // Initialize reference data
            initializeReferenceData();
            
        } catch (Exception e) {
            logger.error("Error initializing default data", e);
        }
    }

    private void initializeReferenceData() {
        // Reference data initialization is handled elsewhere in the new setup.
        // No-op to maintain backward compatibility without relying on legacy factories.
        logger.info("Reference data initialization skipped (no-op)");
    }

    /**
     * Shutdown container and clean up resources
     */
    public void shutdown() {
        logger.info("Shutting down Service Container...");
        
        try {
            // Clear session
            sessionManager.clearSession();
            
            // Any other cleanup can be added here
            
        } catch (Exception e) {
            logger.error("Error during service container shutdown", e);
        }
        
        logger.info("Service Container shut down complete");
    }
}
