package com.syos.integration;

import com.syos.application.dto.requests.ProductRequest;
import com.syos.application.dto.responses.ProductResponse;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.application.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for complete product management workflow
 * Tests: Add Product → Warehouse → Shelf/Web Transfer
 * 
 * Addresses Assignment Requirements:
 * - Demonstrates complete product workflow
 * - Tests all use cases and domain logic
 * - Validates business rules and constraints
 * - Shows clean architecture integration
 */
@DisplayName("Complete Product Management Integration Test")
class CompleteProductManagementIntegrationTest {

    @Mock private ItemMasterFileRepository itemRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private BatchRepository batchRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private ShelfStockRepository shelfStockRepository;
    @Mock private WebInventoryRepository webInventoryRepository;

    private CompleteProductManagementUseCase useCase;
    private UserID testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        useCase = new CompleteProductManagementUseCase(
            itemRepository,
            brandRepository,
            categoryRepository,
            supplierRepository,
            batchRepository,
            warehouseStockRepository,
            shelfStockRepository,
            webInventoryRepository
        );
        
        testUser = UserID.of(1L);
    }

    @Test
    @DisplayName("Should successfully add product with initial stock and transfer to shelf and web")
    void shouldAddProductWithCompleteWorkflow() {
        // Arrange
        ProductRequest request = createValidProductRequest();
        request.setTransferToShelf(true);
        request.setShelfCode("A1-001");
        request.setShelfQuantity(50.0);
        request.setTransferToWeb(true);
        request.setWebQuantity(30.0);

        // Debug: Check if request is valid
        assertTrue(request.isValid(), "Request should be valid: " + request.toString());
        assertTrue(request.hasInitialStock(), "Request should have initial stock");

        // Mock dependencies exist
        when(brandRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByItemCode(any(ItemCode.class))).thenReturn(Optional.empty());
        
        // Mock repository save methods to return saved entities with IDs
        when(itemRepository.save(any(ItemMasterFile.class))).thenAnswer(invocation -> {
            ItemMasterFile item = invocation.getArgument(0);
            return item.withId(1L); // Return with generated ID
        });
        when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> {
            Batch batch = invocation.getArgument(0);
            // Use builder pattern to return batch with ID
            return new Batch.Builder(batch).id(1L).build();
        });
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0); // Return as-is for warehouse stock
        });

        // Act
        ProductResponse response = useCase.addProductWithInitialStock(request, testUser);

        // Assert with detailed error information
        if (response.isFailure()) {
            fail("Product addition should succeed. Error: " + response.getError() + ". Request: " + request.toString());
        }
        assertTrue(response.isSuccess(), "Product addition should succeed");
        assertNotNull(response.getMessage());
        assertEquals("PROD001", response.getItemCode());

        // Verify all save operations were called
        verify(itemRepository, times(1)).save(any(ItemMasterFile.class));
        verify(batchRepository, times(1)).save(any(Batch.class));
        verify(warehouseStockRepository, times(3)).save(any(WarehouseStock.class)); // Initial + 2 transfers
        verify(shelfStockRepository, times(1)).save(any(ShelfStock.class));
    }

    @Test
    @DisplayName("Should validate business rules and reject invalid product data")
    void shouldValidateBusinessRules() {
        // Arrange
        ProductRequest request = createValidProductRequest();
        request.setSellingPrice(5.0); // Less than cost price (10.0)

        // Debug: Check validation expectation
        assertFalse(request.isValid(), "Request should be invalid due to selling price < cost price");

        // Mock dependencies exist
        when(brandRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByItemCode(any(ItemCode.class))).thenReturn(Optional.empty());

        // Act
        ProductResponse response = useCase.addProductWithInitialStock(request, testUser);

        // Assert with detailed information
        assertTrue(response.isFailure(), "Should fail due to invalid selling price. Response: " + response.toString());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Invalid product request") || response.getError().contains("Selling price must be greater than or equal to cost price"), 
            "Error message should mention validation issue. Actual error: " + response.getError());
    }

    @Test
    @DisplayName("Should handle duplicate item code")
    void shouldRejectDuplicateItemCode() {
        // Arrange
        ProductRequest request = createValidProductRequest();
        
        // Mock dependencies
        when(brandRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.existsById(1L)).thenReturn(true);
        
        // Mock existing item
        ItemMasterFile existingItem = createTestItem();
        when(itemRepository.findByItemCode(any(ItemCode.class))).thenReturn(Optional.of(existingItem));

        // Act
        ProductResponse response = useCase.addProductWithInitialStock(request, testUser);

        // Assert
        assertTrue(response.isFailure(), "Should fail due to duplicate item code");
        assertTrue(response.getError().contains("Item code already exists"));
    }

    @Test
    @DisplayName("Should handle missing dependencies")
    void shouldValidateDependencies() {
        // Arrange
        ProductRequest request = createValidProductRequest();
        
        // Mock missing brand
        when(brandRepository.existsById(1L)).thenReturn(false);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.existsById(1L)).thenReturn(true);

        // Act
        ProductResponse response = useCase.addProductWithInitialStock(request, testUser);

        // Assert
        assertTrue(response.isFailure(), "Should fail due to missing brand");
        assertTrue(response.getError().contains("Brand not found"));
    }

    @Test
    @DisplayName("Should successfully receive additional stock for existing product")
    void shouldReceiveAdditionalStock() {
        // Arrange
        String itemCode = "PROD001";
        ProductRequest stockRequest = createStockRequest();
        
        // Debug: Check if stock request is valid
        System.out.println("Stock request details: " + stockRequest.toString());
        System.out.println("Stock request hasInitialStock: " + stockRequest.hasInitialStock());
        
        ItemMasterFile existingItem = createTestItem();
        when(itemRepository.findByItemCode(ItemCode.of(itemCode))).thenReturn(Optional.of(existingItem));
        
        // Mock repository save methods
        when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> {
            Batch batch = invocation.getArgument(0);
            return new Batch.Builder(batch).id(2L).build();
        });
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        ProductResponse response = useCase.receiveStock(itemCode, stockRequest, testUser);

        // Assert with detailed error information
        if (response.isFailure()) {
            fail("Stock receipt should succeed. Error: " + response.getError() + ". Stock request: " + stockRequest.toString());
        }
        assertTrue(response.isSuccess(), "Stock receipt should succeed");
        verify(batchRepository, times(1)).save(any(Batch.class));
        verify(warehouseStockRepository, times(1)).save(any(WarehouseStock.class));
    }

    @Test
    @DisplayName("Should transfer stock from warehouse to shelf with FIFO")
    void shouldTransferStockToShelfWithFIFO() {
        // Arrange
        String itemCode = "PROD001";
        String shelfCode = "A1-001";
        double quantity = 25.0;

        WarehouseStock availableStock = createTestWarehouseStock();
        when(warehouseStockRepository.findAvailableByItemCode(ItemCode.of(itemCode)))
            .thenReturn(java.util.List.of(availableStock));

        // Act
        ProductResponse response = useCase.transferToShelf(itemCode, shelfCode, quantity, testUser);

        // Assert
        assertTrue(response.isSuccess(), "Transfer to shelf should succeed");
        verify(warehouseStockRepository, times(1)).save(any(WarehouseStock.class));
        verify(shelfStockRepository, times(1)).save(any(ShelfStock.class));
    }

    @Test
    @DisplayName("Should prevent transfer of more stock than available")
    void shouldPreventOverTransfer() {
        // Arrange
        String itemCode = "PROD001";
        String shelfCode = "A1-001";
        double quantity = 150.0; // More than available (100.0)

        WarehouseStock availableStock = createTestWarehouseStock();
        when(warehouseStockRepository.findAvailableByItemCode(ItemCode.of(itemCode)))
            .thenReturn(java.util.List.of(availableStock));

        // Act
        ProductResponse response = useCase.transferToShelf(itemCode, shelfCode, quantity, testUser);

        // Assert
        assertTrue(response.isFailure(), "Should fail due to insufficient stock");
        assertTrue(response.getError().contains("Insufficient warehouse stock"));
    }

    @Test
    @DisplayName("Should handle no available warehouse stock")
    void shouldHandleNoAvailableStock() {
        // Arrange
        String itemCode = "PROD001";
        String shelfCode = "A1-001";
        double quantity = 25.0;

        when(warehouseStockRepository.findAvailableByItemCode(ItemCode.of(itemCode)))
            .thenReturn(java.util.List.of()); // Empty list

        // Act
        ProductResponse response = useCase.transferToShelf(itemCode, shelfCode, quantity, testUser);

        // Assert
        assertTrue(response.isFailure(), "Should fail due to no available stock");
        assertTrue(response.getError().contains("No available warehouse stock"));
    }

    // Helper methods to create test data
    private ProductRequest createValidProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setItemCode("PROD001");
        request.setItemName("Test Product");
        request.setDescription("Test Description");
        request.setBrandId(1L);
        request.setCategoryId(1L);
        request.setSupplierId(1L);
        request.setUnitOfMeasure("EACH");
        request.setPackSize(1.0);
        request.setCostPrice(10.0);
        request.setSellingPrice(15.0);
        request.setReorderPoint(50);
        request.setPerishable(true);
        request.setBatchNumber("BATCH001");
        request.setInitialQuantity(100.0);
        request.setManufactureDate(LocalDate.now().minusDays(1));
        request.setExpiryDate(LocalDateTime.now().plusDays(30));
        request.setWarehouseLocation("MAIN-WAREHOUSE");
        return request;
    }

    private ProductRequest createStockRequest() {
        ProductRequest request = new ProductRequest();
        request.setBatchNumber("BATCH002");
        request.setInitialQuantity(50.0);
        request.setManufactureDate(LocalDate.now().minusDays(1));
        request.setExpiryDate(LocalDateTime.now().plusDays(30));
        request.setWarehouseLocation("MAIN-WAREHOUSE");
        request.setCostPrice(10.0);
        return request;
    }

    private ItemMasterFile createTestItem() {
        return ItemMasterFile.createNew(
            ItemCode.of("PROD001"),
            "Test Product",
            "Test Description",
            BrandId.of(1L),
            CategoryId.of(1L),
            SupplierId.of(1L),
            UnitOfMeasure.EACH,
            PackSize.of(1.0),
            Money.of(10.0),
            Money.of(15.0),
            ReorderPoint.of(50),
            true,
            testUser
        ).withId(1L);
    }

    private WarehouseStock createTestWarehouseStock() {
        return WarehouseStock.createNew(
            ItemCode.of("PROD001"),
            1L,
            1L,
            Quantity.of(BigDecimal.valueOf(100.0)),
            LocalDateTime.now().plusDays(30),
            testUser,
            "MAIN-WAREHOUSE"
        );
    }
}
