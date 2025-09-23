package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void create_andValidations() {
        Item item = Item.create(ItemCode.of("PRD-001"), "  Cola  ", Money.of("100.00"), Money.of("120.00"), true, 50);
        assertEquals("Cola", item.getName());
        assertEquals(ItemCode.of("PRD-001"), item.getCode());
        assertEquals(Money.of("100.00"), item.getCostPrice());
        assertEquals(Money.of("120.00"), item.getSellingPrice());
        assertTrue(item.isPerishable());
        assertEquals(50, item.getReorderPoint());

        Item withId = item.withId(10L);
        assertEquals(10L, withId.getId());
        assertNull(item.getId());

        // Invalid cases
        assertThrows(IllegalArgumentException.class, () -> Item.create(ItemCode.of("PRD-002"), " ", Money.of("10.00"), Money.of("12.00"), false, 0));
        assertThrows(IllegalArgumentException.class, () -> Item.create(ItemCode.of("PRD-003"), "X", Money.of("10.00"), Money.of("9.99"), false, 0));
    }
}
