package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.CategoryRepository;
import com.syos.domain.entities.Category;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Simple stub implementation of CategoryRepository.
 * TODO: Implement full functionality as needed.
 */
public class JpaCategoryRepository implements CategoryRepository {
    
    private final EntityManager entityManager;

    public JpaCategoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Category save(Category category) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByCategoryCode(String categoryCode) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        // Stub implementation - return true for testing
        return true;
    }

    @Override
    public boolean existsByCategoryCode(String categoryCode) {
        return false;
    }

    @Override
    public List<Category> findAllActive() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<Category> findRootCategories() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<Category> findByParentCategoryId(Long parentId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<Category> findAll() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isActive(Long id) {
        // Stub implementation - return true for testing
        return true;
    }

    @Override
    public List<Category> getCategoryHierarchy() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public long countActiveCategories() {
        return 0;
    }

    @Override
    public void deleteById(Long id) {
        // Stub implementation
    }
}
