package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.browsing.BrowseProductsUseCase;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.domain.exceptions.DomainException;

import java.math.BigDecimal;

/**
 * Controller for product-related operations in the CLI.
 * 
 * Adapter Layer:
 * - Handles user input and output for product operations
 * - Orchestrates use cases based on user choices
 * - Manages CLI-specific presentation logic
 */
public class ProductController {
    
    private final ConsoleIO consoleIO;
    private final AddProductUseCase addProductUseCase;
    private final BrowseProductsUseCase browseProductsUseCase;

    public ProductController(ConsoleIO consoleIO, 
                           AddProductUseCase addProductUseCase,
                           BrowseProductsUseCase browseProductsUseCase) {
        this.consoleIO = consoleIO;
        this.addProductUseCase = addProductUseCase;
        this.browseProductsUseCase = browseProductsUseCase;
    }

    /**
     * Handle add product workflow
     */
    public void handleAddProduct() {
        consoleIO.println("=== Add New Product ===");
        
        try {
            // Collect product information from user
            String itemCode = consoleIO.readLine("Enter Item Code: ");
            String itemName = consoleIO.readLine("Enter Item Name: ");
            String description = consoleIO.readLine("Enter Description (optional): ");
            
            // For now, use dummy values for foreign keys (will be replaced with proper selection)
            Long brandId = getLongInput("Enter Brand ID: ");
            Long categoryId = getLongInput("Enter Category ID: ");
            Long supplierId = getLongInput("Enter Supplier ID: ");
            
            // Unit of measure selection
            UnitOfMeasure unitOfMeasure = selectUnitOfMeasure();
            
            BigDecimal packSize = getBigDecimalInput("Enter Pack Size: ");
            BigDecimal costPrice = getBigDecimalInput("Enter Cost Price (LKR): ");
            BigDecimal sellingPrice = getBigDecimalInput("Enter Selling Price (LKR): ");
            Integer reorderPoint = getIntegerInput("Enter Reorder Point (default 50): ", 50);
            boolean isPerishable = getBooleanInput("Is this product perishable? (y/n): ");
            
            // Assume current user ID is 1 (will be replaced with proper session management)
            Long createdBy = 1L;
            
            // Create request and execute use case
            AddProductUseCase.AddProductRequest request = new AddProductUseCase.AddProductRequest()
                .itemCode(itemCode)
                .itemName(itemName)
                .description(description.isEmpty() ? null : description)
                .brandId(brandId)
                .categoryId(categoryId)
                .supplierId(supplierId)
                .unitOfMeasure(unitOfMeasure)
                .packSize(packSize)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .reorderPoint(reorderPoint)
                .isPerishable(isPerishable)
                .createdBy(createdBy);
            
            AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
            
            if (response.isSuccess()) {
                consoleIO.println("✓ Product added successfully!");
                consoleIO.println("Item ID: " + response.getItemId());
                consoleIO.println("Item Code: " + response.getItemCode());
            } else {
                consoleIO.println("✗ Failed to add product: " + response.getMessage());
            }
            
        } catch (DomainException e) {
            consoleIO.println("✗ Business Error: " + e.getMessage());
        } catch (Exception e) {
            consoleIO.println("✗ Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Handle browse products workflow
     */
    public void handleBrowseProducts() {
        consoleIO.println("=== Browse Products ===");
        consoleIO.println("1. View All Products");
        consoleIO.println("2. View Featured Products");
        consoleIO.println("3. View Latest Products");
        consoleIO.println("4. Search Products");
        consoleIO.println("5. Back to Main Menu");
        
        int choice = getIntegerInput("Select option (1-5): ", 1);
        
        try {
            switch (choice) {
                case 1:
                    displayAllProducts();
                    break;
                case 2:
                    displayFeaturedProducts();
                    break;
                case 3:
                    displayLatestProducts();
                    break;
                case 4:
                    handleSearchProducts();
                    break;
                case 5:
                    return;
                default:
                    consoleIO.println("Invalid option. Please try again.");
            }
        } catch (Exception e) {
            consoleIO.println("✗ Error browsing products: " + e.getMessage());
        }
    }

    private void displayAllProducts() {
        BrowseProductsUseCase.BrowseProductsResponse response = browseProductsUseCase.getAllProducts();
        displayProductsResponse(response, "All Products");
    }

    private void displayFeaturedProducts() {
        BrowseProductsUseCase.BrowseProductsResponse response = browseProductsUseCase.getFeaturedProducts();
        displayProductsResponse(response, "Featured Products");
    }

    private void displayLatestProducts() {
        BrowseProductsUseCase.BrowseProductsResponse response = browseProductsUseCase.getLatestProducts();
        displayProductsResponse(response, "Latest Products");
    }

    private void handleSearchProducts() {
        String searchTerm = consoleIO.readLine("Enter search term: ");
        BrowseProductsUseCase.BrowseProductsResponse response = browseProductsUseCase.searchProducts(searchTerm);
        displayProductsResponse(response, "Search Results");
    }

    private void displayProductsResponse(BrowseProductsUseCase.BrowseProductsResponse response, String title) {
        consoleIO.println("\n=== " + title + " ===");
        
        if (!response.isSuccess()) {
            consoleIO.println("✗ " + response.getMessage());
            return;
        }
        
        if (response.getProducts().isEmpty()) {
            consoleIO.println("No products found.");
            return;
        }
        
        consoleIO.println("Found " + response.getCount() + " product(s):");
        consoleIO.println();
        
        // Display header
        consoleIO.printf("%-15s %-30s %-15s %-10s %-8s%n", 
            "Code", "Name", "Price (LKR)", "Unit", "Featured");
        consoleIO.println("-".repeat(80));
        
        // Display products
        for (BrowseProductsUseCase.ProductDisplayDto product : response.getProducts()) {
            consoleIO.printf("%-15s %-30s %-15s %-10s %-8s%n",
                product.getItemCode(),
                truncate(product.getItemName(), 30),
                product.getPrice(),
                product.getUnitOfMeasure(),
                product.isFeatured() ? "Yes" : "No"
            );
        }
        
        consoleIO.println();
    }

    // Helper methods for input validation
    private UnitOfMeasure selectUnitOfMeasure() {
        consoleIO.println("\nAvailable Units of Measure:");
        UnitOfMeasure[] units = UnitOfMeasure.values();
        for (int i = 0; i < units.length; i++) {
            consoleIO.printf("%d. %s - %s%n", i + 1, units[i].getDisplayName(), units[i].getDescription());
        }
        
        int choice = getIntegerInput("Select unit (1-" + units.length + "): ", 1);
        if (choice < 1 || choice > units.length) {
            consoleIO.println("Invalid choice. Using 'EACH' as default.");
            return UnitOfMeasure.EACH;
        }
        
        return units[choice - 1];
    }

    private Long getLongInput(String prompt) {
        while (true) {
            try {
                String input = consoleIO.readLine(prompt);
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                consoleIO.println("Please enter a valid number.");
            }
        }
    }

    private BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                String input = consoleIO.readLine(prompt);
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                consoleIO.println("Please enter a valid decimal number.");
            }
        }
    }

    private Integer getIntegerInput(String prompt, Integer defaultValue) {
        try {
            String input = consoleIO.readLine(prompt);
            if (input.trim().isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            if (defaultValue != null) {
                consoleIO.println("Invalid input. Using default value: " + defaultValue);
                return defaultValue;
            }
            consoleIO.println("Please enter a valid integer.");
            return getIntegerInput(prompt, defaultValue);
        }
    }

    private boolean getBooleanInput(String prompt) {
        while (true) {
            String input = consoleIO.readLine(prompt).toLowerCase().trim();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                consoleIO.println("Please enter 'y' for yes or 'n' for no.");
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
