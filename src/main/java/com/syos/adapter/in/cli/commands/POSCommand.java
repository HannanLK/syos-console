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
    private final com.syos.application.services.DiscountService discountService;
    private final com.syos.infrastructure.persistence.repositories.JpaPOSRepository posRepository;

    private boolean personalPurchaseMode = false;

    public POSCommand(ConsoleIO console,
                      SessionManager sessionManager,
                      ShelfStockRepository shelfRepo,
                      ItemMasterFileRepository itemRepo,
                      com.syos.application.services.DiscountService discountService,
                      com.syos.infrastructure.persistence.repositories.JpaPOSRepository posRepository) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.shelfRepo = shelfRepo;
        this.itemRepo = itemRepo;
        this.discountService = discountService;
        this.posRepository = posRepository;
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
        console.println((personalPurchaseMode ? "*** PERSONAL PURCHASE MODE ***" : "WORK MODE") + "  [type 'P' to toggle]");
        console.println("Enter items. Leave item code empty to checkout.");

        List<CartLine> cart = new ArrayList<>();

        while (true) {
            String code = console.readLine("Item Code (blank to checkout, 'P' to toggle mode): ");
            if (code != null && code.trim().equalsIgnoreCase("P")) {
                personalPurchaseMode = !personalPurchaseMode;
                console.printInfo("POS mode changed to: " + (personalPurchaseMode ? "PERSONAL PURCHASE" : "WORK"));
                continue;
            }
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
        console.println(String.format("\nGross Total: LKR %.2f", grandTotal.getAmount().doubleValue()));

        // Calculate discounts per batch
        java.math.BigDecimal discountTotalBD = java.math.BigDecimal.ZERO;
        if (!personalPurchaseMode) {
            for (CartLine line : cart) {
                java.math.BigDecimal remaining = java.math.BigDecimal.valueOf(line.qty);
                List<ShelfStock> stocks = new ArrayList<>(shelfRepo.findAvailableByItemCode(ItemCode.of(line.itemCode)));
                stocks.sort(this::fifoWithExpiryComparator);
                for (ShelfStock ss : stocks) {
                    if (remaining.compareTo(java.math.BigDecimal.ZERO) <= 0) break;
                    java.math.BigDecimal availableHere = ss.getQuantityOnShelf().getValue();
                    java.math.BigDecimal take = remaining.min(availableHere);
                    java.math.BigDecimal d = discountService.calculateBatchDiscount(
                            line.itemId,
                            ss.getBatchId(),
                            ss.getUnitPrice().getAmount(),
                            take.doubleValue()
                    );
                    discountTotalBD = discountTotalBD.add(d);
                    remaining = remaining.subtract(take);
                }
            }
        }
        double discountTotal = discountTotalBD.doubleValue();
        double netTotal = grandTotal.getAmount().doubleValue() - discountTotal;
        if (netTotal < 0) netTotal = 0;
        console.println(String.format("Discounts: -LKR %.2f", discountTotal));
        console.println(String.format("Net Payable: LKR %.2f", netTotal));

        // Personal purchase restrictions
        if (personalPurchaseMode) {
            if (netTotal > 10000.0) {
                console.printError("Personal purchase exceeds limit LKR 10,000. Cancelled.");
                return;
            }
        }

        // Cash tendered - loop until sufficient or cancel
        double cash = -1;
        while (true) {
            String cashInput = console.readLine("Cash tendered (LKR) [enter 'C' to cancel]: ");
            if (cashInput == null) {
                console.printWarning("POS cancelled.");
                return;
            }
            cashInput = cashInput.trim();
            if (cashInput.equalsIgnoreCase("C")) {
                console.printWarning("POS cancelled by user.");
                return;
            }
            try {
                cash = Double.parseDouble(cashInput);
                if (cash < netTotal) {
                    console.printError(String.format("Insufficient cash. Need at least LKR %.2f. Try again or enter 'C' to cancel.", grandTotal.getAmount().doubleValue()));
                    continue;
                }
                break;
            } catch (NumberFormatException ex) {
                console.printError("Invalid cash amount. Please enter a number or 'C' to cancel.");
            }
        }
        double change = cash - netTotal;

        // Build per-batch persistence lines (and then reduce stock)
        java.util.List<com.syos.infrastructure.persistence.repositories.JpaPOSRepository.PosLine> lines = new java.util.ArrayList<>();
        java.math.BigDecimal txDiscountTotalBD = java.math.BigDecimal.ZERO;

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

                // Compute per-batch discount for this allocation
                java.math.BigDecimal lineDiscount = personalPurchaseMode ? java.math.BigDecimal.ZERO :
                        discountService.calculateBatchDiscount(
                                line.itemId,
                                ss.getBatchId(),
                                ss.getUnitPrice().getAmount(),
                                take.doubleValue()
                        );
                txDiscountTotalBD = txDiscountTotalBD.add(lineDiscount);

                // Add line for persistence
                lines.add(new com.syos.infrastructure.persistence.repositories.JpaPOSRepository.PosLine(
                        line.itemId,
                        ss.getBatchId(),
                        take.doubleValue(),
                        ss.getUnitPrice().getAmount(),
                        lineDiscount
                ));

                // Reduce shelf stock and persist
                ShelfStock afterSale = ss.sellStock(Quantity.of(take), userId);
                shelfRepo.save(afterSale);
                remaining = remaining.subtract(take);
            }
        }

        // Create and persist transaction
        com.syos.infrastructure.persistence.entities.TransactionEntity tx = new com.syos.infrastructure.persistence.entities.TransactionEntity();
        tx.setUserId(sessionManager.getCurrentUserId());
        tx.setTransactionType(com.syos.infrastructure.persistence.entities.TransactionEntity.TransactionType.POS);
        tx.setPaymentMethod(com.syos.infrastructure.persistence.entities.TransactionEntity.PaymentMethod.CASH);
        // Set amounts: subtotal before discount, total as gross (kept for compatibility)
        java.math.BigDecimal grossBD = java.math.BigDecimal.valueOf(grandTotal.getAmount().doubleValue());
        tx.setSubtotalAmount(grossBD);
        tx.setTotalAmount(grossBD);
        tx.setDiscountAmount(personalPurchaseMode ? java.math.BigDecimal.ZERO : discountTotalBD);
        tx.setCashTendered(java.math.BigDecimal.valueOf(cash));
        tx.setChangeAmount(java.math.BigDecimal.valueOf(change));
        // Set cashier for POS to satisfy DB check constraint
        tx.setCashierId(sessionManager.getCurrentUserId());
        
        com.syos.infrastructure.persistence.repositories.JpaPOSRepository.PersistResult pr = posRepository.savePOSCheckout(tx, lines);

        // Print console bill with assigned bill number
        console.println("\n===== BILL (POS) =====");
        console.println("Bill No: " + pr.billNumber());
        console.println("Date/Time: " + LocalDateTime.now());
        for (com.syos.infrastructure.persistence.repositories.JpaPOSRepository.PosLine pl : lines) {
            // We don't have item names here; show item id and qty
            console.println(String.format("Item #%d  x %.2f  @ LKR %.2f  Disc: LKR %.2f",
                    pl.itemId(), pl.quantity(), pl.unitPrice().doubleValue(), pl.discount() == null ? 0.0 : pl.discount().doubleValue()));
        }
        console.println(String.format("GROSS: LKR %.2f", grandTotal.getAmount().doubleValue()));
        console.println(String.format("DISCOUNT: -LKR %.2f", personalPurchaseMode ? 0.0 : discountTotalBD.doubleValue()));
        console.println(String.format("NET: LKR %.2f", netTotal));
        console.println(String.format("CASH:  LKR %.2f", cash));
        console.println(String.format("CHANGE: LKR %.2f", change));
        console.println("Channel: POS" + (personalPurchaseMode ? " (PERSONAL PURCHASE)" : ""));
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
