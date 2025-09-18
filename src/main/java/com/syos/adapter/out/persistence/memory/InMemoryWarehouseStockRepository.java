package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.domain.entities.WarehouseStock;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Minimal in-memory WarehouseStockRepository matching current port interface.
 */
public class InMemoryWarehouseStockRepository implements WarehouseStockRepository {
    private final Map<Long, WarehouseStock> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public WarehouseStock save(WarehouseStock warehouseStock) {
        Long id = warehouseStock.getId();
        if (id == null) {
            id = seq.getAndIncrement();
            // Create new WarehouseStock with assigned ID using builder
            WarehouseStock warehouseStockWithId = new WarehouseStock.Builder(warehouseStock)
                    .id(id)
                    .build();
            store.put(id, warehouseStockWithId);
            return warehouseStockWithId;
        } else {
            store.put(id, warehouseStock);
            return warehouseStock;
        }
    }

    @Override
    public Optional<WarehouseStock> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    private Comparator<WarehouseStock> fifoWithExpiryComparator() {
        return Comparator
                .comparing((WarehouseStock ws) -> ws.getExpiryDate() == null)
                .thenComparing(ws -> Optional.ofNullable(ws.getExpiryDate()).orElse(LocalDateTime.MAX))
                .thenComparing(WarehouseStock::getReceivedDate);
    }

    @Override
    public List<WarehouseStock> findAvailableByItemId(Long itemId) {
        return store.values().stream()
                .filter(ws -> Objects.equals(ws.getItemId(), itemId))
                .filter(WarehouseStock::isAvailableForTransfer)
                .sorted(fifoWithExpiryComparator())
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findAvailableByItemCode(ItemCode itemCode) {
        return store.values().stream()
                .filter(ws -> ws.getItemCode().equals(itemCode))
                .filter(WarehouseStock::isAvailableForTransfer)
                .sorted(fifoWithExpiryComparator())
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findByItemIdAndBatchId(Long itemId, Long batchId) {
        return store.values().stream()
                .filter(ws -> Objects.equals(ws.getItemId(), itemId))
                .filter(ws -> Objects.equals(ws.getBatchId(), batchId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findByLocation(String location) {
        return store.values().stream()
                .filter(ws -> Objects.equals(ws.getLocation(), location))
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findReservedStock() {
        return store.values().stream()
                .filter(WarehouseStock::isReserved)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findExpiringWithinDays(int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return store.values().stream()
                .filter(ws -> ws.getExpiryDate() != null)
                .filter(ws -> ws.getExpiryDate().isBefore(threshold))
                .filter(ws -> !ws.isExpired())
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStock> findExpiredStock() {
        return store.values().stream()
                .filter(WarehouseStock::isExpired)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsByItemId(Long itemId) {
        return store.values().stream().anyMatch(ws -> Objects.equals(ws.getItemId(), itemId));
    }

    // Test helpers
    public void clear() { store.clear(); seq.set(1); }
    public List<WarehouseStock> findAll() { return new ArrayList<>(store.values()); }
    public int size() { return store.size(); }
    
    /**
     * Test method to add stock for testing purposes
     */
    public void addStock(long itemId, long batchId, BigDecimal quantity) {
        // Create warehouse stock using builder pattern
        WarehouseStock stock = WarehouseStock.builder()
                .id(seq.getAndIncrement())
                .itemId(itemId)
                .batchId(batchId)
                .itemCode(ItemCode.of("TEST" + itemId))
                .quantityReceived(Quantity.of(quantity))
                .quantityAvailable(Quantity.of(quantity))
                .receivedDate(LocalDateTime.now())
                .receivedBy(UserID.of(1L)) // Test user
                .location("TEST-WAREHOUSE")
                .isReserved(false)
                .lastUpdatedBy(UserID.of(1L))
                .build();
        store.put(stock.getId(), stock);
    }
    
    /**
     * Test method to get total available stock for an item
     */
    public BigDecimal getTotalAvailableStock(long itemId) {
        return store.values().stream()
                .filter(ws -> Objects.equals(ws.getItemId(), itemId))
                .filter(WarehouseStock::isAvailableForTransfer)
                .map(ws -> ws.getQuantityAvailable().toBigDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
