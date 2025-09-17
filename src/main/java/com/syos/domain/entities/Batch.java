package com.syos.domain.entities;

import com.syos.domain.valueobjects.Quantity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Batch entity used to track quantities and expiry at batch-level.
 */
public class Batch {
    private final Long id; // nullable when new
    private final String batchNumber;
    private final Long itemId;
    private final Quantity quantityReceived;
    private final Quantity quantityAvailable;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate; // nullable
    private final LocalDate receivedDate;

    private Batch(Long id, String batchNumber, Long itemId, Quantity quantityReceived, Quantity quantityAvailable,
                  LocalDate manufactureDate, LocalDate expiryDate, LocalDate receivedDate) {
        this.id = id;
        this.batchNumber = Objects.requireNonNull(batchNumber);
        this.itemId = Objects.requireNonNull(itemId);
        this.quantityReceived = Objects.requireNonNull(quantityReceived);
        this.quantityAvailable = Objects.requireNonNull(quantityAvailable);
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.receivedDate = Objects.requireNonNull(receivedDate);
    }

    public static Batch receiveNew(Long itemId, String batchNumber, Quantity quantity, LocalDate mfg, LocalDate expiry, LocalDate receivedDate) {
        if (quantity == null || quantity.toBigDecimal().signum() <= 0) throw new IllegalArgumentException("Batch quantity must be > 0");
        if (batchNumber == null || batchNumber.isBlank()) throw new IllegalArgumentException("batchNumber required");
        if (mfg != null && expiry != null && !expiry.isAfter(mfg)) throw new IllegalArgumentException("expiry must be after manufacture date");
        LocalDate rd = receivedDate != null ? receivedDate : LocalDate.now();
        return new Batch(null, batchNumber.trim(), itemId, quantity, quantity, mfg, expiry, rd);
    }

    public Batch withId(Long id) {
        return new Batch(id, batchNumber, itemId, quantityReceived, quantityAvailable, manufactureDate, expiryDate, receivedDate);
    }

    public Long getId() { return id; }
    public String getBatchNumber() { return batchNumber; }
    public Long getItemId() { return itemId; }
    public Quantity getQuantityReceived() { return quantityReceived; }
    public Quantity getQuantityAvailable() { return quantityAvailable; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDate getReceivedDate() { return receivedDate; }
}
