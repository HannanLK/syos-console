package com.syos.application.ports.out;

import com.syos.domain.entities.WarehouseStock;
import com.syos.domain.valueobjects.ItemCode;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for warehouse stock operations
 */
public interface WarehouseStockRepository {
    
    /**
     * Save warehouse stock
     */
    WarehouseStock save(WarehouseStock warehouseStock);
    
    /**
     * Find warehouse stock by ID
     */
    Optional<WarehouseStock> findById(Long id);
    
    /**
     * Find all available warehouse stock for an item
     */
    List<WarehouseStock> findAvailableByItemId(Long itemId);
    
    /**
     * Find all available warehouse stock for an item code
     */
    List<WarehouseStock> findAvailableByItemCode(ItemCode itemCode);
    
    /**
     * Find warehouse stock by item and batch
     */
    List<WarehouseStock> findByItemIdAndBatchId(Long itemId, Long batchId);
    
    /**
     * Find all warehouse stock in a specific location
     */
    List<WarehouseStock> findByLocation(String location);
    
    /**
     * Find reserved warehouse stock
     */
    List<WarehouseStock> findReservedStock();
    
    /**
     * Find expiring warehouse stock within days
     */
    List<WarehouseStock> findExpiringWithinDays(int days);
    
    /**
     * Find expired warehouse stock
     */
    List<WarehouseStock> findExpiredStock();
    
    /**
     * Delete warehouse stock by ID
     */
    void deleteById(Long id);
    
    /**
     * Check if warehouse stock exists for item
     */
    boolean existsByItemId(Long itemId);
}
