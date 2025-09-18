package com.syos.application.dto.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enhanced Product Request DTO for complete product management workflow
 * Supports: Add Product → Warehouse → Shelf/Web transfer
 */
public class ProductRequest {
    // Core product information
    private String itemCode;
    private String itemName;
    private String description;
    private Long brandId;
    private Long categoryId;
    private Long supplierId;
    private String unitOfMeasure;
    private double packSize;
    private double costPrice;
    private double sellingPrice;
    private int reorderPoint;
    private boolean isPerishable;
    
    // Initial stock information
    private String batchNumber;
    private double initialQuantity;
    private LocalDate manufactureDate;
    private LocalDateTime expiryDate;
    private String warehouseLocation;
    
    // Transfer options
    private boolean transferToShelf;
    private boolean transferToWeb;
    private String shelfCode;
    private double shelfQuantity;
    private double webQuantity;

    // Constructors
    public ProductRequest() {}

    public ProductRequest(String itemCode, String itemName, String description,
                         Long brandId, Long categoryId, Long supplierId,
                         String unitOfMeasure, double packSize,
                         double costPrice, double sellingPrice, int reorderPoint,
                         boolean isPerishable) {
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
        
        // Defaults
        this.warehouseLocation = "MAIN-WAREHOUSE";
        this.transferToShelf = false;
        this.transferToWeb = false;
        this.shelfQuantity = 0;
        this.webQuantity = 0;
    }

    // Validation methods
    public boolean isValid() {
        return itemCode != null && !itemCode.trim().isEmpty() &&
               itemName != null && !itemName.trim().isEmpty() &&
               brandId != null && brandId > 0 &&
               categoryId != null && categoryId > 0 &&
               supplierId != null && supplierId > 0 &&
               costPrice > 0 &&
               sellingPrice > 0 &&
               sellingPrice >= costPrice &&
               reorderPoint >= 0;
    }

    public boolean hasInitialStock() {
        return batchNumber != null && !batchNumber.trim().isEmpty() && initialQuantity > 0;
    }

    // Getters and Setters
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

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public double getPackSize() { return packSize; }
    public void setPackSize(double packSize) { this.packSize = packSize; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }

    public boolean isPerishable() { return isPerishable; }
    public void setPerishable(boolean perishable) { isPerishable = perishable; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public double getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(double initialQuantity) { this.initialQuantity = initialQuantity; }

    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public boolean isTransferToShelf() { return transferToShelf; }
    public void setTransferToShelf(boolean transferToShelf) { this.transferToShelf = transferToShelf; }

    public boolean isTransferToWeb() { return transferToWeb; }
    public void setTransferToWeb(boolean transferToWeb) { this.transferToWeb = transferToWeb; }

    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }

    public double getShelfQuantity() { return shelfQuantity; }
    public void setShelfQuantity(double shelfQuantity) { this.shelfQuantity = shelfQuantity; }

    public double getWebQuantity() { return webQuantity; }
    public void setWebQuantity(double webQuantity) { this.webQuantity = webQuantity; }

    @Override
    public String toString() {
        return String.format("ProductRequest{itemCode='%s', itemName='%s', brandId=%d, categoryId=%d, supplierId=%d}",
                itemCode, itemName, brandId, categoryId, supplierId);
    }
}
