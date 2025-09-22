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
            String jpql = "SELECT p FROM PromotionEntities$PromotionEntity p " +
                    "JOIN PromotionEntities$PromotionItemEntity pi ON pi.id.promotionId = p.id " +
                    "JOIN PromotionEntities$PromotionBatchEntity pb ON pb.id.promotionId = p.id " +
                    "WHERE pi.id.itemId = :itemId AND pb.id.batchId = :batchId " +
                    "AND p.active = true AND p.batchSpecific = true " +
                    "AND p.startDate <= :at AND p.endDate >= :at";
            TypedQuery<PromotionEntity> q = em.createQuery(jpql, PromotionEntity.class);
            q.setParameter("itemId", itemId);
            q.setParameter("batchId", batchId);
            q.setParameter("at", at);
            List<PromotionEntity> list = q.getResultList();
            if (list.isEmpty()) return Optional.empty();
            // In case of multiple, pick the most recent startDate
            list.sort((a,b) -> b.getStartDate().compareTo(a.getStartDate()));
            return Optional.of(list.get(0));
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
