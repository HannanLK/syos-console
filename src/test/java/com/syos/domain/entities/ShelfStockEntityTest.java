package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShelfStockEntityTest {

    private static ItemCode code() { return new ItemCode("ITM01"); }
    private static Quantity qty(int v) { return Quantity.of(java.math.BigDecimal.valueOf(v)); }
    private static Money money(double v) { return Money.of(v); }
    private static UserID uid(long v) { return UserID.of(v); }

    private static ShelfStock newShelf(int q) {
        return ShelfStock.createNew(code(), 1L, 11L, "A1", qty(q),
                LocalDateTime.now().plusDays(10), uid(9L), money(150));
    }

    @Test
    void create_sell_restock_price_display_and_helpers_cover_branches() {
        ShelfStock s = newShelf(10);
        assertTrue(s.isDisplayed());
        assertFalse(s.isExpired());
        assertNotNull(s.getTotalValue());
        assertTrue(s.isAvailableForSale());

        // isExpiringSoon false then true
        assertFalse(s.isExpiringSoon());
        ShelfStock soon = new ShelfStock.Builder(s)
                .expiryDate(LocalDateTime.now().plusDays(2))
                .lastUpdatedBy(uid(1L)).build();
        assertTrue(soon.isExpiringSoon());

        // needsRestocking / overstocked paths
        ShelfStock withLevels = new ShelfStock.Builder(s)
                .minimumStockLevel(qty(8))
                .maximumStockLevel(qty(15))
                .lastUpdatedBy(uid(1L)).build();
        assertFalse(withLevels.needsRestocking());
        assertFalse(withLevels.isOverstocked());
        ShelfStock low = new ShelfStock.Builder(withLevels)
                .quantityOnShelf(qty(5)).lastUpdatedBy(uid(1L)).build();
        assertTrue(low.needsRestocking());
        ShelfStock high = new ShelfStock.Builder(withLevels)
                .quantityOnShelf(qty(20)).lastUpdatedBy(uid(1L)).build();
        assertTrue(high.isOverstocked());

        // sell success and failure
        ShelfStock afterSell = s.sellStock(qty(3), uid(2L));
        assertEquals(7, afterSell.getQuantityOnShelf().getValue().intValue());
        assertThrows(IllegalArgumentException.class, () -> s.sellStock(qty(99), uid(2L)));

        // restock success and failure (exceeding max)
        ShelfStock capped = new ShelfStock.Builder(s)
                .maximumStockLevel(qty(11))
                .lastUpdatedBy(uid(1L)).build();
        assertThrows(IllegalArgumentException.class, () -> capped.restockShelf(qty(5), uid(3L)));
        ShelfStock restocked = s.restockShelf(qty(5), uid(3L));
        assertEquals(15, restocked.getQuantityOnShelf().getValue().intValue());
        assertThrows(IllegalArgumentException.class, () -> s.restockShelf(Quantity.zero(), uid(3L)));

        // price update and display toggles
        ShelfStock priced = s.updatePrice(money(200), uid(4L));
        assertEquals(money(200), priced.getUnitPrice());
        assertThrows(IllegalArgumentException.class, () -> s.updatePrice(money(0), uid(4L)));

        ShelfStock hidden = s.setDisplayStatus(false, uid(5L));
        assertFalse(hidden.isDisplayed());
        ShelfStock moved = s.updateDisplayPosition("B2", uid(5L));
        assertEquals("B2", moved.getDisplayPosition());

        // equals/hashCode on id/code/batch etc.
        ShelfStock s2 = new ShelfStock.Builder(s).lastUpdatedBy(uid(9L)).build();
        assertEquals(s, s2);
        assertEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    void builder_validation_rules_and_nulls() {
        // invalid shelf code
        assertThrows(IllegalArgumentException.class, () ->
                new ShelfStock.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .shelfCode(" ")
                        .quantityOnShelf(qty(1))
                        .placedBy(uid(1L))
                        .unitPrice(money(1))
                        .lastUpdatedBy(uid(1L))
                        .build());
        // negative quantity
        assertThrows(IllegalArgumentException.class, () ->
                new ShelfStock.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .shelfCode("A1")
                        .quantityOnShelf(Quantity.of(java.math.BigDecimal.valueOf(-1)))
                        .placedBy(uid(1L))
                        .unitPrice(money(1))
                        .lastUpdatedBy(uid(1L))
                        .build());
        // non-positive price
        assertThrows(IllegalArgumentException.class, () ->
                new ShelfStock.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .shelfCode("A1")
                        .quantityOnShelf(qty(1))
                        .placedBy(uid(1L))
                        .unitPrice(money(0))
                        .lastUpdatedBy(uid(1L))
                        .build());
        // min > max stock level
        assertThrows(IllegalArgumentException.class, () ->
                new ShelfStock.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .shelfCode("A1")
                        .quantityOnShelf(qty(1))
                        .placedBy(uid(1L))
                        .unitPrice(money(10))
                        .minimumStockLevel(qty(5))
                        .maximumStockLevel(qty(4))
                        .lastUpdatedBy(uid(1L))
                        .build());
    }
}
