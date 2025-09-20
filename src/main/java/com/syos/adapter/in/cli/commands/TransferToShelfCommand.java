package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.domain.valueobjects.UserID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to transfer stock from WAREHOUSE_STOCK to SHELF_STOCK.
 * Provides the core function requested by the scenario: employees/admins can
 * allocate items to store shelves for POS channel.
 */
public class TransferToShelfCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TransferToShelfCommand.class);

    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final CompleteProductManagementUseCase productUseCase;

    public TransferToShelfCommand(ConsoleIO console,
                                  SessionManager sessionManager,
                                  CompleteProductManagementUseCase productUseCase) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.productUseCase = productUseCase;
    }

    @Override
    public void execute() {
        try {
            console.println("\n== Transfer Stock to Shelf ==");
            String itemCode = console.readLine("Item Code: ");
            String shelfCode = console.readLine("Shelf Code (e.g., A1-001): ");
            String qtyStr = console.readLine("Quantity to transfer: ");

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

            var session = sessionManager.getCurrentSession();
            if (session == null) {
                console.printError("Authentication required");
                return;
            }

            var response = productUseCase.transferToShelf(itemCode, shelfCode, qty, UserID.of(session.getUserId()));
            if (response.isSuccess()) {
                console.printSuccess("Stock transferred to shelf successfully.");
            } else {
                console.printError("Failed: " + response.getError());
            }
        } catch (Exception e) {
            logger.error("Error during transfer to shelf", e);
            console.printError("Unexpected error: " + e.getMessage());
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
