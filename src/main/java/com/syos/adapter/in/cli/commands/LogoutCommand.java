package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.in.cli.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to handle user logout
 */
public class LogoutCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LogoutCommand.class);
    private final ConsoleIO console;
    private final MenuNavigator navigator;
    private final MenuFactory menuFactory;

    public LogoutCommand(ConsoleIO console, MenuNavigator navigator, MenuFactory menuFactory) {
        this.console = console;
        this.navigator = navigator;
        this.menuFactory = menuFactory;
    }

    @Override
    public void execute() {
        SessionManager sessionManager = SessionManager.getInstance();
        
        if (!sessionManager.isLoggedIn()) {
            console.printWarning("No user is currently logged in.");
            return;
        }
        
        String username = sessionManager.getCurrentSession().getUsername();
        
        console.print("\nAre you sure you want to logout? (Y/N): ");
        String confirmation = console.readLine().trim().toUpperCase();
        
        if ("Y".equals(confirmation)) {
            sessionManager.clearSession();
            console.printSuccess("Logged out successfully.");
            logger.info("User logged out: {}", username);
            
            console.println("\nReturning to main menu...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Return to main menu
            navigator.clearMenuStack();
            navigator.pushMenu(menuFactory.createMainMenu());
        }
    }
}