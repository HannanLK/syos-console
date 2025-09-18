package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.BrandRepository;
import com.syos.domain.entities.Brand;
import com.syos.infrastructure.persistence.entities.BrandEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of BrandRepository.
 * Minimal CRUD needed for product creation flow.
 */
public class JpaBrandRepository implements BrandRepository {
    
    private final EntityManager entityManager;

    public JpaBrandRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Brand save(Brand brand) {
        if (brand == null) throw new IllegalArgumentException("brand is null");
        entityManager.getTransaction().begin();
        try {
            BrandEntity entity;
            if (brand.getId() == null) {
                entity = new BrandEntity(brand.getBrandCode(), brand.getBrandName(), brand.getDescription());
                entity.setIsActive(brand.isActive());
                entityManager.persist(entity);
            } else {
                entity = entityManager.find(BrandEntity.class, brand.getId());
                if (entity == null) {
                    entity = new BrandEntity(brand.getBrandCode(), brand.getBrandName(), brand.getDescription());
                    entity.setId(brand.getId());
                    entity.setIsActive(brand.isActive());
                    entityManager.persist(entity);
                } else {
                    entity.setBrandCode(brand.getBrandCode());
                    entity.setBrandName(brand.getBrandName());
                    entity.setDescription(brand.getDescription());
                    entity.setIsActive(brand.isActive());
                    entityManager.merge(entity);
                }
            }
            entityManager.getTransaction().commit();
            return mapToDomain(entity);
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public Optional<Brand> findById(Long id) {
        BrandEntity entity = entityManager.find(BrandEntity.class, id);
        return Optional.ofNullable(entity).map(this::mapToDomain);
    }

    @Override
    public Optional<Brand> findByBrandCode(String brandCode) {
        TypedQuery<BrandEntity> q = entityManager.createQuery(
            "SELECT b FROM BrandEntity b WHERE UPPER(b.brandCode) = :code", BrandEntity.class);
        q.setParameter("code", brandCode == null ? null : brandCode.toUpperCase());
        List<BrandEntity> res = q.getResultList();
        return res.isEmpty() ? Optional.empty() : Optional.of(mapToDomain(res.get(0)));
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return entityManager.find(BrandEntity.class, id) != null;
    }

    @Override
    public boolean existsByBrandCode(String brandCode) {
        return findByBrandCode(brandCode).isPresent();
    }

    @Override
    public List<Brand> findAllActive() {
        TypedQuery<BrandEntity> q = entityManager.createQuery(
            "SELECT b FROM BrandEntity b WHERE b.isActive = true ORDER BY b.brandName", BrandEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Brand> findAll() {
        TypedQuery<BrandEntity> q = entityManager.createQuery(
            "SELECT b FROM BrandEntity b ORDER BY b.brandName", BrandEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public boolean isActive(Long id) {
        BrandEntity e = entityManager.find(BrandEntity.class, id);
        return e != null && Boolean.TRUE.equals(e.getIsActive());
    }

    @Override
    public long countActiveBrands() {
        Long count = entityManager.createQuery(
            "SELECT COUNT(b) FROM BrandEntity b WHERE b.isActive = true", Long.class).getSingleResult();
        return count == null ? 0 : count;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        entityManager.getTransaction().begin();
        try {
            BrandEntity e = entityManager.find(BrandEntity.class, id);
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

    private Brand mapToDomain(BrandEntity e) {
        return Brand.reconstruct(
            e.getId(),
            e.getBrandCode(),
            e.getBrandName(),
            e.getDescription(),
            Boolean.TRUE.equals(e.getIsActive()),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
