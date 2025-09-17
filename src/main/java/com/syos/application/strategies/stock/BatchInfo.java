package com.syos.application.strategies.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Lightweight view of a batch for stock selection logic.
 * This is intentionally decoupled from persistence entities to keep the algorithm pure.
 */
public final class BatchInfo {
    private final long batchId;
    private final BigDecimal availableQuantity;
    private final LocalDate receivedDate; // date the batch entered the current pool (e.g., warehouse)
    private final LocalDate expiryDate;   // nullable for non-perishables

    private BatchInfo(long batchId, BigDecimal availableQuantity, LocalDate receivedDate, LocalDate expiryDate) {
        this.batchId = batchId;
        this.availableQuantity = availableQuantity;
        this.receivedDate = receivedDate;
        this.expiryDate = expiryDate;
    }

    public static BatchInfo of(long batchId, BigDecimal availableQuantity, LocalDate receivedDate, LocalDate expiryDate) {
        Objects.requireNonNull(availableQuantity, "availableQuantity");
        Objects.requireNonNull(receivedDate, "receivedDate");
        if (availableQuantity.signum() < 0) throw new IllegalArgumentException("availableQuantity must be >= 0");
        return new BatchInfo(batchId, availableQuantity, receivedDate, expiryDate);
    }

    public long getBatchId() { return batchId; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
}
