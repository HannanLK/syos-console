package com.syos.infrastructure.persistence.repositories;

import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionEntity;
import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Minimal JPA repository to query and create Promotions and batch mappings.
 */
public class JpaPromotionRepository {
    private static final Logger logger = LoggerFactory.getLogger(JpaPromotionRepository.class);
    private final EntityManagerFactory emf;

    public JpaPromotionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Optional<PromotionEntity> findActiveBatchPromotionForItemAndBatch(Long itemId, Long batchId, LocalDateTime at) {
        EntityManager em = emf.createEntityManager();
        try {
            // Use native SQL to avoid reliance on JPA entity registration in HQL parsing
            String sql = "SELECT p.promotion_type, p.discount_value, p.promo_code, p.promo_name, p.start_date, p.end_date " +
                    "FROM promotions p " +
                    "JOIN promotion_items pi ON pi.promotion_id = p.id " +
                    "JOIN promotion_batches pb ON pb.promotion_id = p.id " +
                    "WHERE pi.item_id = ? AND pb.batch_id = ? " +
                    "AND p.is_active = true AND p.is_batch_specific = true " +
                    "AND p.start_date <= ? AND p.end_date >= ? " +
                    "ORDER BY p.start_date DESC LIMIT 1";

            jakarta.persistence.Query q = em.createNativeQuery(sql);
            q.setParameter(1, itemId);
            q.setParameter(2, batchId);
            q.setParameter(3, at);
            q.setParameter(4, at);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return Optional.empty();
            Object[] r = rows.get(0);
            String typeStr = (String) r[0];
            java.math.BigDecimal discount = (java.math.BigDecimal) r[1];
            String code = (String) r[2];
            String name = (String) r[3];
            java.time.LocalDateTime start = (java.time.LocalDateTime) r[4];
            java.time.LocalDateTime end = (java.time.LocalDateTime) r[5];

            PromotionEntity p = new PromotionEntity();
            // Set minimal fields used by DiscountService
            p.setPromotionType(PromotionType.valueOf(typeStr));
            p.setDiscountValue(discount);
            p.setPromoCode(code);
            p.setPromoName(name);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setActive(true);
            p.setBatchSpecific(true);
            return Optional.of(p);
        } finally {
            em.close();
        }
    }

    public PromotionEntity createBasicBatchPromotion(String code, String name, PromotionType type, java.math.BigDecimal value,
                                                     LocalDateTime start, LocalDateTime end,
                                                     Long itemId, List<Long> batchIds) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PromotionEntity p = new PromotionEntity();
            p.setPromoCode(code);
            p.setPromoName(name);
            p.setPromotionType(type);
            p.setDiscountValue(value);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setActive(true);
            p.setBatchSpecific(true);

            em.persist(p);
            em.flush();

            // link item
            em.createNativeQuery("INSERT INTO promotion_items(promotion_id, item_id) VALUES (?,?)")
                    .setParameter(1, p.getId())
                    .setParameter(2, itemId)
                    .executeUpdate();

            // link batches
            for (Long bId : batchIds) {
                em.createNativeQuery("INSERT INTO promotion_batches(promotion_id, batch_id) VALUES (?,?)")
                        .setParameter(1, p.getId())
                        .setParameter(2, bId)
                        .executeUpdate();
            }

            em.getTransaction().commit();
            return p;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
