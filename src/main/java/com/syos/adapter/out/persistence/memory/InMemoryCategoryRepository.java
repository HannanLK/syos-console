package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.CategoryRepository;
import com.syos.domain.entities.Category;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of CategoryRepository for non-DB mode.
 */
public class InMemoryCategoryRepository implements CategoryRepository {
    private final Map<Long, Category> byId = new ConcurrentHashMap<>();
    private final Map<String, Long> idByCode = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public synchronized Category save(Category category) {
        Category toStore = category;
        Long id = category.getId();
        if (id == null) {
            id = idSeq.incrementAndGet();
            toStore = category.withId(id);
        }
        byId.put(id, toStore);
        idByCode.put(toStore.getCategoryCode().toUpperCase(Locale.ROOT), id);
        return toStore;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Category> findByCategoryCode(String categoryCode) {
        if (categoryCode == null) return Optional.empty();
        Long id = idByCode.get(categoryCode.toUpperCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return byId.containsKey(id);
    }

    @Override
    public boolean existsByCategoryCode(String categoryCode) {
        return categoryCode != null && idByCode.containsKey(categoryCode.toUpperCase(Locale.ROOT));
    }

    @Override
    public List<Category> findAllActive() {
        return byId.values().stream().filter(Category::isActive).collect(Collectors.toList());
    }

    @Override
    public List<Category> findRootCategories() {
        return byId.values().stream().filter(Category::isRootCategory).collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentCategoryId(Long parentId) {
        return byId.values().stream()
                .filter(c -> Objects.equals(c.getParentCategoryId(), parentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public boolean isActive(Long id) {
        Category c = byId.get(id);
        return c != null && c.isActive();
    }

    @Override
    public List<Category> getCategoryHierarchy() {
        // Simple flat list for now
        return new ArrayList<>(byId.values());
    }

    @Override
    public long countActiveCategories() {
        return byId.values().stream().filter(Category::isActive).count();
    }

    @Override
    public synchronized void deleteById(Long id) {
        Category c = byId.get(id);
        if (c != null) {
            byId.put(id, c.deactivate());
        }
    }
}
