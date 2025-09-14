package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;

/**
 * Command for browsing products (placeholder)
 */
public class BrowseProductsCommand implements Command {
    private final ConsoleIO console;

    public BrowseProductsCommand(ConsoleIO console) {
        this.console = console;
    }

    @Override
    public void execute() {
        console.println("\n BROWSE PRODUCTS");
        console.println("----------------------");
        console.println("\nProduct browsing feature coming soon...");
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}