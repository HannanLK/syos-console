package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.BrandRepository;
import com.syos.domain.entities.Brand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of BrandRepository for non-DB mode.
 */
public class InMemoryBrandRepository implements BrandRepository {
    private final Map<Long, Brand> byId = new ConcurrentHashMap<>();
    private final Map<String, Long> idByCode = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public synchronized Brand save(Brand brand) {
        Brand toStore = brand;
        Long id = brand.getId();
        if (id == null) {
            id = idSeq.incrementAndGet();
            toStore = brand.withId(id);
        }
        byId.put(id, toStore);
        idByCode.put(toStore.getBrandCode().toUpperCase(Locale.ROOT), id);
        return toStore;
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Brand> findByBrandCode(String brandCode) {
        if (brandCode == null) return Optional.empty();
        Long id = idByCode.get(brandCode.toUpperCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return byId.containsKey(id);
    }

    @Override
    public boolean existsByBrandCode(String brandCode) {
        if (brandCode == null) return false;
        return idByCode.containsKey(brandCode.toUpperCase(Locale.ROOT));
    }

    @Override
    public List<Brand> findAllActive() {
        return byId.values().stream().filter(Brand::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Brand> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public boolean isActive(Long id) {
        Brand b = byId.get(id);
        return b != null && b.isActive();
    }

    @Override
    public long countActiveBrands() {
        return byId.values().stream().filter(Brand::isActive).count();
    }

    @Override
    public synchronized void deleteById(Long id) {
        Brand existing = byId.get(id);
        if (existing != null) {
            Brand deactivated = existing.deactivate();
            byId.put(id, deactivated);
            idByCode.put(deactivated.getBrandCode().toUpperCase(Locale.ROOT), id);
        }
    }
}
