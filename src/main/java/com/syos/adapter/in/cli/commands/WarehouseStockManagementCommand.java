package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.WarehouseStock;
import com.syos.domain.valueobjects.ItemCode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Submenu wrapper for all warehouse-related actions requested in the issue:
 * - View Warehouse Stock
 * - View Shelf Stock
 * - View Web Inventory
 * - Transfer to Shelf (with optional pre-listing of items and codes)
 * - Transfer to Web (with optional pre-listing of items and codes)
 */
public class WarehouseStockManagementCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final WarehouseStockRepository warehouseRepo;
    private final ShelfStockRepository shelfRepo;
    private final WebInventoryRepository webRepo;
    private final ItemMasterFileRepository itemRepo;
    private final CompleteProductManagementUseCase productUseCase;

    public WarehouseStockManagementCommand(ConsoleIO console,
                                           SessionManager sessionManager,
                                           WarehouseStockRepository warehouseRepo,
                                           ShelfStockRepository shelfRepo,
                                           WebInventoryRepository webRepo,
                                           ItemMasterFileRepository itemRepo,
                                           CompleteProductManagementUseCase productUseCase) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.warehouseRepo = warehouseRepo;
        this.shelfRepo = shelfRepo;
        this.webRepo = webRepo;
        this.itemRepo = itemRepo;
        this.productUseCase = productUseCase;
    }

    @Override
    public void execute() {
        while (true) {
            console.println("\n═══════════════════════════════════════");
            console.println("      WAREHOUSE STOCK MANAGEMENT      ");
            console.println("═══════════════════════════════════════");
            console.println("[1] View Warehouse Stock");
            console.println("[2] View Shelf Stock");
            console.println("[3] View Web Inventory");
            console.println("[4] Transfer to Shelf");
            console.println("[5] Transfer to Web");
            console.println("[B] Back");

            String choice = console.readLine("Enter your choice: ");
            switch (choice == null ? "" : choice.trim().toUpperCase()) {
                case "1":
                    new ViewWarehouseInventoryCommand(console, warehouseRepo).execute();
                    break;
                case "2":
                    new ViewShelfInventoryCommand(console, shelfRepo).execute();
                    break;
                case "3":
                    new ViewWebInventoryCommand(console, webRepo).execute();
                    break;
                case "4":
                    preListWarehouseItemsIfRequested();
                    new TransferToShelfCommand(console, sessionManager, productUseCase).execute();
                    break;
                case "5":
                    preListWarehouseItemsIfRequested();
                    new TransferToWebCommand(console, sessionManager, productUseCase).execute();
                    break;
                case "B":
                    return;
                default:
                    console.printError("Invalid choice. Please try again.");
            }
        }
    }

    private void preListWarehouseItemsIfRequested() {
        String see = console.readLine("Do you wish to see items and codes first? (y/n): ");
        if (see != null && see.trim().toLowerCase().startsWith("y")) {
            try {
                // Prefer in-memory repo fast-path if available
                List<WarehouseStock> all = (warehouseRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository)
                        ? ((com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository) warehouseRepo).findAll()
                        : warehouseRepo.findByLocation("MAIN-WAREHOUSE");

                if (all == null || all.isEmpty()) {
                    console.println("No warehouse stock available.");
                    return;
                }

                // Aggregate by item code for a simple overview
                console.println("\nAvailable Warehouse Items:");
                console.println(String.format("%-14s %-28s %-10s", "Item Code", "Item Name", "Qty Avl"));
                console.println("-".repeat(56));

                all.stream()
                        .collect(java.util.stream.Collectors.groupingBy(WarehouseStock::getItemCode))
                        .entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getValue()))
                        .forEach(entry -> {
                            ItemCode code = entry.getKey();
                            String name = lookupItemName(code).orElse("<Unknown>");
                            java.math.BigDecimal qty = entry.getValue().stream()
                                    .map(ws -> ws.getQuantityAvailable().toBigDecimal())
                                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                            console.println(String.format("%-14s %-28s %-10s", code.getValue(), truncate(name, 28), qty.toPlainString()));
                        });
                console.println();
            } catch (Exception ex) {
                console.printWarning("Unable to list items: " + ex.getMessage());
            }
        }
    }

    private Optional<String> lookupItemName(ItemCode code) {
        try {
            return itemRepo.findByItemCode(code).map(ItemMasterFile::getItemName);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
