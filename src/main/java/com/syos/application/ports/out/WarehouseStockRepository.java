package com.syos.application.ports.out;

import com.syos.application.strategies.stock.BatchInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WarehouseStockRepository {
    List<BatchInfo> findAvailableBatchesForItem(long itemId);
    void allocateFromBatches(long itemId, Map<Long, BigDecimal> batchIdToQty);
    void receiveToWarehouse(long itemId, long batchId, BigDecimal quantity);
    void addStock(long itemId, long batchId, BigDecimal quantity);
    BigDecimal getTotalAvailableStock(long itemId);
}
