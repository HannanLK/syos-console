package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.domain.entities.ShelfStock;
import com.syos.domain.valueobjects.ItemCode;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Minimal in-memory ShelfStockRepository matching current port interface.
 */
public class InMemoryShelfStockRepository implements ShelfStockRepository {
    private final Map<Long, ShelfStock> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public void save(ShelfStock shelfStock) {
        Long id = shelfStock.getId();
        if (id == null) {
            id = seq.getAndIncrement();
        }
        store.put(id, shelfStock);
    }

    @Override
    public Optional<ShelfStock> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ShelfStock> findByItemCode(ItemCode itemCode) {
        return store.values().stream()
                .filter(ss -> ss.getItemCode().equals(itemCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findByShelfCode(String shelfCode) {
        return store.values().stream()
                .filter(ss -> Objects.equals(ss.getShelfCode(), shelfCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findByItemIdAndShelfCode(Long itemId, String shelfCode) {
        return store.values().stream()
                .filter(ss -> Objects.equals(ss.getItemId(), itemId))
                .filter(ss -> Objects.equals(ss.getShelfCode(), shelfCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findAvailableByItemCode(ItemCode itemCode) {
        return store.values().stream()
                .filter(ss -> ss.getItemCode().equals(itemCode))
                .filter(ShelfStock::isAvailableForSale)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findDisplayedItems() {
        return store.values().stream()
                .filter(ShelfStock::isDisplayed)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findExpiredItems() {
        return store.values().stream()
                .filter(ShelfStock::isExpired)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findExpiringSoonItems() {
        return store.values().stream()
                .filter(ShelfStock::isExpiringSoon)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findLowStockItems() {
        return store.values().stream()
                .filter(ShelfStock::needsRestocking)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findOverstockedItems() {
        return store.values().stream()
                .filter(ShelfStock::isOverstocked)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShelfStock> findAll() {
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
                .filter(ss -> Objects.equals(ss.getItemId(), itemId))
                .filter(ShelfStock::isAvailableForSale)
                .map(ss -> ss.getQuantityOnShelf().toBigDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
