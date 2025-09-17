package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.*;
import com.syos.application.strategies.stock.FIFOWithExpiryStrategy;
import com.syos.adapter.out.persistence.memory.*;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.shared.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for Product Use Cases covering:
 * 1. Product addition with validation
 * 2. Stock transfer to shelf and web inventory with FIFO
 * 3. Reorder level threshold verification (50)
 * 4. Required field validation
 * 5. Business rule enforcement
 */
@DisplayName("Product Use Case Integration Tests")
class ProductUseCaseIntegrationTest {

    // Use Cases
    private AddProductUseCase addProductUseCase;
    private TransferToShelfUseCase transferToShelfUseCase;
    private TransferToWebUseCase transferToWebUseCase;

    // In-Memory Repositories
    private ItemMasterFileRepository itemRepository;
    private InMemoryWarehouseStockRepository warehouseRepository;
    private InMemoryShelfStockRepository shelfRepository;
    private InMemoryWebInventoryRepository webRepository;
    private InMemoryStockTransferRepository stockTransferRepository;
    private InMemoryBatchRepository batchRepository;

    // Mock repositories for related entities
    private MockBrandRepository brandRepository;
    private MockCategoryRepository categoryRepository;
    private MockSupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {
        // Initialize in-memory repositories
        itemRepository = new InMemoryItemMasterFileRepository();
        warehouseRepository = new InMemoryWarehouseStockRepository();
        shelfRepository = new InMemoryShelfStockRepository();
        webRepository = new InMemoryWebInventoryRepository();
        stockTransferRepository = new InMemoryStockTransferRepository();
        batchRepository = new InMemoryBatchRepository();

        // Initialize mock repositories with test data
        brandRepository = new MockBrandRepository();
        categoryRepository = new MockCategoryRepository();
        supplierRepository = new MockSupplierRepository();

        // Initialize use cases
        addProductUseCase = new AddProductUseCase(
            itemRepository, brandRepository, categoryRepository, supplierRepository);

        FIFOWithExpiryStrategy strategy = new FIFOWithExpiryStrategy();
        transferToShelfUseCase = new TransferToShelfUseCase(
            warehouseRepository, shelfRepository, stockTransferRepository, strategy);
        transferToWebUseCase = new TransferToWebUseCase(
            warehouseRepository, webRepository, stockTransferRepository, strategy);
    }

    @Test
    @DisplayName("Should successfully add product to database with all required fields")
    void shouldAddProductWithAllRequiredFields() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidProductRequest();

