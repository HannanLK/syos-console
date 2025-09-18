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
        // Legacy use case not aligned with current repository ports.
        // Product transfers are handled by CompleteProductManagementUseCase.
        throw new UnsupportedOperationException("TransferToWebUseCase is deprecated. Use CompleteProductManagementUseCase.transferToWeb()");
    }
}
