package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.WebInventory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Command for browsing products from WEB_INVENTORY pool
 * Minimal read-only view listing available web items.
 */
public class BrowseProductsCommand implements Command {
    private final ConsoleIO console;
    private final ItemMasterFileRepository itemRepository;
    private final WebInventoryRepository webInventoryRepository;

    // Backward-compatible constructor (placeholder mode)
    public BrowseProductsCommand(ConsoleIO console) {
        this.console = console;
        this.itemRepository = null;
        this.webInventoryRepository = null;
    }

    public BrowseProductsCommand(ConsoleIO console,
                                 ItemMasterFileRepository itemRepository,
                                 WebInventoryRepository webInventoryRepository) {
        this.console = console;
        this.itemRepository = itemRepository;
        this.webInventoryRepository = webInventoryRepository;
    }

    @Override
    public void execute() {
        console.println("\n BROWSE PRODUCTS");
        console.println("----------------------");

        if (itemRepository == null || webInventoryRepository == null) {
            console.println("\nProduct browsing feature coming soon...");
            console.println("\nPress Enter to continue...");
            console.readLine();
            return;
        }

        try {
            List<WebInventory> items = webInventoryRepository.findAvailableItems();
            if (items == null || items.isEmpty()) {
                console.println("\nNo products are currently available in the web inventory.");
                console.println("\nPress Enter to continue...");
                console.readLine();
                return;
            }

            // Sort by item name (requires lookup), fallback to item code
            items.sort(Comparator.comparing(w -> lookupItemName(w).orElse(w.getItemCode().getValue())));

            console.println("\nAvailable Online Products:");
            console.println(String.format("%-6s %-20s %-12s %-10s %-8s", "No.", "Item Name", "Item Code", "Price(LKR)", "Qty"));
            console.println("-".repeat(60));

            int index = 1;
            for (WebInventory w : items) {
                String name = lookupItemName(w).orElse("<Unknown Item>");
                String code = w.getItemCode().getValue();
                String price = w.getWebPrice().toBigDecimal().toPlainString();
                String qty = w.getQuantityAvailable().toBigDecimal().toPlainString();
                console.println(String.format("%-6d %-20s %-12s %-10s %-8s", index++, truncate(name, 20), code, price, qty));
            }

            console.println("\nNote: This list shows WEB_INVENTORY only. POS uses shelf stock.");
        } catch (Exception e) {
            console.printError("Failed to load products: " + e.getMessage());
        }

        console.println("\nPress Enter to continue...");
        console.readLine();
    }

    private Optional<String> lookupItemName(WebInventory w) {
        try {
            return itemRepository.findById(w.getItemId()).map(ItemMasterFile::getItemName);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}