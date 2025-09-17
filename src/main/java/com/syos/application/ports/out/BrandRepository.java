package com.syos.application.ports.out;

import com.syos.domain.entities.Brand;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Brand domain entity.
 */
public interface BrandRepository {
    
    /**
     * Save a new or existing brand
     */
    Brand save(Brand brand);

    /**
     * Find brand by ID
     */
    Optional<Brand> findById(Long id);

    /**
     * Find brand by brand code
     */
    Optional<Brand> findByBrandCode(String brandCode);

    /**
     * Check if brand exists by ID
     */
    boolean existsById(Long id);

    /**
     * Check if brand exists by brand code
     */
    boolean existsByBrandCode(String brandCode);

    /**
     * Find all active brands
     */
    List<Brand> findAllActive();

    /**
     * Find all brands (active and inactive)
     */
    List<Brand> findAll();

    /**
     * Check if brand is active
     */
    boolean isActive(Long id);

    /**
     * Get total count of active brands
     */
    long countActiveBrands();

    /**
     * Delete brand by ID (soft delete - mark as inactive)
     */
    void deleteById(Long id);
}
