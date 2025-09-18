package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.application.dto.requests.ProductRequest;
import com.syos.application.dto.responses.ProductResponse;
import com.syos.application.usecases.inventory.CompleteProductManagementUseCase;
import com.syos.domain.entities.Brand;
import com.syos.domain.entities.Category;
import com.syos.domain.entities.Supplier;
import com.syos.domain.valueobjects.UserID;
import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Enhanced Product Controller with complete workflow support
 * Handles: Add Product → Warehouse → Shelf/Web Transfer
 * 
 * Design Patterns Used:
 * - Command Pattern: User input commands
 * - MVC Pattern: Controller in presentation layer
 * - Dependency Injection: Use case dependencies
 */
public class EnhancedProductController {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedProductController.class);
    
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final CompleteProductManagementUseCase productUseCase;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public EnhancedProductController(ConsoleIO console,
                                   SessionManager sessionManager,
                                   CompleteProductManagementUseCase productUseCase,
                                   BrandRepository brandRepository,
                                   CategoryRepository categoryRepository,
                                   SupplierRepository supplierRepository) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.productUseCase = productUseCase;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Main product management menu
     */
    public void showProductManagementMenu() {
        if (!sessionManager.isLoggedIn()) {
            console.printError("Please login to access product management");
            return;
        }

        var userRole = sessionManager.getCurrentUserRole();
        if (!"ADMIN".equals(userRole) && !"EMPLOYEE".equals(userRole)) {
            console.printError("Insufficient privileges. Admin or Employee access required.");
            return;
        }

        while (true) {
            console.println("\n" + "=".repeat(60));
            console.println("           🏪 PRODUCT MANAGEMENT SYSTEM");
            console.println("=".repeat(60));
            console.println("1. 📦 Add New Product with Initial Stock");
            console.println("2. 📈 Receive Additional Stock");
            console.println("3. 🏬 Transfer Stock to Shelf");
            console.println("4. 🌐 Transfer Stock to Web Inventory");
            console.println("5. 📋 View Available Brands/Categories/Suppliers");
            console.println("6. 🔙 Back to Main Menu");
            console.println("=".repeat(60));

            String choice = console.readInput("Select option (1-6): ");

            switch (choice) {
                case "1" -> addNewProductWithStock();
                case "2" -> receiveAdditionalStock();
                case "3" -> transferStockToShelf();
                case "4" -> transferStockToWeb();
                case "5" -> viewReferenceData();
                case "6" -> {
                    console.println("Returning to main menu...");
                    return;
                }
                default -> console.printError("Invalid choice. Please select 1-6.");
            }
        }
    }

    /**
     * Add new product with initial stock workflow
     */
    private void addNewProductWithStock() {
        console.println("\n📦 ADD NEW PRODUCT WITH INITIAL STOCK");
        console.println("=" + "=".repeat(50));

        try {
            ProductRequest request = collectProductDetails();
            if (request == null) return;

            // Collect initial stock details
            collectInitialStockDetails(request);

            // Ask about immediate transfers
            collectTransferOptions(request);

            // Execute the complete workflow
            UserID currentUser = UserID.of(sessionManager.getCurrentUserId());
            ProductResponse response = productUseCase.addProductWithInitialStock(request, currentUser);

            if (response.isSuccess()) {
                console.printSuccess("✅ " + response.getMessage());
                console.println("   Product ID: " + response.getProductId());
                console.println("   Item Code: " + response.getItemCode());
                console.println("   Item Name: " + response.getItemName());
            } else {
                console.printError("❌ " + response.getError());
            }

        } catch (Exception e) {
            logger.error("Error in add new product workflow", e);
            console.printError("Error: " + e.getMessage());
        }
    }

    /**
     * Collect product details from user input
     */
    private ProductRequest collectProductDetails() {
        try {
            ProductRequest request = new ProductRequest();

            // Basic product information
            console.println("\n📝 PRODUCT INFORMATION");
            console.println("-".repeat(30));

            request.setItemCode(console.readInput("Item Code (unique): "));
            if (request.getItemCode() == null || request.getItemCode().trim().isEmpty()) {
                console.printError("Item code is required");
                return null;
            }

            request.setItemName(console.readInput("Item Name: "));
            if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
                console.printError("Item name is required");
                return null;
            }

            request.setDescription(console.readInput("Description (optional): "));

            // Brand selection
            console.println("\n🏷️ BRAND SELECTION");
            console.println("-".repeat(20));
            displayBrands();
            String brandInput = console.readInput("Brand ID: ");
            try {
                request.setBrandId(Long.parseLong(brandInput));
            } catch (NumberFormatException e) {
                console.printError("Invalid brand ID");
                return null;
            }

            // Category selection
            console.println("\n📂 CATEGORY SELECTION");
            console.println("-".repeat(25));
            displayCategories();
            String categoryInput = console.readInput("Category ID: ");
            try {
                request.setCategoryId(Long.parseLong(categoryInput));
            } catch (NumberFormatException e) {
                console.printError("Invalid category ID");
                return null;
            }

            // Supplier selection
            console.println("\n🚚 SUPPLIER SELECTION");
            console.println("-".repeat(25));
            displaySuppliers();
            String supplierInput = console.readInput("Supplier ID: ");
            try {
                request.setSupplierId(Long.parseLong(supplierInput));
            } catch (NumberFormatException e) {
                console.printError("Invalid supplier ID");
                return null;
            }

            // Product specifications
            console.println("\n📏 PRODUCT SPECIFICATIONS");
            console.println("-".repeat(30));

            console.println("Unit of Measure options: EACH, PACK, KG, G, L, ML, BOX");
            request.setUnitOfMeasure(console.readInput("Unit of Measure: ").toUpperCase());

            String packSizeInput = console.readInput("Pack Size (default 1.0): ");
            try {
                request.setPackSize(packSizeInput.isEmpty() ? 1.0 : Double.parseDouble(packSizeInput));
            } catch (NumberFormatException e) {
                console.printError("Invalid pack size");
                return null;
            }

            // Pricing
            console.println("\n💰 PRICING INFORMATION");
            console.println("-".repeat(25));

            String costPriceInput = console.readInput("Cost Price (LKR): ");
            try {
                request.setCostPrice(Double.parseDouble(costPriceInput));
                if (request.getCostPrice() <= 0) {
                    console.printError("Cost price must be positive");
                    return null;
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid cost price");
                return null;
            }

            String sellingPriceInput = console.readInput("Selling Price (LKR): ");
            try {
                request.setSellingPrice(Double.parseDouble(sellingPriceInput));
                if (request.getSellingPrice() < request.getCostPrice()) {
                    console.printError("Selling price must be greater than or equal to cost price");
                    return null;
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid selling price");
                return null;
            }

            // Inventory settings
            console.println("\n📊 INVENTORY SETTINGS");
            console.println("-".repeat(25));

            String reorderInput = console.readInput("Reorder Point (default 50): ");
            try {
                request.setReorderPoint(reorderInput.isEmpty() ? 50 : Integer.parseInt(reorderInput));
            } catch (NumberFormatException e) {
                console.printError("Invalid reorder point");
                return null;
            }

            String perishableInput = console.readInput("Is Perishable? (y/n, default n): ");
            request.setPerishable(perishableInput.toLowerCase().startsWith("y"));

            return request;

        } catch (Exception e) {
            logger.error("Error collecting product details", e);
            console.printError("Error collecting product details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Collect initial stock details
     */
    private void collectInitialStockDetails(ProductRequest request) {
        console.println("\n📦 INITIAL STOCK DETAILS");
        console.println("-".repeat(30));

        request.setBatchNumber(console.readInput("Batch Number: "));
        
        String quantityInput = console.readInput("Initial Quantity: ");
        try {
            request.setInitialQuantity(Double.parseDouble(quantityInput));
            if (request.getInitialQuantity() <= 0) {
                console.printError("Initial quantity must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid quantity");
            return;
        }

        if (request.isPerishable()) {
            console.println("Date format: yyyy-MM-dd (e.g., 2024-12-31)");
            
            String mfgDateInput = console.readInput("Manufacture Date (optional): ");
            if (!mfgDateInput.isEmpty()) {
                try {
                    request.setManufactureDate(LocalDate.parse(mfgDateInput));
                } catch (DateTimeParseException e) {
                    console.printError("Invalid manufacture date format");
                    return;
                }
            }

            String expiryDateInput = console.readInput("Expiry Date (optional): ");
            if (!expiryDateInput.isEmpty()) {
                try {
                    LocalDate expiryDate = LocalDate.parse(expiryDateInput);
                    request.setExpiryDate(expiryDate.atStartOfDay());
                } catch (DateTimeParseException e) {
                    console.printError("Invalid expiry date format");
                    return;
                }
            }
        }

        String warehouseLocation = console.readInput("Warehouse Location (default MAIN-WAREHOUSE): ");
        request.setWarehouseLocation(warehouseLocation.isEmpty() ? "MAIN-WAREHOUSE" : warehouseLocation);
    }

    /**
     * Collect transfer options
     */
    private void collectTransferOptions(ProductRequest request) {
        console.println("\n🔄 IMMEDIATE TRANSFER OPTIONS");
        console.println("-".repeat(35));

        String transferToShelf = console.readInput("Transfer some stock to shelf immediately? (y/n): ");
        if (transferToShelf.toLowerCase().startsWith("y")) {
            request.setTransferToShelf(true);
            request.setShelfCode(console.readInput("Shelf Code: "));
            
            String shelfQty = console.readInput("Quantity for shelf: ");
            try {
                double shelfQuantity = Double.parseDouble(shelfQty);
                if (shelfQuantity > request.getInitialQuantity()) {
                    console.printError("Shelf quantity cannot exceed initial quantity");
                    return;
                }
                request.setShelfQuantity(shelfQuantity);
            } catch (NumberFormatException e) {
                console.printError("Invalid shelf quantity");
                return;
            }
        }

        String transferToWeb = console.readInput("Transfer some stock to web inventory immediately? (y/n): ");
        if (transferToWeb.toLowerCase().startsWith("y")) {
            request.setTransferToWeb(true);
            
            String webQty = console.readInput("Quantity for web: ");
            try {
                double webQuantity = Double.parseDouble(webQty);
                double totalTransferred = request.getShelfQuantity() + webQuantity;
                if (totalTransferred > request.getInitialQuantity()) {
                    console.printError("Total transferred quantity cannot exceed initial quantity");
                    return;
                }
                request.setWebQuantity(webQuantity);
            } catch (NumberFormatException e) {
                console.printError("Invalid web quantity");
                return;
            }
        }
    }

    /**
     * Receive additional stock for existing product
     */
    private void receiveAdditionalStock() {
        console.println("\n📈 RECEIVE ADDITIONAL STOCK");
        console.println("=" + "=".repeat(35));

        String itemCode = console.readInput("Item Code: ");
        if (itemCode == null || itemCode.trim().isEmpty()) {
            console.printError("Item code is required");
            return;
        }

        // Create basic request for additional stock
        ProductRequest request = new ProductRequest();
        collectInitialStockDetails(request);

        if (request.getBatchNumber() == null || request.getInitialQuantity() <= 0) {
            console.printError("Valid batch number and quantity required");
            return;
        }

        UserID currentUser = UserID.of(sessionManager.getCurrentUserId());
        ProductResponse response = productUseCase.receiveStock(itemCode, request, currentUser);

        if (response.isSuccess()) {
            console.printSuccess("✅ " + response.getMessage());
        } else {
            console.printError("❌ " + response.getError());
        }
    }

    /**
     * Transfer stock from warehouse to shelf
     */
    private void transferStockToShelf() {
        console.println("\n🏬 TRANSFER STOCK TO SHELF");
        console.println("=" + "=".repeat(35));

        String itemCode = console.readInput("Item Code: ");
        String shelfCode = console.readInput("Shelf Code: ");
        String quantityInput = console.readInput("Quantity to transfer: ");

        try {
            double quantity = Double.parseDouble(quantityInput);
            UserID currentUser = UserID.of(sessionManager.getCurrentUserId());
            
            ProductResponse response = productUseCase.transferToShelf(itemCode, shelfCode, quantity, currentUser);
            
            if (response.isSuccess()) {
                console.printSuccess("✅ " + response.getMessage());
            } else {
                console.printError("❌ " + response.getError());
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid quantity format");
        }
    }

    /**
     * Transfer stock from warehouse to web inventory
     */
    private void transferStockToWeb() {
        console.println("\n🌐 TRANSFER STOCK TO WEB INVENTORY");
        console.println("=" + "=".repeat(40));

        String itemCode = console.readInput("Item Code: ");
        String quantityInput = console.readInput("Quantity to transfer: ");

        try {
            double quantity = Double.parseDouble(quantityInput);
            UserID currentUser = UserID.of(sessionManager.getCurrentUserId());
            
            ProductResponse response = productUseCase.transferToWeb(itemCode, quantity, currentUser);
            
            if (response.isSuccess()) {
                console.printSuccess("✅ " + response.getMessage());
            } else {
                console.printError("❌ " + response.getError());
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid quantity format");
        }
    }

    /**
     * View reference data (brands, categories, suppliers)
     */
    private void viewReferenceData() {
        console.println("\n📋 REFERENCE DATA");
        console.println("=" + "=".repeat(25));

        console.println("\n1. View Brands");
        console.println("2. View Categories");
        console.println("3. View Suppliers");
        console.println("4. Back");

        String choice = console.readInput("Select option (1-4): ");

        switch (choice) {
            case "1" -> displayBrands();
            case "2" -> displayCategories();
            case "3" -> displaySuppliers();
            case "4" -> { /* Return */ }
            default -> console.printError("Invalid choice");
        }
    }

    // Display helper methods
    private void displayBrands() {
        console.println("\n🏷️ AVAILABLE BRANDS:");
        console.println("-".repeat(30));
        try {
            List<Brand> brands = brandRepository.findAll();
            if (brands.isEmpty()) {
                console.println("No brands available");
            } else {
                for (Brand brand : brands) {
                    console.println(String.format("ID: %d | Code: %s | Name: %s", 
                        brand.getId(), brand.getBrandCode(), brand.getBrandName()));
                }
            }
        } catch (Exception e) {
            console.printError("Error loading brands: " + e.getMessage());
        }
    }

    private void displayCategories() {
        console.println("\n📂 AVAILABLE CATEGORIES:");
        console.println("-".repeat(35));
        try {
            List<Category> categories = categoryRepository.findAll();
            if (categories.isEmpty()) {
                console.println("No categories available");
            } else {
                for (Category category : categories) {
                    String parentInfo = category.getParentCategoryId() != null 
                        ? " (Parent: " + category.getParentCategoryId() + ")" 
                        : "";
                    console.println(String.format("ID: %d | Code: %s | Name: %s%s", 
                        category.getId(), category.getCategoryCode(), category.getCategoryName(), parentInfo));
                }
            }
        } catch (Exception e) {
            console.printError("Error loading categories: " + e.getMessage());
        }
    }

    private void displaySuppliers() {
        console.println("\n🚚 AVAILABLE SUPPLIERS:");
        console.println("-".repeat(35));
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            if (suppliers.isEmpty()) {
                console.println("No suppliers available");
            } else {
                for (Supplier supplier : suppliers) {
                    console.println(String.format("ID: %d | Code: %s | Name: %s | Contact: %s", 
                        supplier.getId(), supplier.getSupplierCode(), supplier.getSupplierName(), 
                        supplier.getContactPerson()));
                }
            }
        } catch (Exception e) {
            console.printError("Error loading suppliers: " + e.getMessage());
        }
    }
}
