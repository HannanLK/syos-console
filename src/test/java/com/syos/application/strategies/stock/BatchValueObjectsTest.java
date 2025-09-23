package com.syos.application.strategies.stock;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BatchValueObjectsTest {

    @Test
    void batchAllocation_factoryAndGetters_work() {
        BatchAllocation alloc = BatchAllocation.of(42L, new BigDecimal("3.5"));
        assertEquals(42L, alloc.getBatchId());
        assertEquals(new BigDecimal("3.5"), alloc.getAllocatedQuantity());
    }

    @Test
    void batchAllocation_rejectsNullOrNonPositiveQty() {
        assertThrows(NullPointerException.class, () -> BatchAllocation.of(1L, null));
        assertThrows(IllegalArgumentException.class, () -> BatchAllocation.of(1L, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> BatchAllocation.of(1L, new BigDecimal("-1")));
    }

    @Test
    void batchInfo_factoryAndGetters_work() {
        LocalDate recv = LocalDate.of(2025, 1, 1);
        LocalDate exp = LocalDate.of(2025, 12, 31);
        BatchInfo info = BatchInfo.of(7L, new BigDecimal("10"), recv, exp);
        assertEquals(7L, info.getBatchId());
        assertEquals(new BigDecimal("10"), info.getAvailableQuantity());
        assertEquals(recv, info.getReceivedDate());
        assertEquals(exp, info.getExpiryDate());
    }

    @Test
    void batchInfo_validatesInputs() {
        LocalDate recv = LocalDate.now();
        assertThrows(NullPointerException.class, () -> BatchInfo.of(1L, null, recv, null));
        assertThrows(NullPointerException.class, () -> BatchInfo.of(1L, BigDecimal.ONE, null, null));
        assertThrows(IllegalArgumentException.class, () -> BatchInfo.of(1L, new BigDecimal("-0.01"), recv, null));
        // zero is allowed for availableQuantity
        BatchInfo okZero = BatchInfo.of(2L, BigDecimal.ZERO, recv, null);
        assertEquals(BigDecimal.ZERO, okZero.getAvailableQuantity());
    }
}
