package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShelfStockTest {

    private ItemCode code() { return ItemCode.of("ITEM-002"); }
    private Quantity qty(String v) { return Quantity.of(new BigDecimal(v)); }
    private UserID user(long id) { return UserID.of(id); }

    @Test
    void create_sell_restock_andFlags() {
        ShelfStock shelf = ShelfStock.createNew(code(), 2L, 200L, "S1-A", qty("10"), LocalDateTime.now().plusDays(10), user(1), Money.of("250.00"));
        assertTrue(shelf.isAvailableForSale());
        assertFalse(shelf.isExpired());

        ShelfStock sold = shelf.sellStock(qty("3"), user(2));
        assertEquals(qty("7"), sold.getQuantityOnShelf());

        // Define only minimum to trigger needsRestocking without blocking restock
        ShelfStock withMin = sold.setStockLevels(qty("10"), null, user(99));
        assertTrue(withMin.needsRestocking());

        ShelfStock restocked = withMin.restockShelf(qty("10"), user(3));
        assertEquals(qty("17"), restocked.getQuantityOnShelf());

        // Now set a maximum below current to flag overstocked
        ShelfStock withMax = restocked.setStockLevels(null, qty("15"), user(98));
        assertTrue(withMax.isOverstocked());

        ShelfStock priced = restocked.updatePrice(Money.of("300.00"), user(3));
        assertEquals(Money.of("300.00"), priced.getUnitPrice());

        ShelfStock displayed = priced.setDisplayStatus(true, user(3));
        assertTrue(displayed.isDisplayed());
        ShelfStock moved = displayed.updateDisplayPosition("EYE-LEVEL", user(3));
        assertEquals("EYE-LEVEL", moved.getDisplayPosition());

        ShelfStock levels = moved.setStockLevels(qty("5"), qty("15"), user(3));
        assertEquals(qty("5"), levels.getMinimumStockLevel());
        assertEquals(qty("15"), levels.getMaximumStockLevel());
    }

    @Test
    void validations() {
        assertThrows(IllegalArgumentException.class, () -> new ShelfStock.Builder()
                .itemCode(code()).itemId(1L).batchId(1L).shelfCode("S1")
                .quantityOnShelf(Quantity.of(new BigDecimal("-1")))
                .placedOnShelfDate(LocalDateTime.now()).placedBy(user(1))
                .unitPrice(Money.of("100")).lastUpdatedBy(user(1)).build());
    }
}
