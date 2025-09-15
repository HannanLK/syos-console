package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.out.persistence.InMemoryUserRepository;
import com.syos.application.ports.out.UserRepository;

/**
 * Debug command to show repository state (for development only)
 */
public class DebugCommand implements Command {
    private final ConsoleIO console;
    private final UserRepository userRepository;

    public DebugCommand(ConsoleIO console, UserRepository userRepository) {
        this.console = console;
        this.userRepository = userRepository;
    }

    @Override
    public void execute() {
        console.println("\n═══════════════════════════════════════");
        console.println("            DEBUG INFORMATION");
        console.println("═══════════════════════════════════════");
        
        if (userRepository instanceof InMemoryUserRepository) {
            InMemoryUserRepository repo = (InMemoryUserRepository) userRepository;
            console.println("\nTotal users in repository: " + repo.getUserCount());
            
            console.println("\nRepository contents:");
            repo.printAllUsers(); // This will log to the logger
            
            console.println("\nTest default users:");
            console.println("- admin / admin123");
            console.println("- employee / emp123");
            console.println("- customer / cust123");
        } else {
            console.println("Repository type: " + userRepository.getClass().getSimpleName());
        }
        
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}