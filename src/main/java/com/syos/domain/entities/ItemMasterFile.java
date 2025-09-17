package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.ProductStatus;
import com.syos.shared.enums.UnitOfMeasure;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a product in the Item Master File.
 * This is the authoritative source for all product information in SYOS.
 * 
 * Addresses Scenario Requirements:
 * - Requirement 1: Items entered via unique codes
 * - Requirement 4(a): Item name display (not code) on bills
 * - Clean Architecture: Pure domain logic, no infrastructure dependencies
 */
public class ItemMasterFile {
    private final Long id; // nullable for new items
    private final ItemCode itemCode;
    private final String itemName;
    private final String description;
    private final BrandId brandId;
    private final CategoryId categoryId;
    private final SupplierId supplierId;
    private final UnitOfMeasure unitOfMeasure;
    private final PackSize packSize;
    private final Money costPrice;
    private final Money sellingPrice;
    private final ReorderPoint reorderPoint;
    private final boolean isPerishable;
    private final ProductStatus status;
    private final boolean isFeatured;
    private final boolean isLatest;
    private final LocalDateTime dateAdded;
    private final LocalDateTime lastUpdated;
    private final UserID createdBy;
    private final UserID updatedBy;

    private ItemMasterFile(Builder builder) {
        this.id = builder.id;
        this.itemCode = Objects.requireNonNull(builder.itemCode, "Item code cannot be null");
        this.itemName = validateItemName(builder.itemName);
        this.description = builder.description;
        this.brandId = Objects.requireNonNull(builder.brandId, "Brand ID cannot be null");
        this.categoryId = Objects.requireNonNull(builder.categoryId, "Category ID cannot be null");
        this.supplierId = Objects.requireNonNull(builder.supplierId, "Supplier ID cannot be null");
        this.unitOfMeasure = Objects.requireNonNull(builder.unitOfMeasure, "Unit of measure cannot be null");
        this.packSize = Objects.requireNonNull(builder.packSize, "Pack size cannot be null");
        this.costPrice = Objects.requireNonNull(builder.costPrice, "Cost price cannot be null");
        this.sellingPrice = Objects.requireNonNull(builder.sellingPrice, "Selling price cannot be null");
        this.reorderPoint = Objects.requireNonNull(builder.reorderPoint, "Reorder point cannot be null");
        this.isPerishable = builder.isPerishable;
        this.status = builder.status != null ? builder.status : ProductStatus.ACTIVE;
        this.isFeatured = builder.isFeatured;
        this.isLatest = builder.isLatest;
        this.dateAdded = builder.dateAdded != null ? builder.dateAdded : LocalDateTime.now();
        this.lastUpdated = builder.lastUpdated != null ? builder.lastUpdated : LocalDateTime.now();
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
        
        validateBusinessRules();
    }

