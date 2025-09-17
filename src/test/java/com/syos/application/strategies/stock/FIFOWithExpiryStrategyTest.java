package com.syos.application.strategies.stock;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FIFOWithExpiryStrategyTest {

    @Test
    void fifo_whenNoExpiryOverride() {
        FIFOWithExpiryStrategy strategy = new FIFOWithExpiryStrategy();
        List<BatchInfo> batches = List.of(
                BatchInfo.of(1, new BigDecimal("5"), LocalDate.of(2025,1,1), LocalDate.of(2025,12,31)),
                BatchInfo.of(2, new BigDecimal("10"), LocalDate.of(2025,2,1), LocalDate.of(2025,12,31))
        );
        List<BatchAllocation> alloc = strategy.selectBatchesForDispatch(batches, new BigDecimal("8"));
        assertEquals(2, alloc.size());
        assertEquals(1, alloc.get(0).getBatchId());
        assertEquals(new BigDecimal("5"), alloc.get(0).getAllocatedQuantity());
        assertEquals(2, alloc.get(1).getBatchId());
        assertEquals(new BigDecimal("3"), alloc.get(1).getAllocatedQuantity());
    }

    @Test
    void expiryOverride_whenNewerExpiresSooner() {
        FIFOWithExpiryStrategy strategy = new FIFOWithExpiryStrategy();
        // Batch 1 older received but later expiry; Batch 2 newer but earlier expiry
        List<BatchInfo> batches = List.of(
                BatchInfo.of(1, new BigDecimal("10"), LocalDate.of(2025,1,1), LocalDate.of(2025,3,31)),
                BatchInfo.of(2, new BigDecimal("10"), LocalDate.of(2025,2,1), LocalDate.of(2025,2,28))
        );
        List<BatchAllocation> alloc = strategy.selectBatchesForDispatch(batches, new BigDecimal("6"));
        assertEquals(1, alloc.size());
        assertEquals(2, alloc.get(0).getBatchId(), "Newer batch with earlier expiry should be dispatched first");
        assertEquals(new BigDecimal("6"), alloc.get(0).getAllocatedQuantity());
    }

    @Test
    void nonPerishableOldest_andPerishableNewerSooner() {
        FIFOWithExpiryStrategy strategy = new FIFOWithExpiryStrategy();
        List<BatchInfo> batches = List.of(
                BatchInfo.of(1, new BigDecimal("5"), LocalDate.of(2025,1,1), null), // non-perishable oldest
                BatchInfo.of(2, new BigDecimal("10"), LocalDate.of(2025,2,1), LocalDate.of(2025,2,15))
        );
        List<BatchAllocation> alloc = strategy.selectBatchesForDispatch(batches, new BigDecimal("7"));
        assertEquals(1, alloc.size());
        assertEquals(2, alloc.get(0).getBatchId(), "Perishable sooner should be prioritized over non-perishable oldest");
        assertEquals(new BigDecimal("7"), alloc.get(0).getAllocatedQuantity());
    }
}
