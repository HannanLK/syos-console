package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.CategoryId;
import com.syos.domain.valueobjects.BrandId;
import com.syos.shared.enums.ProductStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ItemMasterFileRepository for testing purposes.
 */
public class InMemoryItemMasterFileRepository implements ItemMasterFileRepository {
    
    private final AtomicLong idSequence = new AtomicLong(1);
    private final Map<Long, ItemMasterFile> itemsById = new HashMap<>();
    private final Map<String, Long> itemIdByCode = new HashMap<>();

    @Override
    public ItemMasterFile save(ItemMasterFile item) {
        if (item.getId() == null) {
            // New item - assign ID
            Long newId = idSequence.getAndIncrement();
            ItemMasterFile itemWithId = item.withId(newId);
            itemsById.put(newId, itemWithId);
            itemIdByCode.put(item.getItemCode().getValue(), newId);
            return itemWithId;
        } else {
            // Update existing item
            itemsById.put(item.getId(), item);
            itemIdByCode.put(item.getItemCode().getValue(), item.getId());
            return item;
        }
    }

    @Override
    public Optional<ItemMasterFile> findById(Long id) {
        return Optional.ofNullable(itemsById.get(id));
    }

    @Override
    public Optional<ItemMasterFile> findByItemCode(ItemCode itemCode) {
        Long id = itemIdByCode.get(itemCode.getValue());
        return id != null ? Optional.ofNullable(itemsById.get(id)) : Optional.empty();
    }

    @Override
    public boolean existsByItemCode(ItemCode itemCode) {
        return itemIdByCode.containsKey(itemCode.getValue());
    }

    @Override
    public List<ItemMasterFile> findAllActive() {
        return itemsById.values().stream()
            .filter(ItemMasterFile::isActive)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findByCategory(CategoryId categoryId) {
        return itemsById.values().stream()
            .filter(item -> item.isActive() && item.getCategoryId().equals(categoryId))
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findByBrand(BrandId brandId) {
        return itemsById.values().stream()
            .filter(item -> item.isActive() && item.getBrandId().equals(brandId))
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findFeaturedItems() {
        return itemsById.values().stream()
            .filter(item -> item.isActive() && item.isFeatured())
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findLatestItems() {
        return itemsById.values().stream()
            .filter(item -> item.isActive() && item.isLatest())
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findItemsRequiringReorder() {
        // This would typically check current stock vs reorder point
        // For now, return empty list as stock checking is not implemented
        return new ArrayList<>();
    }

    @Override
    public List<ItemMasterFile> searchByName(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return itemsById.values().stream()
            .filter(item -> item.isActive() && 
                           item.getItemName().toLowerCase().contains(lowerSearchTerm))
            .collect(Collectors.toList());
    }

    @Override
    public long countActiveItems() {
        return itemsById.values().stream()
            .mapToLong(item -> item.isActive() ? 1 : 0)
            .sum();
    }

    @Override
    public void deleteById(Long id) {
        ItemMasterFile item = itemsById.get(id);
        if (item != null) {
            // Soft delete - mark as inactive
            ItemMasterFile deactivatedItem = item.deactivate(item.getCreatedBy());
            itemsById.put(id, deactivatedItem);
        }
    }

    @Override
    public boolean isActive(Long id) {
        ItemMasterFile item = itemsById.get(id);
        return item != null && item.isActive();
    }
    
    // Test helper methods
    public void clear() {
        itemsById.clear();
        itemIdByCode.clear();
        idSequence.set(1);
    }
    
    public int size() {
        return itemsById.size();
    }
    
    public List<ItemMasterFile> findAll() {
        return new ArrayList<>(itemsById.values());
    }
}
