package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.BatchRepository;
import com.syos.domain.entities.Batch;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.infrastructure.persistence.entities.BatchEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of BatchRepository
 * Handles batch operations for FIFO stock management
 */
public class JpaBatchRepository implements BatchRepository {
    private static final Logger logger = LoggerFactory.getLogger(JpaBatchRepository.class);
    private final EntityManagerFactory emf;

    public JpaBatchRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Batch save(Batch batch) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            BatchEntity entity = toEntity(batch);
            
            if (batch.getId() == null) {
                em.persist(entity);
                logger.debug("Saved new batch: {}", batch.getBatchNumber());
            } else {
                entity = em.merge(entity);
                logger.debug("Updated batch: {}", batch.getBatchNumber());
            }
            
            em.getTransaction().commit();
            
            // Return the domain object with updated ID if it was a new entity
            return toDomain(entity);
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Failed to save batch: {}", batch.getBatchNumber(), e);
            throw new RuntimeException("Failed to save batch", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Batch> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            BatchEntity entity = em.find(BatchEntity.class, id);
            return Optional.ofNullable(entity).map(this::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Batch> findByItemId(Long itemId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b WHERE b.itemId = :itemId ORDER BY b.receivedDate ASC",
                BatchEntity.class
            );
            query.setParameter("itemId", itemId);
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<Batch> findByItemCode(ItemCode itemCode) {
        EntityManager em = emf.createEntityManager();
        try {
            // Need to join with item_master_file to get batches by item code
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b JOIN ItemMasterFileEntity i ON b.itemId = i.id " +
                "WHERE i.itemCode = :itemCode ORDER BY b.receivedDate ASC",
                BatchEntity.class
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
    public List<Batch> findAvailableByItemId(Long itemId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b WHERE b.itemId = :itemId AND b.quantityAvailable > 0 " +
                "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.receivedDate ASC",
                BatchEntity.class
            );
            query.setParameter("itemId", itemId);
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<Batch> findExpiringBatches(int daysAhead) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
            
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b WHERE b.expiryDate IS NOT NULL " +
                "AND DATE(b.expiryDate) <= :cutoffDate AND b.quantityAvailable > 0 " +
                "ORDER BY b.expiryDate ASC",
                BatchEntity.class
            );
            query.setParameter("cutoffDate", cutoffDate);
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<Batch> findExpiredBatches() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b WHERE b.expiryDate IS NOT NULL " +
                "AND b.expiryDate < CURRENT_TIMESTAMP AND b.quantityAvailable > 0 " +
                "ORDER BY b.expiryDate ASC",
                BatchEntity.class
            );
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<Batch> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BatchEntity> query = em.createQuery(
                "SELECT b FROM BatchEntity b ORDER BY b.receivedDate DESC",
                BatchEntity.class
            );
            
            return query.getResultList().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            BatchEntity entity = em.find(BatchEntity.class, id);
            if (entity != null) {
                em.remove(entity);
                logger.debug("Deleted batch with ID: {}", id);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Failed to delete batch with ID: {}", id, e);
            throw new RuntimeException("Failed to delete batch", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(BatchEntity.class, id) != null;
        } finally {
            em.close();
        }
    }

    private BatchEntity toEntity(Batch domain) {
        BatchEntity entity = new BatchEntity();
        entity.setId(domain.getId());
        entity.setItemId(domain.getItemId());
        entity.setBatchNumber(domain.getBatchNumber());
        entity.setQuantityReceived(domain.getQuantityReceived().getValue());
        entity.setQuantityAvailable(domain.getQuantityAvailable().getValue());
        entity.setManufactureDate(domain.getManufactureDate());
        entity.setExpiryDate(domain.getExpiryDate());
        entity.setReceivedDate(domain.getReceivedDate());
        entity.setReceivedBy(domain.getReceivedBy().getValue());
        entity.setCostPerUnit(domain.getCostPerUnit() != null ? domain.getCostPerUnit().getAmount() : null);
        entity.setSupplierBatchNumber(domain.getSupplierBatchNumber());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Batch toDomain(BatchEntity entity) {
        return new Batch.Builder()
                .id(entity.getId())
                .itemId(entity.getItemId())
                .batchNumber(entity.getBatchNumber())
                .quantityReceived(com.syos.domain.valueobjects.Quantity.of(entity.getQuantityReceived()))
                .quantityAvailable(com.syos.domain.valueobjects.Quantity.of(entity.getQuantityAvailable()))
                .manufactureDate(entity.getManufactureDate())
                .expiryDate(entity.getExpiryDate())
                .receivedDate(entity.getReceivedDate())
                .receivedBy(com.syos.domain.valueobjects.UserID.of(entity.getReceivedBy()))
                .costPerUnit(entity.getCostPerUnit() != null ? com.syos.domain.valueobjects.Money.of(entity.getCostPerUnit()) : null)
                .supplierBatchNumber(entity.getSupplierBatchNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