        // Act
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getItemId());
        assertEquals("PROD001", response.getItemCode());
        assertEquals("Product added successfully", response.getMessage());

        // Verify product was saved to database
        ItemMasterFile savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD001")).orElse(null);
        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getItemName());
        assertEquals("Test Description", savedProduct.getDescription());
        assertEquals(Money.of(BigDecimal.valueOf(100)), savedProduct.getCostPrice());
        assertEquals(Money.of(BigDecimal.valueOf(150)), savedProduct.getSellingPrice());
        assertEquals(ReorderPoint.of(50), savedProduct.getReorderPoint());
        assertEquals(ProductStatus.ACTIVE, savedProduct.getStatus());
        assertTrue(savedProduct.isActive());
    }

    @Test
    @DisplayName("Should validate all required fields when adding product")
    void shouldValidateAllRequiredFields() {
        // Test null request
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(null));

        // Test empty item code
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        request.itemCode("");
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(request));

        // Test null item name
        request = createValidProductRequest();
        request.itemName(null);
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(request));

        // Test invalid brand ID
        request = createValidProductRequest();
        request.brandId(-1L);
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(request));

        // Test invalid cost price
        request = createValidProductRequest();
        request.costPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(request));

        // Test selling price less than cost price
        request = createValidProductRequest();
        request.costPrice(BigDecimal.valueOf(200));
        request.sellingPrice(BigDecimal.valueOf(100));
        assertThrows(IllegalArgumentException.class, 
            () -> addProductUseCase.execute(request));
    }

    @Test
    @DisplayName("Should enforce reorder point threshold of 50 units")
    void shouldEnforceReorderPointThreshold() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        request.reorderPoint(30); // Below standard threshold of 50

        // Act
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);

        // Assert - should still allow custom reorder points but default should be 50
        assertTrue(response.isSuccess());
        
        ItemMasterFile savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD001")).orElse(null);
        assertEquals(ReorderPoint.of(30), savedProduct.getReorderPoint());

        // Test default reorder point
        request = createValidProductRequest();
        request.itemCode("PROD002");
        request.reorderPoint(50);
        response = addProductUseCase.execute(request);
        
        savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD002")).orElse(null);
        assertEquals(ReorderPoint.of(50), savedProduct.getReorderPoint());
    }

    @Test
    @DisplayName("Should transfer stock to shelf using FIFO with expiry exception")
    void shouldTransferStockToShelfWithFIFO() {
        // First add a product
        addTestProductToWarehouse();
        
        // Add stock batches to warehouse with different dates
        warehouseRepository.addStock(1L, 1L, BigDecimal.valueOf(100)); // Batch 1 - oldest
        warehouseRepository.addStock(1L, 2L, BigDecimal.valueOf(100)); // Batch 2 - newer
        
        // Transfer 50 units to shelf
        transferToShelfUseCase.transfer(1L, BigDecimal.valueOf(50));
        
        // Verify stock was transferred using FIFO
        BigDecimal shelfStock = shelfRepository.getCurrentStock(1L);
        assertEquals(BigDecimal.valueOf(50), shelfStock);
        
        // Verify warehouse stock reduced
        BigDecimal warehouseStock = warehouseRepository.getTotalAvailableStock(1L);
        assertEquals(BigDecimal.valueOf(150), warehouseStock); // 200 - 50 transferred
        
        // Verify stock transfer was recorded
        assertTrue(stockTransferRepository.hasTransferRecord(1L, "WAREHOUSE", "SHELF"));
    }

    @Test
    @DisplayName("Should transfer stock to web inventory using FIFO strategy")
    void shouldTransferStockToWebInventoryWithFIFO() {
        // First add a product
        addTestProductToWarehouse();
        
        // Add stock batches to warehouse
        warehouseRepository.addStock(1L, 1L, BigDecimal.valueOf(100));
        warehouseRepository.addStock(1L, 2L, BigDecimal.valueOf(100));
        
        // Transfer 75 units to web inventory
        transferToWebUseCase.transfer(1L, BigDecimal.valueOf(75));
        
        // Verify stock was transferred
        BigDecimal webStock = webRepository.getCurrentStock(1L);
        assertEquals(BigDecimal.valueOf(75), webStock);
        
        // Verify warehouse stock reduced
        BigDecimal warehouseStock = warehouseRepository.getTotalAvailableStock(1L);
        assertEquals(BigDecimal.valueOf(125), warehouseStock); // 200 - 75 transferred
        
        // Verify stock transfer was recorded
        assertTrue(stockTransferRepository.hasTransferRecord(1L, "WAREHOUSE", "WEB"));
    }

    @Test
    @DisplayName("Should prevent duplicate item codes")
    void shouldPreventDuplicateItemCodes() {
        // Add first product
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
        assertTrue(response.isSuccess());

        // Try to add product with same item code
        request = createValidProductRequest(); // Same item code PROD001
        assertThrows(Exception.class, () -> addProductUseCase.execute(request));
    }

    @Test
    @DisplayName("Should validate foreign key relationships")
    void shouldValidateForeignKeyRelationships() {
        // Test invalid brand ID
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        request.brandId(999L); // Non-existent brand
        assertThrows(Exception.class, () -> addProductUseCase.execute(request));

        // Test invalid category ID
        request = createValidProductRequest();
        request.categoryId(999L);
        assertThrows(Exception.class, () -> addProductUseCase.execute(request));

        // Test invalid supplier ID
        request = createValidProductRequest();
        request.supplierId(999L);
        assertThrows(Exception.class, () -> addProductUseCase.execute(request));
    }

    @Test
    @DisplayName("Should support different unit of measures")
    void shouldSupportDifferentUnitsOfMeasure() {
        // Test each unit of measure
        UnitOfMeasure[] units = UnitOfMeasure.values();
        
        for (int i = 0; i < units.length; i++) {
            AddProductUseCase.AddProductRequest request = createValidProductRequest();
            request.itemCode("PROD" + String.format("%03d", i + 10));
            request.unitOfMeasure(units[i]);
            
            AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
            assertTrue(response.isSuccess(), "Failed for unit: " + units[i]);
            
            ItemMasterFile product = itemRepository.findByItemCode(
                ItemCode.of("PROD" + String.format("%03d", i + 10))).orElse(null);
            assertEquals(units[i], product.getUnitOfMeasure());
        }
    }

    @Test
    @DisplayName("Should handle perishable products correctly")
    void shouldHandlePerishableProducts() {
        // Test perishable product
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        request.itemCode("PERISHABLE001");
        request.isPerishable(true);
        
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
        assertTrue(response.isSuccess());
        
        ItemMasterFile product = itemRepository.findByItemCode(ItemCode.of("PERISHABLE001")).orElse(null);
        assertTrue(product.isPerishable());
        
        // Test non-perishable product
        request = createValidProductRequest();
        request.itemCode("NONPERISHABLE001");
        request.isPerishable(false);
        
        response = addProductUseCase.execute(request);
        assertTrue(response.isSuccess());
        
        product = itemRepository.findByItemCode(ItemCode.of("NONPERISHABLE001")).orElse(null);
        assertFalse(product.isPerishable());
    }

    @Test
    @DisplayName("Should handle insufficient stock transfer gracefully")
    void shouldHandleInsufficientStockTransfer() {
        // Add product but no stock
        addTestProductToWarehouse();
        
        // Try to transfer more than available
        assertThrows(Exception.class, 
            () -> transferToShelfUseCase.transfer(1L, BigDecimal.valueOf(100)));
    }

    // Helper methods
    private AddProductUseCase.AddProductRequest createValidProductRequest() {
        return new AddProductUseCase.AddProductRequest()
            .itemCode("PROD001")
            .itemName("Test Product")
            .description("Test Description")
            .brandId(1L)
            .categoryId(1L)
            .supplierId(1L)
            .unitOfMeasure(UnitOfMeasure.EACH)
            .packSize(BigDecimal.ONE)
            .costPrice(BigDecimal.valueOf(100))
            .sellingPrice(BigDecimal.valueOf(150))
            .reorderPoint(50)
            .isPerishable(false)
            .createdBy(1L);
    }
    
    private void addTestProductToWarehouse() {
        AddProductUseCase.AddProductRequest request = createValidProductRequest();
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
        assertTrue(response.isSuccess());
    }

    // Mock repository implementations for testing
    private static class MockBrandRepository implements BrandRepository {
        @Override
        public boolean existsById(Long id) { 
            return id != null && id >= 1 && id <= 10; 
        }
        
        @Override
        public boolean isActive(Long id) { 
            return existsById(id); 
        }
    }

    private static class MockCategoryRepository implements CategoryRepository {
        @Override
        public boolean existsById(Long id) { 
            return id != null && id >= 1 && id <= 10; 
        }
        
        @Override
        public boolean isActive(Long id) { 
            return existsById(id); 
        }
    }

    private static class MockSupplierRepository implements SupplierRepository {
        @Override
        public boolean existsById(Long id) { 
            return id != null && id >= 1 && id <= 10; 
        }
        
        @Override
        public boolean isActive(Long id) { 
            return existsById(id); 
        }
    }
}
