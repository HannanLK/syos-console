package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for shelf_stock table
 */
@Entity
@Table(name = "shelf_stock")
public class ShelfStockEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "batch_id", nullable = false)
    private Long batchId;
    
    @Column(name = "shelf_code", nullable = false, length = 20)
    private String shelfCode;
    
    @Column(name = "quantity_on_shelf", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantityOnShelf;
    
    @Column(name = "placed_on_shelf_date", nullable = false)
    private LocalDateTime placedOnShelfDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "placed_by", nullable = false)
    private Long placedBy;
    
    @Column(name = "unit_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "is_displayed")
    private Boolean isDisplayed = true;
    
    @Column(name = "display_position")
    private String displayPosition;
    
    @Column(name = "minimum_stock_level", precision = 12, scale = 3)
    private BigDecimal minimumStockLevel;
    
    @Column(name = "maximum_stock_level", precision = 12, scale = 3)
    private BigDecimal maximumStockLevel;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "last_updated_by", nullable = false)
    private Long lastUpdatedBy;
    
    // Default constructor
    public ShelfStockEntity() {}
    
    public ShelfStockEntity(String itemCode, Long itemId, Long batchId, String shelfCode,
                           BigDecimal quantityOnShelf, LocalDateTime expiryDate,
                           BigDecimal unitPrice, Long placedBy) {
        this.itemCode = itemCode;
        this.itemId = itemId;
        this.batchId = batchId;
        this.shelfCode = shelfCode;
        this.quantityOnShelf = quantityOnShelf;
        this.placedOnShelfDate = LocalDateTime.now();
        this.expiryDate = expiryDate;
        this.unitPrice = unitPrice;
        this.placedBy = placedBy;
        this.isDisplayed = true;
        this.lastUpdated = LocalDateTime.now();
        this.lastUpdatedBy = placedBy;
    }
    
    @PrePersist
    protected void onCreate() {
        if (placedOnShelfDate == null) {
            placedOnShelfDate = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (isDisplayed == null) {
            isDisplayed = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    
    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }
    
    public BigDecimal getQuantityOnShelf() { return quantityOnShelf; }
    public void setQuantityOnShelf(BigDecimal quantityOnShelf) { this.quantityOnShelf = quantityOnShelf; }
    
    public LocalDateTime getPlacedOnShelfDate() { return placedOnShelfDate; }
    public void setPlacedOnShelfDate(LocalDateTime placedOnShelfDate) { this.placedOnShelfDate = placedOnShelfDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public Long getPlacedBy() { return placedBy; }
    public void setPlacedBy(Long placedBy) { this.placedBy = placedBy; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public Boolean getIsDisplayed() { return isDisplayed; }
    public void setIsDisplayed(Boolean isDisplayed) { this.isDisplayed = isDisplayed; }
    
    public String getDisplayPosition() { return displayPosition; }
    public void setDisplayPosition(String displayPosition) { this.displayPosition = displayPosition; }
    
    public BigDecimal getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(BigDecimal minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    
    public BigDecimal getMaximumStockLevel() { return maximumStockLevel; }
    public void setMaximumStockLevel(BigDecimal maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Long getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(Long lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
