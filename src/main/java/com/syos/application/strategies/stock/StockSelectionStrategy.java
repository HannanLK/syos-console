package com.syos.application.strategies.stock;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy Pattern: Defines a stock selection policy for dispatching items from inventory.
 * Implementations can provide FIFO, LIFO, or other batch selection rules.
 */
public interface StockSelectionStrategy {
    /**
     * Selects batch allocations to fulfill a required quantity, according to the strategy.
     * Implementations must not mutate the given input list; they return allocations instead.
     *
     * @param batches sorted or unsorted list of available batches (order is not assumed)
     * @param requiredQuantity quantity needed to be dispatched (must be > 0)
     * @return allocations across one or more batches summing up to the requested quantity where possible
     */
    List<BatchAllocation> selectBatchesForDispatch(List<BatchInfo> batches, BigDecimal requiredQuantity);
}
