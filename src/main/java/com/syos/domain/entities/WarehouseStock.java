package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing warehouse stock before transfer to shelves or web inventory.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 2(a): Items stocked according to code, date of purchase, quantity, expiry
 * - Requirement 2(b): FIFO stock movement with expiry date priority
 * - Clean Architecture: Pure domain logic with business rules
 */
public class WarehouseStock {
    private final Long id;
    private final ItemCode itemCode;
    private final Long itemId;
    private final Long batchId;
    private final Quantity quantityReceived;
    private final Quantity quantityAvailable;
    private final LocalDateTime receivedDate;
    private final LocalDateTime expiryDate;
    private final UserID receivedBy;
    private final String location;
    private final boolean isReserved;
    private final UserID reservedBy;
    private final LocalDateTime reservedAt;
    private final LocalDateTime lastUpdated;
    private final UserID lastUpdatedBy;

    private WarehouseStock(Builder builder) {
        this.id = builder.id;
        this.itemCode = Objects.requireNonNull(builder.itemCode, "Item code cannot be null");
        this.itemId = Objects.requireNonNull(builder.itemId, "Item ID cannot be null");
        this.batchId = Objects.requireNonNull(builder.batchId, "Batch ID cannot be null");
        this.quantityReceived = Objects.requireNonNull(builder.quantityReceived, "Quantity received cannot be null");
        this.quantityAvailable = Objects.requireNonNull(builder.quantityAvailable, "Quantity available cannot be null");
        this.receivedDate = Objects.requireNonNull(builder.receivedDate, "Received date cannot be null");
        this.expiryDate = builder.expiryDate;
        this.receivedBy = Objects.requireNonNull(builder.receivedBy, "Received by cannot be null");
        this.location = builder.location != null ? builder.location : "MAIN-WAREHOUSE";
        this.isReserved = builder.isReserved;
        this.reservedBy = builder.reservedBy;
        this.reservedAt = builder.reservedAt;
        this.lastUpdated = builder.lastUpdated != null ? builder.lastUpdated : LocalDateTime.now();
        this.lastUpdatedBy = Objects.requireNonNull(builder.lastUpdatedBy, "Last updated by cannot be null");
        
        validateBusinessRules();
    }

    private void validateBusinessRules() {
        if (quantityReceived.isZeroOrNegative()) {
            throw new IllegalArgumentException("Quantity received must be positive");
        }
        
        if (quantityAvailable.isNegative()) {
            throw new IllegalArgumentException("Quantity available cannot be negative");
        }
        
        if (quantityAvailable.isGreaterThan(quantityReceived)) {
            throw new IllegalArgumentException("Quantity available cannot exceed quantity received");
        }
        
        if (isReserved && (reservedBy == null || reservedAt == null)) {
            throw new IllegalArgumentException("Reserved stock must have reservedBy and reservedAt");
        }
        
        if (!isReserved && (reservedBy != null || reservedAt != null)) {
            throw new IllegalArgumentException("Non-reserved stock cannot have reservation details");
        }
    }

    /**
     * Factory method to create new warehouse stock entry
     */
    public static WarehouseStock createNew(ItemCode itemCode, Long itemId, Long batchId,
                                         Quantity quantityReceived, LocalDateTime expiryDate,
                                         UserID receivedBy, String location) {
        return new Builder()
                .itemCode(itemCode)
                .itemId(itemId)
                .batchId(batchId)
                .quantityReceived(quantityReceived)
                .quantityAvailable(quantityReceived) // Initially all available
                .receivedDate(LocalDateTime.now())
                .expiryDate(expiryDate)
                .receivedBy(receivedBy)
                .location(location)
                .isReserved(false)
                .lastUpdatedBy(receivedBy)
                .build();
    }

    /**
     * Reserve stock for transfer
     */
    public WarehouseStock reserve(Quantity quantity, UserID reservedBy) {
        if (isReserved) {
            throw new IllegalStateException("Stock is already reserved");
        }
        
        if (quantity.isGreaterThan(quantityAvailable)) {
            throw new IllegalArgumentException("Cannot reserve more than available quantity");
        }

        return new Builder(this)
                .isReserved(true)
                .reservedBy(reservedBy)
                .reservedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(reservedBy)
                .build();
    }

