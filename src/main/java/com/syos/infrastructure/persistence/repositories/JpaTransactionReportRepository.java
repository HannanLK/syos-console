package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.TransactionReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * JPA implementation for transaction reporting queries using lightweight projections.
 */
public class JpaTransactionReportRepository implements TransactionReportRepository {
    private final EntityManagerFactory emf;

    public JpaTransactionReportRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Object[] findDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX).withNano(0);
        EntityManager em = emf.createEntityManager();
        try {
            Object[] row = (Object[]) em.createQuery(
                "SELECT COUNT(t), COALESCE(SUM(t.totalAmount), 0), COALESCE(SUM(t.discountAmount), 0) " +
                "FROM TransactionEntity t " +
                "WHERE t.transactionDate >= :start AND t.transactionDate <= :end AND t.status = com.syos.infrastructure.persistence.entities.TransactionEntity$TransactionStatus.COMPLETED"
            )
            .setParameter("start", start)
            .setParameter("end", end)
            .getSingleResult();
            if (row == null) {
                return new Object[]{0L, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO};
            }
            return row;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findDailyItemAggregates(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX).withNano(0);
        EntityManager em = emf.createEntityManager();
        try {
            @SuppressWarnings("unchecked")
            TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createQuery(
                "SELECT i.itemCode, i.itemName, SUM(it.quantity), COALESCE(SUM(it.subtotal), 0) " +
                "FROM TransactionItemEntity it " +
                "JOIN it.transaction t " +
                "JOIN it.item i " +
                "WHERE t.transactionDate >= :start AND t.transactionDate <= :end " +
                "AND t.status = com.syos.infrastructure.persistence.entities.TransactionEntity$TransactionStatus.COMPLETED " +
                "GROUP BY i.itemCode, i.itemName " +
                "ORDER BY SUM(it.quantity) DESC"
            );
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findChannelSummary(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        EntityManager em = emf.createEntityManager();
        try {
            @SuppressWarnings("unchecked")
            TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createQuery(
                "SELECT CAST(t.transactionType AS string), COUNT(t), COALESCE(SUM(t.totalAmount), 0), " +
                "CASE WHEN COUNT(t) = 0 THEN 0 ELSE COALESCE(SUM(t.totalAmount), 0) / COUNT(t) END " +
                "FROM TransactionEntity t " +
                "WHERE t.transactionDate >= :start AND t.transactionDate < :end " +
                "AND t.status = com.syos.infrastructure.persistence.entities.TransactionEntity$TransactionStatus.COMPLETED " +
                "GROUP BY t.transactionType ORDER BY t.transactionType"
            );
            q.setParameter("start", startInclusive);
            q.setParameter("end", endExclusive);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
