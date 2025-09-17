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
 * Use case: Transfer stock from Warehouse to Web Inventory using the configured StockSelectionStrategy.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 3: Separate web inventory from store shelf
 * - FIFO with expiry override for stock allocation
 * - Stock tracking for both channels
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

        // Get available batches from warehouse
        List<BatchInfo> available = warehouseRepo.findAvailableBatchesForItem(itemId);
        
        // Check total availability
        BigDecimal totalAvailable = available.stream()
            .map(BatchInfo::getAvailableQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (totalAvailable.compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                "Not enough stock in warehouse. Available: " + totalAvailable + ", Required: " + quantity);
        }

        // Use strategy to select batches (FIFO with expiry override)
        List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(available, quantity);
        
        // Process each allocation
        Map<Long, BigDecimal> toAllocate = new HashMap<>();
        for (BatchAllocation alloc : allocations) {
            toAllocate.put(alloc.getBatchId(), alloc.getAllocatedQuantity());
            
            // Add to web inventory
            webRepo.addToWebInventory(itemId, alloc.getBatchId(), alloc.getAllocatedQuantity());
            
            // Record transfer
            transferRepo.recordTransfer(itemId, alloc.getBatchId(), "WAREHOUSE", "WEB", "AUTO");
        }

        // Reduce warehouse stock
        warehouseRepo.allocateFromBatches(itemId, toAllocate);
    }
}
