package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseStockTest {

    private ItemCode code() { return ItemCode.of("ITEM-001"); }
    private Quantity qty(int v) { return Quantity.of(new java.math.BigDecimal(v)); }
    private UserID user(long id) { return UserID.of(id); }

    @Test
    void create_reserve_transfer_andValidations() {
        WarehouseStock s = WarehouseStock.createNew(code(), 1L, 100L, qty(10), LocalDateTime.now().plusDays(30), user(10), "WH-A");
        assertEquals(qty(10), s.getQuantityAvailable());
        assertFalse(s.isReserved());
        assertFalse(s.isExpired());

        WarehouseStock reserved = s.reserve(qty(5), user(22));
        assertTrue(reserved.isReserved());
        assertNotNull(reserved.getReservedBy());
        assertNotNull(reserved.getReservedAt());

        WarehouseStock afterTransfer = reserved.transfer(qty(5), user(22));
        assertEquals(0, afterTransfer.getQuantityAvailable().compareTo(qty(5))); // still 5 left
        assertFalse(afterTransfer.isReserved());
        assertNull(afterTransfer.getReservedBy());

        assertThrows(IllegalArgumentException.class, () -> s.reserve(qty(999), user(1))); // over reserve
        WarehouseStock s2 = WarehouseStock.createNew(code(), 1L, 100L, qty(5), null, user(1), null);
        assertFalse(s2.isExpiringSoon()); // no expiry

        assertThrows(IllegalArgumentException.class, () -> new WarehouseStock.Builder()
                .itemCode(code()).itemId(1L).batchId(1L)
                .quantityReceived(Quantity.of(new BigDecimal("0")))
                .quantityAvailable(Quantity.of(new BigDecimal("0")))
                .receivedDate(LocalDateTime.now()).receivedBy(user(1)).lastUpdatedBy(user(1))
                .build()); // quantityReceived must be positive

        assertThrows(IllegalArgumentException.class, () -> new WarehouseStock.Builder()
                .itemCode(code()).itemId(1L).batchId(1L)
                .quantityReceived(Quantity.of(new BigDecimal("5")))
                .quantityAvailable(Quantity.of(new BigDecimal("6")))
                .receivedDate(LocalDateTime.now()).receivedBy(user(1)).lastUpdatedBy(user(1))
                .build()); // available cannot exceed received

        assertThrows(IllegalArgumentException.class, () -> new WarehouseStock.Builder()
                .itemCode(code()).itemId(1L).batchId(1L)
                .quantityReceived(Quantity.of(new BigDecimal("5")))
                .quantityAvailable(Quantity.of(new BigDecimal("5")))
                .receivedDate(LocalDateTime.now()).receivedBy(user(1)).lastUpdatedBy(user(1))
                .isReserved(true)
                .build()); // reserved but missing details

        assertThrows(IllegalArgumentException.class, () -> new WarehouseStock.Builder()
                .itemCode(code()).itemId(1L).batchId(1L)
                .quantityReceived(Quantity.of(new BigDecimal("5")))
                .quantityAvailable(Quantity.of(new BigDecimal("5")))
                .receivedDate(LocalDateTime.now()).receivedBy(user(1)).lastUpdatedBy(user(1))
                .reservedBy(user(2))
                .build()); // non-reserved cannot have reservation details
    }
}
