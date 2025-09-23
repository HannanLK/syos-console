package com.syos.shared.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void unitOfMeasure_helpersWork() {
        assertTrue(UnitOfMeasure.KG.isWeightMeasure());
        assertFalse(UnitOfMeasure.KG.isVolumeMeasure());
        assertFalse(UnitOfMeasure.KG.isCountMeasure());

        assertTrue(UnitOfMeasure.L.isVolumeMeasure());
        assertTrue(UnitOfMeasure.ML.isVolumeMeasure());

        assertTrue(UnitOfMeasure.EACH.isCountMeasure());
        assertEquals("Each", UnitOfMeasure.EACH.getDisplayName());
        assertNotNull(UnitOfMeasure.EACH.getDescription());
        assertEquals("Each", UnitOfMeasure.EACH.toString());
    }

    @Test
    void productStatus_helpersWork() {
        assertTrue(ProductStatus.ACTIVE.isAvailableForSale());
        assertTrue(ProductStatus.ACTIVE.canBeReordered());
        assertFalse(ProductStatus.INACTIVE.isAvailableForSale());
        assertEquals("Active", ProductStatus.ACTIVE.getDisplayName());
        assertNotNull(ProductStatus.ACTIVE.getDescription());
        assertEquals("Active", ProductStatus.ACTIVE.toString());
    }

    @Test
    void userRole_fromString_parsesAndRejectsInvalid() {
        assertEquals(UserRole.CUSTOMER, UserRole.fromString("customer"));
        assertEquals(UserRole.EMPLOYEE, UserRole.fromString(" EMPLOYEE "));
        assertEquals(UserRole.ADMIN, UserRole.fromString("AdMiN"));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromString("owner"));
    }
}
