package com.syos.domain.entities;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a batch of products received from supplier.
 * Essential for FIFO inventory management and expiry tracking.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 2(a): Batch tracking with purchase date, quantity, expiry
 * - Requirement 2(b): FIFO stock selection with expiry priority
 * - Clean Architecture: Pure domain logic with business rules
 */
public class Batch {
    private final Long id;
    private final Long itemId;
    private final String batchNumber;
    private final Quantity quantityReceived;
    private final Quantity quantityAvailable;
    private final LocalDate manufactureDate;
    private final LocalDateTime expiryDate;
    private final LocalDate receivedDate;
    private final UserID receivedBy;
    private final Money costPerUnit;
    private final String supplierBatchNumber;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Batch(Builder builder) {
        this.id = builder.id;
        this.itemId = Objects.requireNonNull(builder.itemId, "Item ID cannot be null");
        this.batchNumber = Objects.requireNonNull(builder.batchNumber, "Batch number cannot be null");
        this.quantityReceived = Objects.requireNonNull(builder.quantityReceived, "Quantity received cannot be null");
        this.quantityAvailable = Objects.requireNonNull(builder.quantityAvailable, "Quantity available cannot be null");
        this.manufactureDate = builder.manufactureDate;
        this.expiryDate = builder.expiryDate;
        this.receivedDate = builder.receivedDate != null ? builder.receivedDate : LocalDate.now();
        this.receivedBy = Objects.requireNonNull(builder.receivedBy, "Received by cannot be null");
        this.costPerUnit = builder.costPerUnit;
        this.supplierBatchNumber = builder.supplierBatchNumber;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();
        
        validateBusinessRules();
    }

    private void validateBusinessRules() {
        if (batchNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Batch number cannot be empty");
        }
        
        if (quantityReceived.isZeroOrNegative()) {
            throw new IllegalArgumentException("Quantity received must be positive");
        }
        
        if (quantityAvailable.isNegative()) {
            throw new IllegalArgumentException("Quantity available cannot be negative");
        }
        
        if (quantityAvailable.isGreaterThan(quantityReceived)) {
            throw new IllegalArgumentException("Quantity available cannot exceed quantity received");
        }
        
        if (manufactureDate != null && expiryDate != null && 
            !expiryDate.toLocalDate().isAfter(manufactureDate)) {
            throw new IllegalArgumentException("Expiry date must be after manufacture date");
        }
        
        if (costPerUnit != null && costPerUnit.isZeroOrNegative()) {
            throw new IllegalArgumentException("Cost per unit must be positive");
        }
    }

    /**
     * Factory method to create new batch
     */
    public static Batch createNew(Long itemId, String batchNumber, Quantity quantityReceived,
                                LocalDate manufactureDate, LocalDateTime expiryDate,
                                UserID receivedBy, Money costPerUnit) {
        return new Builder()
                .itemId(itemId)
                .batchNumber(batchNumber)
                .quantityReceived(quantityReceived)
                .quantityAvailable(quantityReceived) // Initially all available
                .manufactureDate(manufactureDate)
                .expiryDate(expiryDate)
                .receivedBy(receivedBy)
                .costPerUnit(costPerUnit)
                .build();
    }

    /**
     * Reduce available quantity (when stock is transferred)
     */
    public Batch reduceQuantity(Quantity quantity) {
        if (quantity.isGreaterThan(quantityAvailable)) {
            throw new IllegalArgumentException("Cannot reduce more than available quantity");
        }

        Quantity newAvailable = quantityAvailable.subtract(quantity);

        return new Builder(this)
                .quantityAvailable(newAvailable)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if batch is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if batch is expiring soon (within 7 days)
     */
    public boolean isExpiringSoon() {
        return expiryDate != null && 
               LocalDateTime.now().plusDays(7).isAfter(expiryDate) && 
               !isExpired();
    }

    /**
     * Check if batch has available stock
     */
    public boolean hasAvailableStock() {
        return !quantityAvailable.isZeroOrNegative();
    }

    /**
     * Get days until expiry (null if no expiry date)
     */
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) return null;
        
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate expiry = expiryDate.toLocalDate();
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(now, expiry);
        return days;
    }

    /**
     * Get age of batch in days
     */
    public long getAgeInDays() {
        return receivedDate.until(LocalDate.now()).getDays();
    }

    /**
     * Check if this batch should be selected before another batch (FIFO with expiry priority)
     */
    public boolean shouldBeSelectedBefore(Batch other) {
        // If one has expiry and other doesn't, prioritize the one with expiry
        if (this.expiryDate != null && other.expiryDate == null) {
            return true;
        }
        if (this.expiryDate == null && other.expiryDate != null) {
            return false;
        }
        
        // If both have expiry dates, prioritize the one expiring sooner
        if (this.expiryDate != null && other.expiryDate != null) {
            return this.expiryDate.isBefore(other.expiryDate);
        }
        
        // If neither has expiry, use FIFO (older batch first)
        return this.receivedDate.isBefore(other.receivedDate);
    }

    // Getters
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public String getBatchNumber() { return batchNumber; }
    public Quantity getQuantityReceived() { return quantityReceived; }
    public Quantity getQuantityAvailable() { return quantityAvailable; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public UserID getReceivedBy() { return receivedBy; }
    public Money getCostPerUnit() { return costPerUnit; }
    public String getSupplierBatchNumber() { return supplierBatchNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(itemId, batch.itemId) &&
               Objects.equals(batchNumber, batch.batchNumber) &&
               Objects.equals(receivedDate, batch.receivedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, batchNumber, receivedDate);
    }

    /**
     * Builder pattern for complex object construction
     */
    public static class Builder {
        private Long id;
        private Long itemId;
        private String batchNumber;
        private Quantity quantityReceived;
        private Quantity quantityAvailable;
        private LocalDate manufactureDate;
        private LocalDateTime expiryDate;
        private LocalDate receivedDate;
        private UserID receivedBy;
        private Money costPerUnit;
        private String supplierBatchNumber;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder() {}

        public Builder(Batch existing) {
            this.id = existing.id;
            this.itemId = existing.itemId;
            this.batchNumber = existing.batchNumber;
            this.quantityReceived = existing.quantityReceived;
            this.quantityAvailable = existing.quantityAvailable;
            this.manufactureDate = existing.manufactureDate;
            this.expiryDate = existing.expiryDate;
            this.receivedDate = existing.receivedDate;
            this.receivedBy = existing.receivedBy;
            this.costPerUnit = existing.costPerUnit;
            this.supplierBatchNumber = existing.supplierBatchNumber;
            this.createdAt = existing.createdAt;
            this.updatedAt = existing.updatedAt;
        }

        public Builder id(Long id) { this.id = id; return this; }
        public Builder itemId(Long itemId) { this.itemId = itemId; return this; }
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder quantityReceived(Quantity quantityReceived) { this.quantityReceived = quantityReceived; return this; }
        public Builder quantityAvailable(Quantity quantityAvailable) { this.quantityAvailable = quantityAvailable; return this; }
        public Builder manufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder receivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; return this; }
        public Builder receivedBy(UserID receivedBy) { this.receivedBy = receivedBy; return this; }
        public Builder costPerUnit(Money costPerUnit) { this.costPerUnit = costPerUnit; return this; }
        public Builder supplierBatchNumber(String supplierBatchNumber) { this.supplierBatchNumber = supplierBatchNumber; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Batch build() {
            return new Batch(this);
        }
    }
}
