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

    public TransferToShelfUseCase(WarehouseStockRepository warehouseRepo,
                                  ShelfStockRepository shelfRepo,
                                  StockTransferRepository transferRepo,
                                  StockSelectionStrategy strategy) {
        WarehouseStockRepository warehouseRepo1 = Objects.requireNonNull(warehouseRepo);
        ShelfStockRepository shelfRepo1 = Objects.requireNonNull(shelfRepo);
        StockTransferRepository transferRepo1 = Objects.requireNonNull(transferRepo);
        StockSelectionStrategy strategy1 = Objects.requireNonNull(strategy);
    }

    public void transfer(long itemId, BigDecimal quantity) {
        // Legacy use case isn't aligned with current repository ports.
        // Product transfers are handled by CompleteProductManagementUseCase.
        throw new UnsupportedOperationException("TransferToShelfUseCase is deprecated. Use CompleteProductManagementUseCase.transferToShelf()");
    }
}
