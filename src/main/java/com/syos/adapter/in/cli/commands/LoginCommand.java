package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.exceptions.AuthenticationException;
import com.syos.application.usecases.auth.LoginUseCase;
import com.syos.domain.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to handle user login
 */
public class LoginCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);
    private final ConsoleIO console;
    private final LoginUseCase loginUseCase;
    private final MenuNavigator navigator;
    private final MenuFactory menuFactory;

    public LoginCommand(ConsoleIO console, LoginUseCase loginUseCase, 
                       MenuNavigator navigator, MenuFactory menuFactory) {
        this.console = console;
        this.loginUseCase = loginUseCase;
        this.navigator = navigator;
        this.menuFactory = menuFactory;
    }

    @Override
    public void execute() {
        console.println("\n╔══════════════════════════════════════╗");
        console.println("║           USER LOGIN                 ║");
        console.println("╚══════════════════════════════════════╝");
        
        console.print("\nUsername: ");
        String username = console.readLine();
        
        console.print("Password: ");
        String password = console.readPassword();
        
        try {
            // Attempt login
            User user = loginUseCase.login(username, password);
            
            // Create session
            UserSession session = new UserSession(user);
            SessionManager.getInstance().createSession(session);
            
            // Success message
            console.println();
            console.printSuccess("Login successful!");
            console.println("\n╔══════════════════════════════════════╗");
            console.println("║  Welcome back, " + padRight(user.getName().getValue(), 20) + " ║");
            console.println("║  Role: " + padRight(user.getRole().toString(), 29) + " ║");
            console.println("║  SYNEX Points: " + padRight(String.format("%.2f", user.getSynexPoints().getValue()), 21) + " ║");
            console.println("╚══════════════════════════════════════╝");
            
            console.println("\nPress Enter to continue...");
            console.readLine();
            
            // Navigate to role-specific menu
            navigator.clearMenuStack();
            navigator.pushMenu(menuFactory.createMenuForRole(user.getRole()));
            
        } catch (AuthenticationException e) {
            console.printError(e.getMessage());
            logger.warn("Login failed for user: {}", username);
            console.println("\nPress Enter to continue...");
            console.readLine();
        } catch (Exception e) {
            console.printError("An unexpected error occurred during login");
            logger.error("Unexpected login error", e);
            console.println("\nPress Enter to continue...");
            console.readLine();
        }
    }
    
    private String padRight(String text, int length) {
        if (text.length() > length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }
}