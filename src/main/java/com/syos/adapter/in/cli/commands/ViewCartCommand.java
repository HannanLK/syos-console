package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.WebInventory;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Simple in-memory shopping cart and checkout for WEB channel.
 * - Per-user cart stored statically in this command (session-scoped).
 * - Stock deducted from WEB_INVENTORY on successful card payment.
 * - Payment rule: any 16-digit number except 0767600730204128 succeeds.
 */
public class ViewCartCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final ItemMasterFileRepository itemRepo;
    private final WebInventoryRepository webRepo;

    // userId -> (itemCode -> qty)
    private static final Map<Long, Map<String, Double>> CARTS = new HashMap<>();

    // very light web order history log
    public static class WebOrder {
        public final long orderNo;
        public final long userId;
        public final LocalDateTime dateTime;
        public final List<OrderLine> lines;
        public final double total;
        public WebOrder(long orderNo, long userId, LocalDateTime dateTime, List<OrderLine> lines, double total) {
            this.orderNo = orderNo; this.userId = userId; this.dateTime = dateTime; this.lines = lines; this.total = total;
        }
    }
    public static class OrderLine { public final String name; public final double qty; public final double unit; public final double lineTotal; public OrderLine(String n,double q,double u){name=n;qty=q;unit=u;lineTotal=q*u;} }
    private static long WEB_ORDER_SEQ = 1;
    private static final List<WebOrder> ORDERS = new ArrayList<>();

    public ViewCartCommand(ConsoleIO console, SessionManager sessionManager,
                           ItemMasterFileRepository itemRepo, WebInventoryRepository webRepo) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.itemRepo = itemRepo;
        this.webRepo = webRepo;
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isCustomer()) {
            console.printError("Customer login required to view cart.");
            return;
        }
        long userId = sessionManager.getCurrentUserId();
        Map<String, Double> cart = CARTS.computeIfAbsent(userId, k -> new LinkedHashMap<>());

        while (true) {
            console.println("\n\u2554\u2550\u2550 SHOPPING CART \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            if (cart.isEmpty()) {
                console.println("Your cart is empty.");
            } else {
                printCart(cart);
            }
            console.println("\nOptions: 1) Add  2) Update  3) Remove  4) Checkout  5) Back");
            String choice = console.readLine("Choose: ");
            switch (choice) {
                case "1" -> addToCart(cart);
                case "2" -> updateCart(cart);
                case "3" -> removeFromCart(cart);
                case "4" -> { if (checkout(cart, userId)) return; }
                case "5" -> { return; }
                default -> console.printError("Invalid choice");
            }
        }
    }

    private void printCart(Map<String, Double> cart) {
        console.println(String.format("%-14s %-24s %-8s %-10s", "Item Code", "Name", "Qty", "Price"));
        double total = 0.0;
        for (var e : cart.entrySet()) {
            String code = e.getKey();
            double qty = e.getValue();
            var itemOpt = itemRepo.findByItemCode(ItemCode.of(code));
            String name = itemOpt.map(ItemMasterFile::getItemName).orElse(code);
            double price = itemOpt.map(i -> i.getSellingPrice().toBigDecimal().doubleValue()).orElse(0.0);
            console.println(String.format("%-14s %-24s %-8.2f %-10.2f", code, name, qty, price));
            total += price * qty;
        }
        console.println(String.format("\nTotal: LKR %.2f", total));
    }

    private void addToCart(Map<String, Double> cart) {
        String code = console.readLine("Item Code to add: ");
        String qtyStr = console.readLine("Quantity: ");
        try {
            double qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { console.printError("Quantity must be positive."); return; }
            // basic availability check in WEB_INVENTORY
            double available = webRepo.findByItemCode(ItemCode.of(code)).stream()
                    .filter(WebInventory::isAvailableForPurchase)
                    .map(w -> w.getQuantityAvailable().toBigDecimal())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    .doubleValue();
            double existing = cart.getOrDefault(code.trim(), 0.0);
            if (available < existing + qty) {
                console.printError("Insufficient WEB stock. Available: " + available);
                return;
            }
            cart.put(code.trim(), existing + qty);
            console.printSuccess("Added to cart.");
        } catch (NumberFormatException ex) {
            console.printError("Invalid quantity");
        }
    }

    private void updateCart(Map<String, Double> cart) {
        String code = console.readLine("Item Code to update: ");
        if (!cart.containsKey(code)) { console.printError("Item not in cart"); return; }
        String qtyStr = console.readLine("New Quantity (0 to remove): ");
        try {
            double qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { cart.remove(code); console.printWarning("Removed from cart."); return; }
            double available = webRepo.findByItemCode(ItemCode.of(code)).stream()
                    .filter(WebInventory::isAvailableForPurchase)
                    .map(w -> w.getQuantityAvailable().toBigDecimal())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    .doubleValue();
            if (available < qty) { console.printError("Insufficient WEB stock. Available: " + available); return; }
            cart.put(code, qty);
            console.printSuccess("Updated.");
        } catch (NumberFormatException ex) {
            console.printError("Invalid quantity");
        }
    }

    private void removeFromCart(Map<String, Double> cart) {
        String code = console.readLine("Item Code to remove: ");
        if (cart.remove(code) != null) console.printSuccess("Removed."); else console.printWarning("Item not found in cart.");
    }

    private boolean checkout(Map<String, Double> cart, long userId) {
        if (cart.isEmpty()) { console.printWarning("Cart is empty."); return false; }
        // compute total
        double total = cart.entrySet().stream().mapToDouble(e ->
                itemRepo.findByItemCode(ItemCode.of(e.getKey()))
                        .map(i -> i.getSellingPrice().toBigDecimal().doubleValue() * e.getValue())
                        .orElse(0.0)
        ).sum();
        console.println(String.format("Grand Total: LKR %.2f", total));
        String card = console.readLine("Card Number (16 digits): ");
        if (card == null || !card.matches("\\d{16}")) { console.printError("Invalid card number"); return false; }
        if ("0767600730204128".equals(card)) { console.printError("Payment declined."); return false; }

        // Deduct from WEB_INVENTORY using FIFO with expiry override
        UserID uid = UserID.of(userId);
        for (var e : cart.entrySet()) {
            String code = e.getKey();
            double qty = e.getValue();
            java.math.BigDecimal remaining = java.math.BigDecimal.valueOf(qty);
            List<WebInventory> stocks = new ArrayList<>(webRepo.findByItemCode(ItemCode.of(code)));
            stocks.removeIf(s -> !s.isAvailableForPurchase());
            stocks.sort(this::fifoWithExpiryComparator);
            for (WebInventory wi : stocks) {
                if (remaining.compareTo(java.math.BigDecimal.ZERO) <= 0) break;
                java.math.BigDecimal availableHere = wi.getQuantityAvailable().toBigDecimal();
                java.math.BigDecimal take = remaining.min(availableHere);
                WebInventory after = wi.sellStock(Quantity.of(take), uid);
                webRepo.save(after);
                remaining = remaining.subtract(take);
            }
        }

        // Log order
        List<OrderLine> lines = new ArrayList<>();
        cart.forEach((code, qty) -> {
            ItemMasterFile item = itemRepo.findByItemCode(ItemCode.of(code)).orElse(null);
            if (item != null) {
                double unit = item.getSellingPrice().toBigDecimal().doubleValue();
                lines.add(new OrderLine(item.getItemName(), qty, unit));
            }
        });
        long orderNo = WEB_ORDER_SEQ++;
        ORDERS.add(new WebOrder(orderNo, userId, LocalDateTime.now(), lines, total));

        // Clear cart
        cart.clear();

        console.printSuccess("Payment successful. Order No: " + orderNo);
        console.println("Channel: WEB");
        console.println("Press Enter to continue...");
        console.readLine();
        return true;
    }

    // expiry first, then addedToWebDate FIFO
    private int fifoWithExpiryComparator(WebInventory a, WebInventory b) {
        var ea = a.getExpiryDate();
        var eb = b.getExpiryDate();
        if (ea != null && eb != null && !ea.equals(eb)) {
            return ea.isBefore(eb) ? -1 : 1;
        }
        var pa = a.getAddedToWebDate();
        var pb = b.getAddedToWebDate();
        if (pa == null && pb == null) return 0;
        if (pa == null) return 1;
        if (pb == null) return -1;
        return pa.isBefore(pb) ? -1 : (pa.isAfter(pb) ? 1 : 0);
    }

    // Exposed for OrderHistoryCommand
    public static List<WebOrder> getOrdersForUser(long userId) {
        List<WebOrder> list = new ArrayList<>();
        for (WebOrder o : ORDERS) if (o.userId == userId) list.add(o);
        return list;
    }
}
