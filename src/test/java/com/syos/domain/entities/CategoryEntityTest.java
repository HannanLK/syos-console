package com.syos.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryEntityTest {

    @Test
    void createRoot_createSub_reconstruct_update_activate_deactivate_and_validation() {
        Category root = Category.createRootCategory("BEV", "Beverages", "All drinks", 1);
        assertTrue(root.isRootCategory());
        assertFalse(root.isSubCategory());
        Category rootId = root.withId(5L);
        assertEquals(5L, rootId.getId());

        Category sub = Category.createSubCategory(5L, "SOFT", "Soft Drinks", "Fizz", 2);
        assertTrue(sub.isSubCategory());
        assertFalse(sub.isRootCategory());

        Category recon = Category.reconstruct(9L, 5L, "JUICE", "Juices", "Fruit juices", 3, true, null, null);
        assertEquals("JUICE", recon.getCategoryCode());

        Category updated = recon.updateDisplayOrder(4);
        assertEquals(4, updated.getDisplayOrder());
        Category deactivated = recon.deactivate();
        assertFalse(deactivated.isActive());
        Category activated = deactivated.activate();
        assertTrue(activated.isActive());

        // equality/hashCode based on code
        Category c1 = Category.createRootCategory("BEV", "Beverages", "d", 1);
        assertEquals(root, c1);
        assertEquals(root.hashCode(), c1.hashCode());
        assertTrue(root.toString().contains("categoryCode"));

        // validation failures
        assertThrows(IllegalArgumentException.class, () -> Category.createRootCategory(" ", "n", "d", 1));
        assertThrows(IllegalArgumentException.class, () -> Category.createRootCategory("CODE", " ", "d", 1));
        assertThrows(IllegalArgumentException.class, () -> Category.createSubCategory(null, "SUB", "Name", "d", 1));
    }
}
