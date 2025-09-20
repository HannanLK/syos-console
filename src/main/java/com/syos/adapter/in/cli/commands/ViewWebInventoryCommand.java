package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.domain.entities.WebInventory;

import java.util.List;

/**
 * Displays WEB_INVENTORY items available for online channel.
 */
public class ViewWebInventoryCommand implements Command {
    private final ConsoleIO console;
    private final WebInventoryRepository webRepo;

    public ViewWebInventoryCommand(ConsoleIO console, WebInventoryRepository webRepo) {
        this.console = console;
        this.webRepo = webRepo;
    }

    @Override
    public void execute() {
        console.println("\n== Web Inventory ==");
        List<WebInventory> all = webRepo.findAll();
        if (all == null || all.isEmpty()) {
            console.println("No web inventory available.");
        } else {
            for (WebInventory wi : all) {
                console.println("Item " + wi.getItemCode().getValue() +
                        " | Batch " + wi.getBatchId() +
                        " | Avail " + wi.getQuantityAvailable().getValue() +
                        " | Published " + wi.isPublished());
            }
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
