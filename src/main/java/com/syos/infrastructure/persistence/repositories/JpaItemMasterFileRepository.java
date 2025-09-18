package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.entities.ItemMasterFileEntity;
import com.syos.shared.enums.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ItemMasterFileRepository.
 * 
 * Infrastructure Layer:
 * - Implements domain repository interface
 * - Handles entity-domain mapping
 * - Manages database operations
 * 
 * Adapter Pattern (Pattern #11):
 * - Adapts JPA EntityManager to domain repository interface
 */
public class JpaItemMasterFileRepository implements ItemMasterFileRepository {
    
    private final EntityManager entityManager;

    public JpaItemMasterFileRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ItemMasterFile save(ItemMasterFile item) {
        entityManager.getTransaction().begin();
        try {
            ItemMasterFileEntity entity = item.getId() == null ?
                mapToNewEntity(item) : updateExistingEntity(item);

            if (entity.getId() == null) {
                entityManager.persist(entity);
                entityManager.flush(); // ensure ID is generated
            } else {
                entity = entityManager.merge(entity);
            }

            entityManager.getTransaction().commit();
            return mapToDomain(entity);
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        }
    }

    @Override
    public Optional<ItemMasterFile> findById(Long id) {
        ItemMasterFileEntity entity = entityManager.find(ItemMasterFileEntity.class, id);
        return entity != null ? Optional.of(mapToDomain(entity)) : Optional.empty();
    }

    @Override
    public Optional<ItemMasterFile> findByItemCode(ItemCode itemCode) {
        try {
            TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
                "SELECT i FROM ItemMasterFileEntity i WHERE i.itemCode = :itemCode", 
                ItemMasterFileEntity.class);
            query.setParameter("itemCode", itemCode.getValue());
            
            ItemMasterFileEntity entity = query.getSingleResult();
            return Optional.of(mapToDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByItemCode(ItemCode itemCode) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(i) FROM ItemMasterFileEntity i WHERE i.itemCode = :itemCode", 
            Long.class);
        query.setParameter("itemCode", itemCode.getValue());
        return query.getSingleResult() > 0;
    }

    @Override
    public List<ItemMasterFile> findAllActive() {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.status = :status ORDER BY i.itemName", 
            ItemMasterFileEntity.class);
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findByCategory(CategoryId categoryId) {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.categoryId = :categoryId AND i.status = :status ORDER BY i.itemName", 
            ItemMasterFileEntity.class);
        query.setParameter("categoryId", categoryId.getValue());
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findByBrand(BrandId brandId) {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.brandId = :brandId AND i.status = :status ORDER BY i.itemName", 
            ItemMasterFileEntity.class);
        query.setParameter("brandId", brandId.getValue());
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findFeaturedItems() {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.isFeatured = true AND i.status = :status ORDER BY i.dateAdded DESC", 
            ItemMasterFileEntity.class);
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findLatestItems() {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.isLatest = true AND i.status = :status ORDER BY i.dateAdded DESC", 
            ItemMasterFileEntity.class);
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> findItemsRequiringReorder() {
        // This would typically join with stock tables, but for now return items with high reorder points
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE i.reorderPoint >= 50 AND i.status = :status ORDER BY i.reorderPoint DESC", 
            ItemMasterFileEntity.class);
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ItemMasterFile> searchByName(String searchTerm) {
        TypedQuery<ItemMasterFileEntity> query = entityManager.createQuery(
            "SELECT i FROM ItemMasterFileEntity i WHERE LOWER(i.itemName) LIKE LOWER(:searchTerm) AND i.status = :status ORDER BY i.itemName", 
            ItemMasterFileEntity.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        query.setParameter("status", ProductStatus.ACTIVE);
        
        return query.getResultList().stream()
            .map(this::mapToDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countActiveItems() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(i) FROM ItemMasterFileEntity i WHERE i.status = :status", 
            Long.class);
        query.setParameter("status", ProductStatus.ACTIVE);
        return query.getSingleResult();
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete - mark as inactive
        ItemMasterFileEntity entity = entityManager.find(ItemMasterFileEntity.class, id);
        if (entity != null) {
            entity.setStatus(ProductStatus.INACTIVE);
            entityManager.merge(entity);
        }
    }

    @Override
    public boolean isActive(Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(i) FROM ItemMasterFileEntity i WHERE i.id = :id AND i.status = :status", 
            Long.class);
        query.setParameter("id", id);
        query.setParameter("status", ProductStatus.ACTIVE);
        return query.getSingleResult() > 0;
    }

    // Mapping methods
    private ItemMasterFileEntity mapToNewEntity(ItemMasterFile item) {
        return new ItemMasterFileEntity(
            item.getItemCode().getValue(),
            item.getItemName(),
            item.getDescription(),
            item.getBrandId().getValue(),
            item.getCategoryId().getValue(),
            item.getSupplierId().getValue(),
            item.getUnitOfMeasure(),
            item.getPackSize().getValue(),
            item.getCostPrice().toBigDecimal(),
            item.getSellingPrice().toBigDecimal(),
            item.getReorderPoint().getValue(),
            item.isPerishable(),
            item.getCreatedBy() != null ? item.getCreatedBy().getValue() : null
        );
    }

    private ItemMasterFileEntity updateExistingEntity(ItemMasterFile item) {
        ItemMasterFileEntity entity = entityManager.find(ItemMasterFileEntity.class, item.getId());
        if (entity != null) {
            entity.setItemName(item.getItemName());
            entity.setDescription(item.getDescription());
            entity.setCostPrice(item.getCostPrice().toBigDecimal());
            entity.setSellingPrice(item.getSellingPrice().toBigDecimal());
            entity.setReorderPoint(item.getReorderPoint().getValue());
            entity.setStatus(item.getStatus());
            entity.setIsFeatured(item.isFeatured());
            entity.setIsLatest(item.isLatest());
            entity.setUpdatedBy(item.getUpdatedBy() != null ? item.getUpdatedBy().getValue() : null);
        }
        return entity;
    }

    private ItemMasterFile mapToDomain(ItemMasterFileEntity entity) {
        return new ItemMasterFile.Builder()
            .id(entity.getId())
            .itemCode(ItemCode.of(entity.getItemCode()))
            .itemName(entity.getItemName())
            .description(entity.getDescription())
            .brandId(BrandId.of(entity.getBrandId()))
            .categoryId(CategoryId.of(entity.getCategoryId()))
            .supplierId(SupplierId.of(entity.getSupplierId()))
            .unitOfMeasure(entity.getUnitOfMeasure())
            .packSize(PackSize.of(entity.getPackSize()))
            .costPrice(Money.of(entity.getCostPrice()))
            .sellingPrice(Money.of(entity.getSellingPrice()))
            .reorderPoint(ReorderPoint.of(entity.getReorderPoint()))
            .isPerishable(entity.getIsPerishable())
            .status(entity.getStatus())
            .isFeatured(entity.getIsFeatured())
            .isLatest(entity.getIsLatest())
            .dateAdded(entity.getDateAdded())
            .lastUpdated(entity.getLastUpdated())
            .createdBy(entity.getCreatedBy() != null ? UserID.of(entity.getCreatedBy()) : null)
            .updatedBy(entity.getUpdatedBy() != null ? UserID.of(entity.getUpdatedBy()) : null)
            .build();
    }
}
