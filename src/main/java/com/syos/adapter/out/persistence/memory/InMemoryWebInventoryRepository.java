package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.WebInventoryRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryWebInventoryRepository implements WebInventoryRepository {
    // itemId -> (batchId -> qty)
    private final Map<Long, Map<Long, BigDecimal>> qtyByItemBatch = new HashMap<>();

    @Override
    public void addToWebInventory(long itemId, long batchId, BigDecimal quantity) {
        qtyByItemBatch.computeIfAbsent(itemId, k -> new HashMap<>()).merge(batchId, quantity, BigDecimal::add);
    }

    // Test helper
    public BigDecimal getWebQty(long itemId, long batchId) {
        return qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap()).getOrDefault(batchId, BigDecimal.ZERO);
    }
}
