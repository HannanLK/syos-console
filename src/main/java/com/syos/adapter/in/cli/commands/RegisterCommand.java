package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.menu.MenuFactory;
import com.syos.adapter.in.cli.menu.MenuNavigator;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.exceptions.RegistrationException;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Command to handle customer registration
 */
public class RegisterCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCommand.class);
    private final ConsoleIO console;
    private final RegisterCustomerUseCase registerUseCase;

    private final MenuNavigator navigator;
    private final MenuFactory menuFactory;

    public RegisterCommand(ConsoleIO console, RegisterCustomerUseCase registerUseCase, 
                          MenuNavigator navigator, MenuFactory menuFactory) {
        this.console = console;
        this.registerUseCase = registerUseCase;
        this.navigator = navigator;
        this.menuFactory = menuFactory;
    }

    @Override
    public void execute() {
        console.println("\n═══════════════════════════════════════");
        console.println("         CUSTOMER REGISTRATION");
        console.println("═══════════════════════════════════════");
        console.println("Please provide the following information:");
        
        console.print("\nFull Name: ");
        String name = console.readLine();
        
        console.print("Username: ");
        String username = console.readLine();
        
        console.print("Email Address: ");
        String email = console.readLine();
        
        console.print("Password (minimum 8 characters): ");
        String password = console.readPassword();
        
        console.print("Confirm Password: ");
        String confirmPassword = console.readPassword();
        
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            console.printError("Passwords do not match!");
            console.println("\nPress Enter to continue...");
            console.readLine();
            return;
        }
        
        try {
            console.println("\nProcessing registration...");
            
            // Attempt registration
            User user = registerUseCase.register(username, password, name, email);
            
            // Success message
            console.println();
            console.printSuccess("Registration successful!");
            console.println("\n╔══════════════════════════════════════╗");
            console.println("║     REGISTRATION COMPLETE            ║");
            console.println("╠══════════════════════════════════════╣");
            console.println("║  Username: " + padRight(user.getUsername().getValue(), 25) + " ║");
            console.println("║  Name: " + padRight(user.getName().getValue(), 29) + " ║");
            console.println("║  Email: " + padRight(user.getEmail().getValue(), 28) + " ║");
            console.println("║                                      ║");
            console.println("║  Automatically logging you in...     ║");
            console.println("╚══════════════════════════════════════╝");
            
            logger.info("New customer registered: {}", username);
            
            // Automatically log in the user
            UserSession session = new UserSession(user);
            SessionManager.getInstance().createSession(session);
            
            // Display user profile with greeting
            displayUserProfile(user);
            
            console.println("\nPress Enter to continue to your dashboard...");
            console.readLine();
            
            // Navigate to customer menu
            navigator.clearMenuStack();
            navigator.pushMenu(menuFactory.createMenuForRole(user.getRole()));
            
        } catch (RegistrationException e) {
            console.printError("Registration Error: " + e.getMessage());
            logger.warn("Registration failed: {}", e.getMessage());
            console.println("\nPress Enter to continue...");
            console.readLine();
        } catch (Exception e) {
            console.printError("Registration failed: " + e.getMessage());
            logger.error("Unexpected registration error", e);
            e.printStackTrace(); // For debugging
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
        console.println("\n═════════════════════════════════════════");
        console.println("             USER PROFILE                ");
        console.println("═════════════════════════════════════════");
        console.println(greeting + ", " + padRight(user.getName().getValue() + "!", 32 - greeting.length()));
        console.println();
        console.println("  Username: " + padRight(user.getUsername().getValue(), 25));
        console.println("  Email: " + padRight(user.getEmail().getValue(), 28));
        console.println("  Synex Points: " + padRight(String.format("%.2f", user.getSynexPoints().getValue()), 21));
        console.println("  Member Since: " + padRight(memberSince, 21));
        console.println("═════════════════════════════════════════");
    }
    
    private String padRight(String text, int length) {
        if (text.length() > length) {
            return text.substring(0, length - 3) + "...";
        }
        return text + " ".repeat(length - text.length());
    }
}