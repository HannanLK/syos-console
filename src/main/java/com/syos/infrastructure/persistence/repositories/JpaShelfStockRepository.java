package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.domain.entities.ShelfStock;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal JPA implementation for ShelfStockRepository using native SQL against V4 schema.
 * Persists to shelf_stock and resolves/creates SHELF locations by shelf_code.
 */
public class JpaShelfStockRepository implements ShelfStockRepository {
    private final EntityManagerFactory emf;

    public JpaShelfStockRepository(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf);
    }

    @Override
    public void save(ShelfStock shelfStock) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Ensure SHELF location exists (location_code = shelfCode)
            Long locationId = getOrCreateLocation(em, shelfStock.getShelfCode(), "SHELF");

            // Upsert into shelf_stock by (item_id, batch_id, location_id)
            Query q = em.createNativeQuery(
                    "INSERT INTO shelf_stock(item_id, batch_id, location_id, quantity, last_restocked) " +
                    "VALUES (?,?,?,?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (item_id, batch_id, location_id) DO UPDATE " +
                    "SET quantity = shelf_stock.quantity + EXCLUDED.quantity, " +
                    "updated_at = CURRENT_TIMESTAMP, last_restocked = CURRENT_TIMESTAMP");
            q.setParameter(1, shelfStock.getItemId());
            q.setParameter(2, shelfStock.getBatchId());
            q.setParameter(3, locationId);
            q.setParameter(4, shelfStock.getQuantityOnShelf().toBigDecimal());
            q.executeUpdate();

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<ShelfStock> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            List<ShelfStock> list = mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE ss.id = ?")
                .setParameter(1, id)
                .getResultList());
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findByItemCode(ItemCode itemCode) {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE im.item_code = ?")
                .setParameter(1, itemCode.getValue())
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findByShelfCode(String shelfCode) {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE l.location_code = ?")
                .setParameter(1, shelfCode)
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findByItemIdAndShelfCode(Long itemId, String shelfCode) {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE ss.item_id = ? AND l.location_code = ?")
                .setParameter(1, itemId)
                .setParameter(2, shelfCode)
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findAvailableByItemCode(ItemCode itemCode) {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE im.item_code = ? AND ss.quantity > 0")
                .setParameter(1, itemCode.getValue())
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findDisplayedItems() {
        // Not tracked separately in V4; return all with quantity > 0
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE ss.quantity > 0")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findExpiredItems() {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE b.expiry_date IS NOT NULL AND b.expiry_date < CURRENT_DATE")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findExpiringSoonItems() {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE b.expiry_date IS NOT NULL AND b.expiry_date <= CURRENT_DATE + INTERVAL '7 days'")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findLowStockItems() {
        // As we don't store min/max in V4, approximate: quantity < 50
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE ss.quantity < 50")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findOverstockedItems() {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id " +
                    "WHERE ss.quantity >= 50")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public List<ShelfStock> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return mapToDomain(em.createNativeQuery(
                    "SELECT ss.id, im.item_code, ss.item_id, ss.batch_id, l.location_code AS shelf_code, " +
                    "ss.quantity, b.expiry_date, im.selling_price, ss.updated_at " +
                    "FROM shelf_stock ss " +
                    "JOIN item_master_file im ON ss.item_id = im.id " +
                    "JOIN locations l ON ss.location_id = l.id " +
                    "LEFT JOIN batches b ON ss.batch_id = b.id")
                .getResultList());
        } finally { em.close(); }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM shelf_stock WHERE id = ?").setParameter(1, id).executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Object single = em.createNativeQuery("SELECT 1 FROM shelf_stock WHERE id = ?")
                    .setParameter(1, id)
                    .getResultStream().findFirst().orElse(null);
            return single != null;
        } finally { em.close(); }
    }

    private Long getOrCreateLocation(EntityManager em, String code, String type) {
        Object idObj = em.createNativeQuery("SELECT id FROM locations WHERE location_code = ?")
                .setParameter(1, code)
                .getResultStream().findFirst().orElse(null);
        if (idObj != null) return ((Number) idObj).longValue();

        em.createNativeQuery("INSERT INTO locations(location_code, location_name, location_type, is_active) VALUES (?,?,?,true)")
                .setParameter(1, code)
                .setParameter(2, code)
                .setParameter(3, type)
                .executeUpdate();
        Object newId = em.createNativeQuery("SELECT id FROM locations WHERE location_code = ?")
                .setParameter(1, code)
                .getSingleResult();
        return ((Number) newId).longValue();
    }

    private UserID safeUserId(Long v) {
        // Map null/0/negative to a safe system user (ID=1) to satisfy domain constraints
        if (v == null || v <= 0L) {
            return UserID.of(1L);
        }
        return UserID.of(v);
    }

    @SuppressWarnings("unchecked")
    private List<ShelfStock> mapToDomain(List<Object[]> rows) {
        List<ShelfStock> list = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String itemCode = (String) r[1];
            Long itemId = ((Number) r[2]).longValue();
            Long batchId = ((Number) r[3]).longValue();
            String shelfCode = (String) r[4];
            BigDecimal qty = (BigDecimal) r[5];
            java.sql.Date expDate = null;
            LocalDateTime expiry = null;
            if (r[6] instanceof java.sql.Date d) {
                expDate = d;
                expiry = d.toLocalDate().atStartOfDay();
            } else if (r[6] instanceof Timestamp ts) {
                expiry = ts.toLocalDateTime();
            }
            BigDecimal price = (BigDecimal) r[7];
            LocalDateTime updatedAt;
            if (r[8] instanceof Timestamp ts2) {
                updatedAt = ts2.toLocalDateTime();
            } else {
                updatedAt = LocalDateTime.now();
            }

            ShelfStock ss = new ShelfStock.Builder()
                    .id(id)
                    .itemCode(ItemCode.of(itemCode))
                    .itemId(itemId)
                    .batchId(batchId)
                    .shelfCode(shelfCode)
                    .quantityOnShelf(Quantity.of(qty))
                    .expiryDate(expiry)
                    .placedBy(safeUserId(null))
                    .unitPrice(Money.of(price))
                    .lastUpdated(updatedAt)
                    .lastUpdatedBy(safeUserId(null))
                    .isDisplayed(true)
                    .build();
            list.add(ss);
        }
        return list;
    }
}
