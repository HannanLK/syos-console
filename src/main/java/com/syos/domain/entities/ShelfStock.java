package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing stock placed on store shelves for customer purchase.
 * Manages shelf-specific inventory separate from warehouse and web inventory.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 2(b): Items moved to shelf from store with FIFO/expiry priority
 * - Requirement 4(a): Display item name (not code) for customer-facing operations
 * - Clean Architecture: Pure domain logic with business rules
 */
public class ShelfStock {
    private final Long id;
    private final ItemCode itemCode;
    private final Long itemId;
    private final Long batchId;
    private final String shelfCode;
    private final Quantity quantityOnShelf;
    private final LocalDateTime placedOnShelfDate;
    private final LocalDateTime expiryDate;
    private final UserID placedBy;
    private final Money unitPrice;
    private final boolean isDisplayed;
    private final String displayPosition;
    private final Quantity minimumStockLevel;
    private final Quantity maximumStockLevel;
    private final LocalDateTime lastUpdated;
    private final UserID lastUpdatedBy;

    private ShelfStock(Builder builder) {
        this.id = builder.id;
        this.itemCode = Objects.requireNonNull(builder.itemCode, "Item code cannot be null");
        this.itemId = Objects.requireNonNull(builder.itemId, "Item ID cannot be null");
        this.batchId = Objects.requireNonNull(builder.batchId, "Batch ID cannot be null");
        this.shelfCode = Objects.requireNonNull(builder.shelfCode, "Shelf code cannot be null");
        this.quantityOnShelf = Objects.requireNonNull(builder.quantityOnShelf, "Quantity on shelf cannot be null");
        this.placedOnShelfDate = builder.placedOnShelfDate != null ? builder.placedOnShelfDate : LocalDateTime.now();
        this.expiryDate = builder.expiryDate;
        this.placedBy = Objects.requireNonNull(builder.placedBy, "Placed by cannot be null");
        this.unitPrice = Objects.requireNonNull(builder.unitPrice, "Unit price cannot be null");
        this.isDisplayed = builder.isDisplayed;
        this.displayPosition = builder.displayPosition;
        this.minimumStockLevel = builder.minimumStockLevel;
        this.maximumStockLevel = builder.maximumStockLevel;
        this.lastUpdated = builder.lastUpdated != null ? builder.lastUpdated : LocalDateTime.now();
        this.lastUpdatedBy = Objects.requireNonNull(builder.lastUpdatedBy, "Last updated by cannot be null");
        
        validateBusinessRules();
    }

    private void validateBusinessRules() {
        if (shelfCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Shelf code cannot be empty");
        }
        
        if (quantityOnShelf.isNegative()) {
            throw new IllegalArgumentException("Quantity on shelf cannot be negative");
        }
        
        if (unitPrice.isZeroOrNegative()) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        
        if (minimumStockLevel != null && maximumStockLevel != null && 
            minimumStockLevel.isGreaterThan(maximumStockLevel)) {
            throw new IllegalArgumentException("Minimum stock level cannot exceed maximum stock level");
        }
    }

    /**
     * Factory method to create new shelf stock
     */
    public static ShelfStock createNew(ItemCode itemCode, Long itemId, Long batchId,
                                     String shelfCode, Quantity quantity, LocalDateTime expiryDate,
                                     UserID placedBy, Money unitPrice) {
        return new Builder()
                .itemCode(itemCode)
                .itemId(itemId)
                .batchId(batchId)
                .shelfCode(shelfCode)
                .quantityOnShelf(quantity)
                .expiryDate(expiryDate)
                .placedBy(placedBy)
                .unitPrice(unitPrice)
                .isDisplayed(true)
                .lastUpdatedBy(placedBy)
                .build();
    }

    /**
     * Sell stock (reduce quantity)
     */
    public ShelfStock sellStock(Quantity soldQuantity, UserID soldBy) {
        if (soldQuantity.isGreaterThan(quantityOnShelf)) {
            throw new IllegalArgumentException("Cannot sell more than available on shelf");
        }

        Quantity newQuantity = quantityOnShelf.subtract(soldQuantity);

        return new Builder(this)
                .quantityOnShelf(newQuantity)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(soldBy)
                .build();
    }

    /**
     * Restock shelf (add quantity)
     */
    public ShelfStock restockShelf(Quantity additionalQuantity, UserID restockedBy) {
        if (additionalQuantity.isZeroOrNegative()) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }

        Quantity newQuantity = quantityOnShelf.add(additionalQuantity);

        // Check maximum stock level if defined
        if (maximumStockLevel != null && newQuantity.isGreaterThan(maximumStockLevel)) {
            throw new IllegalArgumentException("Restocking would exceed maximum stock level");
        }

