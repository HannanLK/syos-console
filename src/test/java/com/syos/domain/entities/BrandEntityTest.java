package com.syos.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrandEntityTest {

    @Test
    void create_reconstruct_activate_deactivate_withId_and_validation() {
        // happy paths
        Brand b = Brand.create("coke", "Coca-Cola", "Drinks");
        assertTrue(b.isActive());
        Brand withId = b.withId(10L);
        assertEquals(10L, withId.getId());

        Brand deactivated = withId.deactivate();
        assertFalse(deactivated.isActive());
        Brand reactivated = deactivated.activate();
        assertTrue(reactivated.isActive());

        Brand reconstructed = Brand.reconstruct(1L, "PEPSI", "Pepsi", "Desc", true, null, null);
        assertEquals("PEPSI", reconstructed.getBrandCode());
        assertEquals("Pepsi", reconstructed.getBrandName());

        // equals/hashCode based on brandCode
        Brand b2 = Brand.create("coke", "Coca-Cola", "Drinks");
        assertEquals(b, b2);
        assertEquals(b.hashCode(), b2.hashCode());
        assertTrue(b.toString().contains("brandCode"));

        // validation failures
        assertThrows(IllegalArgumentException.class, () -> Brand.create(" ", "name", "d"));
        assertThrows(IllegalArgumentException.class, () -> Brand.create("CODE", " ", "d"));
    }
}
