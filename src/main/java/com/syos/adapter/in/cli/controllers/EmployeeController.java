package com.syos.adapter.in.cli.controllers;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.ports.out.*;
import com.syos.domain.entities.User;
import com.syos.domain.entities.Brand;
import com.syos.domain.entities.Category;
import com.syos.domain.entities.Supplier;
import com.syos.shared.enums.UnitOfMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Employee Controller for SYOS System
 * 
 * Handles all employee functions including:
 * - POS System
 * - Inventory Management (including Add Product feature)
 * - Customer Service
 * - Reports & Analytics
 * 
 * Note: SYNEX Points are NOT included as this is customer-specific functionality
 */
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    
    private final ConsoleIO console;
    private final SessionManager sessionManager;
    private final AddProductUseCase addProductUseCase;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public EmployeeController(ConsoleIO console, 
                            SessionManager sessionManager,
                            AddProductUseCase addProductUseCase,
                            BrandRepository brandRepository,
                            CategoryRepository categoryRepository,
                            SupplierRepository supplierRepository) {
        this.console = console;
        this.sessionManager = sessionManager;
        this.addProductUseCase = addProductUseCase;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Display the main employee dashboard
     * Note: No SYNEX Points functionality as this is customer-specific
     */
    public void displayEmployeeDashboard() {
        UserSession currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            console.printError("No active session. Please login again.");
            return;
        }
        
        logger.info("Employee {} accessing dashboard", currentSession.getUsername());
        
        console.println("\n" + "=".repeat(50));
        console.printSuccess("👨‍💼 EMPLOYEE DASHBOARD");
        console.println("=".repeat(50));
        console.println("Welcome, " + currentSession.getName() + "!");
        console.println();
        
        // Display menu options (NO SYNEX Points)
        console.println("📋 Available Functions:");
        List<String> menuOptions = Arrays.asList(
            "1. POS System",
            "2. Inventory Management", 
            "3. Customer Service",
            "4. Reports & Analytics",
            "5. Personal Purchase Mode",
            "6. Account Settings",
            "7. Logout"
            // NOTE: SYNEX Points options REMOVED - customer-specific functionality
        );
        
        menuOptions.forEach(console::println);
        console.println("=".repeat(50));
    }

    /**
     * Handle employee menu selection
     */
    public void handleMenuSelection(int choice) {
        logger.info("Employee menu selection: {}", choice);
        
        switch (choice) {
            case 1: handlePOSSystem(); break;
            case 2: handleInventoryManagement(); break;
            case 3: handleCustomerService(); break;
            case 4: handleReportsAnalytics(); break;
            case 5: handlePersonalPurchaseMode(); break;
            case 6: handleAccountSettings(); break;
            case 7: handleLogout(); break;
            default: 
                console.printError("❌ Invalid selection. Please choose 1-7.");
                break;
        }
    }

    private void handlePOSSystem() {
        console.printInfo("🛒 POS System - Feature coming soon...");
    }

    /**
     * Handle inventory management including Add Product feature
     */
    private void handleInventoryManagement() {
        console.println("\n📦 INVENTORY MANAGEMENT");
        console.println("1. Add New Product");  // This is the implemented feature
        console.println("2. Update Product Information");
        console.println("3. View Product Details");
        console.println("4. Manage Product Categories");
        console.println("5. Manage Brands");
        console.println("6. Product Status (Active/Inactive)");
        console.println("7. Back to Employee Dashboard");
        
        int choice = console.readInt("Select option: ");
        
        switch (choice) {
            case 1: handleAddProduct(); break;  // Implemented feature
            case 2: console.printInfo("Feature coming soon..."); break;
            case 3: console.printInfo("Feature coming soon..."); break;
            case 4: console.printInfo("Feature coming soon..."); break;
            case 5: console.printInfo("Feature coming soon..."); break;
            case 6: console.printInfo("Feature coming soon..."); break;
            case 7: return;
            default: console.printError("Invalid selection");
        }
    }

    /**
     * Handle Add Product feature - FULLY IMPLEMENTED
     * This replaces the "This feature will be available soon" message
     */
    private void handleAddProduct() {
        console.println("\n" + "=".repeat(60));
        console.printSuccess("🆕 ADD NEW PRODUCT");
        console.println("=".repeat(60));
        console.println("Please enter product details:");
        console.println();
        
        try {
            // Collect product information with validation
            AddProductUseCase.AddProductRequest request = collectProductInformation();
            
            if (request == null) {
                console.printWarning("Product addition cancelled.");
                return;
            }
            
            // Confirm before saving
            if (confirmProductAddition(request)) {
                // Execute the use case
                AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);
                
                if (response.isSuccess()) {
                    console.printSuccess("✅ Product added successfully!");
                    console.println("Product ID: " + response.getItemId());
                    console.println("Item Code: " + response.getItemCode());
                    
                    logger.info("Employee {} added new product: {} (ID: {})", 
                        sessionManager.getCurrentSession().getUsername(),
                        request.getItemCode(), 
                        response.getItemId());
                    
                    // Offer additional actions
                    offerPostAdditionActions(response);
                } else {
                    console.printError("❌ Failed to add product: " + response.getMessage());
                }
            } else {
                console.printInfo("Product addition cancelled.");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error adding product: {}", e.getMessage());
            console.printError("❌ Validation Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding product", e);
            console.printError("❌ An error occurred while adding the product. Please try again.");
        }
    }

    /**
     * Collect all product information from user input
     */
    private AddProductUseCase.AddProductRequest collectProductInformation() {
        try {
            AddProductUseCase.AddProductRequest request = new AddProductUseCase.AddProductRequest();
            
            // Item Code
            String itemCode = console.readString("Item Code (e.g., PROD001): ");
            if (itemCode == null || itemCode.trim().isEmpty()) {
                console.printError("Item code is required");
                return null;
            }
            request.itemCode(itemCode.trim().toUpperCase());
            
            // Item Name
            String itemName = console.readString("Product Name: ");
            if (itemName == null || itemName.trim().isEmpty()) {
                console.printError("Product name is required");
                return null;
            }
            request.itemName(itemName.trim());
            
            // Description (Optional)
            String description = console.readString("Description (optional): ");
            request.description(description != null ? description.trim() : "");
            
            // Category Selection
            Long categoryId = selectCategory();
            if (categoryId == null) return null;
            request.categoryId(categoryId);
            
            // Brand Selection
            Long brandId = selectBrand();
            if (brandId == null) return null;
            request.brandId(brandId);
            
            // Supplier Selection
            Long supplierId = selectSupplier();
            if (supplierId == null) return null;
            request.supplierId(supplierId);
            
            // Unit of Measure
            UnitOfMeasure unitOfMeasure = selectUnitOfMeasure();
            if (unitOfMeasure == null) return null;
            request.unitOfMeasure(unitOfMeasure);
            
            // Pack Size (Optional)
            String packSizeStr = console.readString("Pack Size (optional, e.g., 500g, 1L): ");
            if (packSizeStr != null && !packSizeStr.trim().isEmpty()) {
                try {
                    BigDecimal packSize = new BigDecimal(packSizeStr.replaceAll("[^0-9.]", ""));
                    request.packSize(packSize);
                } catch (NumberFormatException e) {
                    console.printWarning("Invalid pack size format, setting to 1.0");
                    request.packSize(BigDecimal.ONE);
                }
            } else {
                request.packSize(BigDecimal.ONE);
            }
            
            // Cost Price
            BigDecimal costPrice = readPrice("Cost Price (LKR): ");
            if (costPrice == null) return null;
            request.costPrice(costPrice);
            
            // Selling Price
            BigDecimal sellingPrice = readPrice("Selling Price (LKR): ");
            if (sellingPrice == null) return null;
            if (sellingPrice.compareTo(costPrice) < 0) {
                console.printWarning("⚠️ Warning: Selling price is lower than cost price!");
                boolean confirm = console.readBoolean("Continue anyway? (y/n): ");
                if (!confirm) return null;
            }
            request.sellingPrice(sellingPrice);
            
            // Reorder Point
            Integer reorderPoint = console.readInt("Reorder Point (minimum stock level): ");
            if (reorderPoint == null || reorderPoint < 1) {
                console.printError("Reorder point must be at least 1");
                return null;
            }
            request.reorderPoint(reorderPoint);
            
            // Is Perishable
            boolean isPerishable = console.readBoolean("Is this product perishable? (y/n): ");
            request.isPerishable(isPerishable);
            
            // Set created by current user
            request.createdBy(sessionManager.getCurrentSession().getUserId());
            
            return request;
            
        } catch (Exception e) {
            logger.error("Error collecting product information", e);
            console.printError("Error collecting product information: " + e.getMessage());
            return null;
        }
    }

    private Long selectCategory() {
        try {
            console.println("\n📂 Available Categories:");
            List<Category> categories = categoryRepository.findAll();
            
            if (categories.isEmpty()) {
                console.printError("No categories available. Please contact admin to add categories.");
                return null;
            }
            
            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                console.println((i + 1) + ". " + category.getCategoryName());
            }
            
            int choice = console.readInt("Select Category (1-" + categories.size() + "): ");
            if (choice < 1 || choice > categories.size()) {
                console.printError("Invalid category selection");
                return null;
            }
            
            Category selected = categories.get(choice - 1);
            console.printInfo("Selected: " + selected.getCategoryName());
            return selected.getId();
            
        } catch (Exception e) {
            logger.error("Error selecting category", e);
            console.printError("Error loading categories");
            return null;
        }
    }

    private Long selectBrand() {
        try {
            console.println("\n🏷️ Available Brands:");
            List<Brand> brands = brandRepository.findAll();
            
            if (brands.isEmpty()) {
                console.printError("No brands available. Please contact admin to add brands.");
                return null;
            }
            
            for (int i = 0; i < brands.size(); i++) {
                Brand brand = brands.get(i);
                console.println((i + 1) + ". " + brand.getBrandName());
            }
            
            int choice = console.readInt("Select Brand (1-" + brands.size() + "): ");
            if (choice < 1 || choice > brands.size()) {
                console.printError("Invalid brand selection");
                return null;
            }
            
            Brand selected = brands.get(choice - 1);
            console.printInfo("Selected: " + selected.getBrandName());
            return selected.getId();
            
        } catch (Exception e) {
            logger.error("Error selecting brand", e);
            console.printError("Error loading brands");
            return null;
        }
    }

    private Long selectSupplier() {
        try {
            console.println("\n🚚 Available Suppliers:");
            List<Supplier> suppliers = supplierRepository.findAll();
            
            if (suppliers.isEmpty()) {
                console.printError("No suppliers available. Please contact admin to add suppliers.");
                return null;
            }
            
            for (int i = 0; i < suppliers.size(); i++) {
                Supplier supplier = suppliers.get(i);
                console.println((i + 1) + ". " + supplier.getSupplierName());
            }
            
            int choice = console.readInt("Select Supplier (1-" + suppliers.size() + "): ");
            if (choice < 1 || choice > suppliers.size()) {
                console.printError("Invalid supplier selection");
                return null;
            }
            
            Supplier selected = suppliers.get(choice - 1);
            console.printInfo("Selected: " + selected.getSupplierName());
            return selected.getId();
            
        } catch (Exception e) {
            logger.error("Error selecting supplier", e);
            console.printError("Error loading suppliers");
            return null;
        }
    }

    private UnitOfMeasure selectUnitOfMeasure() {
        console.println("\n📏 Available Units of Measure:");
        UnitOfMeasure[] units = UnitOfMeasure.values();
        
        for (int i = 0; i < units.length; i++) {
            console.println((i + 1) + ". " + units[i].name());
        }
        
        int choice = console.readInt("Select Unit of Measure (1-" + units.length + "): ");
        if (choice < 1 || choice > units.length) {
            console.printError("Invalid unit selection");
            return null;
        }
        
        UnitOfMeasure selected = units[choice - 1];
        console.printInfo("Selected: " + selected.name());
        return selected;
    }

    private BigDecimal readPrice(String prompt) {
        try {
            String priceStr = console.readString(prompt);
            if (priceStr == null || priceStr.trim().isEmpty()) {
                console.printError("Price is required");
                return null;
            }
            
            BigDecimal price = new BigDecimal(priceStr.trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                console.printError("Price must be greater than zero");
                return null;
            }
            
            return price;
        } catch (NumberFormatException e) {
            console.printError("Invalid price format. Please enter a valid number.");
            return null;
        }
    }

    private boolean confirmProductAddition(AddProductUseCase.AddProductRequest request) {
        console.println("\n" + "=".repeat(60));
        console.printSuccess("🔍 CONFIRM PRODUCT DETAILS");
        console.println("=".repeat(60));
        console.println("Item Code: " + request.getItemCode());
        console.println("Product Name: " + request.getItemName());
        console.println("Description: " + request.getDescription());
        console.println("Unit of Measure: " + request.getUnitOfMeasure());
        console.println("Pack Size: " + request.getPackSize());
        console.println("Cost Price: LKR " + request.getCostPrice());
        console.println("Selling Price: LKR " + request.getSellingPrice());
        console.println("Reorder Point: " + request.getReorderPoint());
        console.println("Perishable: " + (request.isPerishable() ? "Yes" : "No"));
        console.println("=".repeat(60));
        
        return console.readBoolean("Add this product to the system? (y/n): ");
    }

    private void offerPostAdditionActions(AddProductUseCase.AddProductResponse response) {
        console.println("\n📋 What would you like to do next?");
        console.println("1. Add Another Product");
        console.println("2. View Product Details");
        console.println("3. Return to Inventory Management");
        console.println("4. Return to Employee Dashboard");
        
        int choice = console.readInt("Select option: ");
        
        switch (choice) {
            case 1: 
                console.println(); 
                handleAddProduct(); 
                break;
            case 2: 
                console.printInfo("View Product Details - Feature coming soon..."); 
                break;
            case 3: 
                handleInventoryManagement(); 
                break;
            case 4: 
                return;
            default: 
                console.printWarning("Invalid selection, returning to dashboard...");
                break;
        }
    }

    private void handleCustomerService() {
        console.printInfo("👥 Customer Service - Feature coming soon...");
    }

    private void handleReportsAnalytics() {
        console.printInfo("📊 Reports & Analytics - Feature coming soon...");
    }

    private void handlePersonalPurchaseMode() {
        console.printInfo("🛍️ Personal Purchase Mode - Feature coming soon...");
    }

    private void handleAccountSettings() {
        console.printInfo("⚙️ Account Settings - Feature coming soon...");
    }

    private void handleLogout() {
        UserSession sess = sessionManager.getCurrentSession();
        String uname = (sess != null) ? sess.getUsername() : "unknown";
        logger.info("Employee {} logging out", uname);
        
        sessionManager.clearSession();
        console.printSuccess("👋 Logged out successfully. Thank you!");
    }
}
