package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.commands.*;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.shared.enums.UserRole;

/**
 * Factory responsible for building menus used by the CLI application.
 *
 * This provides a minimal implementation sufficient for navigation
 * referenced by LoginCommand and LogoutCommand.
 */
public class MenuFactory {
    private final ConsoleIO console;
    private final MenuNavigator navigator;

    // Simple, in-memory use case wiring for demo/console purposes
    private final InMemoryUserRepository userRepository;
    private final LoginUseCase loginUseCase;
    private final RegisterCustomerUseCase registerUseCase;

    public MenuFactory(ConsoleIO console, MenuNavigator navigator) {
        this.console = console != null ? console : new StandardConsoleIO();
        this.navigator = navigator;
        this.userRepository = new InMemoryUserRepository();
        this.loginUseCase = new LoginUseCase(userRepository);
        this.registerUseCase = new RegisterCustomerUseCase(userRepository);
    }

    /**
     * Create the main (unauthenticated) menu
     */
    public Menu createMainMenu() {
        Menu.Builder b = new Menu.Builder().title("SYOS - Main Menu");

        // 1. Browse products (placeholder)
        b.addItem(new MenuItem("1", "Browse Products", new BrowseProductsCommand(console)));

        // 2. Login
        b.addItem(new MenuItem("2", "Login", new LoginCommand(console, loginUseCase, navigator, this)));

        // 3. Register (Customer)
        b.addItem(new MenuItem("3", "Register (Customer)", new RegisterCommand(console, registerUseCase)));

        // 4. Exit
        b.addItem(new MenuItem("4", "Exit", new ExitCommand(console, navigator)));

        return b.prompt("Enter your choice: ").build();
    }

    /**
     * Create a role-specific menu after successful login.
     */
    public Menu createMenuForRole(UserRole role) {
        String title = switch (role) {
            case ADMIN -> "Admin Dashboard";
            case EMPLOYEE -> "Employee Dashboard";
            case CUSTOMER -> "Customer Dashboard";
        };

        Menu.Builder b = new Menu.Builder().title(title);

        // Common item: Browse products (still useful post-login)
        b.addItem(new MenuItem("1", "Browse Products", new BrowseProductsCommand(console)));

        // Common item: Logout
        b.addItem(new MenuItem("9", "Logout", new LogoutCommand(console, navigator, this)));

        // Common item: Exit application
        b.addItem(new MenuItem("0", "Exit", new ExitCommand(console, navigator)));

        return b.prompt("Enter your choice: ").build();
    }
}
