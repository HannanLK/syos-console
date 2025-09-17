package com.syos.application.ports.out;

import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.CategoryId;
import com.syos.domain.valueobjects.BrandId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ItemMasterFile domain entity.
 * 
 * Port interface (Hexagonal Architecture):
 * - Defines contract for data persistence
 * - Implementation will be in infrastructure layer
 * - Keeps application layer independent of persistence details
 */
public interface ItemMasterFileRepository {
    
    /**
     * Save a new or existing item
     */
    ItemMasterFile save(ItemMasterFile item);

    /**
     * Find item by ID
     */
    Optional<ItemMasterFile> findById(Long id);

    /**
     * Find item by item code
     */
    Optional<ItemMasterFile> findByItemCode(ItemCode itemCode);

    /**
     * Check if item exists by item code
     */
    boolean existsByItemCode(ItemCode itemCode);

    /**
     * Find all active items
     */
    List<ItemMasterFile> findAllActive();

    /**
     * Find items by category
     */
    List<ItemMasterFile> findByCategory(CategoryId categoryId);

    /**
     * Find items by brand
     */
    List<ItemMasterFile> findByBrand(BrandId brandId);

    /**
     * Find featured items
     */
    List<ItemMasterFile> findFeaturedItems();

    /**
     * Find latest items
     */
    List<ItemMasterFile> findLatestItems();

    /**
     * Find items requiring reorder
     */
    List<ItemMasterFile> findItemsRequiringReorder();

    /**
     * Search items by name (partial match)
     */
    List<ItemMasterFile> searchByName(String searchTerm);

    /**
     * Get total count of active items
     */
    long countActiveItems();

    /**
     * Delete item by ID (soft delete - mark as inactive)
     */
    void deleteById(Long id);

    /**
     * Check if item is active
     */
    boolean isActive(Long id);
}
