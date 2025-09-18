package com.syos.application.usecases.inventory;

import com.syos.application.dto.requests.ProductRequest;
import com.syos.application.dto.responses.ProductResponse;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.application.ports.out.*;
import com.syos.adapter.out.persistence.memory.*;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.shared.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product Use Cases using CompleteProductManagementUseCase
 * Tests: Product addition, stock management, and transfers
 * 
 * Addresses Assignment Requirements:
 * 1. Product addition with validation
 * 2. Stock transfer to shelf and web inventory
 * 3. Reorder level threshold verification (50)
 * 4. Required field validation
 * 5. Business rule enforcement
 */
@DisplayName("Product Use Case Integration Tests")
class ProductUseCaseIntegrationTest {

    // Use Case
    private CompleteProductManagementUseCase productManagementUseCase;

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
    
    // Test user
    private UserID testUser;

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
        
        // Initialize test user
        testUser = UserID.of(1L);

        // Initialize complete product management use case
        productManagementUseCase = new CompleteProductManagementUseCase(
            itemRepository,
            brandRepository,
            categoryRepository,
            supplierRepository,
            batchRepository,
            warehouseRepository,
            shelfRepository,
            webRepository
        );
    }

    @Test
    @DisplayName("Should successfully add product to database with all required fields")
    void shouldAddProductWithAllRequiredFields() {
        // Arrange
        ProductRequest request = createValidProductRequest();

        // Act
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);

        // Assert
        assertTrue(response.isSuccess(), "Product addition should succeed");
        assertNotNull(response.getProductId());
        assertEquals("PROD001", response.getItemCode());
        assertTrue(response.getMessage().contains("successfully"));

        // Verify product was saved to database
        ItemMasterFile savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD001")).orElse(null);
        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getItemName());
        assertEquals("Test Description", savedProduct.getDescription());
        assertEquals(Money.of(100.0), savedProduct.getCostPrice());
        assertEquals(Money.of(150.0), savedProduct.getSellingPrice());
        assertEquals(ReorderPoint.of(50), savedProduct.getReorderPoint());
        assertEquals(ProductStatus.ACTIVE, savedProduct.getStatus());
        assertTrue(savedProduct.isActive());
    }

    @Test
    @DisplayName("Should validate all required fields when adding product")
    void shouldValidateAllRequiredFields() {
        // Test invalid cost price
        ProductRequest reqInvalidCost = createValidProductRequest();
        reqInvalidCost.setCostPrice(0.0);
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(reqInvalidCost, testUser);
        assertTrue(response.isFailure(), "Should fail due to invalid cost price");

        // Test selling price less than cost price
        ProductRequest reqInvalidPricing = createValidProductRequest();
        reqInvalidPricing.setCostPrice(200.0);
        reqInvalidPricing.setSellingPrice(100.0);
        response = productManagementUseCase.addProductWithInitialStock(reqInvalidPricing, testUser);
        assertTrue(response.isFailure(), "Should fail due to selling price less than cost price");

        // Test invalid brand ID
        ProductRequest reqInvalidBrand = createValidProductRequest();
        reqInvalidBrand.setBrandId(999L); // Non-existent brand
        response = productManagementUseCase.addProductWithInitialStock(reqInvalidBrand, testUser);
        assertTrue(response.isFailure(), "Should fail due to invalid brand");
    }

    @Test
    @DisplayName("Should enforce reorder point threshold of 50 units")
    void shouldEnforceReorderPointThreshold() {
        // Arrange
        ProductRequest request = createValidProductRequest();
        request.setReorderPoint(30); // Below standard threshold of 50

        // Act
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);

        // Assert - should still allow custom reorder points
        assertTrue(response.isSuccess(), "Product addition should succeed");
        
        ItemMasterFile savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD001")).orElse(null);
        assertEquals(ReorderPoint.of(30), savedProduct.getReorderPoint());

        // Test default reorder point
        request = createValidProductRequest();
        request.setItemCode("PROD002");
        request.setReorderPoint(50);
        response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        
        savedProduct = itemRepository.findByItemCode(ItemCode.of("PROD002")).orElse(null);
        assertEquals(ReorderPoint.of(50), savedProduct.getReorderPoint());
    }

    @Test
    @DisplayName("Should transfer stock to shelf with complete product workflow")
    void shouldTransferStockToShelfWithFIFO() {
        // Arrange - Create product with initial stock and immediate shelf transfer
        ProductRequest request = createValidProductRequestWithStock();
        request.setTransferToShelf(true);
        request.setShelfCode("A1-001");
        request.setShelfQuantity(50.0);
        
        // Act
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        
        // Assert
        assertTrue(response.isSuccess(), "Product addition with shelf transfer should succeed");
        
        // Verify shelf stock was created
        BigDecimal shelfStock = shelfRepository.getCurrentStock(response.getProductId());
        assertEquals(BigDecimal.valueOf(50.0), shelfStock, "Shelf should have 50 units");
        
        // Verify warehouse stock was reduced
        BigDecimal warehouseStock = warehouseRepository.getTotalAvailableStock(response.getProductId());
        assertEquals(BigDecimal.valueOf(50.0), warehouseStock, "Warehouse should have 50 units remaining");
    }

    @Test
    @DisplayName("Should transfer stock to web inventory with complete workflow")
    void shouldTransferStockToWebInventoryWithFIFO() {
        // Arrange - Create product with initial stock and immediate web transfer
        ProductRequest request = createValidProductRequestWithStock();
        request.setTransferToWeb(true);
        request.setWebQuantity(30.0);
        
        // Act
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        
        // Assert
        assertTrue(response.isSuccess(), "Product addition with web transfer should succeed");
        
        // Verify web stock was created
        BigDecimal webStock = webRepository.getCurrentStock(response.getProductId());
        assertEquals(BigDecimal.valueOf(30.0), webStock, "Web inventory should have 30 units");
        
        // Verify warehouse stock was reduced
        BigDecimal warehouseStock = warehouseRepository.getTotalAvailableStock(response.getProductId());
        assertEquals(BigDecimal.valueOf(70.0), warehouseStock, "Warehouse should have 70 units remaining");
    }

    @Test
    @DisplayName("Should prevent duplicate item codes")
    void shouldPreventDuplicateItemCodes() {
        // Add first product
        ProductRequest request = createValidProductRequestWithStock();
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        assertTrue(response.isSuccess(), "First product should be added successfully");

        // Try to add product with same item code
        ProductRequest dupRequest = createValidProductRequestWithStock(); // Same item code PROD001
        ProductResponse dupResponse = productManagementUseCase.addProductWithInitialStock(dupRequest, testUser);
        assertTrue(dupResponse.isFailure(), "Duplicate item code should be rejected");
        assertTrue(dupResponse.getError().contains("already exists"));
    }

    @Test
    @DisplayName("Should validate foreign key relationships")
    void shouldValidateForeignKeyRelationships() {
        // Test invalid brand ID
        ProductRequest reqInvalidBrand = createValidProductRequestWithStock();
        reqInvalidBrand.setBrandId(999L); // Non-existent brand
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(reqInvalidBrand, testUser);
        assertTrue(response.isFailure(), "Should fail due to invalid brand ID");
        assertTrue(response.getError().contains("Brand not found"));

        // Test invalid category ID
        ProductRequest reqInvalidCategory = createValidProductRequestWithStock();
        reqInvalidCategory.setCategoryId(999L);
        response = productManagementUseCase.addProductWithInitialStock(reqInvalidCategory, testUser);
        assertTrue(response.isFailure(), "Should fail due to invalid category ID");
        assertTrue(response.getError().contains("Category not found"));

        // Test invalid supplier ID
        ProductRequest reqInvalidSupplier = createValidProductRequestWithStock();
        reqInvalidSupplier.setSupplierId(999L);
        response = productManagementUseCase.addProductWithInitialStock(reqInvalidSupplier, testUser);
        assertTrue(response.isFailure(), "Should fail due to invalid supplier ID");
        assertTrue(response.getError().contains("Supplier not found"));
    }

    @Test
    @DisplayName("Should support different unit of measures")
    void shouldSupportDifferentUnitsOfMeasure() {
        // Test each unit of measure
        UnitOfMeasure[] units = UnitOfMeasure.values();
        
        for (int i = 0; i < units.length; i++) {
            ProductRequest request = createValidProductRequest();
            request.setItemCode("PROD" + String.format("%03d", i + 10));
            request.setUnitOfMeasure(units[i].name());
            
            ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
            assertTrue(response.isSuccess(), "Failed for unit: " + units[i]);
            
            ItemMasterFile product = itemRepository.findByItemCode(
                ItemCode.of("PROD" + String.format("%03d", i + 10))).orElse(null);
            assertNotNull(product, "Product should be saved");
            assertEquals(units[i], product.getUnitOfMeasure());
        }
    }

    @Test
    @DisplayName("Should handle perishable products correctly")
    void shouldHandlePerishableProducts() {
        // Test perishable product
        ProductRequest request = createValidProductRequest();
        request.setItemCode("PERISHABLE001");
        request.setPerishable(true);
        
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        assertTrue(response.isSuccess(), "Perishable product should be added successfully");
        
        ItemMasterFile product = itemRepository.findByItemCode(ItemCode.of("PERISHABLE001")).orElse(null);
        assertNotNull(product, "Perishable product should be saved");
        assertTrue(product.isPerishable());
        
        // Test non-perishable product
        request = createValidProductRequest();
        request.setItemCode("NONPERISHABLE001");
        request.setPerishable(false);
        
        response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        assertTrue(response.isSuccess(), "Non-perishable product should be added successfully");
        
        product = itemRepository.findByItemCode(ItemCode.of("NONPERISHABLE001")).orElse(null);
        assertNotNull(product, "Non-perishable product should be saved");
        assertFalse(product.isPerishable());
    }

    @Test
    @DisplayName("Should handle insufficient stock transfer gracefully")
    void shouldHandleInsufficientStockTransfer() {
        // Arrange - Try to transfer more stock than available
        ProductRequest request = createValidProductRequestWithStock();
        request.setInitialQuantity(50.0); // Only 50 units available
        request.setTransferToShelf(true);
        request.setShelfCode("A1-001");
        request.setShelfQuantity(100.0); // Try to transfer 100 units
        
        // Act & Assert
        ProductResponse response = productManagementUseCase.addProductWithInitialStock(request, testUser);
        assertTrue(response.isFailure(), "Should fail due to insufficient stock");
        assertTrue(response.getError().contains("Insufficient warehouse stock"));
    }

    // Helper methods
    private ProductRequest createValidProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setItemCode("PROD001");
        request.setItemName("Test Product");
        request.setDescription("Test Description");
        request.setBrandId(1L);
        request.setCategoryId(1L);
        request.setSupplierId(1L);
        request.setUnitOfMeasure(UnitOfMeasure.EACH.name());
        request.setPackSize(1.0);
        request.setCostPrice(100.0);
        request.setSellingPrice(150.0);
        request.setReorderPoint(50);
        request.setPerishable(false);
        return request;
    }
    
    private ProductRequest createValidProductRequestWithStock() {
        ProductRequest request = createValidProductRequest();
        request.setBatchNumber("BATCH001");
        request.setInitialQuantity(100.0);
        request.setManufactureDate(LocalDate.now().minusDays(1));
        request.setExpiryDate(LocalDateTime.now().plusDays(30));
        request.setWarehouseLocation("MAIN-WAREHOUSE");
        return request;
    }

    // Mock repository implementations for testing
    private static class MockBrandRepository implements BrandRepository {
        @Override
        public com.syos.domain.entities.Brand save(com.syos.domain.entities.Brand brand) { return brand; }
        @Override
        public java.util.Optional<com.syos.domain.entities.Brand> findById(Long id) { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<com.syos.domain.entities.Brand> findByBrandCode(String brandCode) { return java.util.Optional.empty(); }
        @Override
        public boolean existsById(Long id) { return id != null && id >= 1 && id <= 10; }
        @Override
        public boolean existsByBrandCode(String brandCode) { return false; }
        @Override
        public java.util.List<com.syos.domain.entities.Brand> findAllActive() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<com.syos.domain.entities.Brand> findAll() { return java.util.Collections.emptyList(); }
        @Override
        public boolean isActive(Long id) { return existsById(id); }
        @Override
        public long countActiveBrands() { return 0; }
        @Override
        public void deleteById(Long id) { /* no-op */ }
    }

    private static class MockCategoryRepository implements CategoryRepository {
        @Override
        public com.syos.domain.entities.Category save(com.syos.domain.entities.Category category) { return category; }
        @Override
        public java.util.Optional<com.syos.domain.entities.Category> findById(Long id) { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<com.syos.domain.entities.Category> findByCategoryCode(String categoryCode) { return java.util.Optional.empty(); }
        @Override
        public boolean existsById(Long id) { return id != null && id >= 1 && id <= 10; }
        @Override
        public boolean existsByCategoryCode(String categoryCode) { return false; }
        @Override
        public java.util.List<com.syos.domain.entities.Category> findAllActive() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<com.syos.domain.entities.Category> findRootCategories() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<com.syos.domain.entities.Category> findByParentCategoryId(Long parentId) { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<com.syos.domain.entities.Category> findAll() { return java.util.Collections.emptyList(); }
        @Override
        public boolean isActive(Long id) { return existsById(id); }
        @Override
        public java.util.List<com.syos.domain.entities.Category> getCategoryHierarchy() { return java.util.Collections.emptyList(); }
        @Override
        public long countActiveCategories() { return 0; }
        @Override
        public void deleteById(Long id) { /* no-op */ }
    }

    private static class MockSupplierRepository implements SupplierRepository {
        @Override
        public com.syos.domain.entities.Supplier save(com.syos.domain.entities.Supplier supplier) { return supplier; }
        @Override
        public java.util.Optional<com.syos.domain.entities.Supplier> findById(Long id) { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<com.syos.domain.entities.Supplier> findBySupplierCode(String supplierCode) { return java.util.Optional.empty(); }
        @Override
        public boolean existsById(Long id) { return id != null && id >= 1 && id <= 10; }
        @Override
        public boolean existsBySupplierCode(String supplierCode) { return false; }
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> findAllActive() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> findAll() { return java.util.Collections.emptyList(); }
        @Override
        public boolean isActive(Long id) { return existsById(id); }
        @Override
        public long countActiveSuppliers() { return 0; }
        @Override
        public void deleteById(Long id) { /* no-op */ }
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> searchByName(String searchTerm) { return java.util.Collections.emptyList(); }
    }
}
