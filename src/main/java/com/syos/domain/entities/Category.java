package com.syos.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a product category in the SYOS system.
 * Supports hierarchical category structure (parent-child relationships).
 * 
 * Addresses Scenario Requirements:
 * - Browsing by category (dynamic categorization)
 * - Hierarchical navigation (categories/sub-categories)
 */
public class Category {
    private final Long id;
    private final Long parentCategoryId; // nullable for root categories
    private final String categoryCode;
    private final String categoryName;
    private final String description;
    private final int displayOrder;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Category(Long id, Long parentCategoryId, String categoryCode, String categoryName,
                    String description, int displayOrder, boolean isActive,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.parentCategoryId = parentCategoryId;
        this.categoryCode = validateCategoryCode(categoryCode);
        this.categoryName = validateCategoryName(categoryName);
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    private String validateCategoryCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Category code cannot be null or empty");
        }
        return code.trim().toUpperCase();
    }

    private String validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        return name.trim();
    }

    /**
     * Factory method to create root category
     */
    public static Category createRootCategory(String categoryCode, String categoryName, 
                                            String description, int displayOrder) {
        return new Category(null, null, categoryCode, categoryName, description, 
                           displayOrder, true, null, null);
    }

    /**
     * Factory method to create subcategory
     */
    public static Category createSubCategory(Long parentCategoryId, String categoryCode, 
                                           String categoryName, String description, int displayOrder) {
        if (parentCategoryId == null) {
            throw new IllegalArgumentException("Parent category ID cannot be null for subcategory");
        }
        return new Category(null, parentCategoryId, categoryCode, categoryName, 
                           description, displayOrder, true, null, null);
    }

    /**
     * Factory method for repository reconstruction
     */
    public static Category reconstruct(Long id, Long parentCategoryId, String categoryCode, 
                                     String categoryName, String description, int displayOrder,
                                     boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Category(id, parentCategoryId, categoryCode, categoryName, description,
                           displayOrder, isActive, createdAt, updatedAt);
    }

    public Category withId(Long id) {
        return new Category(id, parentCategoryId, categoryCode, categoryName, description,
                           displayOrder, isActive, createdAt, updatedAt);
    }

    public Category updateDisplayOrder(int newDisplayOrder) {
        return new Category(id, parentCategoryId, categoryCode, categoryName, description,
                           newDisplayOrder, isActive, createdAt, LocalDateTime.now());
    }

    public Category deactivate() {
        return new Category(id, parentCategoryId, categoryCode, categoryName, description,
                           displayOrder, false, createdAt, LocalDateTime.now());
    }

    public Category activate() {
        return new Category(id, parentCategoryId, categoryCode, categoryName, description,
                           displayOrder, true, createdAt, LocalDateTime.now());
    }

    /**
     * Business logic: Check if this is a root category
     */
    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    /**
     * Business logic: Check if this is a subcategory
     */
    public boolean isSubCategory() {
        return parentCategoryId != null;
    }

    // Getters
    public Long getId() { return id; }
    public Long getParentCategoryId() { return parentCategoryId; }
    public String getCategoryCode() { return categoryCode; }
    public String getCategoryName() { return categoryName; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryCode, category.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryCode);
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", parentCategoryId=" + parentCategoryId +
                ", isActive=" + isActive +
                '}';
    }
}
