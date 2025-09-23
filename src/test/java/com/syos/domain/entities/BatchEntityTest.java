package com.syos.domain.entities;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BatchEntityTest {

    private static Quantity q(int v) { return Quantity.of(BigDecimal.valueOf(v)); }
    private static Money money(double v) { return Money.of(v); }

    private static Batch mk(LocalDate received, LocalDateTime expiry, int avail) {
        return new Batch.Builder()
                .id(1L)
                .itemId(10L)
                .batchNumber("B-1")
                .quantityReceived(q(100))
                .quantityAvailable(Quantity.of(BigDecimal.valueOf(avail)))
                .manufactureDate(LocalDate.now().minusDays(5))
                .expiryDate(expiry)
                .receivedDate(received)
                .receivedBy(UserID.of(9L))
                .costPerUnit(money(10))
                .supplierBatchNumber("S-B-1")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void reduceQuantity_expiry_helpers_and_selection_rules() {
        Batch b = mk(LocalDate.now().minusDays(10), LocalDateTime.now().plusDays(20), 50);
        assertFalse(b.isExpired());
        assertFalse(b.isExpiringSoon());
        assertTrue(b.hasAvailableStock());
        assertNotNull(b.getDaysUntilExpiry());
        assertTrue(b.getAgeInDays() >= 10);

        // expiring soon within 7 days
        Batch soon = mk(LocalDate.now().minusDays(1), LocalDateTime.now().plusDays(3), 5);
        assertTrue(soon.isExpiringSoon());

        // reduce ok and fail
        Batch after = b.reduceQuantity(Quantity.of(BigDecimal.valueOf(20)));
        assertEquals(30, after.getQuantityAvailable().getValue().intValue());
        assertThrows(IllegalArgumentException.class, () -> b.reduceQuantity(Quantity.of(BigDecimal.valueOf(999))));

        // selection rules: expiry priority over FIFO
        Batch olderLaterExpiry = mk(LocalDate.now().minusDays(30), LocalDateTime.now().plusDays(30), 10);
        Batch newerSoonerExpiry = mk(LocalDate.now().minusDays(5), LocalDateTime.now().plusDays(2), 10);
        assertTrue(newerSoonerExpiry.shouldBeSelectedBefore(olderLaterExpiry));

        // no expiry then FIFO by receivedDate (older first)
        Batch noExpOld = mk(LocalDate.now().minusDays(10), null, 1);
        Batch noExpNew = mk(LocalDate.now().minusDays(1), null, 1);
        assertTrue(noExpOld.shouldBeSelectedBefore(noExpNew));
    }

    @Test
    void equals_and_hashcode() {
        Batch b1 = mk(LocalDate.now().minusDays(1), null, 10);
        Batch b2 = new Batch.Builder(b1).build();
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }
}