    private String validateItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        return name.trim();
    }

    private void validateBusinessRules() {
        if (sellingPrice.isLessThan(costPrice)) {
            throw new IllegalArgumentException("Selling price must be greater than or equal to cost price");
        }
        
        if (costPrice.isZeroOrNegative()) {
            throw new IllegalArgumentException("Cost price must be positive");
        }
    }

    /**
     * Factory method to create a new ItemMasterFile for addition to catalog
     */
    public static ItemMasterFile createNew(ItemCode itemCode, String itemName, String description,
                                          BrandId brandId, CategoryId categoryId, SupplierId supplierId,
                                          UnitOfMeasure unitOfMeasure, PackSize packSize,
                                          Money costPrice, Money sellingPrice, ReorderPoint reorderPoint,
                                          boolean isPerishable, UserID createdBy) {
        return new Builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .description(description)
                .brandId(brandId)
                .categoryId(categoryId)
                .supplierId(supplierId)
                .unitOfMeasure(unitOfMeasure)
                .packSize(packSize)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .reorderPoint(reorderPoint)
                .isPerishable(isPerishable)
                .createdBy(createdBy)
                .build();
    }

    /**
     * Create ItemMasterFile with ID (for repository reconstruction)
     */
    public ItemMasterFile withId(Long id) {
        return new Builder(this).id(id).build();
    }

    /**
     * Create updated version of this item with new selling price
     */
    public ItemMasterFile updateSellingPrice(Money newSellingPrice, UserID updatedBy) {
        return new Builder(this)
                .sellingPrice(newSellingPrice)
                .updatedBy(updatedBy)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Mark item as featured
     */
    public ItemMasterFile markAsFeatured(UserID updatedBy) {
        return new Builder(this)
                .isFeatured(true)
                .updatedBy(updatedBy)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Mark item as latest arrival
     */
    public ItemMasterFile markAsLatest(UserID updatedBy) {
        return new Builder(this)
                .isLatest(true)
                .updatedBy(updatedBy)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Deactivate the item
     */
    public ItemMasterFile deactivate(UserID updatedBy) {
        return new Builder(this)
                .status(ProductStatus.INACTIVE)
                .updatedBy(updatedBy)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // Getters
    public Long getId() { return id; }
    public ItemCode getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public BrandId getBrandId() { return brandId; }
    public CategoryId getCategoryId() { return categoryId; }
    public SupplierId getSupplierId() { return supplierId; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public PackSize getPackSize() { return packSize; }
    public Money getCostPrice() { return costPrice; }
    public Money getSellingPrice() { return sellingPrice; }
    public ReorderPoint getReorderPoint() { return reorderPoint; }
    public boolean isPerishable() { return isPerishable; }
    public ProductStatus getStatus() { return status; }
    public boolean isFeatured() { return isFeatured; }
    public boolean isLatest() { return isLatest; }
    public LocalDateTime getDateAdded() { return dateAdded; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public UserID getCreatedBy() { return createdBy; }
    public UserID getUpdatedBy() { return updatedBy; }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemMasterFile that = (ItemMasterFile) o;
        return Objects.equals(itemCode, that.itemCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode);
    }

    /**
     * Builder pattern for complex object construction
     * Implements Builder Pattern (Pattern #8)
     */
    public static class Builder {
        private Long id;
        private ItemCode itemCode;
        private String itemName;
        private String description;
        private BrandId brandId;
        private CategoryId categoryId;
        private SupplierId supplierId;
        private UnitOfMeasure unitOfMeasure;
        private PackSize packSize;
        private Money costPrice;
        private Money sellingPrice;
        private ReorderPoint reorderPoint;
        private boolean isPerishable;
        private ProductStatus status;
        private boolean isFeatured;
        private boolean isLatest;
        private LocalDateTime dateAdded;
        private LocalDateTime lastUpdated;
        private UserID createdBy;
        private UserID updatedBy;

        public Builder() {}

        public Builder(ItemMasterFile existing) {
            this.id = existing.id;
            this.itemCode = existing.itemCode;
            this.itemName = existing.itemName;
            this.description = existing.description;
            this.brandId = existing.brandId;
            this.categoryId = existing.categoryId;
            this.supplierId = existing.supplierId;
            this.unitOfMeasure = existing.unitOfMeasure;
            this.packSize = existing.packSize;
            this.costPrice = existing.costPrice;
            this.sellingPrice = existing.sellingPrice;
            this.reorderPoint = existing.reorderPoint;
            this.isPerishable = existing.isPerishable;
            this.status = existing.status;
            this.isFeatured = existing.isFeatured;
            this.isLatest = existing.isLatest;
            this.dateAdded = existing.dateAdded;
            this.lastUpdated = existing.lastUpdated;
            this.createdBy = existing.createdBy;
            this.updatedBy = existing.updatedBy;
        }

        public Builder id(Long id) { this.id = id; return this; }
        public Builder itemCode(ItemCode itemCode) { this.itemCode = itemCode; return this; }
        public Builder itemName(String itemName) { this.itemName = itemName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder brandId(BrandId brandId) { this.brandId = brandId; return this; }
        public Builder categoryId(CategoryId categoryId) { this.categoryId = categoryId; return this; }
        public Builder supplierId(SupplierId supplierId) { this.supplierId = supplierId; return this; }
        public Builder unitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder packSize(PackSize packSize) { this.packSize = packSize; return this; }
        public Builder costPrice(Money costPrice) { this.costPrice = costPrice; return this; }
        public Builder sellingPrice(Money sellingPrice) { this.sellingPrice = sellingPrice; return this; }
        public Builder reorderPoint(ReorderPoint reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public Builder isPerishable(boolean isPerishable) { this.isPerishable = isPerishable; return this; }
        public Builder status(ProductStatus status) { this.status = status; return this; }
        public Builder isFeatured(boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder isLatest(boolean isLatest) { this.isLatest = isLatest; return this; }
        public Builder dateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder createdBy(UserID createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedBy(UserID updatedBy) { this.updatedBy = updatedBy; return this; }

        public ItemMasterFile build() {
            return new ItemMasterFile(this);
        }
    }
}
