package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.StockTransferRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.ports.out.WebInventoryRepository;
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
 * Use case: Transfer stock from Warehouse to WebInventory using the configured StockSelectionStrategy.
 */
public class TransferToWebUseCase {
    private final WarehouseStockRepository warehouseRepo;
    private final WebInventoryRepository webRepo;
    private final StockTransferRepository transferRepo;
    private final StockSelectionStrategy strategy;

    public TransferToWebUseCase(WarehouseStockRepository warehouseRepo,
                                WebInventoryRepository webRepo,
                                StockTransferRepository transferRepo,
                                StockSelectionStrategy strategy) {
        this.warehouseRepo = Objects.requireNonNull(warehouseRepo);
        this.webRepo = Objects.requireNonNull(webRepo);
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
            webRepo.addToWebInventory(itemId, alloc.getBatchId(), alloc.getAllocatedQuantity());
            transferRepo.recordTransfer(itemId, alloc.getBatchId(), "WAREHOUSE", "WEB", "AUTO");
        }
        warehouseRepo.allocateFromBatches(itemId, toAllocate);
    }
}
