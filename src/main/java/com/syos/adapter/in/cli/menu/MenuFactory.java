package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.commands.*;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.UserRepository;
import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.SupplierRepository;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.shared.enums.UserRole;

/**
 * Factory for creating menus based on user roles and context
 */
public class MenuFactory {
    private final ConsoleIO console;
    private final MenuNavigator navigator;
    private final LoginUseCase loginUseCase;
    private final RegisterCustomerUseCase registerUseCase;
    private final UserRepository userRepository;

    // Optional dependencies for enhanced features
    private final AddProductUseCase addProductUseCase;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final SessionManager sessionManager;
    private final ItemMasterFileRepository itemRepository;
    private final WebInventoryRepository webInventoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final ShelfStockRepository shelfStockRepository;
    private final CompleteProductManagementUseCase productManagementUseCase;

    public MenuFactory(ConsoleIO console, MenuNavigator navigator,
                     LoginUseCase loginUseCase, RegisterCustomerUseCase registerUseCase,
                     UserRepository userRepository) {
        this.console = console;
        this.navigator = navigator;
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.userRepository = userRepository;
        // default optional dependencies to null for backward compatibility
        this.addProductUseCase = null;
        this.brandRepository = null;
        this.categoryRepository = null;
        this.supplierRepository = null;
        this.sessionManager = null;
        this.itemRepository = null;
        this.webInventoryRepository = null;
        this.warehouseStockRepository = null;
        this.shelfStockRepository = null;
        this.productManagementUseCase = null;
    }

    // Overloaded constructor to enable Add Product command and other features
    public MenuFactory(ConsoleIO console, MenuNavigator navigator,
                       LoginUseCase loginUseCase, RegisterCustomerUseCase registerUseCase,
                       UserRepository userRepository,
                       AddProductUseCase addProductUseCase,
                       BrandRepository brandRepository,
                       CategoryRepository categoryRepository,
                       SupplierRepository supplierRepository,
                       SessionManager sessionManager,
                       ItemMasterFileRepository itemRepository,
                       WebInventoryRepository webInventoryRepository,
                       WarehouseStockRepository warehouseStockRepository,
                       ShelfStockRepository shelfStockRepository,
                       CompleteProductManagementUseCase productManagementUseCase) {
        this.console = console;
        this.navigator = navigator;
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.userRepository = userRepository;
        this.addProductUseCase = addProductUseCase;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.sessionManager = sessionManager;
        this.itemRepository = itemRepository;
        this.webInventoryRepository = webInventoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.shelfStockRepository = shelfStockRepository;
        this.productManagementUseCase = productManagementUseCase;
    }

    /**
     * Create the main menu for unauthenticated users
     */
    public Menu createMainMenu() {
        return new Menu.Builder()
            .title("SYNEX OUTLET STORE - Main Menu")
            .addItem(new MenuItem("1", "Browse Products", 
                new BrowseProductsCommand(console, itemRepository, webInventoryRepository)))
            .addItem(new MenuItem("2", "Login", 
                new LoginCommand(console, loginUseCase, navigator, this)))
            .addItem(new MenuItem("3", "Register", 
                new RegisterCommand(console, registerUseCase, navigator, this)))
            .addItem(new MenuItem("4", "Exit", 
                new ExitCommand(console, navigator)))
            .prompt("Enter your choice: ")
            .build();
    }

    /**
     * Create a menu based on a user role
     */
    public Menu createMenuForRole(UserRole role) {
        switch (role) {
            case CUSTOMER:
                return createCustomerMenu();
            case EMPLOYEE:
                return createEmployeeMenu();
            case ADMIN:
                return createAdminMenu();
            default:
                return createMainMenu();
        }
    }

