package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.ShelfStockRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryShelfStockRepository implements ShelfStockRepository {
    // itemId -> (batchId -> qty)
    private final Map<Long, Map<Long, BigDecimal>> qtyByItemBatch = new HashMap<>();

    @Override
    public void addToShelf(long itemId, long batchId, BigDecimal quantity) {
        qtyByItemBatch.computeIfAbsent(itemId, k -> new HashMap<>()).merge(batchId, quantity, BigDecimal::add);
    }

    @Override
    public BigDecimal getCurrentStock(long itemId) {
        return qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap())
            .values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Test helper
    public BigDecimal getShelfQty(long itemId, long batchId) {
        return qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap()).getOrDefault(batchId, BigDecimal.ZERO);
    }
}
