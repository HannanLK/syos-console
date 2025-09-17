package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.browsing.BrowseProductsUseCase;
import com.syos.shared.enums.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ProductController covering terminal interaction and product addition flow.
 * Validates that:
 * 1. Required fields are properly collected from terminal input
 * 2. Product addition workflow works end-to-end
 * 3. Error handling displays appropriate messages
 * 4. Validation feedback is shown to user
 */
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Mock
    private ConsoleIO consoleIO;
    
    @Mock
    private AddProductUseCase addProductUseCase;
    
    @Mock
    private BrowseProductsUseCase browseProductsUseCase;
    
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productController = new ProductController(consoleIO, addProductUseCase, browseProductsUseCase);
    }

    @Test
    @DisplayName("Should collect all required fields from terminal and add product successfully")
    void shouldCollectRequiredFieldsAndAddProduct() {
        // Arrange - Mock terminal input sequence
        when(consoleIO.readLine("Enter Item Code: ")).thenReturn("PROD001");
        when(consoleIO.readLine("Enter Item Name: ")).thenReturn("Test Product");
        when(consoleIO.readLine("Enter Description (optional): ")).thenReturn("Test Description");
        when(consoleIO.readLine("Enter Brand ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Category ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Supplier ID: ")).thenReturn("1");
        when(consoleIO.readLine("Select unit (1-" + UnitOfMeasure.values().length + "): ")).thenReturn("1");
        when(consoleIO.readLine("Enter Pack Size: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Cost Price (LKR): ")).thenReturn("100");
        when(consoleIO.readLine("Enter Selling Price (LKR): ")).thenReturn("150");
        when(consoleIO.readLine("Enter Reorder Point (default 50): ")).thenReturn("50");
        when(consoleIO.readLine("Is this product perishable? (y/n): ")).thenReturn("n");

        // Mock successful product addition
        AddProductUseCase.AddProductResponse successResponse = 
            AddProductUseCase.AddProductResponse.success(1L, "PROD001");
        when(addProductUseCase.execute(any(AddProductUseCase.AddProductRequest.class)))
            .thenReturn(successResponse);

        // Act
        productController.handleAddProduct();

        // Assert - Verify all required prompts were shown
        verify(consoleIO).println("=== Add New Product ===");
        verify(consoleIO).readLine("Enter Item Code: ");
        verify(consoleIO).readLine("Enter Item Name: ");
        verify(consoleIO).readLine("Enter Description (optional): ");
        verify(consoleIO).readLine("Enter Brand ID: ");
        verify(consoleIO).readLine("Enter Category ID: ");
        verify(consoleIO).readLine("Enter Supplier ID: ");
        verify(consoleIO).readLine("Enter Pack Size: ");
        verify(consoleIO).readLine("Enter Cost Price (LKR): ");
        verify(consoleIO).readLine("Enter Selling Price (LKR): ");
        verify(consoleIO).readLine("Enter Reorder Point (default 50): ");
        verify(consoleIO).readLine("Is this product perishable? (y/n): ");

        // Verify success message
        verify(consoleIO).println("✓ Product added successfully!");
        verify(consoleIO).println("Item ID: 1");
        verify(consoleIO).println("Item Code: PROD001");

        // Verify use case was called with correct data
        verify(addProductUseCase).execute(argThat(request -> 
            "PROD001".equals(request.getItemCode()) &&
            "Test Product".equals(request.getItemName()) &&
            "Test Description".equals(request.getDescription()) &&
            request.getBrandId().equals(1L) &&
            request.getCategoryId().equals(1L) &&
            request.getSupplierId().equals(1L) &&
            request.getUnitOfMeasure() == UnitOfMeasure.EACH &&
            request.getPackSize().equals(BigDecimal.ONE) &&
            request.getCostPrice().equals(BigDecimal.valueOf(100)) &&
            request.getSellingPrice().equals(BigDecimal.valueOf(150)) &&
            request.getReorderPoint().equals(50) &&
            !request.isPerishable() &&
            request.getCreatedBy().equals(1L)
        ));
    }

    @Test
    @DisplayName("Should handle default reorder point of 50 when empty input")
    void shouldHandleDefaultReorderPoint() {
        // Arrange
        when(consoleIO.readLine("Enter Item Code: ")).thenReturn("PROD001");
        when(consoleIO.readLine("Enter Item Name: ")).thenReturn("Test Product");
        when(consoleIO.readLine("Enter Description (optional): ")).thenReturn("");
        when(consoleIO.readLine("Enter Brand ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Category ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Supplier ID: ")).thenReturn("1");
        when(consoleIO.readLine("Select unit (1-" + UnitOfMeasure.values().length + "): ")).thenReturn("1");
        when(consoleIO.readLine("Enter Pack Size: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Cost Price (LKR): ")).thenReturn("100");
        when(consoleIO.readLine("Enter Selling Price (LKR): ")).thenReturn("150");
        when(consoleIO.readLine("Enter Reorder Point (default 50): ")).thenReturn(""); // Empty for default
        when(consoleIO.readLine("Is this product perishable? (y/n): ")).thenReturn("n");

        AddProductUseCase.AddProductResponse successResponse = 
            AddProductUseCase.AddProductResponse.success(1L, "PROD001");
        when(addProductUseCase.execute(any())).thenReturn(successResponse);

        // Act
        productController.handleAddProduct();

        // Assert - Verify default reorder point of 50 was used
        verify(addProductUseCase).execute(argThat(request -> 
            request.getReorderPoint().equals(50)
        ));
    }

    @Test
    @DisplayName("Should handle perishable product input correctly")
    void shouldHandlePerishableInput() {
        // Arrange
        when(consoleIO.readLine("Enter Item Code: ")).thenReturn("FRESH001");
        when(consoleIO.readLine("Enter Item Name: ")).thenReturn("Fresh Milk");
        when(consoleIO.readLine("Enter Description (optional): ")).thenReturn("Fresh dairy product");
        when(consoleIO.readLine("Enter Brand ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Category ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Supplier ID: ")).thenReturn("1");
        when(consoleIO.readLine("Select unit (1-" + UnitOfMeasure.values().length + "): ")).thenReturn("5"); // L (Liter)
        when(consoleIO.readLine("Enter Pack Size: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Cost Price (LKR): ")).thenReturn("200");
        when(consoleIO.readLine("Enter Selling Price (LKR): ")).thenReturn("250");
        when(consoleIO.readLine("Enter Reorder Point (default 50): ")).thenReturn("30");
        when(consoleIO.readLine("Is this product perishable? (y/n): ")).thenReturn("y"); // Yes, perishable

        AddProductUseCase.AddProductResponse successResponse = 
            AddProductUseCase.AddProductResponse.success(2L, "FRESH001");
        when(addProductUseCase.execute(any())).thenReturn(successResponse);

        // Act
        productController.handleAddProduct();

        // Assert - Verify perishable flag was set correctly
        verify(addProductUseCase).execute(argThat(request -> 
            request.isPerishable() == true &&
            request.getUnitOfMeasure() == UnitOfMeasure.L &&
            request.getReorderPoint().equals(30)
        ));
    }

    @Test
    @DisplayName("Should display error message when product addition fails")
    void shouldDisplayErrorWhenAdditionFails() {
        // Arrange
        when(consoleIO.readLine("Enter Item Code: ")).thenReturn("DUPLICATE001");
        when(consoleIO.readLine("Enter Item Name: ")).thenReturn("Duplicate Product");
        when(consoleIO.readLine("Enter Description (optional): ")).thenReturn("");
        when(consoleIO.readLine("Enter Brand ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Category ID: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Supplier ID: ")).thenReturn("1");
        when(consoleIO.readLine("Select unit (1-" + UnitOfMeasure.values().length + "): ")).thenReturn("1");
        when(consoleIO.readLine("Enter Pack Size: ")).thenReturn("1");
        when(consoleIO.readLine("Enter Cost Price (LKR): ")).thenReturn("100");
        when(consoleIO.readLine("Enter Selling Price (LKR): ")).thenReturn("150");
        when(consoleIO.readLine("Enter Reorder Point (default 50): ")).thenReturn("50");
        when(consoleIO.readLine("Is this product perishable? (y/n): ")).thenReturn("n");

        // Mock failed product addition
        AddProductUseCase.AddProductResponse failureResponse = 
            AddProductUseCase.AddProductResponse.failure("Item code already exists");
        when(addProductUseCase.execute(any())).thenReturn(failureResponse);

        // Act
        productController.handleAddProduct();

        // Assert - Verify error message was displayed
        verify(consoleIO).println("✗ Failed to add product: Item code already exists");
        verify(consoleIO, never()).println("✓ Product added successfully!");
    }
}
