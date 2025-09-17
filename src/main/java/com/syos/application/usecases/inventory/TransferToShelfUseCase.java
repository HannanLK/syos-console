package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.application.ports.out.StockTransferRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.strategies.stock.BatchAllocation;
import com.syos.application.strategies.stock.BatchInfo;
import com.syos.application.strategies.stock.StockSelectionStrategy;
import com.syos.domain.exceptions.InsufficientStockException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Use case: Transfer stock from Warehouse to Shelf using the configured StockSelectionStrategy.
 */
public class TransferToShelfUseCase {
    private final WarehouseStockRepository warehouseRepo;
    private final ShelfStockRepository shelfRepo;
    private final StockTransferRepository transferRepo;
    private final StockSelectionStrategy strategy;

    public TransferToShelfUseCase(WarehouseStockRepository warehouseRepo,
                                  ShelfStockRepository shelfRepo,
                                  StockTransferRepository transferRepo,
                                  StockSelectionStrategy strategy) {
        this.warehouseRepo = Objects.requireNonNull(warehouseRepo);
        this.shelfRepo = Objects.requireNonNull(shelfRepo);
        this.transferRepo = Objects.requireNonNull(transferRepo);
        this.strategy = Objects.requireNonNull(strategy);
    }

    public void transfer(long itemId, BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be > 0");
        }
        List<BatchInfo> available = warehouseRepo.findAvailableBatchesForItem(itemId);
        BigDecimal totalAvailable = available.stream().map(BatchInfo::getAvailableQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailable.compareTo(quantity) < 0) {
            throw new InsufficientStockException("Not enough stock in warehouse");
        }
        List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(available, quantity);
        Map<Long, BigDecimal> toAllocate = new HashMap<>();
        for (BatchAllocation alloc : allocations) {
            toAllocate.put(alloc.getBatchId(), alloc.getAllocatedQuantity());
            shelfRepo.addToShelf(itemId, alloc.getBatchId(), alloc.getAllocatedQuantity());
            transferRepo.recordTransfer(itemId, alloc.getBatchId(), "WAREHOUSE", "SHELF", "AUTO");
        }
        warehouseRepo.allocateFromBatches(itemId, toAllocate);
    }
}
