package com.syos.shared.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsExtendedTest {

    @Test
    void productStatus_allBranches() {
        assertFalse(ProductStatus.DISCONTINUED.isAvailableForSale());
        assertFalse(ProductStatus.DISCONTINUED.canBeReordered());
        assertEquals("Discontinued", ProductStatus.DISCONTINUED.getDisplayName());
        assertTrue(ProductStatus.DISCONTINUED.getDescription().toLowerCase().contains("permanent"));
        // toString returns display name
        assertEquals("Inactive", ProductStatus.INACTIVE.toString());
    }

    @Test
    void unitOfMeasure_moreBranches() {
        // Count measures include BOX and PACK
        assertTrue(UnitOfMeasure.BOX.isCountMeasure());
        assertTrue(UnitOfMeasure.PACK.isCountMeasure());
        assertFalse(UnitOfMeasure.BOX.isWeightMeasure());
        assertFalse(UnitOfMeasure.PACK.isVolumeMeasure());

        // Weight measures include G and KG
        assertTrue(UnitOfMeasure.G.isWeightMeasure());
        assertTrue(UnitOfMeasure.KG.isWeightMeasure());
        assertFalse(UnitOfMeasure.G.isVolumeMeasure());

        // Volume only for L/ML
        assertTrue(UnitOfMeasure.L.isVolumeMeasure());
        assertTrue(UnitOfMeasure.ML.isVolumeMeasure());
        assertFalse(UnitOfMeasure.EACH.isVolumeMeasure());

        // toString returns display name
        assertEquals("mL", UnitOfMeasure.ML.toString());
    }
}
