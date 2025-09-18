package com.syos.application.ports.out;

import com.syos.domain.entities.ShelfStock;
import com.syos.domain.valueobjects.ItemCode;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShelfStock entities
 * Manages shelf inventory and stock levels
 */
public interface ShelfStockRepository {
    void save(ShelfStock shelfStock);
    Optional<ShelfStock> findById(Long id);
    List<ShelfStock> findByItemCode(ItemCode itemCode);
    List<ShelfStock> findByShelfCode(String shelfCode);
    List<ShelfStock> findByItemIdAndShelfCode(Long itemId, String shelfCode);
    List<ShelfStock> findAvailableByItemCode(ItemCode itemCode);
    List<ShelfStock> findDisplayedItems();
    List<ShelfStock> findExpiredItems();
    List<ShelfStock> findExpiringSoonItems();
    List<ShelfStock> findLowStockItems();
    List<ShelfStock> findOverstockedItems();
    List<ShelfStock> findAll();
    void delete(Long id);
    boolean existsById(Long id);
}
