package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.domain.entities.ShelfStock;

import java.util.List;

/**
 * Displays SHELF_STOCK by shelf code and batch.
 */
public class ViewShelfInventoryCommand implements Command {
    private final ConsoleIO console;
    private final ShelfStockRepository shelfRepo;

    public ViewShelfInventoryCommand(ConsoleIO console, ShelfStockRepository shelfRepo) {
        this.console = console;
        this.shelfRepo = shelfRepo;
    }

    @Override
    public void execute() {
        console.println("\n== Shelf Stock ==");
        List<ShelfStock> all = (shelfRepo instanceof com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository)
                ? ((com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository) shelfRepo).findAll()
                : shelfRepo.findAll();

        if (all == null || all.isEmpty()) {
            console.println("No shelf stock available.");
        } else {
            for (ShelfStock ss : all) {
                console.println("Shelf " + ss.getShelfCode() +
                        " | Item " + ss.getItemCode().getValue() +
                        " | Avail " + ss.getQuantityOnShelf().getValue() +
                        " | Exp " + (ss.getExpiryDate() == null ? "-" : ss.getExpiryDate()));
            }
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
