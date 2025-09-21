package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.BillReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * JPA implementation for bill reporting queries using lightweight projections.
 */
public class JpaBillReportRepository implements BillReportRepository {
    private final EntityManagerFactory emf;

    public JpaBillReportRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Object[]> listBillsBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        EntityManager em = emf.createEntityManager();
        try {
            @SuppressWarnings("unchecked")
            TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createQuery(
                "SELECT b.billSerialNumber, b.billDate, CAST(t.transactionType AS string), b.totalAmount, b.customerName " +
                "FROM BillEntity b JOIN b.transaction t " +
                "WHERE b.billDate >= :start AND b.billDate < :end " +
                "ORDER BY b.billDate DESC"
            );
            q.setParameter("start", startInclusive);
            q.setParameter("end", endExclusive);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> listRecentBills(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            @SuppressWarnings("unchecked")
            TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createQuery(
                "SELECT b.billSerialNumber, b.billDate, CAST(t.transactionType AS string), b.totalAmount, b.customerName " +
                "FROM BillEntity b JOIN b.transaction t ORDER BY b.billDate DESC"
            );
            q.setMaxResults(Math.max(1, limit));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> listBillsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX).withNano(0);
        return listBillsBetween(start, end);
    }
}
