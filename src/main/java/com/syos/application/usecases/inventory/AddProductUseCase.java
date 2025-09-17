package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.SupplierRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.domain.exceptions.DomainException;

import java.math.BigDecimal;

/**
 * Use case for adding new products to the Item Master File.
 * 
 * Addresses Scenario Requirements:
 * - Add products to the system with complete information
 * - Validate relationships with brands, categories, and suppliers
 * - Ensure business rule compliance
 * 
 * Clean Architecture:
 * - Depends only on domain entities and repository interfaces
 * - Contains business logic for product creation workflow
 */
public class AddProductUseCase {
    
    private final ItemMasterFileRepository itemRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public AddProductUseCase(ItemMasterFileRepository itemRepository,
                            BrandRepository brandRepository,
                            CategoryRepository categoryRepository,
                            SupplierRepository supplierRepository) {
        this.itemRepository = itemRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Execute the add product use case
     */
    public AddProductResponse execute(AddProductRequest request) {
        validateRequest(request);
        
        // Check if item code already exists
        if (itemRepository.existsByItemCode(ItemCode.of(request.getItemCode()))) {
            throw new DomainException("Item with code '" + request.getItemCode() + "' already exists");
        }

        // Validate foreign key relationships
        validateRelationships(request);

        // Create domain entity
        ItemMasterFile item = ItemMasterFile.createNew(
            ItemCode.of(request.getItemCode()),
            request.getItemName(),
            request.getDescription(),
            BrandId.of(request.getBrandId()),
            CategoryId.of(request.getCategoryId()),
            SupplierId.of(request.getSupplierId()),
            request.getUnitOfMeasure(),
            PackSize.of(request.getPackSize()),
            Money.of(request.getCostPrice()),
            Money.of(request.getSellingPrice()),
            ReorderPoint.of(request.getReorderPoint()),
            request.isPerishable(),
            UserID.of(request.getCreatedBy())
        );

        // Save to repository
        ItemMasterFile savedItem = itemRepository.save(item);

        return AddProductResponse.success(savedItem.getId(), savedItem.getItemCode().getValue());
    }

    private void validateRequest(AddProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getItemCode() == null || request.getItemCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Item code is required");
        }

        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }

        if (request.getBrandId() == null || request.getBrandId() <= 0) {
            throw new IllegalArgumentException("Valid brand ID is required");
        }

        if (request.getCategoryId() == null || request.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Valid category ID is required");
        }

        if (request.getSupplierId() == null || request.getSupplierId() <= 0) {
            throw new IllegalArgumentException("Valid supplier ID is required");
        }

        if (request.getCostPrice() == null || request.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cost price must be positive");
        }

        if (request.getSellingPrice() == null || request.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selling price must be positive");
        }

        if (request.getSellingPrice().compareTo(request.getCostPrice()) < 0) {
            throw new IllegalArgumentException("Selling price must be greater than or equal to cost price");
        }
    }

    private void validateRelationships(AddProductRequest request) {
        // Validate brand exists and is active
        if (!brandRepository.existsById(request.getBrandId()) || 
            !brandRepository.isActive(request.getBrandId())) {
            throw new DomainException("Brand with ID " + request.getBrandId() + " does not exist or is inactive");
        }

        // Validate category exists and is active
        if (!categoryRepository.existsById(request.getCategoryId()) || 
            !categoryRepository.isActive(request.getCategoryId())) {
            throw new DomainException("Category with ID " + request.getCategoryId() + " does not exist or is inactive");
        }

        // Validate supplier exists and is active
        if (!supplierRepository.existsById(request.getSupplierId()) || 
            !supplierRepository.isActive(request.getSupplierId())) {
            throw new DomainException("Supplier with ID " + request.getSupplierId() + " does not exist or is inactive");
        }
    }

    /**
     * Request DTO for adding a product
     */
    public static class AddProductRequest {
        private String itemCode;
        private String itemName;
        private String description;
        private Long brandId;
        private Long categoryId;
        private Long supplierId;
        private UnitOfMeasure unitOfMeasure;
        private BigDecimal packSize;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private Integer reorderPoint;
        private boolean isPerishable;
        private Long createdBy;

        // Constructors
        public AddProductRequest() {}

        // Builder-style setters for fluent API
        public AddProductRequest itemCode(String itemCode) { this.itemCode = itemCode; return this; }
        public AddProductRequest itemName(String itemName) { this.itemName = itemName; return this; }
        public AddProductRequest description(String description) { this.description = description; return this; }
        public AddProductRequest brandId(Long brandId) { this.brandId = brandId; return this; }
        public AddProductRequest categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public AddProductRequest supplierId(Long supplierId) { this.supplierId = supplierId; return this; }
        public AddProductRequest unitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public AddProductRequest packSize(BigDecimal packSize) { this.packSize = packSize; return this; }
        public AddProductRequest costPrice(BigDecimal costPrice) { this.costPrice = costPrice; return this; }
        public AddProductRequest sellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; return this; }
        public AddProductRequest reorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public AddProductRequest isPerishable(boolean isPerishable) { this.isPerishable = isPerishable; return this; }
        public AddProductRequest createdBy(Long createdBy) { this.createdBy = createdBy; return this; }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getDescription() { return description; }
        public Long getBrandId() { return brandId; }
        public Long getCategoryId() { return categoryId; }
        public Long getSupplierId() { return supplierId; }
        public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
        public BigDecimal getPackSize() { return packSize; }
        public BigDecimal getCostPrice() { return costPrice; }
        public BigDecimal getSellingPrice() { return sellingPrice; }
        public Integer getReorderPoint() { return reorderPoint; }
        public boolean isPerishable() { return isPerishable; }
        public Long getCreatedBy() { return createdBy; }
    }

    /**
     * Response DTO for add product operation
     */
    public static class AddProductResponse {
        private final boolean success;
        private final Long itemId;
        private final String itemCode;
        private final String message;

        private AddProductResponse(boolean success, Long itemId, String itemCode, String message) {
            this.success = success;
            this.itemId = itemId;
            this.itemCode = itemCode;
            this.message = message;
        }

        public static AddProductResponse success(Long itemId, String itemCode) {
            return new AddProductResponse(true, itemId, itemCode, "Product added successfully");
        }

        public static AddProductResponse failure(String message) {
            return new AddProductResponse(false, null, null, message);
        }

        public boolean isSuccess() { return success; }
        public Long getItemId() { return itemId; }
        public String getItemCode() { return itemCode; }
        public String getMessage() { return message; }
    }
}