    /**
     * Create customer-specific menu
     */
    private Menu createCustomerMenu() {
        return new Menu.Builder()
            .title("CUSTOMER NAVIGATION MENU")
            .addItem(new MenuItem("1", "Browse Products", 
                new BrowseProductsCommand(console, itemRepository, webInventoryRepository)))
            .addItem(new MenuItem("2", "View Cart", 
                new ViewCartCommand(console, sessionManager, itemRepository, webInventoryRepository)))
            .addItem(new MenuItem("3", "Order History", 
                new OrderHistoryCommand(console, sessionManager)))
            .addItem(new MenuItem("4", "Logout", 
                new LogoutCommand(console, navigator, this)))
            .prompt("Enter your choice: ")
            .build();
    }

    /**
     * Create employee-specific menu
     */
    private Menu createEmployeeMenu() {
        return new Menu.Builder()
            .title("EMPLOYEE DASHBOARD")
            .addItem(new MenuItem("1", "Point of Sale (POS)", 
                (sessionManager != null && shelfStockRepository != null && itemRepository != null)
                    ? new POSCommand(console, sessionManager, shelfStockRepository, itemRepository)
                    : createPlaceholderCommand("Point of Sale")))
            .addItem(new MenuItem("2", "Add Product",
                (addProductUseCase != null && brandRepository != null && categoryRepository != null && supplierRepository != null && sessionManager != null && productManagementUseCase != null)
                    ? new AddProductCommand(console, addProductUseCase, brandRepository, categoryRepository, supplierRepository, sessionManager, productManagementUseCase)
                    : createPlaceholderCommand("Add Product")))
            .addItem(new MenuItem("3", "Warehouse Stock Management",
                (productManagementUseCase != null && sessionManager != null && warehouseStockRepository != null && shelfStockRepository != null && webInventoryRepository != null && itemRepository != null)
                    ? new WarehouseStockManagementCommand(console, sessionManager, warehouseStockRepository, shelfStockRepository, webInventoryRepository, itemRepository, productManagementUseCase)
                    : createPlaceholderCommand("Warehouse Stock Management")))
            .addItem(new MenuItem("L", "Logout",
                new LogoutCommand(console, navigator, this)))
            .prompt("Enter your choice: ")
            .build();
    }

    /**
     * Create admin-specific menu
     */
    private Menu createAdminMenu() {
        return new Menu.Builder()
            .title("ADMINISTRATOR DASHBOARD")
            .addItem(new MenuItem("1", "Point of Sale (POS)", 
                (sessionManager != null && shelfStockRepository != null && itemRepository != null)
                    ? new POSCommand(console, sessionManager, shelfStockRepository, itemRepository)
                    : createPlaceholderCommand("Point of Sale")))
            .addItem(new MenuItem("2", "Add Product",
                (addProductUseCase != null && brandRepository != null && categoryRepository != null && supplierRepository != null && sessionManager != null && productManagementUseCase != null)
                    ? new AddProductCommand(console, addProductUseCase, brandRepository, categoryRepository, supplierRepository, sessionManager, productManagementUseCase)
                    : createPlaceholderCommand("Add Product")))
            .addItem(new MenuItem("3", "Warehouse Stock Management",
                (productManagementUseCase != null && sessionManager != null && warehouseStockRepository != null && shelfStockRepository != null && webInventoryRepository != null && itemRepository != null)
                    ? new WarehouseStockManagementCommand(console, sessionManager, warehouseStockRepository, shelfStockRepository, webInventoryRepository, itemRepository, productManagementUseCase)
                    : createPlaceholderCommand("Warehouse Stock Management")))
            .addItem(new MenuItem("L", "Logout", 
                new LogoutCommand(console, navigator, this)))
            .prompt("Enter your choice: ")
            .build();
    }

    /**
     * Create a placeholder command for features not yet implemented
     */
    private Command createPlaceholderCommand(String featureName) {
        return () -> {
            console.println("\n╔══════════════════════════════════════╗");
            console.println("║  " + padCenter(featureName.toUpperCase(), 36) + "  ║");
            console.println("╚══════════════════════════════════════╝");
            console.println("\nThis feature is coming soon...");
            console.println("\nPress Enter to continue...");
            console.readLine();
        };
    }

    private String padCenter(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
}