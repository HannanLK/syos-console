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
        WarehouseStockEntity entity = null;
        try {
            em.getTransaction().begin();

            entity = toEntity(warehouseStock);

            // Resolve and set required DB-level fields before persist/merge
            // 1) location_id must reference a WAREHOUSE location (trigger enforces this)
            Long locId = resolveWarehouseLocationId(em, entity.getLocation());
            entity.setLocationId(locId);

            // 2) Generate a warehouse_code if the schema requires it
            if (entity.getWarehouseCode() == null || entity.getWarehouseCode().isEmpty()) {
                entity.setWarehouseCode(generateWarehouseCode(entity));
            }

            WarehouseStockEntity saved;

            if (warehouseStock.getId() == null) {
                // Use native insert to ensure all required columns (quantity, location_id, etc.) are set consistently
                Long newId = nativeInsertWarehouseStock(em, entity);
                saved = em.find(WarehouseStockEntity.class, newId);
                logger.debug("Saved new warehouse stock (native) for item code: {}", warehouseStock.getItemCode().getValue());
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
            // Defensive fallback: some runtimes may still miss location_id in INSERT; try native insert
            if (isWarehouseLocationTriggerError(e) && entity != null) {
                try {
                    logger.warn("Falling back to native INSERT for warehouse_stock due to location trigger error (itemCode={})", warehouseStock.getItemCode().getValue());
                    em.getTransaction().begin();
                    Long newId = nativeInsertWarehouseStock(em, entity);
                    em.getTransaction().commit();
                    WarehouseStockEntity reloaded = em.find(WarehouseStockEntity.class, newId);
                    return toDomain(reloaded);
                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.error("Fallback native insert failed for warehouse stock (itemCode={})", warehouseStock.getItemCode().getValue(), ex);
                }
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
            // Use native query to compute availability from canonical columns (quantity - reserved_quantity)
            // This avoids inconsistencies if quantity_available is not in sync and aligns with pre-listing logic
            String sql = "SELECT " +
                    " ws.id AS id, " +
                    " im.item_code AS item_code, " +
                    " ws.item_id AS item_id, " +
                    " ws.batch_id AS batch_id, " +
                    " COALESCE(ws.quantity_received, ws.quantity) AS quantity_received, " +
                    " COALESCE(ws.quantity_available, (ws.quantity - COALESCE(ws.reserved_quantity, 0))) AS quantity_available, " +
                    " ws.received_date AS received_date, " +
                    " b.expiry_date AS expiry_date, " +
                    " COALESCE(ws.created_by, 0) AS received_by, " +
                    " COALESCE(l.location_name, 'MAIN-WAREHOUSE') AS location, " +
                    " false AS is_reserved, " +
                    " NULL::BIGINT AS reserved_by, " +
                    " NULL::TIMESTAMP AS reserved_at, " +
                    " ws.last_updated AS last_updated, " +
                    " COALESCE(ws.created_by, 0) AS last_updated_by " +
                    " FROM warehouse_stock ws " +
                    " JOIN item_master_file im ON im.id = ws.item_id " +
                    " JOIN batches b ON b.id = ws.batch_id " +
                    " JOIN locations l ON l.id = ws.location_id " +
                    " WHERE im.item_code = :itemCode " +
                    "   AND COALESCE(ws.quantity_available, (ws.quantity - COALESCE(ws.reserved_quantity, 0))) > 0 " +
                    " ORDER BY ws.received_date ASC";

            jakarta.persistence.Query query = em.createNativeQuery(sql);
            query.setParameter("itemCode", itemCode.getValue());
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rows = query.getResultList();

            java.util.List<WarehouseStock> results = new java.util.ArrayList<>();
            for (Object[] r : rows) {
                int i = 0;
                Long id = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                String code = (String) r[i++];
                Long itemId = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                Long batchId = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                java.math.BigDecimal qtyReceived = (java.math.BigDecimal) r[i++];
                java.math.BigDecimal qtyAvailable = (java.math.BigDecimal) r[i++];
                Object receivedObj = r[i++];
                Object expiryObj = r[i++];
                Long receivedBy = r[i++] == null ? 0L : ((Number) r[i-1]).longValue();
                String loc = (String) r[i++];
                // skip is_reserved, reserved_by, reserved_at
                i += 3;
                Object lastUpdatedObj = r[i++];
                Long lastUpdatedBy = r[i++] == null ? 0L : ((Number) r[i-1]).longValue();

                java.time.LocalDateTime receivedLdt = toLocalDateTimeSafe(receivedObj);
                java.time.LocalDateTime expiryLdt = toStartOfDaySafe(expiryObj);
                java.time.LocalDateTime lastUpdatedLdt = toLocalDateTimeSafe(lastUpdatedObj);

                WarehouseStock ws = WarehouseStock.builder()
                        .id(id)
                        .itemCode(com.syos.domain.valueobjects.ItemCode.of(code))
                        .itemId(itemId)
                        .batchId(batchId)
                        .quantityReceived(com.syos.domain.valueobjects.Quantity.of(qtyReceived))
                        .quantityAvailable(com.syos.domain.valueobjects.Quantity.of(qtyAvailable))
                        .receivedDate(receivedLdt != null ? receivedLdt : java.time.LocalDateTime.now())
                        .expiryDate(expiryLdt)
                        .receivedBy(com.syos.domain.valueobjects.UserID.of(receivedBy))
                        .location(loc)
                        .isReserved(false)
                        .reservedBy(null)
                        .reservedAt(null)
                        .lastUpdated(lastUpdatedLdt != null ? lastUpdatedLdt : java.time.LocalDateTime.now())
                        .lastUpdatedBy(com.syos.domain.valueobjects.UserID.of(lastUpdatedBy))
                        .build();
                results.add(ws);
            }
            return results;
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
            // Normalize common aliases to match seeded defaults
            String locInput = (location == null ? "" : location.trim());
            String locUp = locInput.toUpperCase();
            // Build SQL with flexible matching; accept aliases and case-insensitive compare
            String sql = "SELECT " +
                    " ws.id AS id, " +
                    " im.item_code AS item_code, " +
                    " ws.item_id AS item_id, " +
                    " ws.batch_id AS batch_id, " +
                    " ws.quantity AS quantity_received, " +
                    " (ws.quantity - COALESCE(ws.reserved_quantity, 0)) AS quantity_available, " +
                    " ws.received_date AS received_date, " +
                    " b.expiry_date AS expiry_date, " +
                    " COALESCE(ws.created_by, 0) AS received_by, " +
                    " COALESCE(l.location_name, 'MAIN-WAREHOUSE') AS location, " +
                    " false AS is_reserved, " +
                    " NULL::BIGINT AS reserved_by, " +
                    " NULL::TIMESTAMP AS reserved_at, " +
                    " ws.last_updated AS last_updated, " +
                    " COALESCE(ws.created_by, 0) AS last_updated_by " +
                " FROM warehouse_stock ws " +
                " JOIN item_master_file im ON im.id = ws.item_id " +
                " JOIN batches b ON b.id = ws.batch_id " +
                " JOIN locations l ON l.id = ws.location_id " +
                " WHERE (" +
                "   UPPER(l.location_code) = :locUp " +
                "   OR UPPER(l.location_name) = :locUp " +
                "   OR (:locUp IN ('MAIN-WAREHOUSE','MAIN WAREHOUSE','MAINWAREHOUSE') AND (l.location_code = 'MAIN_WH' OR UPPER(l.location_name) = 'MAIN WAREHOUSE'))" +
                ") " +
                " ORDER BY ws.received_date ASC";

            jakarta.persistence.Query query = em.createNativeQuery(sql);
            query.setParameter("locUp", locUp);
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rows = query.getResultList();

            // Fallback: if nothing matched, and caller used generic MAIN-WAREHOUSE alias, return all WAREHOUSE stocks
            if ((rows == null || rows.isEmpty()) && (locUp.equals("MAIN-WAREHOUSE") || locUp.equals("MAIN WAREHOUSE") || locUp.equals("MAINWAREHOUSE"))) {
                String sqlAll = "SELECT " +
                        " ws.id AS id, " +
                        " im.item_code AS item_code, " +
                        " ws.item_id AS item_id, " +
                        " ws.batch_id AS batch_id, " +
                        " ws.quantity AS quantity_received, " +
                        " (ws.quantity - COALESCE(ws.reserved_quantity, 0)) AS quantity_available, " +
                        " ws.received_date AS received_date, " +
                        " b.expiry_date AS expiry_date, " +
                        " COALESCE(ws.created_by, 0) AS received_by, " +
                        " COALESCE(l.location_name, 'MAIN-WAREHOUSE') AS location, " +
                        " false AS is_reserved, " +
                        " NULL::BIGINT AS reserved_by, " +
                        " NULL::TIMESTAMP AS reserved_at, " +
                        " ws.last_updated AS last_updated, " +
                        " COALESCE(ws.created_by, 0) AS last_updated_by " +
                    " FROM warehouse_stock ws " +
                    " JOIN item_master_file im ON im.id = ws.item_id " +
                    " JOIN batches b ON b.id = ws.batch_id " +
                    " JOIN locations l ON l.id = ws.location_id " +
                    " WHERE l.location_type = 'WAREHOUSE' " +
                    " ORDER BY ws.received_date ASC"; // show all warehouses
                jakarta.persistence.Query qAll = em.createNativeQuery(sqlAll);
                rows = qAll.getResultList();
            }

            java.util.List<WarehouseStock> results = new java.util.ArrayList<>();
            for (Object[] r : rows) {
                int i = 0;
                Long id = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                String itemCode = (String) r[i++];
                Long itemId = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                Long batchId = r[i++] == null ? null : ((Number) r[i-1]).longValue();
                java.math.BigDecimal qtyReceived = (java.math.BigDecimal) r[i++];
                java.math.BigDecimal qtyAvailable = (java.math.BigDecimal) r[i++];
                Object receivedObj = r[i++];
                Object expiryObj = r[i++];
                Long receivedBy = r[i++] == null ? 0L : ((Number) r[i-1]).longValue();
                String loc = (String) r[i++];
                // skip is_reserved
                i++;
                // reserved_by
                i++;
                // reserved_at
                i++;
                Object lastUpdatedObj = r[i++];
                Long lastUpdatedBy = r[i++] == null ? 0L : ((Number) r[i-1]).longValue();

                java.time.LocalDateTime receivedLdt = toLocalDateTimeSafe(receivedObj);
                java.time.LocalDateTime expiryLdt = toStartOfDaySafe(expiryObj);
                java.time.LocalDateTime lastUpdatedLdt = toLocalDateTimeSafe(lastUpdatedObj);

                WarehouseStock ws = WarehouseStock.builder()
                        .id(id)
                        .itemCode(com.syos.domain.valueobjects.ItemCode.of(itemCode))
                        .itemId(itemId)
                        .batchId(batchId)
                        .quantityReceived(com.syos.domain.valueobjects.Quantity.of(qtyReceived))
                        .quantityAvailable(com.syos.domain.valueobjects.Quantity.of(qtyAvailable))
                        .receivedDate(receivedLdt != null ? receivedLdt : java.time.LocalDateTime.now())
                        .expiryDate(expiryLdt)
                        .receivedBy(com.syos.domain.valueobjects.UserID.of(receivedBy))
                        .location(loc != null ? loc : locInput)
                        .isReserved(false)
                        .reservedBy(null)
                        .reservedAt(null)
                        .lastUpdated(lastUpdatedLdt != null ? lastUpdatedLdt : java.time.LocalDateTime.now())
                        .lastUpdatedBy(com.syos.domain.valueobjects.UserID.of(lastUpdatedBy))
                        .build();
                results.add(ws);
            }
            return results;
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

    // Resolve WAREHOUSE location id using name or code, with safe fallbacks
    private Long resolveWarehouseLocationId(EntityManager em, String providedLocation) {
        String candidate = (providedLocation == null || providedLocation.trim().isEmpty()) ? "Main Warehouse" : providedLocation.trim();
        String upper = candidate.toUpperCase();
        // Try by exact code or name (case-insensitive)
        String sql = "SELECT id FROM locations WHERE location_type = 'WAREHOUSE' AND is_active = true AND (UPPER(location_name) = :v OR UPPER(location_code) = :v) ORDER BY CASE WHEN UPPER(location_code) = :v THEN 0 ELSE 1 END LIMIT 1";
        jakarta.persistence.Query q = em.createNativeQuery(sql);
        q.setParameter("v", upper);
        @SuppressWarnings("unchecked")
        java.util.List<Object> res = q.getResultList();
        if (!res.isEmpty()) {
            Object v = res.get(0);
            if (v instanceof Number) return ((Number) v).longValue();
            if (v != null) return Long.valueOf(v.toString());
        }
        // Fallback to seeded defaults
        String fallbackSql = "SELECT id FROM locations WHERE (location_code = 'MAIN_WH' OR location_name = 'Main Warehouse') AND location_type = 'WAREHOUSE' ORDER BY CASE WHEN location_code = 'MAIN_WH' THEN 0 ELSE 1 END LIMIT 1";
        q = em.createNativeQuery(fallbackSql);
        res = q.getResultList();
        if (!res.isEmpty()) {
            Object v = res.get(0);
            if (v instanceof Number) return ((Number) v).longValue();
            if (v != null) return Long.valueOf(v.toString());
        }
        // Last resort: any active warehouse
        q = em.createNativeQuery("SELECT id FROM locations WHERE location_type = 'WAREHOUSE' AND is_active = true ORDER BY id ASC LIMIT 1");
        res = q.getResultList();
        if (!res.isEmpty()) {
            Object v = res.get(0);
            if (v instanceof Number) return ((Number) v).longValue();
            if (v != null) return Long.valueOf(v.toString());
        }
        throw new IllegalStateException("No WAREHOUSE location found in database");
    }

    // Generate a warehouse_code compatible with older schema
    private String generateWarehouseCode(WarehouseStockEntity e) {
        String itemPart = e.getItemId() != null ? e.getItemId().toString() : "0";
        String batchPart = e.getBatchId() != null ? e.getBatchId().toString() : "0";
        String ts = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now());
        return "WH-" + itemPart + "-" + batchPart + "-" + ts;
    }

    private boolean isWarehouseLocationTriggerError(Throwable e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                String up = msg.toUpperCase();
                if (up.contains("LOCATION MUST BE OF TYPE WAREHOUSE") || up.contains("VALIDATE_WAREHOUSE_LOCATION") || up.contains("CHECK_WAREHOUSE_LOCATION")) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    private Long nativeInsertWarehouseStock(EntityManager em, WarehouseStockEntity e) {
        // Build a native insert that explicitly sets location_id and core columns
        String sql = "INSERT INTO warehouse_stock (" +
                "warehouse_code, item_id, batch_id, location_id, quantity, reserved_quantity, received_date, last_updated, created_by, " +
                "item_code, quantity_received, quantity_available, expiry_date, received_by, location, is_reserved, reserved_by, reserved_at, last_updated_by" +
                ") VALUES (?,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,?, ?, ?) RETURNING id";

        jakarta.persistence.Query q = em.createNativeQuery(sql);
        int i = 1;
        q.setParameter(i++, e.getWarehouseCode());
        q.setParameter(i++, e.getItemId());
        q.setParameter(i++, e.getBatchId());
        q.setParameter(i++, e.getLocationId());
        // quantity – align to quantity_received for initial receipt
        q.setParameter(i++, e.getQuantityReceived());
        // reserved_quantity literal 0 (no param)
        q.setParameter(i++, e.getReceivedDate() != null ? e.getReceivedDate() : java.time.LocalDateTime.now());
        q.setParameter(i++, e.getLastUpdated() != null ? e.getLastUpdated() : java.time.LocalDateTime.now());
        // created_by – use receivedBy as creator if available
        q.setParameter(i++, e.getReceivedBy());
        q.setParameter(i++, e.getItemCode());
        q.setParameter(i++, e.getQuantityReceived());
        q.setParameter(i++, e.getQuantityAvailable() != null ? e.getQuantityAvailable() : e.getQuantityReceived());
        q.setParameter(i++, e.getExpiryDate());
        q.setParameter(i++, e.getReceivedBy());
        q.setParameter(i++, e.getLocation());
        q.setParameter(i++, e.getIsReserved() != null ? e.getIsReserved() : Boolean.FALSE);
        q.setParameter(i++, e.getReservedBy());
        q.setParameter(i++, e.getReservedAt());
        q.setParameter(i++, e.getLastUpdatedBy());

        Object idObj = q.getSingleResult();
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return Long.valueOf(idObj.toString());
    }

    // --- Safe date/time conversion helpers to tolerate JDBC driver return types ---
    private java.time.LocalDateTime toLocalDateTimeSafe(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) v;
        if (v instanceof java.sql.Timestamp) return ((java.sql.Timestamp) v).toLocalDateTime();
        if (v instanceof java.sql.Date) return ((java.sql.Date) v).toLocalDate().atStartOfDay();
        if (v instanceof java.util.Date) return new java.sql.Timestamp(((java.util.Date) v).getTime()).toLocalDateTime();
        if (v instanceof java.time.LocalDate) return ((java.time.LocalDate) v).atStartOfDay();
        // Unknown type; best-effort parse if it's a string
        if (v instanceof CharSequence) {
            String s = v.toString().trim();
            try { return java.time.LocalDateTime.parse(s); } catch (Exception ignore) {}
            try { return java.time.LocalDate.parse(s).atStartOfDay(); } catch (Exception ignore) {}
        }
        return null;
    }

    private java.time.LocalDateTime toStartOfDaySafe(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDateTime) return ((java.time.LocalDateTime) v);
        if (v instanceof java.time.LocalDate) return ((java.time.LocalDate) v).atStartOfDay();
        if (v instanceof java.sql.Date) return ((java.sql.Date) v).toLocalDate().atStartOfDay();
        if (v instanceof java.sql.Timestamp) return ((java.sql.Timestamp) v).toLocalDateTime();
        if (v instanceof java.util.Date) return new java.sql.Timestamp(((java.util.Date) v).getTime()).toLocalDateTime();
        if (v instanceof CharSequence) {
            String s = v.toString().trim();
            try { return java.time.LocalDate.parse(s).atStartOfDay(); } catch (Exception ignore) {}
            try { return java.time.LocalDateTime.parse(s); } catch (Exception ignore) {}
        }
        return null;
    }
}
