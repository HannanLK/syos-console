package com.syos.infrastructure.persistence.repositories;

import com.syos.infrastructure.persistence.entities.BillEntity;
import com.syos.infrastructure.persistence.entities.ItemMasterFileEntity;
import com.syos.infrastructure.persistence.entities.TransactionEntity;
import com.syos.infrastructure.persistence.entities.TransactionItemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Minimal persistence helper for POS checkout that saves Transaction, its Items, and Bill.
 * Uses existing JPA entities and relies on V9 migration aligning schema.
 */
public class JpaPOSRepository {
    private final EntityManagerFactory emf;

    public JpaPOSRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Persist transaction with items and generate a bill with sequential number.
     */
    public PersistResult savePOSCheckout(TransactionEntity tx, List<PosLine> lines) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Ensure transaction number is set (DB requires NOT NULL & UNIQUE)
            if (tx.getTransactionNumber() == null || tx.getTransactionNumber().trim().isEmpty()) {
                tx.setTransactionNumber("TX" + System.currentTimeMillis());
            }

            // Persist transaction first
            em.persist(tx);
            em.flush();

            // Attach and persist items
            for (PosLine line : lines) {
                TransactionItemEntity it = new TransactionItemEntity();
                it.setTransaction(tx);
                // set item reference via getReference to avoid loading fully
                com.syos.infrastructure.persistence.entities.ItemMasterFileEntity itemRef = em.getReference(com.syos.infrastructure.persistence.entities.ItemMasterFileEntity.class, line.itemId());
                it.setItem(itemRef);
                it.setQuantity((int)Math.round(line.quantity()));
                it.setUnitPrice(line.unitPrice());
                it.setSubtotal(line.unitPrice().multiply(java.math.BigDecimal.valueOf(line.quantity())));
                if (line.discount() != null) it.setDiscountApplied(line.discount());
                em.persist(it);
            }

            // Generate next bill serial number (sequential starting at 1)
            long nextSerial = nextBillNumber(em);
            String billNumber = String.valueOf(nextSerial);

            BillEntity bill = new BillEntity(billNumber, tx);
            bill.setBillDate(LocalDateTime.now());
            bill.setCustomerName(null); // POS cash sale
            em.persist(bill);

            em.getTransaction().commit();
            return new PersistResult(tx.getTransactionId(), billNumber);
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private long nextBillNumber(EntityManager em) {
        try {
            TypedQuery<String> q = em.createQuery("SELECT b.billSerialNumber FROM BillEntity b ORDER BY CAST(b.billSerialNumber AS long) DESC", String.class);
            q.setMaxResults(1);
            List<String> last = q.getResultList();
            if (last.isEmpty()) return 1L;
            try {
                return Long.parseLong(last.get(0)) + 1L;
            } catch (NumberFormatException e) {
                // Fallback: count + 1
                Long count = em.createQuery("SELECT COUNT(b) FROM BillEntity b", Long.class).getSingleResult();
                return count + 1L;
            }
        } catch (Exception e) {
            // Fallback if table empty or casting unsupported
            Long count = em.createQuery("SELECT COUNT(b) FROM BillEntity b", Long.class).getSingleResult();
            return count + 1L;
        }
    }

    // Helper to resolve ItemMasterFileEntity by id (for TransactionItemEntity construction when needed)
    public ItemMasterFileEntity loadItem(EntityManager em, Long itemId) {
        return em.find(ItemMasterFileEntity.class, itemId);
    }

    public static BigDecimal toBD(double v) { return BigDecimal.valueOf(v).setScale(2, java.math.RoundingMode.HALF_UP); }

    public record PosLine(Long itemId, Long batchId, double quantity, BigDecimal unitPrice, BigDecimal discount) {}
    public record PersistResult(Long transactionId, String billNumber) {}
}
