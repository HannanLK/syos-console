package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.commands.*;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.shared.enums.UserRole;

/**
 * Factory for creating menus based on user roles and context
 */
public class MenuFactory {
    private final ConsoleIO console;
    private final MenuNavigator navigator;
    private final LoginUseCase loginUseCase;
    private final RegisterCustomerUseCase registerUseCase;

    public MenuFactory(ConsoleIO console, MenuNavigator navigator,
                      LoginUseCase loginUseCase, RegisterCustomerUseCase registerUseCase) {
        this.console = console;
        this.navigator = navigator;
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
    }

    /**
     * Create the main menu for unauthenticated users
     */
    public Menu createMainMenu() {
        return new Menu.Builder()
            .title("SYNEX OUTLET STORE - Main Menu")
            .addItem(new MenuItem("1", "Browse Products", 
                new BrowseProductsCommand(console)))
            .addItem(new MenuItem("2", "Login", 
                new LoginCommand(console, loginUseCase, navigator, this)))
            .addItem(new MenuItem("3", "Register", 
                new RegisterCommand(console, registerUseCase)))
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
            .title("CUSTOMER DASHBOARD")
            .addItem(new MenuItem("1", "Browse Products", 
                new BrowseProductsCommand(console)))
            .addItem(new MenuItem("2", "View Cart", 
                createPlaceholderCommand("Shopping Cart")))
            .addItem(new MenuItem("3", "Order History", 
                createPlaceholderCommand("Order History")))
            .addItem(new MenuItem("4", "View SYNEX Points", 
                createPlaceholderCommand("SYNEX Points")))
            .addItem(new MenuItem("5", "Account Information", 
                createPlaceholderCommand("Account Information")))
            .addItem(new MenuItem("L", "Logout", 
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
                createPlaceholderCommand("Point of Sale")))
            .addItem(new MenuItem("2", "Add Items", 
                createPlaceholderCommand("Add Items")))
            .addItem(new MenuItem("3", "View Shelf Stock", 
                createPlaceholderCommand("Shelf Stock")))
            .addItem(new MenuItem("4", "View Discounts", 
                createPlaceholderCommand("Discounts")))
            .addItem(new MenuItem("5", "Reports & Insights", 
                createPlaceholderCommand("Reports & Insights")))
            .addItem(new MenuItem("6", "Add Customer", 
                createPlaceholderCommand("Add Customer")))
            .addItem(new MenuItem("7", "View Products Stock", 
                createPlaceholderCommand("Products Stock")))
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
                createPlaceholderCommand("Point of Sale")))
            .addItem(new MenuItem("2", "Inventory Management", 
                createPlaceholderCommand("Inventory Management")))
            .addItem(new MenuItem("3", "User Management", 
                createPlaceholderCommand("User Management")))
            .addItem(new MenuItem("4", "Reports & Analytics", 
                createPlaceholderCommand("Reports & Analytics")))
            .addItem(new MenuItem("5", "System Settings", 
                createPlaceholderCommand("System Settings")))
            .addItem(new MenuItem("6", "Add/Manage Users", 
                createPlaceholderCommand("Add/Manage Users")))
            .addItem(new MenuItem("7", "View Sales/Insights", 
                createPlaceholderCommand("Sales & Insights")))
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