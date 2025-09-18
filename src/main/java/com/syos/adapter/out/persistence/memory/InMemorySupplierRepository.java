package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.SupplierRepository;
import com.syos.domain.entities.Supplier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of SupplierRepository for non-DB mode.
 */
public class InMemorySupplierRepository implements SupplierRepository {
    private final Map<Long, Supplier> byId = new ConcurrentHashMap<>();
    private final Map<String, Long> idByCode = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public synchronized Supplier save(Supplier supplier) {
        Supplier toStore = supplier;
        Long id = supplier.getId();
        if (id == null) {
            id = idSeq.incrementAndGet();
            toStore = supplier.withId(id);
        }
        byId.put(id, toStore);
        idByCode.put(toStore.getSupplierCode().toUpperCase(Locale.ROOT), id);
        return toStore;
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Supplier> findBySupplierCode(String supplierCode) {
        if (supplierCode == null) return Optional.empty();
        Long id = idByCode.get(supplierCode.toUpperCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return byId.containsKey(id);
    }

    @Override
    public boolean existsBySupplierCode(String supplierCode) {
        return supplierCode != null && idByCode.containsKey(supplierCode.toUpperCase(Locale.ROOT));
    }

    @Override
    public List<Supplier> findAllActive() {
        return byId.values().stream().filter(Supplier::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Supplier> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public boolean isActive(Long id) {
        Supplier s = byId.get(id);
        return s != null && s.isActive();
    }

    @Override
    public long countActiveSuppliers() {
        return byId.values().stream().filter(Supplier::isActive).count();
    }

    @Override
    public synchronized void deleteById(Long id) {
        Supplier s = byId.get(id);
        if (s != null) {
            byId.put(id, s.deactivate());
        }
    }

    @Override
    public List<Supplier> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return findAll();
        }
        String term = searchTerm.toLowerCase(Locale.ROOT);
        return byId.values().stream()
                .filter(s -> s.getSupplierName() != null && s.getSupplierName().toLowerCase(Locale.ROOT).contains(term))
                .collect(Collectors.toList());
    }
}
