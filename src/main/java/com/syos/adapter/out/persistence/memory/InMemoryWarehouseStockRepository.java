package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.strategies.stock.BatchInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryWarehouseStockRepository implements WarehouseStockRepository {
    private static final class BatchMeta {
        long batchId;
        LocalDate received;
        LocalDate expiry;
        BatchMeta(long batchId, LocalDate received, LocalDate expiry) {
            this.batchId = batchId; this.received = received; this.expiry = expiry;
        }
    }

    // itemId -> (batchId -> qty)
    private final Map<Long, Map<Long, BigDecimal>> qtyByItemBatch = new HashMap<>();
    // batchId -> meta
    private final Map<Long, BatchMeta> metaByBatch = new HashMap<>();

    // For tests: capture last allocation
    private Map<Long, BigDecimal> lastAllocation = Collections.emptyMap();

    public void registerBatchMeta(long batchId, LocalDate received, LocalDate expiry) {
        metaByBatch.put(batchId, new BatchMeta(batchId, received, expiry));
    }

    @Override
    public List<BatchInfo> findAvailableBatchesForItem(long itemId) {
        Map<Long, BigDecimal> byBatch = qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap());
        List<BatchInfo> list = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : byBatch.entrySet()) {
            BatchMeta m = metaByBatch.get(e.getKey());
            if (m == null) continue;
            if (e.getValue().signum() > 0) {
                list.add(BatchInfo.of(m.batchId, e.getValue(), m.received, m.expiry));
            }
        }
        return list;
    }

    @Override
    public void allocateFromBatches(long itemId, Map<Long, BigDecimal> batchIdToQty) {
        lastAllocation = new HashMap<>(batchIdToQty);
        Map<Long, BigDecimal> byBatch = qtyByItemBatch.computeIfAbsent(itemId, k -> new HashMap<>());
        for (Map.Entry<Long, BigDecimal> e : batchIdToQty.entrySet()) {
            byBatch.compute(e.getKey(), (k, v) -> {
                BigDecimal current = v == null ? BigDecimal.ZERO : v;
                return current.subtract(e.getValue());
            });
        }
    }

    @Override
    public void receiveToWarehouse(long itemId, long batchId, BigDecimal quantity) {
        Map<Long, BigDecimal> byBatch = qtyByItemBatch.computeIfAbsent(itemId, k -> new HashMap<>());
        byBatch.merge(batchId, quantity, BigDecimal::add);
        // if meta missing, set default received now, expiry null
        metaByBatch.putIfAbsent(batchId, new BatchMeta(batchId, LocalDate.now(), null));
    }

    @Override
    public void addStock(long itemId, long batchId, BigDecimal quantity) {
        receiveToWarehouse(itemId, batchId, quantity);
    }

    @Override
    public BigDecimal getTotalAvailableStock(long itemId) {
        return qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap())
            .values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Test helper
    public Map<Long, BigDecimal> getLastAllocation() { return lastAllocation; }
    public BigDecimal getWarehouseQty(long itemId, long batchId) {
        return qtyByItemBatch.getOrDefault(itemId, Collections.emptyMap()).getOrDefault(batchId, BigDecimal.ZERO);
    }
}
