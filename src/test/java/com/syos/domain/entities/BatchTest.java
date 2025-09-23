package com.syos.domain.entities;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BatchTest {

    private Quantity qty(String v) { return Quantity.of(new BigDecimal(v)); }
    private UserID user(long id) { return UserID.of(id); }

    @Test
    void create_reduce_andFlags() {
        LocalDate manuf = LocalDate.now().minusDays(10);
        LocalDateTime exp = LocalDateTime.now().plusDays(6);
        Batch b = Batch.createNew(1L, "B-1", qty("100"), manuf, exp, user(1), Money.of("10.00"));
        assertTrue(b.hasAvailableStock());
        assertFalse(b.isExpired());
        assertTrue(b.isExpiringSoon());
        assertNotNull(b.getDaysUntilExpiry());

        Batch after = b.reduceQuantity(qty("25"));
        assertEquals(qty("75"), after.getQuantityAvailable());

        assertThrows(IllegalArgumentException.class, () -> after.reduceQuantity(qty("1000")));
    }

    @Test
    void shouldBeSelectedBefore_branches() {
        Batch noExpiryOlder = new Batch.Builder()
                .itemId(1L).batchNumber("A").quantityReceived(qty("10"))
                .quantityAvailable(qty("10")).receivedBy(user(1))
                .receivedDate(LocalDate.now().minusDays(5))
                .build();
        Batch noExpiryNewer = new Batch.Builder()
                .itemId(1L).batchNumber("B").quantityReceived(qty("10"))
                .quantityAvailable(qty("10")).receivedBy(user(1))
                .receivedDate(LocalDate.now().minusDays(2))
                .build();
        assertTrue(noExpiryOlder.shouldBeSelectedBefore(noExpiryNewer)); // FIFO by receivedDate

        Batch expSooner = new Batch.Builder()
                .itemId(1L).batchNumber("C").quantityReceived(qty("10"))
                .quantityAvailable(qty("10")).receivedBy(user(1))
                .expiryDate(LocalDateTime.now().plusDays(3))
                .build();
        Batch expLater = new Batch.Builder()
                .itemId(1L).batchNumber("D").quantityReceived(qty("10"))
                .quantityAvailable(qty("10")).receivedBy(user(1))
                .expiryDate(LocalDateTime.now().plusDays(10))
                .build();
        assertTrue(expSooner.shouldBeSelectedBefore(expLater)); // earlier expiry prioritized

        Batch onlyOneHasExpiry = new Batch.Builder()
                .itemId(1L).batchNumber("E").quantityReceived(qty("10"))
                .quantityAvailable(qty("10")).receivedBy(user(1))
                .expiryDate(LocalDateTime.now().plusDays(12))
                .build();
        assertTrue(onlyOneHasExpiry.shouldBeSelectedBefore(noExpiryOlder));
        assertFalse(noExpiryOlder.shouldBeSelectedBefore(onlyOneHasExpiry));
    }

    @Test
    void validations() {
        // Empty batch number
        assertThrows(IllegalArgumentException.class, () -> new Batch.Builder()
                .itemId(1L).batchNumber(" ")
                .quantityReceived(qty("1"))
                .quantityAvailable(qty("1"))
                .receivedBy(user(1))
                .build());

        // Quantity received must be positive
        assertThrows(IllegalArgumentException.class, () -> new Batch.Builder()
                .itemId(1L).batchNumber("B1")
                .quantityReceived(qty("0"))
                .quantityAvailable(qty("0"))
                .receivedBy(user(1))
                .build());

        // Available cannot exceed received
        assertThrows(IllegalArgumentException.class, () -> new Batch.Builder()
                .itemId(1L).batchNumber("B2")
                .quantityReceived(qty("5"))
                .quantityAvailable(qty("6"))
                .receivedBy(user(1))
                .build());

        // Expiry must be after manufacture date
        assertThrows(IllegalArgumentException.class, () -> new Batch.Builder()
                .itemId(1L).batchNumber("B3")
                .quantityReceived(qty("5")).quantityAvailable(qty("5"))
                .manufactureDate(LocalDate.now())
                .expiryDate(LocalDateTime.now())
                .receivedBy(user(1))
                .build());

        // Cost per unit must be positive when provided
        assertThrows(IllegalArgumentException.class, () -> new Batch.Builder()
                .itemId(1L).batchNumber("B4")
                .quantityReceived(qty("5")).quantityAvailable(qty("5"))
                .receivedBy(user(1))
                .costPerUnit(Money.of("0"))
                .build());
    }
}
