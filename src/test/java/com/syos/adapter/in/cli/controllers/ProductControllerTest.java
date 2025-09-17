package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.usecases.browsing.BrowseProductsUseCase;
import com.syos.application.usecases.inventory.AddProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController CLI")
class ProductControllerTest {

    private FakeConsoleIO console;

    @Mock
    private AddProductUseCase addProductUseCase;

    @Mock
    private BrowseProductsUseCase browseProductsUseCase;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        console = new FakeConsoleIO();
        controller = new ProductController(console, addProductUseCase, browseProductsUseCase);
    }

    @Test
    @DisplayName("handleBrowseProducts: option 1 shows all products and formatted list")
    void handleBrowseProducts_option1_showsAll() {
        // Arrange: user selects option 1
        console.enqueueInputs("1");
        List<BrowseProductsUseCase.ProductDisplayDto> products = List.of(
            new BrowseProductsUseCase.ProductDisplayDto(1L, "ITM001", "Coke", "desc", "150", "Each", 1.0, true, false, false),
            new BrowseProductsUseCase.ProductDisplayDto(2L, "ITM002", "Pepsi", "desc", "140", "Each", 1.0, false, true, false)
        );
        when(browseProductsUseCase.getAllProducts())
            .thenReturn(BrowseProductsUseCase.BrowseProductsResponse.success(products, "All products retrieved successfully"));

        // Act
        controller.handleBrowseProducts();

        // Assert
        String out = console.getOutput();
        assertThat(out).contains("=== Browse Products ===");
        assertThat(out).contains("Found 2 product(s):");
        assertThat(out).contains("ITM001");
        assertThat(out).contains("ITM002");
        verify(browseProductsUseCase).getAllProducts();
    }

    @Test
    @DisplayName("handleBrowseProducts: option 4 search flow prints results")
    void handleBrowseProducts_option4_search() {
        // Arrange
        console.enqueueInputs("4", "coke");
        List<BrowseProductsUseCase.ProductDisplayDto> products = List.of(
            new BrowseProductsUseCase.ProductDisplayDto(1L, "ITM001", "Coke", "desc", "150", "Each", 1.0, false, false, false)
        );
        when(browseProductsUseCase.searchProducts("coke"))
            .thenReturn(BrowseProductsUseCase.BrowseProductsResponse.success(products, "Search results for 'coke' retrieved successfully"));

        // Act
        controller.handleBrowseProducts();

        // Assert
        String out = console.getOutput();
        assertThat(out).contains("Search Results");
        assertThat(out).contains("ITM001");
        verify(browseProductsUseCase).searchProducts("coke");
    }

    @Test
    @DisplayName("handleAddProduct: happy path prints success and IDs")
    void handleAddProduct_happyPath() {
        // Arrange: user input sequence for all prompts
        console.enqueueInputs(
            "ITM001",      // Item Code
            "Coke",        // Item Name
            "Cola Drink",  // Description
            "1",           // Brand ID
            "1",           // Category ID
            "1",           // Supplier ID
            "1",           // Unit selection (EACH)
            "1",           // Pack Size
            "100",         // Cost Price
            "150",         // Selling Price
            "50",          // Reorder Point
            "n"            // Perishable
        );

        when(addProductUseCase.execute(any(AddProductUseCase.AddProductRequest.class)))
            .thenReturn(AddProductUseCase.AddProductResponse.success(100L, "ITM001"));

        // Act
        controller.handleAddProduct();

        // Assert
        String out = console.getOutput();
        assertThat(out).contains("Product added successfully");
        assertThat(out).contains("Item ID: 100");
        assertThat(out).contains("Item Code: ITM001");
        verify(addProductUseCase).execute(any(AddProductUseCase.AddProductRequest.class));
    }

    // --- Test Fake Console ---
    private static class FakeConsoleIO implements ConsoleIO {
        private final StringBuilder out = new StringBuilder();
        private final Deque<String> inputs = new ArrayDeque<>();
        private final List<String> errors = new ArrayList<>();

        void enqueueInputs(String... in) {
            for (String s : in) inputs.addLast(s);
        }

        String getOutput() { return out.toString(); }

        @Override
        public void print(String message) { out.append(message); }

        @Override
        public void println(String message) { out.append(message).append(System.lineSeparator()); }

        @Override
        public void println() { out.append(System.lineSeparator()); }

        @Override
        public String readLine() { return inputs.isEmpty() ? "" : inputs.removeFirst(); }

        @Override
        public String readLine(String prompt) { print(prompt); return readLine(); }

        @Override
        public String readPassword() { return readLine(); }

        @Override
        public void printf(String format, Object... args) { out.append(String.format(format, args)); }

        @Override
        public void clear() { /* no-op for tests */ }

        @Override
        public void printError(String message) { errors.add(message); out.append("ERROR: ").append(message).append(System.lineSeparator()); }

        @Override
        public void printSuccess(String message) { out.append("SUCCESS: ").append(message).append(System.lineSeparator()); }

        @Override
        public void printWarning(String message) { out.append("WARNING: ").append(message).append(System.lineSeparator()); }
    }
}
