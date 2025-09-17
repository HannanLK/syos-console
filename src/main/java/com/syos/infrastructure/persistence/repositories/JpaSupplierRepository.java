package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.SupplierRepository;
import com.syos.domain.entities.Supplier;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Simple stub implementation of SupplierRepository.
 * TODO: Implement full functionality as needed.
 */
public class JpaSupplierRepository implements SupplierRepository {
    
    private final EntityManager entityManager;

    public JpaSupplierRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Supplier save(Supplier supplier) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Supplier> findBySupplierCode(String supplierCode) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        // Stub implementation - return true for testing
        return true;
    }

    @Override
    public boolean existsBySupplierCode(String supplierCode) {
        return false;
    }

    @Override
    public List<Supplier> findAllActive() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<Supplier> findAll() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isActive(Long id) {
        // Stub implementation - return true for testing
        return true;
    }

    @Override
    public long countActiveSuppliers() {
        return 0;
    }

    @Override
    public void deleteById(Long id) {
        // Stub implementation
    }

    @Override
    public List<Supplier> searchByName(String searchTerm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