        return new Builder(this)
                .quantityOnShelf(newQuantity)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(restockedBy)
                .build();
    }

    /**
     * Update unit price
     */
    public ShelfStock updatePrice(Money newPrice, UserID updatedBy) {
        if (newPrice.isZeroOrNegative()) {
            throw new IllegalArgumentException("Price must be positive");
        }

        return new Builder(this)
                .unitPrice(newPrice)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Change display status
     */
    public ShelfStock setDisplayStatus(boolean displayed, UserID updatedBy) {
        return new Builder(this)
                .isDisplayed(displayed)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Update display position
     */
    public ShelfStock updateDisplayPosition(String newPosition, UserID updatedBy) {
        return new Builder(this)
                .displayPosition(newPosition)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Set stock level thresholds
     */
    public ShelfStock setStockLevels(Quantity minimum, Quantity maximum, UserID updatedBy) {
        if (minimum != null && maximum != null && minimum.isGreaterThan(maximum)) {
            throw new IllegalArgumentException("Minimum cannot exceed maximum");
        }

        return new Builder(this)
                .minimumStockLevel(minimum)
                .maximumStockLevel(maximum)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Check if stock is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if stock is expiring soon (within 3 days for shelf stock)
     */
    public boolean isExpiringSoon() {
        return expiryDate != null && 
               LocalDateTime.now().plusDays(3).isAfter(expiryDate) && 
               !isExpired();
    }

    /**
     * Check if stock is available for sale
     */
    public boolean isAvailableForSale() {
        return !quantityOnShelf.isZeroOrNegative() && isDisplayed && !isExpired();
    }

    /**
     * Check if stock needs restocking
     */
    public boolean needsRestocking() {
        return minimumStockLevel != null && quantityOnShelf.isLessThan(minimumStockLevel);
    }

    /**
     * Check if shelf is overstocked
     */
    public boolean isOverstocked() {
        return maximumStockLevel != null && quantityOnShelf.isGreaterThan(maximumStockLevel);
    }

    /**
     * Calculate total value of stock on shelf
     */
    public Money getTotalValue() {
        return unitPrice.multiply(quantityOnShelf.getValue());
    }

    // Getters
    public Long getId() { return id; }
    public ItemCode getItemCode() { return itemCode; }
    public Long getItemId() { return itemId; }
    public Long getBatchId() { return batchId; }
    public String getShelfCode() { return shelfCode; }
    public Quantity getQuantityOnShelf() { return quantityOnShelf; }
    public LocalDateTime getPlacedOnShelfDate() { return placedOnShelfDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public UserID getPlacedBy() { return placedBy; }
    public Money getUnitPrice() { return unitPrice; }
    public boolean isDisplayed() { return isDisplayed; }
    public String getDisplayPosition() { return displayPosition; }
    public Quantity getMinimumStockLevel() { return minimumStockLevel; }
    public Quantity getMaximumStockLevel() { return maximumStockLevel; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public UserID getLastUpdatedBy() { return lastUpdatedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShelfStock that = (ShelfStock) o;
        return Objects.equals(itemCode, that.itemCode) &&
               Objects.equals(batchId, that.batchId) &&
               Objects.equals(shelfCode, that.shelfCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode, batchId, shelfCode);
    }

    /**
     * Builder pattern for complex object construction
     */
    public static class Builder {
        private Long id;
        private ItemCode itemCode;
        private Long itemId;
        private Long batchId;
        private String shelfCode;
        private Quantity quantityOnShelf;
        private LocalDateTime placedOnShelfDate;
        private LocalDateTime expiryDate;
        private UserID placedBy;
        private Money unitPrice;
        private boolean isDisplayed = true;
        private String displayPosition;
        private Quantity minimumStockLevel;
        private Quantity maximumStockLevel;
        private LocalDateTime lastUpdated;
        private UserID lastUpdatedBy;

        public Builder() {}

        public Builder(ShelfStock existing) {
            this.id = existing.id;
            this.itemCode = existing.itemCode;
            this.itemId = existing.itemId;
            this.batchId = existing.batchId;
            this.shelfCode = existing.shelfCode;
            this.quantityOnShelf = existing.quantityOnShelf;
            this.placedOnShelfDate = existing.placedOnShelfDate;
            this.expiryDate = existing.expiryDate;
            this.placedBy = existing.placedBy;
            this.unitPrice = existing.unitPrice;
            this.isDisplayed = existing.isDisplayed;
            this.displayPosition = existing.displayPosition;
            this.minimumStockLevel = existing.minimumStockLevel;
            this.maximumStockLevel = existing.maximumStockLevel;
            this.lastUpdated = existing.lastUpdated;
            this.lastUpdatedBy = existing.lastUpdatedBy;
        }

        public Builder id(Long id) { this.id = id; return this; }
        public Builder itemCode(ItemCode itemCode) { this.itemCode = itemCode; return this; }
        public Builder itemId(Long itemId) { this.itemId = itemId; return this; }
        public Builder batchId(Long batchId) { this.batchId = batchId; return this; }
        public Builder shelfCode(String shelfCode) { this.shelfCode = shelfCode; return this; }
        public Builder quantityOnShelf(Quantity quantityOnShelf) { this.quantityOnShelf = quantityOnShelf; return this; }
        public Builder placedOnShelfDate(LocalDateTime placedOnShelfDate) { this.placedOnShelfDate = placedOnShelfDate; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder placedBy(UserID placedBy) { this.placedBy = placedBy; return this; }
        public Builder unitPrice(Money unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder isDisplayed(boolean isDisplayed) { this.isDisplayed = isDisplayed; return this; }
        public Builder displayPosition(String displayPosition) { this.displayPosition = displayPosition; return this; }
        public Builder minimumStockLevel(Quantity minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; return this; }
        public Builder maximumStockLevel(Quantity maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder lastUpdatedBy(UserID lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; return this; }

        public ShelfStock build() {
            return new ShelfStock(this);
        }
    }
}
