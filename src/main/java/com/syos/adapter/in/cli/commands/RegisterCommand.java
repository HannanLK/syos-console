package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.exceptions.RegistrationException;
import com.syos.application.usecases.auth.RegisterCustomerUseCase;
import com.syos.domain.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to handle customer registration
 */
public class RegisterCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCommand.class);
    private final ConsoleIO console;
    private final RegisterCustomerUseCase registerUseCase;

    public RegisterCommand(ConsoleIO console, RegisterCustomerUseCase registerUseCase) {
        this.console = console;
        this.registerUseCase = registerUseCase;
    }

    @Override
    public void execute() {
        console.println("\n CUSTOMER REGISTRATION");
        console.println("----------------------");
        console.println("\nPlease provide the following information:");
        
        console.print("\nFull Name: ");
        String name = console.readLine();
        
        console.print("Username (3-20 characters, letters/numbers/_): ");
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
            console.println("║  You can now login with your        ║");
            console.println("║  credentials.                        ║");
            console.println("╚══════════════════════════════════════╝");
            
            logger.info("New customer registered: {}", username);
            
        } catch (RegistrationException e) {
            console.printError(e.getMessage());
            logger.warn("Registration failed: {}", e.getMessage());
        } catch (Exception e) {
            console.printError("Registration failed: " + e.getMessage());
            logger.error("Unexpected registration error", e);
        }
        
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
    
    private String padRight(String text, int length) {
        if (text.length() > length) {
            return text.substring(0, length - 3) + "...";
        }
        return text + " ".repeat(length - text.length());
    }
}