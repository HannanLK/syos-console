package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.dto.requests.ProductRequest;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.domain.valueobjects.UserID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CLI command to receive stock into WAREHOUSE_STOCK for an existing item.
 * This unblocks transfers when warehouse is empty by letting users add batches.
 */
public class ReceiveStockInWarehouseCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveStockInWarehouseCommand.class);

    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final CompleteProductManagementUseCase productUseCase;

    public ReceiveStockInWarehouseCommand(ConsoleIO console,
                                          SessionManager sessionManager,
                                          CompleteProductManagementUseCase productUseCase) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.productUseCase = productUseCase;
    }

    @Override
    public void execute() {
        try {
            console.println("\n== Receive Stock in Warehouse ==");
            String itemCode = console.readLine("Item Code: ");
            if (itemCode == null || itemCode.trim().isEmpty()) {
                console.printError("Item code is required");
                return;
            }

            String batchNumber = console.readLine("Batch Number: ");
            if (batchNumber == null || batchNumber.trim().isEmpty()) {
                console.printError("Batch number is required");
                return;
            }

            String qtyStr = console.readLine("Quantity received: ");
            double qty;
            try {
                qty = Double.parseDouble(qtyStr);
                if (qty <= 0) {
                    console.printError("Quantity must be positive");
                    return;
                }
            } catch (NumberFormatException ex) {
                console.printError("Invalid quantity");
                return;
            }

            String costStr = console.readLine("Cost price per unit (optional, Enter to skip): ");
            Double cost = null;
            if (costStr != null && !costStr.trim().isEmpty()) {
                try { cost = Double.parseDouble(costStr.trim()); } catch (NumberFormatException ignore) {}
            }

            String mfgStr = console.readLine("Manufacture date (YYYY-MM-DD, optional): ");
            LocalDate mfg = null;
            try { if (mfgStr != null && !mfgStr.isBlank()) mfg = LocalDate.parse(mfgStr.trim()); } catch (Exception e) { console.printWarning("Ignoring invalid manufacture date"); }

            String expStr = console.readLine("Expiry date (YYYY-MM-DDTHH:MM, optional): ");
            LocalDateTime exp = null;
            try { if (expStr != null && !expStr.isBlank()) exp = LocalDateTime.parse(expStr.trim()); } catch (Exception e) { console.printWarning("Ignoring invalid expiry date"); }

            var session = sessionManager.getCurrentSession();
            if (session == null) {
                console.printError("Authentication required");
                return;
            }

            // Build request for receiving stock
            ProductRequest req = new ProductRequest();
            req.setItemCode(itemCode.trim());
            req.setBatchNumber(batchNumber.trim());
            req.setInitialQuantity(qty);
            req.setManufactureDate(mfg);
            req.setExpiryDate(exp);
            // Keep default warehouse location (MAIN-WAREHOUSE)

            var resp = productUseCase.receiveStock(itemCode.trim(), req, UserID.of(session.getUserId()));
            if (resp.isSuccess()) {
                console.printSuccess("Stock received into warehouse successfully.");
            } else {
                console.printError("Failed to receive stock: " + resp.getError());
            }
        } catch (Exception e) {
            logger.error("Error receiving stock", e);
            console.printError("Unexpected error: " + e.getMessage());
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
