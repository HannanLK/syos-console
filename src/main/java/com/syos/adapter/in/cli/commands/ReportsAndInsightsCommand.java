package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.ports.out.BatchRepository;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.WarehouseStock;
import com.syos.domain.entities.ShelfStock;
import com.syos.domain.entities.WebInventory;
import com.syos.domain.entities.Batch;
import com.syos.domain.valueobjects.ItemCode;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reports and Insights menu for Admin and Employee.
 * Minimal implementation to satisfy current requirements:
 * - Provides navigation and basic reports using available repositories
 * - Gracefully degrades with placeholders when transactional data is not yet implemented
 */
public class ReportsAndInsightsCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final ItemMasterFileRepository itemRepo;
    private final WarehouseStockRepository warehouseRepo;
    private final ShelfStockRepository shelfRepo;
    private final WebInventoryRepository webRepo;
    private final BatchRepository batchRepo;
    private final com.syos.application.ports.out.TransactionReportRepository txReportRepo;
    private final com.syos.application.ports.out.BillReportRepository billReportRepo;

    public ReportsAndInsightsCommand(
            ConsoleIO console,
            SessionManager sessionManager,
            ItemMasterFileRepository itemRepo,
            WarehouseStockRepository warehouseRepo,
            ShelfStockRepository shelfRepo,
            WebInventoryRepository webRepo,
            BatchRepository batchRepo,
            com.syos.application.ports.out.TransactionReportRepository txReportRepo,
            com.syos.application.ports.out.BillReportRepository billReportRepo
    ) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.itemRepo = itemRepo;
        this.warehouseRepo = warehouseRepo;
        this.shelfRepo = shelfRepo;
        this.webRepo = webRepo;
        this.batchRepo = batchRepo;
        this.txReportRepo = txReportRepo;
        this.billReportRepo = billReportRepo;
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn() || !(sessionManager.isAdmin() || sessionManager.isEmployee())) {
            console.printError("Admin or Employee access required.");
            return;
        }
        while (true) {
            console.println("\n════════════════════════════════════════════");
            console.println("            REPORTS & INSIGHTS");
            console.println("════════════════════════════════════════════");
            console.println("[1] Standard Reports");
            console.println("[2] Advanced Retail Insights");
            console.println("[B] Back");
            String choice = readKey();
            switch (choice) {
                case "1":
                    standardReportsMenu();
                    break;
                case "2":
                    insightsMenu();
                    break;
                case "B":
                    return;
                default:
                    console.printError("Invalid choice.");
            }
        }
    }

    private void standardReportsMenu() {
        while (true) {
            console.println("\n--- Standard Reports ---");
            console.println("[1] Daily Sales Report");
            console.println("[2] Channel Sales Report");
            console.println("[3] Stock Report (Batch-wise)");
            console.println("[4] Inventory Location Report");
            console.println("[5] Reorder Report (< 50 units)");
            console.println("[6] Bill Report");
            console.println("[7] Reshelving Report (Shelf Restock)");
            console.println("[8] Web Allocation Report (Web Replenishment)");
            console.println("[B] Back");
            String c = readKey();
            switch (c) {
                case "1" -> showDailySalesReport();
                case "2" -> showChannelSalesReport();
                case "3" -> showStockReport();
                case "4" -> showInventoryLocationReport();
                case "5" -> showReorderReport();
                case "6" -> showBillReport();
                case "7" -> showReshelvingReport();
                case "8" -> showWebAllocationReport();
                case "B" -> { return; }
                default -> console.printError("Invalid choice.");
            }
        }
    }

    private void insightsMenu() {
        while (true) {
            console.println("\n--- Advanced Retail Insights ---");
            console.println("[1] Inventory Turnover");
            console.println("[2] Peak Shopping Hours");
            console.println("[3] Product Category Performance");
            console.println("[4] Channel Performance Analysis");
            console.println("[5] Customer Behavior Analytics");
            console.println("[6] Cross-Channel Insights");
            console.println("[7] Inventory Efficiency");
            console.println("[8] Seasonal Trends");
            console.println("[B] Back");
            String c = readKey();
            if ("B".equals(c)) return;
            console.println("\nThis insight will be available once transaction analytics are enabled. (Placeholder)\n");
            console.readLine("Press Enter to continue...");
        }
    }

    // ========== Report Implementations (Minimal Viable) ==========

    private void showStockReport() {
        console.println("\nStock Report (Batch-wise by location)");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            String term = readOptional("Item code/name contains (optional): ");
            String expBeforeStr = readOptional("Expiry before (yyyy-MM-dd, optional): ");
            String expAfterStr = readOptional("Expiry after (yyyy-MM-dd, optional): ");
            java.time.LocalDate expBefore = parseDate(expBeforeStr);
            java.time.LocalDate expAfter = parseDate(expAfterStr);

            List<Batch> batches = batchRepo != null ? batchRepo.findAll() : Collections.emptyList();
            if (batches.isEmpty()) {
                console.println("No batch data available. Showing aggregated stock by location instead.\n");
                showInventoryLocationReport();
                return;
            }
            console.println(String.format("%-14s %-28s %-12s %-12s %-10s %-10s", "Item Code", "Item Name", "MFG", "EXP", "Recv", "Avail"));
            for (Batch b : batches) {
                Long itemId = b.getItemId();
                String code = "";
                String name = "<Unknown>";
                if (itemId != null) {
                    try {
                        var opt = itemRepo.findById(itemId);
                        if (opt.isPresent()) {
                            var imf = opt.get();
                            code = imf.getItemCode().getValue();
                            name = imf.getItemName();
                        }
                    } catch (Exception ignored) { }
                }
                if (code.isBlank()) code = "#" + (itemId != null ? itemId : 0);
                // Apply filters
                if (!containsIgnoreCase(code, term) && !containsIgnoreCase(name, term)) continue;
                if (expBefore != null && b.getExpiryDate() != null && !b.getExpiryDate().toLocalDate().isBefore(expBefore)) continue;
                if (expAfter != null && b.getExpiryDate() != null && !b.getExpiryDate().toLocalDate().isAfter(expAfter)) continue;

                String mfg = b.getManufactureDate() != null ? b.getManufactureDate().format(dtf) : "-";
                String exp = b.getExpiryDate() != null ? b.getExpiryDate().format(dtf) : "-";
                String recv = safeBD(b.getQuantityReceived());
                String avail = safeBD(b.getQuantityAvailable());
                console.println(String.format("%-14s %-28s %-12s %-12s %-10s %-10s", code, truncate(name,28), mfg, exp, recv, avail));
            }
        } catch (Exception ex) {
            console.printError("Failed to build stock report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showInventoryLocationReport() {
        console.println("\nInventory Location Report (STORE/SHELF/WEB)");
        try {
            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("WAREHOUSE_STOCK", sumWarehouse());
            totals.put("SHELF_STOCK", sumShelf());
            totals.put("WEB_INVENTORY", sumWeb());
            console.println(String.format("%-20s %-15s", "Location", "Total Qty"));
            for (var e : totals.entrySet()) {
                console.println(String.format("%-20s %-15s", e.getKey(), e.getValue().toPlainString()));
            }
        } catch (Exception ex) {
            console.printError("Failed to build inventory location report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showReorderReport() {
        console.println("\nReorder Report (Items below threshold across all locations)");
        try {
            String thrStr = readOptional("Threshold [default=50]: ");
            java.math.BigDecimal threshold = parseThreshold(thrStr, new java.math.BigDecimal("50"));
            String term = readOptional("Item code/name contains (optional): ");
            // Aggregate quantities across locations per item code
            Map<ItemCode, BigDecimal> byCode = aggregateTotalsByItemCode();
            List<ItemMasterFile> allActive = itemRepo.findAllActive();
            boolean any = false;
            console.println(String.format("%-14s %-28s %-10s", "Item Code", "Item Name", "Total Qty"));
            for (ItemMasterFile item : allActive) {
                ItemCode code = item.getItemCode();
                if (!containsIgnoreCase(code.getValue(), term) && !containsIgnoreCase(item.getItemName(), term)) continue;
                BigDecimal total = byCode.getOrDefault(code, BigDecimal.ZERO);
                if (total.compareTo(threshold) < 0) {
                    any = true;
                    console.println(String.format("%-14s %-28s %-10s", code.getValue(), truncate(item.getItemName(), 28), total.toPlainString()));
                }
            }
            if (!any) console.println("No items below threshold or no stock data available.");
        } catch (Exception ex) {
            console.printError("Failed to build reorder report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showReshelvingReport() {
        console.println("\nReshelving Report (Move from Warehouse to Shelf)");
        try {
            String thrStr = readOptional("Shelf threshold [default=50]: ");
            java.math.BigDecimal threshold = parseThreshold(thrStr, new java.math.BigDecimal("50"));
            String term = readOptional("Item code/name contains (optional): ");
            Map<ItemCode, BigDecimal> shelfTotals = aggregateByItemCodeShelf();
            Map<ItemCode, BigDecimal> warehouseTotals = aggregateByItemCodeWarehouse();
            boolean any = false;
            console.println(String.format("%-14s %-28s %-12s %-12s", "Item Code", "Item Name", "ShelfQty", "WarehouseQty"));
            Set<ItemCode> codes = new HashSet<>();
            codes.addAll(shelfTotals.keySet());
            codes.addAll(warehouseTotals.keySet());
            for (ItemCode code : codes) {
                BigDecimal s = shelfTotals.getOrDefault(code, BigDecimal.ZERO);
                BigDecimal w = warehouseTotals.getOrDefault(code, BigDecimal.ZERO);
                String name = lookupName(code);
                if (!containsIgnoreCase(code.getValue(), term) && !containsIgnoreCase(name, term)) continue;
                if (s.compareTo(threshold) < 0 && w.compareTo(BigDecimal.ZERO) > 0) {
                    any = true;
                    console.println(String.format("%-14s %-28s %-12s %-12s", code.getValue(), truncate(name,28), s.toPlainString(), w.toPlainString()));
                }
            }
            if (!any) console.println("No shelf replenishment needed based on current thresholds.");
        } catch (Exception ex) {
            console.printError("Failed to build reshelving report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showWebAllocationReport() {
        console.println("\nWeb Allocation Report (Move from Warehouse to Web)");
        try {
            String thrStr = readOptional("Web inventory threshold [default=50]: ");
            java.math.BigDecimal threshold = parseThreshold(thrStr, new java.math.BigDecimal("50"));
            String term = readOptional("Item code/name contains (optional): ");
            Map<ItemCode, BigDecimal> webTotals = aggregateByItemCodeWeb();
            Map<ItemCode, BigDecimal> warehouseTotals = aggregateByItemCodeWarehouse();
            boolean any = false;
            console.println(String.format("%-14s %-28s %-12s %-12s", "Item Code", "Item Name", "WebQty", "WarehouseQty"));
            Set<ItemCode> codes = new HashSet<>();
            codes.addAll(webTotals.keySet());
            codes.addAll(warehouseTotals.keySet());
            for (ItemCode code : codes) {
                BigDecimal wv = webTotals.getOrDefault(code, BigDecimal.ZERO);
                BigDecimal wh = warehouseTotals.getOrDefault(code, BigDecimal.ZERO);
                String name = lookupName(code);
                if (!containsIgnoreCase(code.getValue(), term) && !containsIgnoreCase(name, term)) continue;
                if (wv.compareTo(threshold) < 0 && wh.compareTo(BigDecimal.ZERO) > 0) {
                    any = true;
                    console.println(String.format("%-14s %-28s %-12s %-12s", code.getValue(), truncate(name,28), wv.toPlainString(), wh.toPlainString()));
                }
            }
            if (!any) console.println("No web allocation needed based on current thresholds.");
        } catch (Exception ex) {
            console.printError("Failed to build web allocation report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showDailySalesReport() {
        console.println("\nDaily Sales Report");
        try {
            String d = readOptional("Filter by date (yyyy-MM-dd) [default=today]: ");
            java.time.LocalDate date = parseDate(d);
            if (date == null) date = java.time.LocalDate.now();
            Object[] summary = (txReportRepo != null) ? txReportRepo.findDailySummary(date) : new Object[]{0L, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO};
            long txCount = ((Number) summary[0]).longValue();
            java.math.BigDecimal total = (java.math.BigDecimal) summary[1];
            java.math.BigDecimal discount = (java.math.BigDecimal) summary[2];
            java.math.BigDecimal net = total.subtract(discount == null ? java.math.BigDecimal.ZERO : discount);
            console.println(String.format("Date: %s", date));
            console.println(String.format("Transactions: %d", txCount));
            console.println(String.format("Total Revenue (Gross): %s", total.toPlainString()));
            console.println(String.format("Total Discount: %s", (discount == null ? java.math.BigDecimal.ZERO : discount).toPlainString()));
            console.println(String.format("Net Revenue: %s", net.toPlainString()));

            // Items sold
            String itemFilter = readOptional("Item code/name contains (optional): ");
            console.println("\nItems Sold");
            if (txReportRepo != null) {
                java.util.List<Object[]> rows = txReportRepo.findDailyItemAggregates(date);
                if (rows == null || rows.isEmpty()) {
                    console.println("No items for the specified date.");
                } else {
                    // Apply filter in-memory
                    java.util.List<Object[]> filtered = rows.stream().filter(r -> {
                        String code = (String) r[0];
                        String name = (String) r[1];
                        return containsIgnoreCase(code, itemFilter) || containsIgnoreCase(name, itemFilter);
                    }).collect(java.util.stream.Collectors.toList());
                    if (filtered.isEmpty()) {
                        console.println("No items match the filter.");
                    } else {
                        console.println(String.format("%-14s %-28s %-10s %-12s", "Item Code", "Item Name", "Qty", "Revenue"));
                        for (Object[] r : filtered) {
                            String code = (String) r[0];
                            String name = (String) r[1];
                            long qty = ((Number) r[2]).longValue();
                            java.math.BigDecimal rev = (java.math.BigDecimal) r[3];
                            console.println(String.format("%-14s %-28s %-10d %-12s", code, truncate(name, 28), qty, rev.toPlainString()));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            console.printError("Failed to build daily sales report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showChannelSalesReport() {
        console.println("\nChannel Sales Report (POS vs WEB)");
        try {
            String s = readOptional("Start date (yyyy-MM-dd) [default=today]: ");
            String e = readOptional("End date (yyyy-MM-dd) [default=+1 day]: ");
            String ch = readOptional("Filter channel (POS/WEB, optional): ");
            java.time.LocalDate startDate = parseDate(s);
            if (startDate == null) startDate = java.time.LocalDate.now();
            java.time.LocalDate endDate = parseDate(e);
            if (endDate == null) endDate = startDate.plusDays(1);
            java.time.LocalDateTime start = startDate.atStartOfDay();
            java.time.LocalDateTime end = endDate.atStartOfDay();
            java.util.List<Object[]> rows = (txReportRepo != null) ? txReportRepo.findChannelSummary(start, end) : java.util.Collections.emptyList();
            if (!ch.isBlank()) {
                String chc = ch.trim().toUpperCase();
                rows = rows.stream().filter(r -> chc.equals(((String) r[0]).toUpperCase())).collect(java.util.stream.Collectors.toList());
            }
            if (rows.isEmpty()) {
                console.println("No transactions found for the selected range.");
            } else {
                console.println(String.format("Range: %s to %s", startDate, endDate.minusDays(1)));
                console.println(String.format("%-8s %-10s %-14s %-14s", "Channel", "Count", "Total(LKR)", "Avg Order"));
                long totalCount = 0;
                java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;
                for (Object[] r : rows) {
                    String channel = (String) r[0];
                    long count = ((Number) r[1]).longValue();
                    java.math.BigDecimal sum = (java.math.BigDecimal) r[2];
                    java.math.BigDecimal avg = (java.math.BigDecimal) r[3];
                    totalCount += count;
                    grandTotal = grandTotal.add(sum);
                    console.println(String.format("%-8s %-10d %-14s %-14s", channel, count, sum.toPlainString(), avg.toPlainString()));
                }
                console.println(String.format("%-8s %-10d %-14s", "TOTAL", totalCount, grandTotal.toPlainString()));
            }
        } catch (Exception ex) {
            console.printError("Failed to build channel sales report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    private void showBillReport() {
        console.println("\nBill Report (All customer transactions)");
        try {
            String mode = readOptional("Filter mode: [1] Specific date, [2] Date range, [Enter]=today: ");
            java.util.List<Object[]> rows = java.util.Collections.emptyList();
            if (billReportRepo != null) {
                if ("2".equals(mode)) {
                    String s = readOptional("Start date (yyyy-MM-dd): ");
                    String e = readOptional("End date (yyyy-MM-dd): ");
                    java.time.LocalDate sd = parseDate(s);
                    java.time.LocalDate ed = parseDate(e);
                    if (sd != null && ed != null) {
                        rows = billReportRepo.listBillsBetween(sd.atStartOfDay(), ed.plusDays(1).atStartOfDay());
                    } else {
                        console.printError("Invalid dates entered. Falling back to today.");
                        java.time.LocalDate today = java.time.LocalDate.now();
                        rows = billReportRepo.listBillsForDate(today);
                    }
                } else {
                    String d = readOptional("Date (yyyy-MM-dd) [default=today]: ");
                    java.time.LocalDate date = parseDate(d);
                    if (date == null) date = java.time.LocalDate.now();
                    rows = billReportRepo.listBillsForDate(date);
                    if (rows.isEmpty()) {
                        rows = billReportRepo.listRecentBills(10);
                        if (!rows.isEmpty()) {
                            console.println("No bills for the selected date. Showing last 10 bills:");
                        }
                    }
                }
            }
            if (rows.isEmpty()) {
                console.println("No bills available.");
            } else {
                String ch = readOptional("Filter channel (POS/WEB, optional): ");
                String customer = readOptional("Customer name contains (optional): ");
                java.util.List<Object[]> filtered = rows.stream().filter(r -> {
                    String channel = (String) r[2];
                    String cust = (String) r[4];
                    boolean okCh = ch.isBlank() || (channel != null && channel.equalsIgnoreCase(ch));
                    boolean okCu = containsIgnoreCase(cust, customer);
                    return okCh && okCu;
                }).collect(java.util.stream.Collectors.toList());
                if (filtered.isEmpty()) {
                    console.println("No bills match the filters.");
                } else {
                    console.println(String.format("%-8s %-20s %-8s %-12s %-20s", "Serial", "Date/Time", "Type", "Amount", "Customer"));
                    for (Object[] r : filtered) {
                        String serial = (String) r[0];
                        java.time.LocalDateTime when = (java.time.LocalDateTime) r[1];
                        String type = (String) r[2];
                        java.math.BigDecimal total = (java.math.BigDecimal) r[3];
                        String cust = (String) r[4];
                        String dt = when.toString().replace('T', ' ');
                        console.println(String.format("%-8s %-20s %-8s %-12s %-20s", serial, dt, type, total.toPlainString(), cust != null ? cust : "-"));
                    }
                }
            }
        } catch (Exception ex) {
            console.printError("Failed to build bill report: " + ex.getMessage());
        }
        console.readLine("\nPress Enter to continue...");
    }

    // ========== Helpers ==========

    private String readKey() {
        String s = console.readLine("Enter choice: ");
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        if (s.equalsIgnoreCase("b")) return "B";
        return s;
    }

    private String lookupName(ItemCode code) {
        try {
            return itemRepo.findByItemCode(code).map(ItemMasterFile::getItemName).orElse("<Unknown>");
        } catch (Exception e) {
            return "<Unknown>";
        }
    }

    private BigDecimal sumWarehouse() {
        try {
            List<WarehouseStock> list;
            if (warehouseRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository mem) {
                list = mem.findAll();
            } else {
                list = warehouseRepo.findByLocation("MAIN-WAREHOUSE");
            }
            return list.stream()
                    .map(ws -> ws.getQuantityAvailable().toBigDecimal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal sumShelf() {
        try {
            List<ShelfStock> list;
            if (shelfRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository mem) {
                list = mem.findAll();
            } else {
                list = shelfRepo.findAll();
            }
            return list.stream()
                    .map(ss -> ss.getQuantityOnShelf().toBigDecimal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal sumWeb() {
        try {
            List<WebInventory> list;
            if (webRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWebInventoryRepository mem) {
                list = mem.findAll();
            } else {
                list = webRepo.findAll();
            }
            return list.stream()
                    .map(wi -> wi.getQuantityAvailable().toBigDecimal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private Map<ItemCode, BigDecimal> aggregateTotalsByItemCode() {
        Map<ItemCode, BigDecimal> totals = new HashMap<>();
        mergeMap(totals, aggregateByItemCodeWarehouse());
        mergeMap(totals, aggregateByItemCodeShelf());
        mergeMap(totals, aggregateByItemCodeWeb());
        return totals;
    }

    private Map<ItemCode, BigDecimal> aggregateByItemCodeWarehouse() {
        Map<ItemCode, BigDecimal> map = new HashMap<>();
        try {
            List<WarehouseStock> list;
            if (warehouseRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository mem) {
                list = mem.findAll();
            } else {
                list = warehouseRepo.findByLocation("MAIN-WAREHOUSE");
            }
            for (WarehouseStock ws : list) {
                map.merge(ws.getItemCode(), ws.getQuantityAvailable().toBigDecimal(), BigDecimal::add);
            }
        } catch (Exception ignored) { }
        return map;
    }

    private Map<ItemCode, BigDecimal> aggregateByItemCodeShelf() {
        Map<ItemCode, BigDecimal> map = new HashMap<>();
        try {
            List<ShelfStock> list;
            if (shelfRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository mem) {
                list = mem.findAll();
            } else {
                list = shelfRepo.findAll();
            }
            for (ShelfStock ss : list) {
                map.merge(ss.getItemCode(), ss.getQuantityOnShelf().toBigDecimal(), BigDecimal::add);
            }
        } catch (Exception ignored) { }
        return map;
    }

    private Map<ItemCode, BigDecimal> aggregateByItemCodeWeb() {
        Map<ItemCode, BigDecimal> map = new HashMap<>();
        try {
            List<WebInventory> list;
            if (webRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWebInventoryRepository mem) {
                list = mem.findAll();
            } else {
                list = webRepo.findAll();
            }
            for (WebInventory wi : list) {
                map.merge(wi.getItemCode(), wi.getQuantityAvailable().toBigDecimal(), BigDecimal::add);
            }
        } catch (Exception ignored) { }
        return map;
    }

    private void mergeMap(Map<ItemCode, BigDecimal> base, Map<ItemCode, BigDecimal> add) {
        for (var e : add.entrySet()) {
            base.merge(e.getKey(), e.getValue(), BigDecimal::add);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String safeBD(com.syos.domain.valueobjects.Quantity q) {
        return q != null ? q.toBigDecimal().toPlainString() : "0";
    }

    // ====== Input helpers for filters ======
    private String readOptional(String prompt) {
        String s = console.readLine(prompt);
        return s == null ? "" : s.trim();
    }
    private java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(s.trim());
        } catch (Exception e) {
            console.printError("Invalid date format. Expected yyyy-MM-dd. Ignoring filter.");
            return null;
        }
    }
    private java.math.BigDecimal parseThreshold(String s, java.math.BigDecimal def) {
        if (s == null || s.isBlank()) return def;
        try { return new java.math.BigDecimal(s.trim()); } catch (Exception e) {
            console.printError("Invalid number. Using default " + def.toPlainString());
            return def;
        }
    }
    private boolean containsIgnoreCase(String haystack, String needle) {
        if (needle == null || needle.isBlank()) return true;
        if (haystack == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }
}
