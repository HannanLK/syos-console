package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.infrastructure.persistence.repositories.JpaPromotionRepository;
import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple CLI to create batch-specific promotions and link to item and batches.
 * Available to Employees and Admins.
 */
public class ManagePromotionsCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final JpaPromotionRepository promoRepo;

    public ManagePromotionsCommand(ConsoleIO console, SessionManager sessionManager, JpaPromotionRepository promoRepo) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.promoRepo = promoRepo;
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn()) {
            console.printError("Login required");
            return;
        }
        console.println("\n=== DISCOUNTS & PROMOTIONS ===");
        console.println("1) Create Batch-Specific Promotion");
        console.println("B) Back");
        String choice = console.readLine("Choose: ");
        if (!"1".equalsIgnoreCase(choice)) return;

        String code = console.readLine("Promo Code: ");
        String name = console.readLine("Promo Name: ");
        String typeStr = console.readLine("Type [PERCENTAGE/FIXED_AMOUNT]: ");
        String valStr = console.readLine("Discount Value (number): ");
        String itemIdStr = console.readLine("Target Item ID: ");
        String batchesStr = console.readLine("Batch IDs (comma separated): ");
        String startStr = console.readLine("Start (YYYY-MM-DDTHH:MM, empty=now): ");
        String endStr = console.readLine("End (YYYY-MM-DDTHH:MM, empty=+30d): ");
        try {
            PromotionType type = PromotionType.valueOf(typeStr.trim().toUpperCase());
            BigDecimal value = new BigDecimal(valStr.trim());
            Long itemId = Long.parseLong(itemIdStr.trim());
            List<Long> batchIds = new ArrayList<>();
            for (String s : batchesStr.split(",")) {
                if (!s.isBlank()) batchIds.add(Long.parseLong(s.trim()));
            }
            LocalDateTime start = (startStr == null || startStr.isBlank()) ? LocalDateTime.now() : LocalDateTime.parse(startStr.trim());
            LocalDateTime end = (endStr == null || endStr.isBlank()) ? LocalDateTime.now().plusDays(30) : LocalDateTime.parse(endStr.trim());
            promoRepo.createBasicBatchPromotion(code.trim(), name.trim(), type, value, start, end, itemId, batchIds);
            console.printSuccess("Promotion created and linked to batches.");
        } catch (Exception ex) {
            console.printError("Failed to create promotion: " + ex.getMessage());
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
