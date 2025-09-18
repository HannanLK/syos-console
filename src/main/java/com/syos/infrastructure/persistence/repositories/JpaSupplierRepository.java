package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.SupplierRepository;
import com.syos.domain.entities.Supplier;
import com.syos.infrastructure.persistence.entities.SupplierEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of SupplierRepository (minimal functionality).
 */
public class JpaSupplierRepository implements SupplierRepository {
    
    private final EntityManager entityManager;

    public JpaSupplierRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Supplier save(Supplier supplier) {
        if (supplier == null) throw new IllegalArgumentException("supplier is null");
        entityManager.getTransaction().begin();
        try {
            SupplierEntity e;
            if (supplier.getId() == null) {
                e = new SupplierEntity(
                    supplier.getSupplierCode(),
                    supplier.getSupplierName(),
                    supplier.getContactPerson(),
                    supplier.getSupplierPhone(),
                    supplier.getSupplierEmail(),
                    supplier.getSupplierAddress()
                );
                e.setIsActive(supplier.isActive());
                entityManager.persist(e);
            } else {
                e = entityManager.find(SupplierEntity.class, supplier.getId());
                if (e == null) {
                    e = new SupplierEntity(
                        supplier.getSupplierCode(),
                        supplier.getSupplierName(),
                        supplier.getContactPerson(),
                        supplier.getSupplierPhone(),
                        supplier.getSupplierEmail(),
                        supplier.getSupplierAddress()
                    );
                    e.setId(supplier.getId());
                    e.setIsActive(supplier.isActive());
                    entityManager.persist(e);
                } else {
                    e.setSupplierCode(supplier.getSupplierCode());
                    e.setSupplierName(supplier.getSupplierName());
                    e.setContactPerson(supplier.getContactPerson());
                    e.setSupplierPhone(supplier.getSupplierPhone());
                    e.setSupplierEmail(supplier.getSupplierEmail());
                    e.setSupplierAddress(supplier.getSupplierAddress());
                    e.setIsActive(supplier.isActive());
                    entityManager.merge(e);
                }
            }
            entityManager.getTransaction().commit();
            return mapToDomain(e);
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        SupplierEntity e = entityManager.find(SupplierEntity.class, id);
        return Optional.ofNullable(e).map(this::mapToDomain);
    }

    @Override
    public Optional<Supplier> findBySupplierCode(String supplierCode) {
        TypedQuery<SupplierEntity> q = entityManager.createQuery(
            "SELECT s FROM SupplierEntity s WHERE UPPER(s.supplierCode) = :code", SupplierEntity.class);
        q.setParameter("code", supplierCode == null ? null : supplierCode.toUpperCase());
        List<SupplierEntity> res = q.getResultList();
        return res.isEmpty() ? Optional.empty() : Optional.of(mapToDomain(res.get(0)));
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return entityManager.find(SupplierEntity.class, id) != null;
    }

    @Override
    public boolean existsBySupplierCode(String supplierCode) {
        return findBySupplierCode(supplierCode).isPresent();
    }

    @Override
    public List<Supplier> findAllActive() {
        TypedQuery<SupplierEntity> q = entityManager.createQuery(
            "SELECT s FROM SupplierEntity s WHERE s.isActive = true ORDER BY s.supplierName", SupplierEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Supplier> findAll() {
        TypedQuery<SupplierEntity> q = entityManager.createQuery(
            "SELECT s FROM SupplierEntity s ORDER BY s.supplierName", SupplierEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public boolean isActive(Long id) {
        SupplierEntity e = entityManager.find(SupplierEntity.class, id);
        return e != null && Boolean.TRUE.equals(e.getIsActive());
    }

    @Override
    public long countActiveSuppliers() {
        Long count = entityManager.createQuery(
            "SELECT COUNT(s) FROM SupplierEntity s WHERE s.isActive = true", Long.class).getSingleResult();
        return count == null ? 0 : count;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        entityManager.getTransaction().begin();
        try {
            SupplierEntity e = entityManager.find(SupplierEntity.class, id);
            if (e != null) {
                e.setIsActive(false);
                entityManager.merge(e);
            }
            entityManager.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public List<Supplier> searchByName(String searchTerm) {
        String term = (searchTerm == null) ? "" : searchTerm.trim().toUpperCase();
        TypedQuery<SupplierEntity> q = entityManager.createQuery(
            "SELECT s FROM SupplierEntity s WHERE UPPER(s.supplierName) LIKE :term ORDER BY s.supplierName",
            SupplierEntity.class);
        q.setParameter("term", "%" + term + "%");
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private Supplier mapToDomain(SupplierEntity e) {
        return Supplier.reconstruct(
            e.getId(),
            e.getSupplierCode(),
            e.getSupplierName(),
            e.getSupplierPhone(),
            e.getSupplierEmail(),
            e.getSupplierAddress(),
            e.getContactPerson(),
            Boolean.TRUE.equals(e.getIsActive()),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