    /**
     * Transfer stock (reduce available quantity)
     */
    public WarehouseStock transfer(Quantity quantity, UserID transferredBy) {
        if (quantity.isGreaterThan(quantityAvailable)) {
            throw new IllegalArgumentException("Cannot transfer more than available quantity");
        }

        Quantity newAvailable = quantityAvailable.subtract(quantity);

        return new Builder(this)
                .quantityAvailable(newAvailable)
                .isReserved(false)
                .reservedBy(null)
                .reservedAt(null)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(transferredBy)
                .build();
    }

    /**
     * Cancel reservation
     */
    public WarehouseStock cancelReservation(UserID cancelledBy) {
        if (!isReserved) {
            throw new IllegalStateException("No reservation to cancel");
        }

        return new Builder(this)
                .isReserved(false)
                .reservedBy(null)
                .reservedAt(null)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(cancelledBy)
                .build();
    }

    /**
     * Check if stock is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if stock is expiring soon (within 7 days)
     */
    public boolean isExpiringSoon() {
        return expiryDate != null && 
               LocalDateTime.now().plusDays(7).isAfter(expiryDate) && 
               !isExpired();
    }

    /**
     * Check if stock is available for transfer
     */
    public boolean isAvailableForTransfer() {
        return !quantityAvailable.isZeroOrNegative() && !isReserved && !isExpired();
    }

    // Getters
    public Long getId() { return id; }
    public ItemCode getItemCode() { return itemCode; }
    public Long getItemId() { return itemId; }
    public Long getBatchId() { return batchId; }
    public Quantity getQuantityReceived() { return quantityReceived; }
    public Quantity getQuantityAvailable() { return quantityAvailable; }
    public LocalDateTime getReceivedDate() { return receivedDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public UserID getReceivedBy() { return receivedBy; }
    public String getLocation() { return location; }
    public boolean isReserved() { return isReserved; }
    public UserID getReservedBy() { return reservedBy; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public UserID getLastUpdatedBy() { return lastUpdatedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarehouseStock that = (WarehouseStock) o;
        return Objects.equals(itemCode, that.itemCode) &&
               Objects.equals(batchId, that.batchId) &&
               Objects.equals(receivedDate, that.receivedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode, batchId, receivedDate);
    }

    /**
     * Builder pattern for complex object construction
     */
    public static class Builder {
        private Long id;
        private ItemCode itemCode;
        private Long itemId;
        private Long batchId;
        private Quantity quantityReceived;
        private Quantity quantityAvailable;
        private LocalDateTime receivedDate;
        private LocalDateTime expiryDate;
        private UserID receivedBy;
        private String location;
        private boolean isReserved;
        private UserID reservedBy;
        private LocalDateTime reservedAt;
        private LocalDateTime lastUpdated;
        private UserID lastUpdatedBy;

        public Builder() {}

        public Builder(WarehouseStock existing) {
            this.id = existing.id;
            this.itemCode = existing.itemCode;
            this.itemId = existing.itemId;
            this.batchId = existing.batchId;
            this.quantityReceived = existing.quantityReceived;
            this.quantityAvailable = existing.quantityAvailable;
            this.receivedDate = existing.receivedDate;
            this.expiryDate = existing.expiryDate;
            this.receivedBy = existing.receivedBy;
            this.location = existing.location;
            this.isReserved = existing.isReserved;
            this.reservedBy = existing.reservedBy;
            this.reservedAt = existing.reservedAt;
            this.lastUpdated = existing.lastUpdated;
            this.lastUpdatedBy = existing.lastUpdatedBy;
        }

        public Builder id(Long id) { this.id = id; return this; }
        public Builder itemCode(ItemCode itemCode) { this.itemCode = itemCode; return this; }
        public Builder itemId(Long itemId) { this.itemId = itemId; return this; }
        public Builder batchId(Long batchId) { this.batchId = batchId; return this; }
        public Builder quantityReceived(Quantity quantityReceived) { this.quantityReceived = quantityReceived; return this; }
        public Builder quantityAvailable(Quantity quantityAvailable) { this.quantityAvailable = quantityAvailable; return this; }
        public Builder receivedDate(LocalDateTime receivedDate) { this.receivedDate = receivedDate; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder receivedBy(UserID receivedBy) { this.receivedBy = receivedBy; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder isReserved(boolean isReserved) { this.isReserved = isReserved; return this; }
        public Builder reservedBy(UserID reservedBy) { this.reservedBy = reservedBy; return this; }
        public Builder reservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder lastUpdatedBy(UserID lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; return this; }

        public WarehouseStock build() {
            return new WarehouseStock(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
