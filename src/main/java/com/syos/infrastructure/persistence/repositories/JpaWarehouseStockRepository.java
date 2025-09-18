package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.domain.entities.WarehouseStock;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.infrastructure.persistence.entities.WarehouseStockEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of WarehouseStockRepository
 * Handles warehouse stock operations with proper domain-infrastructure mapping
 */
public class JpaWarehouseStockRepository implements WarehouseStockRepository {
    private static final Logger logger = LoggerFactory.getLogger(JpaWarehouseStockRepository.class);
    private final EntityManagerFactory emf;

    public JpaWarehouseStockRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public WarehouseStock save(WarehouseStock warehouseStock) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            WarehouseStockEntity entity = toEntity(warehouseStock);
            WarehouseStockEntity saved;

            if (warehouseStock.getId() == null) {
                em.persist(entity);
                em.flush(); // ensure ID is generated
                saved = entity;
                logger.debug("Saved new warehouse stock for item code: {}", warehouseStock.getItemCode().getValue());
            } else {
                saved = em.merge(entity);
                logger.debug("Updated warehouse stock for item code: {}", warehouseStock.getItemCode().getValue());
            }

            em.getTransaction().commit();
            return toDomain(saved);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Failed to save warehouse stock for item code: {}", warehouseStock.getItemCode().getValue(), e);
            throw new RuntimeException("Failed to save warehouse stock", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<WarehouseStock> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            WarehouseStockEntity entity = em.find(WarehouseStockEntity.class, id);
            return Optional.ofNullable(entity).map(this::toDomain);
        } finally {
            em.close();
        }
    }

    public List<WarehouseStock> findByItemCode(ItemCode itemCode) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.itemCode = :itemCode ORDER BY ws.receivedDate ASC",
                WarehouseStockEntity.class
            );
            query.setParameter("itemCode", itemCode.getValue());
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<WarehouseStock> findAvailableByItemCode(ItemCode itemCode) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.itemCode = :itemCode AND ws.quantityAvailable > 0 ORDER BY ws.receivedDate ASC",
                WarehouseStockEntity.class
            );
            query.setParameter("itemCode", itemCode.getValue());
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    public List<WarehouseStock> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws ORDER BY ws.receivedDate DESC",
                WarehouseStockEntity.class
            );
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            WarehouseStockEntity entity = em.find(WarehouseStockEntity.class, id);
            if (entity != null) {
                em.remove(entity);
                logger.debug("Deleted warehouse stock with ID: {}", id);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Failed to delete warehouse stock with ID: {}", id, e);
            throw new RuntimeException("Failed to delete warehouse stock", e);
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findAvailableByItemId(Long itemId) {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.itemId = :itemId AND ws.quantityAvailable > 0 ORDER BY ws.receivedDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            query.setParameter("itemId", itemId);
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findByItemIdAndBatchId(Long itemId, Long batchId) {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.itemId = :itemId AND ws.batchId = :batchId ORDER BY ws.receivedDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            query.setParameter("itemId", itemId);
            query.setParameter("batchId", batchId);
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findByLocation(String location) {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.location = :location ORDER BY ws.receivedDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            query.setParameter("location", location);
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findReservedStock() {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.isReserved = true ORDER BY ws.receivedDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findExpiringWithinDays(int days) {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().plusDays(days);
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.expiryDate IS NOT NULL AND ws.expiryDate <= :cutoff ORDER BY ws.expiryDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            query.setParameter("cutoff", cutoff);
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<WarehouseStock> findExpiredStock() {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<com.syos.infrastructure.persistence.entities.WarehouseStockEntity> query = em.createQuery(
                "SELECT ws FROM WarehouseStockEntity ws WHERE ws.expiryDate IS NOT NULL AND ws.expiryDate < CURRENT_TIMESTAMP ORDER BY ws.expiryDate ASC",
                com.syos.infrastructure.persistence.entities.WarehouseStockEntity.class
            );
            return query.getResultList().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByItemId(Long itemId) {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(ws) FROM WarehouseStockEntity ws WHERE ws.itemId = :itemId",
                Long.class
            );
            query.setParameter("itemId", itemId);
            Long count = query.getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    private WarehouseStockEntity toEntity(WarehouseStock domain) {
        WarehouseStockEntity entity = new WarehouseStockEntity();
        entity.setId(domain.getId());
        entity.setItemCode(domain.getItemCode().getValue());
        entity.setItemId(domain.getItemId());
        entity.setBatchId(domain.getBatchId());
        entity.setQuantityReceived(domain.getQuantityReceived().getValue());
        entity.setQuantityAvailable(domain.getQuantityAvailable().getValue());
        entity.setReceivedDate(domain.getReceivedDate());
        entity.setExpiryDate(domain.getExpiryDate());
        entity.setReceivedBy(domain.getReceivedBy().getValue());
        entity.setLocation(domain.getLocation());
        entity.setIsReserved(domain.isReserved());
        entity.setReservedBy(domain.getReservedBy() != null ? domain.getReservedBy().getValue() : null);
        entity.setReservedAt(domain.getReservedAt());
        entity.setLastUpdated(domain.getLastUpdated());
        entity.setLastUpdatedBy(domain.getLastUpdatedBy().getValue());
        return entity;
    }

    private WarehouseStock toDomain(WarehouseStockEntity entity) {
        return WarehouseStock.builder()
                .id(entity.getId())
                .itemCode(com.syos.domain.valueobjects.ItemCode.of(entity.getItemCode()))
                .itemId(entity.getItemId())
                .batchId(entity.getBatchId())
                .quantityReceived(com.syos.domain.valueobjects.Quantity.of(entity.getQuantityReceived()))
                .quantityAvailable(com.syos.domain.valueobjects.Quantity.of(entity.getQuantityAvailable()))
                .receivedDate(entity.getReceivedDate())
                .expiryDate(entity.getExpiryDate())
                .receivedBy(com.syos.domain.valueobjects.UserID.of(entity.getReceivedBy()))
                .location(entity.getLocation())
                .isReserved(entity.getIsReserved())
                .reservedBy(entity.getReservedBy() != null ? com.syos.domain.valueobjects.UserID.of(entity.getReservedBy()) : null)
                .reservedAt(entity.getReservedAt())
                .lastUpdated(entity.getLastUpdated())
                .lastUpdatedBy(com.syos.domain.valueobjects.UserID.of(entity.getLastUpdatedBy()))
                .build();
    }
}
