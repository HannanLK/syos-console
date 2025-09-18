package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing web inventory separate from physical shelf stock.
 * Manages online inventory for web transactions with specific web features.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 3: Separate web inventory from shelf stock
 * - Requirement 3(a): Web inventory maintained separately
 * - Requirement 3(b): Web and over-counter transactions identified separately
 * - Clean Architecture: Pure domain logic with business rules
 */
public class WebInventory {
    private final Long id;
    private final ItemCode itemCode;
    private final Long itemId;
    private final Long batchId;
    private final Quantity quantityAvailable;
    private final LocalDateTime addedToWebDate;
    private final LocalDateTime expiryDate;
    private final UserID addedBy;
    private final Money webPrice;
    private final boolean isPublished;
    private final boolean isFeatured;
    private final int stockLevel; // 0-100 indicator for web display
    private final String webDescription;
    private final String seoKeywords;
    private final LocalDateTime lastUpdated;
    private final UserID lastUpdatedBy;

    private WebInventory(Builder builder) {
        this.id = builder.id;
        this.itemCode = Objects.requireNonNull(builder.itemCode, "Item code cannot be null");
        this.itemId = Objects.requireNonNull(builder.itemId, "Item ID cannot be null");
        this.batchId = Objects.requireNonNull(builder.batchId, "Batch ID cannot be null");
        this.quantityAvailable = Objects.requireNonNull(builder.quantityAvailable, "Quantity available cannot be null");
        this.addedToWebDate = builder.addedToWebDate != null ? builder.addedToWebDate : LocalDateTime.now();
        this.expiryDate = builder.expiryDate;
        this.addedBy = Objects.requireNonNull(builder.addedBy, "Added by cannot be null");
        this.webPrice = Objects.requireNonNull(builder.webPrice, "Web price cannot be null");
        this.isPublished = builder.isPublished;
        this.isFeatured = builder.isFeatured;
        this.stockLevel = builder.stockLevel;
        this.webDescription = builder.webDescription;
        this.seoKeywords = builder.seoKeywords;
        this.lastUpdated = builder.lastUpdated != null ? builder.lastUpdated : LocalDateTime.now();
        this.lastUpdatedBy = Objects.requireNonNull(builder.lastUpdatedBy, "Last updated by cannot be null");
        
        validateBusinessRules();
    }

    private void validateBusinessRules() {
        if (quantityAvailable.isNegative()) {
            throw new IllegalArgumentException("Quantity available cannot be negative");
        }
        
        if (webPrice.isZeroOrNegative()) {
            throw new IllegalArgumentException("Web price must be positive");
        }
        
        if (stockLevel < 0 || stockLevel > 100) {
            throw new IllegalArgumentException("Stock level must be between 0 and 100");
        }
    }

    /**
     * Factory method to create new web inventory entry
     */
    public static WebInventory createNew(ItemCode itemCode, Long itemId, Long batchId,
                                       Quantity quantity, LocalDateTime expiryDate,
                                       UserID addedBy, Money webPrice) {
        return new Builder()
                .itemCode(itemCode)
                .itemId(itemId)
                .batchId(batchId)
                .quantityAvailable(quantity)
                .expiryDate(expiryDate)
                .addedBy(addedBy)
                .webPrice(webPrice)
                .isPublished(true)
                .isFeatured(false)
                .stockLevel(calculateInitialStockLevel(quantity))
                .lastUpdatedBy(addedBy)
                .build();
    }

    /**
     * Sell web inventory (reduce quantity)
     */
    public WebInventory sellStock(Quantity soldQuantity, UserID soldBy) {
        if (soldQuantity.isGreaterThan(quantityAvailable)) {
            throw new IllegalArgumentException("Cannot sell more than available in web inventory");
        }

        Quantity newQuantity = quantityAvailable.subtract(soldQuantity);
        int newStockLevel = calculateStockLevel(newQuantity);

        return new Builder(this)
                .quantityAvailable(newQuantity)
                .stockLevel(newStockLevel)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(soldBy)
                .build();
    }

    /**
     * Restock web inventory
     */
    public WebInventory restockWeb(Quantity additionalQuantity, UserID restockedBy) {
        if (additionalQuantity.isZeroOrNegative()) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }

        Quantity newQuantity = quantityAvailable.add(additionalQuantity);
        int newStockLevel = calculateStockLevel(newQuantity);

