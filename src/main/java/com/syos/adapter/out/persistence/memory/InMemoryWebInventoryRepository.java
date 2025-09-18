package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.domain.entities.WebInventory;
import com.syos.domain.valueobjects.ItemCode;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryWebInventoryRepository implements WebInventoryRepository {
    private final Map<Long, WebInventory> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public void save(WebInventory webInventory) {
        Long id = webInventory.getId();
        if (id == null) {
            id = seq.getAndIncrement();
        }
        store.put(id, webInventory);
    }

    @Override
    public Optional<WebInventory> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<WebInventory> findByItemCode(ItemCode itemCode) {
        return store.values().stream()
                .filter(w -> w.getItemCode().equals(itemCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findByItemId(Long itemId) {
        return store.values().stream()
                .filter(w -> Objects.equals(w.getItemId(), itemId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findAvailableItems() {
        return store.values().stream()
                .filter(WebInventory::isAvailableForPurchase)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findPublishedItems() {
        return store.values().stream()
                .filter(WebInventory::isPublished)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findFeaturedItems() {
        return store.values().stream()
                .filter(WebInventory::isFeatured)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findExpiredItems() {
        return store.values().stream()
                .filter(WebInventory::isExpired)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findLowStockItems() {
        return store.values().stream()
                .filter(WebInventory::isLowStock)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebInventory> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void delete(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
    
    /**
     * Test method to get current stock quantity for an item
     */
    public BigDecimal getCurrentStock(long itemId) {
        return store.values().stream()
                .filter(w -> Objects.equals(w.getItemId(), itemId))
                .filter(WebInventory::isAvailableForPurchase)
                .map(w -> w.getQuantityAvailable().toBigDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
