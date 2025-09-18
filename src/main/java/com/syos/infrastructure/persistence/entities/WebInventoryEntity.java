package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for web_inventory table
 */
@Entity
@Table(name = "web_inventory")
public class WebInventoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "batch_id", nullable = false)
    private Long batchId;
    
    @Column(name = "quantity_available", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantityAvailable;
    
    @Column(name = "added_to_web_date", nullable = false)
    private LocalDateTime addedToWebDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "added_by", nullable = false)
    private Long addedBy;
    
    @Column(name = "web_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal webPrice;
    
    @Column(name = "is_published")
    private Boolean isPublished = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "stock_level")
    private Integer stockLevel = 50;
    
    @Column(name = "web_description")
    private String webDescription;
    
    @Column(name = "seo_keywords")
    private String seoKeywords;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "last_updated_by", nullable = false)
    private Long lastUpdatedBy;
    
    // Default constructor
    public WebInventoryEntity() {}
    
    public WebInventoryEntity(String itemCode, Long itemId, Long batchId,
                             BigDecimal quantityAvailable, LocalDateTime expiryDate,
                             BigDecimal webPrice, Long addedBy) {
        this.itemCode = itemCode;
        this.itemId = itemId;
        this.batchId = batchId;
        this.quantityAvailable = quantityAvailable;
        this.addedToWebDate = LocalDateTime.now();
        this.expiryDate = expiryDate;
        this.webPrice = webPrice;
        this.addedBy = addedBy;
        this.isPublished = true;
        this.isFeatured = false;
        this.stockLevel = calculateStockLevel(quantityAvailable);
        this.lastUpdated = LocalDateTime.now();
        this.lastUpdatedBy = addedBy;
    }
    
    private Integer calculateStockLevel(BigDecimal quantity) {
        if (quantity == null) return 0;
        int qty = quantity.intValue();
        if (qty <= 5) return 10;     // Low stock
        if (qty <= 20) return 50;    // Medium stock
        return 90;                   // High stock
    }
    
    @PrePersist
    protected void onCreate() {
        if (addedToWebDate == null) {
            addedToWebDate = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (isPublished == null) {
            isPublished = true;
        }
        if (isFeatured == null) {
            isFeatured = false;
        }
        if (stockLevel == null) {
            stockLevel = calculateStockLevel(quantityAvailable);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        // Recalculate stock level when quantity changes
        if (quantityAvailable != null) {
            stockLevel = calculateStockLevel(quantityAvailable);
        }
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
    
    public BigDecimal getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(BigDecimal quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    
    public LocalDateTime getAddedToWebDate() { return addedToWebDate; }
    public void setAddedToWebDate(LocalDateTime addedToWebDate) { this.addedToWebDate = addedToWebDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public Long getAddedBy() { return addedBy; }
    public void setAddedBy(Long addedBy) { this.addedBy = addedBy; }
    
    public BigDecimal getWebPrice() { return webPrice; }
    public void setWebPrice(BigDecimal webPrice) { this.webPrice = webPrice; }
    
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Integer getStockLevel() { return stockLevel; }
    public void setStockLevel(Integer stockLevel) { this.stockLevel = stockLevel; }
    
    public String getWebDescription() { return webDescription; }
    public void setWebDescription(String webDescription) { this.webDescription = webDescription; }
    
    public String getSeoKeywords() { return seoKeywords; }
    public void setSeoKeywords(String seoKeywords) { this.seoKeywords = seoKeywords; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Long getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(Long lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
