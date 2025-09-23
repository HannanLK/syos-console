package com.syos.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierEntityTest {

    @Test
    void create_reconstruct_update_activate_deactivate_equals_and_validation() {
        Supplier s = Supplier.create("SUP1", "ACME Ltd", "+94 77 123 4567", "mail@acme.lk", "Colombo", "John");
        assertTrue(s.isActive());
        Supplier withId = s.withId(7L);
        assertEquals(7L, withId.getId());

        Supplier updated = withId.updateContactInfo("0112223333", "new@acme.lk", "Kandy", "Jane");
        assertEquals("0112223333", updated.getSupplierPhone());
        assertEquals("new@acme.lk", updated.getSupplierEmail());
        assertEquals("Kandy", updated.getSupplierAddress());
        assertEquals("Jane", updated.getContactPerson());

        Supplier deactivated = updated.deactivate();
        assertFalse(deactivated.isActive());
        Supplier activated = deactivated.activate();
        assertTrue(activated.isActive());

        Supplier reconstructed = Supplier.reconstruct(1L, "SUPX", "X Ltd", "077", "x@mail", "Galle", "Max", true, null, null);
        assertEquals("SUPX", reconstructed.getSupplierCode());

        // equals/hashCode based on supplierCode
        Supplier s2 = Supplier.create("SUP1", "ACME Ltd", "077", null, null, null);
        assertEquals(s, s2);
        assertEquals(s.hashCode(), s2.hashCode());
        assertTrue(s.toString().contains("supplierCode"));

        // validation failures for code/name/phone
        assertThrows(IllegalArgumentException.class, () -> Supplier.create(" ", "Name", "077", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Supplier.create("CODE", " ", "077", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Supplier.create("CODE", "Name", " ", null, null, null));
    }
}
