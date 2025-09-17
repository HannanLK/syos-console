package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.BatchRepository;
import com.syos.domain.entities.Batch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBatchRepository implements BatchRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final Map<Long, Batch> byId = new HashMap<>();

    private InMemoryWarehouseStockRepository warehouseRepo; // optional link for meta registration

    public void linkWarehouseRepo(InMemoryWarehouseStockRepository repo) {
        this.warehouseRepo = repo;
    }

    @Override
    public Batch save(Batch batch) {
        Long id = batch.getId();
        if (id == null) {
            id = seq.getAndIncrement();
            Batch withId = batch.withId(id);
            byId.put(id, withId);
            if (warehouseRepo != null) {
                warehouseRepo.registerBatchMeta(id, withId.getReceivedDate(), withId.getExpiryDate());
            }
            return withId;
        } else {
            byId.put(id, batch);
            return batch;
        }
    }

    // Test helper
    public Batch findById(long id) { return byId.get(id); }
}
