package com.syos.application.strategies.stock;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an allocation from a specific batch.
 */
public final class BatchAllocation {
    private final long batchId;
    private final BigDecimal allocatedQuantity;

    private BatchAllocation(long batchId, BigDecimal allocatedQuantity) {
        this.batchId = batchId;
        this.allocatedQuantity = allocatedQuantity;
    }

    public static BatchAllocation of(long batchId, BigDecimal allocatedQuantity) {
        Objects.requireNonNull(allocatedQuantity, "allocatedQuantity");
        if (allocatedQuantity.signum() <= 0) throw new IllegalArgumentException("allocatedQuantity must be > 0");
        return new BatchAllocation(batchId, allocatedQuantity);
    }

    public long getBatchId() { return batchId; }
    public BigDecimal getAllocatedQuantity() { return allocatedQuantity; }
}
