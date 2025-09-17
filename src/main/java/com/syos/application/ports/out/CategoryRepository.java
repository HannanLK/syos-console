package com.syos.application.ports.out;

import com.syos.domain.entities.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category domain entity.
 */
public interface CategoryRepository {
    
    /**
     * Save a new or existing category
     */
    Category save(Category category);

    /**
     * Find category by ID
     */
    Optional<Category> findById(Long id);

    /**
     * Find category by category code
     */
    Optional<Category> findByCategoryCode(String categoryCode);

    /**
     * Check if category exists by ID
     */
    boolean existsById(Long id);

    /**
     * Check if category exists by category code
     */
    boolean existsByCategoryCode(String categoryCode);

    /**
     * Find all active categories
     */
    List<Category> findAllActive();

    /**
     * Find root categories (no parent)
     */
    List<Category> findRootCategories();

    /**
     * Find subcategories by parent ID
     */
    List<Category> findByParentCategoryId(Long parentId);

    /**
     * Find all categories (active and inactive)
     */
    List<Category> findAll();

    /**
     * Check if category is active
     */
    boolean isActive(Long id);

    /**
     * Get category hierarchy (parent-child relationships)
     */
    List<Category> getCategoryHierarchy();

    /**
     * Get total count of active categories
     */
    long countActiveCategories();

    /**
     * Delete category by ID (soft delete - mark as inactive)
     */
    void deleteById(Long id);
}