        return new Builder(this)
                .quantityAvailable(newQuantity)
                .stockLevel(newStockLevel)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(restockedBy)
                .build();
    }

    /**
     * Update web price
     */
    public WebInventory updateWebPrice(Money newPrice, UserID updatedBy) {
        if (newPrice.isZeroOrNegative()) {
            throw new IllegalArgumentException("Web price must be positive");
        }

        return new Builder(this)
                .webPrice(newPrice)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Publish/unpublish item on web
     */
    public WebInventory setPublishStatus(boolean published, UserID updatedBy) {
        return new Builder(this)
                .isPublished(published)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Set featured status
     */
    public WebInventory setFeaturedStatus(boolean featured, UserID updatedBy) {
        return new Builder(this)
                .isFeatured(featured)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Update web description and SEO
     */
    public WebInventory updateWebContent(String description, String keywords, UserID updatedBy) {
        return new Builder(this)
                .webDescription(description)
                .seoKeywords(keywords)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(updatedBy)
                .build();
    }

    /**
     * Check if item is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if item is expiring soon (within 5 days for web inventory)
     */
    public boolean isExpiringSoon() {
        return expiryDate != null && 
               LocalDateTime.now().plusDays(5).isAfter(expiryDate) && 
               !isExpired();
    }

    /**
     * Check if item is available for web purchase
     */
    public boolean isAvailableForPurchase() {
        return !quantityAvailable.isZeroOrNegative() && isPublished && !isExpired();
    }

    /**
     * Check if web inventory is low
     */
    public boolean isLowStock() {
        return stockLevel <= 20; // 20% or below
    }

    /**
     * Calculate total value of web inventory
     */
    public Money getTotalValue() {
        return webPrice.multiply(quantityAvailable.getValue());
    }

    // Helper methods
    private static int calculateInitialStockLevel(Quantity quantity) {
        // Simple calculation - clamp quantity value to 0..100 range for display
        java.math.BigDecimal q = quantity.getValue();
        if (q.compareTo(java.math.BigDecimal.valueOf(100)) >= 0) return 100;
        if (q.compareTo(java.math.BigDecimal.ZERO) <= 0) return 0;
        return q.setScale(0, java.math.RoundingMode.DOWN).intValue();
    }

    private int calculateStockLevel(Quantity quantity) {
        // Maintain relative stock level based on changes
        java.math.BigDecimal prev = this.quantityAvailable.getValue();
        if (prev.compareTo(java.math.BigDecimal.ONE) < 0) {
            prev = java.math.BigDecimal.ONE; // avoid division by zero
        }
        java.math.BigDecimal ratio = quantity.getValue().divide(prev, 4, java.math.RoundingMode.HALF_UP);
        int newLevel = (int) Math.round(this.stockLevel * ratio.doubleValue());
        return Math.max(0, Math.min(100, newLevel));
    }

    // Getters
    public Long getId() { return id; }
    public ItemCode getItemCode() { return itemCode; }
    public Long getItemId() { return itemId; }
    public Long getBatchId() { return batchId; }
    public Quantity getQuantityAvailable() { return quantityAvailable; }
    public LocalDateTime getAddedToWebDate() { return addedToWebDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public UserID getAddedBy() { return addedBy; }
    public Money getWebPrice() { return webPrice; }
    public boolean isPublished() { return isPublished; }
    public boolean isFeatured() { return isFeatured; }
    public int getStockLevel() { return stockLevel; }
    public String getWebDescription() { return webDescription; }
    public String getSeoKeywords() { return seoKeywords; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public UserID getLastUpdatedBy() { return lastUpdatedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebInventory that = (WebInventory) o;
        return Objects.equals(itemCode, that.itemCode) &&
               Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode, batchId);
    }

    /**
     * Builder pattern for complex object construction
     */
    public static class Builder {
        private Long id;
        private ItemCode itemCode;
        private Long itemId;
        private Long batchId;
        private Quantity quantityAvailable;
        private LocalDateTime addedToWebDate;
        private LocalDateTime expiryDate;
        private UserID addedBy;
        private Money webPrice;
        private boolean isPublished = true;
        private boolean isFeatured = false;
        private int stockLevel = 50;
        private String webDescription;
        private String seoKeywords;
        private LocalDateTime lastUpdated;
        private UserID lastUpdatedBy;

        public Builder() {}

        public Builder(WebInventory existing) {
            this.id = existing.id;
            this.itemCode = existing.itemCode;
            this.itemId = existing.itemId;
            this.batchId = existing.batchId;
            this.quantityAvailable = existing.quantityAvailable;
            this.addedToWebDate = existing.addedToWebDate;
            this.expiryDate = existing.expiryDate;
            this.addedBy = existing.addedBy;
            this.webPrice = existing.webPrice;
            this.isPublished = existing.isPublished;
            this.isFeatured = existing.isFeatured;
            this.stockLevel = existing.stockLevel;
            this.webDescription = existing.webDescription;
            this.seoKeywords = existing.seoKeywords;
            this.lastUpdated = existing.lastUpdated;
            this.lastUpdatedBy = existing.lastUpdatedBy;
        }

        public Builder id(Long id) { this.id = id; return this; }
        public Builder itemCode(ItemCode itemCode) { this.itemCode = itemCode; return this; }
        public Builder itemId(Long itemId) { this.itemId = itemId; return this; }
        public Builder batchId(Long batchId) { this.batchId = batchId; return this; }
        public Builder quantityAvailable(Quantity quantityAvailable) { this.quantityAvailable = quantityAvailable; return this; }
        public Builder addedToWebDate(LocalDateTime addedToWebDate) { this.addedToWebDate = addedToWebDate; return this; }
        public Builder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder addedBy(UserID addedBy) { this.addedBy = addedBy; return this; }
        public Builder webPrice(Money webPrice) { this.webPrice = webPrice; return this; }
        public Builder isPublished(boolean isPublished) { this.isPublished = isPublished; return this; }
        public Builder isFeatured(boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder stockLevel(int stockLevel) { this.stockLevel = stockLevel; return this; }
        public Builder webDescription(String webDescription) { this.webDescription = webDescription; return this; }
        public Builder seoKeywords(String seoKeywords) { this.seoKeywords = seoKeywords; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder lastUpdatedBy(UserID lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; return this; }

        public WebInventory build() {
            return new WebInventory(this);
        }
    }
}
