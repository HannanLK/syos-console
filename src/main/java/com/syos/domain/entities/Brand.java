package com.syos.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a brand in the SYOS system.
 * Supports hierarchical product organization and supplier relationships.
 */
public class Brand {
    private final Long id;
    private final String brandCode;
    private final String brandName;
    private final String description;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Brand(Long id, String brandCode, String brandName, String description,
                  boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.brandCode = validateBrandCode(brandCode);
        this.brandName = validateBrandName(brandName);
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    private String validateBrandCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand code cannot be null or empty");
        }
        return code.trim().toUpperCase();
    }

    private String validateBrandName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name cannot be null or empty");
        }
        return name.trim();
    }

    /**
     * Factory method to create new brand
     */
    public static Brand create(String brandCode, String brandName, String description) {
        return new Brand(null, brandCode, brandName, description, true, null, null);
    }

    /**
     * Factory method for repository reconstruction
     */
    public static Brand reconstruct(Long id, String brandCode, String brandName, String description,
                                   boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Brand(id, brandCode, brandName, description, isActive, createdAt, updatedAt);
    }

    public Brand withId(Long id) {
        return new Brand(id, brandCode, brandName, description, isActive, createdAt, updatedAt);
    }

    public Brand deactivate() {
        return new Brand(id, brandCode, brandName, description, false, createdAt, LocalDateTime.now());
    }

    public Brand activate() {
        return new Brand(id, brandCode, brandName, description, true, createdAt, LocalDateTime.now());
    }

    // Getters
    public Long getId() { return id; }
    public String getBrandCode() { return brandCode; }
    public String getBrandName() { return brandName; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return Objects.equals(brandCode, brand.brandCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brandCode);
    }

    @Override
    public String toString() {
        return "Brand{" +
                "brandCode='" + brandCode + '\'' +
                ", brandName='" + brandName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
