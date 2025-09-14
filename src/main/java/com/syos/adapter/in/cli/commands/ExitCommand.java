package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.menu.MenuNavigator;

/**
 * Command to exit the application
 */
public class ExitCommand implements Command {
    private final ConsoleIO console;
    private final MenuNavigator navigator;

    public ExitCommand(ConsoleIO console, MenuNavigator navigator) {
        this.console = console;
        this.navigator = navigator;
    }

    @Override
    public void execute() {
        console.print("\nAre you sure you want to exit? (Y/N): ");
        String confirmation = console.readLine().trim().toUpperCase();
        
        if ("Y".equals(confirmation)) {
            console.println("\n════════════════════════════════════════════════════════");
            console.println("   Thank you for visiting Synex Outlet Store!");
            console.println("   We hope to see you again soon!");
            console.println("════════════════════════════════════════════════════════\n");
            navigator.stop();
        }
    }
}