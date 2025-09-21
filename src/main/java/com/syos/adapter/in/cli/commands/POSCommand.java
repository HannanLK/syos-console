package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.ShelfStock;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Minimal POS command that sells from SHELF_STOCK using FIFO with expiry override.
 * - Employee-operated cash-only
 * - Item code + quantity entry
 * - Running total and change calculation
 * - Reduces shelf stock upon completion
 * - Prints a simple bill summary (console)
 */
public class POSCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(POSCommand.class);

    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final ShelfStockRepository shelfRepo;
    private final ItemMasterFileRepository itemRepo;

    // very simple in-memory bill sequence for the current process
    private static long BILL_SEQ = 1;

    public POSCommand(ConsoleIO console,
                      SessionManager sessionManager,
                      ShelfStockRepository shelfRepo,
                      ItemMasterFileRepository itemRepo) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.shelfRepo = shelfRepo;
        this.itemRepo = itemRepo;
    }

    private static class CartLine {
        String itemCode;
        long itemId;
        String itemName;
        double qty;
        Money unitPrice;
        Money total() { return unitPrice.multiply(qty); }
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn()) {
            console.printError("Please login to use POS.");
            return;
        }

        console.println("\n=== POINT OF SALE (POS) ===");
        console.println("Enter items. Leave item code empty to checkout.");

        List<CartLine> cart = new ArrayList<>();

        while (true) {
            String code = console.readLine("Item Code (blank to checkout): ");
            if (code == null || code.trim().isEmpty()) break;
            String qtyStr = console.readLine("Quantity: ");
            double qty;
            try {
                qty = Double.parseDouble(qtyStr);
                if (qty <= 0) {
                    console.printError("Quantity must be positive.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                console.printError("Invalid quantity.");
                continue;
            }

            Optional<ItemMasterFile> itemOpt = itemRepo.findByItemCode(ItemCode.of(code.trim()));
            if (itemOpt.isEmpty()) {
                console.printError("Unknown item code: " + code);
                continue;
            }
            ItemMasterFile item = itemOpt.get();

            // Check shelf availability with FIFO / expiry override
            java.math.BigDecimal availableBD = shelfRepo.findAvailableByItemCode(ItemCode.of(code.trim()))
                    .stream()
                    .sorted(this::fifoWithExpiryComparator)
                    .map(ss -> ss.getQuantityOnShelf().getValue())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            double available = availableBD.doubleValue();

            if (available < qty) {
                console.printError("Insufficient SHELF_STOCK. Available: " + available);
                continue;
            }

            CartLine line = new CartLine();
            line.itemCode = code.trim();
            line.itemId = item.getId();
            line.itemName = item.getItemName();
            line.qty = qty;
            line.unitPrice = item.getSellingPrice();
            cart.add(line);

            // Show running total
            Money total = cart.stream().map(CartLine::total).reduce(Money.zero(), Money::add);
            console.printInfo(String.format("Added %s x %.2f. Running total: LKR %.2f",
                    line.itemName, qty, total.getAmount().doubleValue()));
        }

        if (cart.isEmpty()) {
            console.printWarning("No items added. POS cancelled.");
            return;
        }

        // Compute total
        Money grandTotal = cart.stream().map(CartLine::total).reduce(Money.zero(), Money::add);
        console.println(String.format("\nGrand Total: LKR %.2f", grandTotal.getAmount().doubleValue()));

        // Cash tendered
        double cash;
        try {
            cash = Double.parseDouble(console.readLine("Cash tendered (LKR): "));
        } catch (NumberFormatException ex) {
            console.printError("Invalid cash amount. POS cancelled.");
            return;
        }
        if (cash < grandTotal.getAmount().doubleValue()) {
            console.printError("Insufficient cash. POS cancelled.");
            return;
        }
        double change = cash - grandTotal.getAmount().doubleValue();

        // Reduce shelf stock using FIFO with expiry override
        UserID userId = UserID.of(sessionManager.getCurrentUserId());
        for (CartLine line : cart) {
            java.math.BigDecimal remaining = java.math.BigDecimal.valueOf(line.qty);
            List<ShelfStock> stocks = new ArrayList<>(shelfRepo.findAvailableByItemCode(ItemCode.of(line.itemCode)));
            stocks.sort(this::fifoWithExpiryComparator);

            for (ShelfStock ss : stocks) {
                if (remaining.compareTo(java.math.BigDecimal.ZERO) <= 0) break;
                java.math.BigDecimal availableHere = ss.getQuantityOnShelf().getValue();
                java.math.BigDecimal take = remaining.min(availableHere);
                ShelfStock afterSale = ss.sellStock(Quantity.of(take), userId);
                shelfRepo.save(afterSale);
                remaining = remaining.subtract(take);
            }
        }

        // Print simple console bill
        long billNo = BILL_SEQ++;
        console.println("\n===== BILL (POS) =====");
        console.println("Bill No: " + billNo);
        console.println("Date/Time: " + LocalDateTime.now());
        for (CartLine l : cart) {
            console.println(String.format("%s  x %.2f  @ LKR %.2f  = LKR %.2f",
                    l.itemName, l.qty, l.unitPrice.getAmount().doubleValue(), l.total().getAmount().doubleValue()));
        }
        console.println(String.format("TOTAL: LKR %.2f", grandTotal.getAmount().doubleValue()));
        console.println(String.format("CASH:  LKR %.2f", cash));
        console.println(String.format("CHANGE: LKR %.2f", change));
        console.println("Channel: POS");
        console.println("======================");

        console.println("\nPress Enter to continue...");
        console.readLine();
    }

    // Comparator: FIFO by placedOnShelfDate, but if expiry A is sooner than B, prioritize sooner expiry
    private int fifoWithExpiryComparator(ShelfStock a, ShelfStock b) {
        LocalDateTime ea = a.getExpiryDate();
        LocalDateTime eb = b.getExpiryDate();
        if (ea != null && eb != null && !ea.equals(eb)) {
            return ea.isBefore(eb) ? -1 : 1; // earlier expiry first
        }
        // fallback FIFO by placed date
        LocalDateTime pa = a.getPlacedOnShelfDate();
        LocalDateTime pb = b.getPlacedOnShelfDate();
        if (pa == null && pb == null) return 0;
        if (pa == null) return 1;
        if (pb == null) return -1;
        return pa.isBefore(pb) ? -1 : (pa.isAfter(pb) ? 1 : 0);
    }
}
