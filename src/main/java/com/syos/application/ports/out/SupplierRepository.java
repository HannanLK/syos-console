package com.syos.application.ports.out;

import com.syos.domain.entities.Supplier;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Supplier domain entity.
 */
public interface SupplierRepository {
    
    /**
     * Save a new or existing supplier
     */
    Supplier save(Supplier supplier);

    /**
     * Find supplier by ID
     */
    Optional<Supplier> findById(Long id);

    /**
     * Find supplier by supplier code
     */
    Optional<Supplier> findBySupplierCode(String supplierCode);

    /**
     * Check if supplier exists by ID
     */
    boolean existsById(Long id);

    /**
     * Check if supplier exists by supplier code
     */
    boolean existsBySupplierCode(String supplierCode);

    /**
     * Find all active suppliers
     */
    List<Supplier> findAllActive();

    /**
     * Find all suppliers (active and inactive)
     */
    List<Supplier> findAll();

    /**
     * Check if supplier is active
     */
    boolean isActive(Long id);

    /**
     * Get total count of active suppliers
     */
    long countActiveSuppliers();

    /**
     * Delete supplier by ID (soft delete - mark as inactive)
     */
    void deleteById(Long id);

    /**
     * Search suppliers by name (partial match)
     */
    List<Supplier> searchByName(String searchTerm);
}
