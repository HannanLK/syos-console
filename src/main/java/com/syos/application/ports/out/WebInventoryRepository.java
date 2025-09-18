package com.syos.application.ports.out;

import com.syos.domain.entities.WebInventory;
import com.syos.domain.valueobjects.ItemCode;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WebInventory entities
 * Manages web-specific inventory separate from physical store
 */
public interface WebInventoryRepository {
    void save(WebInventory webInventory);
    Optional<WebInventory> findById(Long id);
    List<WebInventory> findByItemCode(ItemCode itemCode);
    List<WebInventory> findByItemId(Long itemId);
    List<WebInventory> findAvailableItems();
    List<WebInventory> findPublishedItems();
    List<WebInventory> findFeaturedItems();
    List<WebInventory> findExpiredItems();
    List<WebInventory> findLowStockItems();
    List<WebInventory> findAll();
    void delete(Long id);
    boolean existsById(Long id);
}
