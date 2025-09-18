package com.syos.application.ports.out;

import com.syos.domain.entities.Batch;
import com.syos.domain.valueobjects.ItemCode;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Batch entities
 * Supports FIFO stock selection and expiry management
 */
public interface BatchRepository {
    Batch save(Batch batch);
    Optional<Batch> findById(Long id);
    List<Batch> findByItemId(Long itemId);
    List<Batch> findByItemCode(ItemCode itemCode);
    List<Batch> findAvailableByItemId(Long itemId);
    List<Batch> findExpiringBatches(int daysAhead);
    List<Batch> findExpiredBatches();
    List<Batch> findAll();
    void delete(Long id);
    boolean existsById(Long id);
}
