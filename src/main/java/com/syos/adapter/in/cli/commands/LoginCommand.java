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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        console.println("\n═══════════════════════════════════════");
        console.println("              USER LOGIN");
        console.println("═══════════════════════════════════════");
        
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
            
            // Display user profile with greeting
            displayUserProfile(user);
            
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
    
    private void displayUserProfile(User user) {
        // Time-based greeting
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        
        // Format member since date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String memberSince = user.getCreatedAt().format(formatter);
        
        console.println("\n╔══════════════════════════════════════╗");
        console.println("║           USER PROFILE                 ║");
        console.println("╠══════════════════════════════════════╣");
        console.println("║                                      ║");
        console.println("║  " + greeting + ", " + padRight(user.getName().getValue() + "!", 32 - greeting.length()) + " ║");
        console.println("║                                      ║");
        console.println("║  Username: " + padRight(user.getUsername().getValue(), 25) + " ║");
        console.println("║  Email: " + padRight(user.getEmail().getValue(), 28) + " ║");
        console.println("║  Synex Points: " + padRight(String.format("%.2f", user.getSynexPoints().getValue()), 21) + " ║");
        console.println("║  Member Since: " + padRight(memberSince, 21) + " ║");
        console.println("║                                      ║");
        console.println("╚══════════════════════════════════════╝");
    }
    
    private String padRight(String text, int length) {
        if (text.length() > length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }
}