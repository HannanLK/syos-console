package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.BatchRepository;
import com.syos.domain.entities.Batch;
import com.syos.domain.valueobjects.ItemCode;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Minimal in-memory BatchRepository implementation for compilation/testing.
 * Note: Does not mutate domain object IDs; stores generated IDs internally.
 */
public class InMemoryBatchRepository implements BatchRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final Map<Long, Batch> byId = new HashMap<>();

    @Override
    public Batch save(Batch batch) {
        Long id = batch.getId();
        if (id == null) {
            id = seq.getAndIncrement();
            // Create a new Batch with the assigned ID using builder
            Batch batchWithId = new Batch.Builder(batch)
                    .id(id)
                    .build();
            byId.put(id, batchWithId);
            return batchWithId;
        } else {
            byId.put(id, batch);
            return batch;
        }
    }

    @Override
    public Optional<Batch> findById(Long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public java.util.List<Batch> findByItemId(Long itemId) {
        return byId.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue().getItemId(), itemId))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<Batch> findByItemCode(ItemCode itemCode) {
        // Not tracked by item code in-memory; return all for now
        return new java.util.ArrayList<>(byId.values());
    }

    @Override
    public java.util.List<Batch> findAvailableByItemId(Long itemId) {
        return findByItemId(itemId).stream()
                .filter(Batch::hasAvailableStock)
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<Batch> findExpiringBatches(int daysAhead) {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().plusDays(daysAhead);
        return byId.values().stream()
                .filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<Batch> findExpiredBatches() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return byId.values().stream()
                .filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(now))
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<Batch> findAll() {
        return new java.util.ArrayList<>(byId.values());
    }

    @Override
    public void delete(Long id) {
        byId.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return byId.containsKey(id);
    }
}
