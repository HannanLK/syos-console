package com.syos.infrastructure.persistence.entities;

import com.syos.shared.enums.ProductStatus;
import com.syos.shared.enums.UnitOfMeasure;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Item Master File table.
 * Maps to the item_master_file database table.
 * 
 * Infrastructure Layer:
 * - Handles data persistence concerns
 * - Maps domain entities to database structure
 * - Uses JPA annotations for ORM mapping
 */
@Entity
@Table(name = "item_master_file")
public class ItemMasterFileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", unique = true, nullable = false, length = 50)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false)
    private UnitOfMeasure unitOfMeasure;
    
    @Column(name = "pack_size", precision = 10, scale = 3)
    private BigDecimal packSize;
    
    @Column(name = "cost_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal costPrice;
    
    @Column(name = "selling_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal sellingPrice;
    
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;
    
    @Column(name = "is_perishable", nullable = false)
    private Boolean isPerishable = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest = false;
    
    @CreationTimestamp
    @Column(name = "date_added", nullable = false, updatable = false)
    private LocalDateTime dateAdded;
    
    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // JPA requires default constructor
    public ItemMasterFileEntity() {}
    
    // Constructor for creating new entities
    public ItemMasterFileEntity(String itemCode, String itemName, String description,
                               Long brandId, Long categoryId, Long supplierId,
                               UnitOfMeasure unitOfMeasure, BigDecimal packSize,
                               BigDecimal costPrice, BigDecimal sellingPrice, 
                               Integer reorderPoint, Boolean isPerishable, Long createdBy) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.unitOfMeasure = unitOfMeasure;
        this.packSize = packSize;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.reorderPoint = reorderPoint;
        this.isPerishable = isPerishable;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    
    public BigDecimal getPackSize() { return packSize; }
    public void setPackSize(BigDecimal packSize) { this.packSize = packSize; }
    
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    
    public Boolean getIsPerishable() { return isPerishable; }
    public void setIsPerishable(Boolean isPerishable) { this.isPerishable = isPerishable; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }
    
    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    @PrePersist
    protected void onCreate() {
        if (dateAdded == null) {
            dateAdded = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
