package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.domain.entities.WarehouseStock;

import java.util.List;

/**
 * Displays WAREHOUSE_STOCK by batches using FIFO/expiry order as returned by repository.
 */
public class ViewWarehouseInventoryCommand implements Command {
    private final ConsoleIO console;
    private final WarehouseStockRepository warehouseRepo;

    public ViewWarehouseInventoryCommand(ConsoleIO console, WarehouseStockRepository warehouseRepo) {
        this.console = console;
        this.warehouseRepo = warehouseRepo;
    }

    @Override
    public void execute() {
        console.println("\n== Warehouse Stock ==");
        List<WarehouseStock> all = (warehouseRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository)
                ? ((com.syos.adapter.out.persistence.memory.InMemoryWarehouseStockRepository) warehouseRepo).findAll()
                : warehouseRepo.findByLocation("MAIN-WAREHOUSE");

        if (all == null || all.isEmpty()) {
            console.println("No warehouse stock available.");
        } else {
            for (WarehouseStock ws : all) {
                console.println("Item " + ws.getItemCode().getValue() +
                        " | Batch " + ws.getBatchId() +
                        " | Avail " + ws.getQuantityAvailable().getValue() +
                        " | Exp " + (ws.getExpiryDate() == null ? "-" : ws.getExpiryDate()));
            }
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
