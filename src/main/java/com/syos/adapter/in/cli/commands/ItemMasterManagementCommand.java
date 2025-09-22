package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Admin command to manage Item Master (stock catalog):
 * - List items
 * - Update item fields (name, description, price, status, reorder point)
 * - Delete item
 */
public class ItemMasterManagementCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final ItemMasterFileRepository itemRepository;
    private final com.syos.application.usecases.inventory.CompleteProductManagementUseCase productUseCase;

    public ItemMasterManagementCommand(ConsoleIO console, SessionManager sessionManager, ItemMasterFileRepository itemRepository,
                                       com.syos.application.usecases.inventory.CompleteProductManagementUseCase productUseCase) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.itemRepository = itemRepository;
        this.productUseCase = productUseCase;
    }

    @Override
    public void execute() {
        if (sessionManager == null || !sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            console.printError("Admin access required.");
            return;
        }
        while (true) {
            console.println("\n=== ITEM MASTER MANAGEMENT ===");
            console.println("1. List Active Items");
            console.println("2. Update Item");
            console.println("3. Delete Item");
            console.println("4. Back");
            String choice = console.readLine("Choose: ");
            switch (choice) {
                case "1" -> listItems();
                case "2" -> updateItem();
                case "3" -> deleteItem();
                case "4" -> { return; }
                default -> console.printError("Invalid choice");
            }
        }
    }

    private void listItems() {
        List<ItemMasterFile> items = itemRepository.findAllActive();
        if (items == null || items.isEmpty()) {
            console.println("No items found.");
            return;
        }
        console.println(String.format("%-6s %-14s %-28s %-12s %-12s %-8s", "ID", "Code", "Name", "Cost", "Price", "Status"));
        for (ItemMasterFile i : items) {
            console.println(String.format("%-6d %-14s %-28s %-12s %-12s %-8s",
                    i.getId() != null ? i.getId() : 0,
                    i.getItemCode().getValue(),
                    truncate(i.getItemName(), 28),
                    i.getCostPrice().toBigDecimal().toPlainString(),
                    i.getSellingPrice().toBigDecimal().toPlainString(),
                    i.getStatus().name()));
        }
    }

    private void updateItem() {
        String idOrCode = console.readLine("Enter Item ID or Code: ");
        Optional<ItemMasterFile> opt = parseId(idOrCode)
                .flatMap(id -> itemRepository.findById(id))
                .or(() -> findByCode(idOrCode));
        if (opt.isEmpty()) {
            console.printError("Item not found.");
            return;
        }
        ItemMasterFile item = opt.get();
        console.println("Editing: " + item.getItemCode().getValue() + " - " + item.getItemName());
        String newName = console.readLine("New Name (blank to keep): ");
        String newDesc = console.readLine("New Description (blank to keep): ");
        String newCost = console.readLine("New Cost Price (blank to keep): ");
        String newPrice = console.readLine("New Selling Price (blank to keep): ");
        String newReorder = console.readLine("New Reorder Point (blank to keep): ");
        String newStatus = console.readLine("New Status [ACTIVE/INACTIVE] (blank to keep): ");
        try {
            // Use builder(existing) to apply updates immutably
            ItemMasterFile.Builder builder = new ItemMasterFile.Builder(item);
            boolean changed = false;
            if (newName != null && !newName.isBlank()) {
                builder.itemName(newName.trim());
                changed = true;
            }
            if (newDesc != null && !newDesc.isBlank()) {
                builder.description(newDesc.trim());
                changed = true;
            }
            if (newCost != null && !newCost.isBlank()) {
                builder.costPrice(Money.of(new BigDecimal(newCost.trim())));
                changed = true;
            }
            if (newPrice != null && !newPrice.isBlank()) {
                builder.sellingPrice(Money.of(new BigDecimal(newPrice.trim())));
                changed = true;
            }
            if (newReorder != null && !newReorder.isBlank()) {
                builder.reorderPoint(ReorderPoint.of(Integer.parseInt(newReorder.trim())));
                changed = true;
            }
            if (newStatus != null && !newStatus.isBlank()) {
                ProductStatus ps = ProductStatus.valueOf(newStatus.trim().toUpperCase());
                builder.status(ps);
                changed = true;
            }
            if (!changed) {
                console.printWarning("No changes provided.");
                return;
            }
            ItemMasterFile updated = builder.build();
            itemRepository.save(updated);
            console.printSuccess("Item updated.");
        } catch (Exception ex) {
            console.printError("Update failed: " + ex.getMessage());
        }
    }

    private void deleteItem() {
        String idOrCode = console.readLine("Enter Item ID or Code to delete: ");
        Optional<ItemMasterFile> opt = parseId(idOrCode)
                .flatMap(id -> itemRepository.findById(id))
                .or(() -> findByCode(idOrCode));
        if (opt.isEmpty()) {
            console.printError("Item not found.");
            return;
        }
        ItemMasterFile item = opt.get();
        String confirm = console.readLine("Delete '" + item.getItemCode().getValue() + " - " + item.getItemName() + "'? (y/N): ");
        if (!"y".equalsIgnoreCase(confirm)) {
            console.println("Cancelled.");
            return;
        }
        try {
            itemRepository.deleteById(item.getId());
            console.printSuccess("Item deleted.");
        } catch (Exception ex) {
            console.printError("Delete failed: " + ex.getMessage());
        }
    }

    private void receiveStock() {
        // Delegate to the centralized receive stock command used in Warehouse menu
        new ReceiveStockInWarehouseCommand(console, sessionManager, productUseCase).execute();
    }

    private Optional<Long> parseId(String s) {
        try {
            return Optional.of(Long.parseLong(s.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<ItemMasterFile> findByCode(String code) {
        try {
            return itemRepository.findByItemCode(ItemCode.of(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
