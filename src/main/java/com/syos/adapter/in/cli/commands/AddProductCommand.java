package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.SupplierRepository;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.domain.entities.Brand;
import com.syos.domain.entities.Category;
import com.syos.domain.entities.Supplier;
import com.syos.shared.enums.UnitOfMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command to handle adding new products to the system
 * Replaces the "This feature will be available soon" placeholder
 */
public class AddProductCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(AddProductCommand.class);

    private final ConsoleIO console;
    private final AddProductUseCase addProductUseCase;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final SessionManager sessionManager;

    public AddProductCommand(ConsoleIO console,
                             AddProductUseCase addProductUseCase,
                             BrandRepository brandRepository,
                             CategoryRepository categoryRepository,
                             SupplierRepository supplierRepository,
                             SessionManager sessionManager) {
        this.console = console;
        this.addProductUseCase = addProductUseCase;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute() {
        try {
            console.println("\n" + "=".repeat(50));
            console.printSuccess("🛒 ADD NEW PRODUCT");
            console.println("=".repeat(50));

            UserSession session = sessionManager.getCurrentSession();
            if (session == null) {
                console.printError("Authentication required to add products");
                return;
            }

            // Collect product information
            AddProductUseCase.AddProductRequest request = collectProductInformation();
            if (request == null) {
                console.printWarning("Product creation cancelled");
                return;
            }

            // Set the user who's creating the product
            request.createdBy(session.getUserId());

            // Execute the use case
            AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);

            // Display result
            if (response.isSuccess()) {
                console.printSuccess("✅ Product added successfully!");
                console.println("Product ID: " + response.getItemId());
                console.println("Item Code: " + response.getItemCode());
                console.println(response.getMessage());
            } else {
                console.printError("❌ Failed to add product: " + response.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error adding product", e);
            console.printError("An error occurred while adding the product: " + e.getMessage());
        }

        console.println("\nPress Enter to continue...");
        console.readLine();
    }

    private AddProductUseCase.AddProductRequest collectProductInformation() {
        try {
            AddProductUseCase.AddProductRequest request = new AddProductUseCase.AddProductRequest();

            // Item Code
            String itemCode = console.readLine("Enter Item Code: ");
            if (itemCode == null || itemCode.trim().isEmpty()) {
                console.printError("Item code is required");
                return null;
            }
            request.itemCode(itemCode.trim());

            // Item Name
            String itemName = console.readLine("Enter Item Name: ");
            if (itemName == null || itemName.trim().isEmpty()) {
                console.printError("Item name is required");
                return null;
            }
            request.itemName(itemName.trim());

            // Description
            String description = console.readLine("Enter Description (optional): ");
            request.description(description != null ? description.trim() : "");

            // Brand Selection
            Long brandId = selectBrand();
            if (brandId == null) return null;
            request.brandId(brandId);

            // Category Selection
            Long categoryId = selectCategory();
            if (categoryId == null) return null;
            request.categoryId(categoryId);

            // Supplier Selection
            Long supplierId = selectSupplier();
            if (supplierId == null) return null;
            request.supplierId(supplierId);

            // Unit of Measure
            UnitOfMeasure unitOfMeasure = selectUnitOfMeasure();
            if (unitOfMeasure == null) return null;
            request.unitOfMeasure(unitOfMeasure);

            // Pack Size
            BigDecimal packSize = readPositiveDecimal("Enter Pack Size: ");
            if (packSize == null) return null;
            request.packSize(packSize);

            // Cost Price
            BigDecimal costPrice = readPositiveDecimal("Enter Cost Price (LKR): ");
            if (costPrice == null) return null;
            request.costPrice(costPrice);

            // Selling Price
            BigDecimal sellingPrice = readPositiveDecimal("Enter Selling Price (LKR): ");
            if (sellingPrice == null) return null;
            if (sellingPrice.compareTo(costPrice) < 0) {
                console.printError("Selling price must be greater than or equal to cost price");
                return null;
            }
            request.sellingPrice(sellingPrice);

            // Reorder Point
            String reorderStr = console.readLine("Enter Reorder Point (default: 50): ");
            int reorderPoint = (reorderStr == null || reorderStr.trim().isEmpty()) ? 50 : Integer.parseInt(reorderStr.trim());
            request.reorderPoint(reorderPoint);

            // Perishable
            String perishableStr = console.readLine("Is this product perishable? (y/N): ");
            boolean isPerishable = perishableStr != null && perishableStr.toLowerCase().startsWith("y");
            request.isPerishable(isPerishable);

            return request;

        } catch (NumberFormatException e) {
            console.printError("Invalid number format");
            return null;
        } catch (Exception e) {
            console.printError("Error collecting product information: " + e.getMessage());
            return null;
        }
    }

    private Long selectBrand() {
        List<Brand> brands = brandRepository.findAllActive();
        if (brands.isEmpty()) {
            console.printWarning("No active brands found.");
            String create = console.readLine("Would you like to create a brand now? (Y/n): ");
            boolean doCreate = create == null || create.trim().isEmpty() || create.trim().toLowerCase().startsWith("y");
            if (doCreate) {
                Long newId = createBrandInline();
                if (newId != null) {
                    return newId;
                }
            }
            console.printError("Please add brands first and try again.");
            return null;
        }

        console.println("\nAvailable Brands:");
        for (int i = 0; i < brands.size(); i++) {
            Brand brand = brands.get(i);
            console.println((i + 1) + ". " + brand.getBrandName());
        }

        String choice = console.readLine("Select Brand (1-" + brands.size() + "): ");
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < brands.size()) {
                return brands.get(index).getId();
            } else {
                console.printError("Invalid selection");
                return null;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid selection");
            return null;
        }
    }

    private Long selectCategory() {
        List<Category> categories = categoryRepository.findAllActive();
        if (categories.isEmpty()) {
            console.printWarning("No active categories found.");
            String create = console.readLine("Would you like to create a category now? (Y/n): ");
            boolean doCreate = create == null || create.trim().isEmpty() || create.trim().toLowerCase().startsWith("y");
            if (doCreate) {
                Long newId = createCategoryInline();
                if (newId != null) {
                    return newId;
                }
            }
            console.printError("Please add categories first and try again.");
            return null;
        }

        console.println("\nAvailable Categories:");
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            console.println((i + 1) + ". " + category.getCategoryName());
        }

        String choice = console.readLine("Select Category (1-" + categories.size() + "): ");
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < categories.size()) {
                return categories.get(index).getId();
            } else {
                console.printError("Invalid selection");
                return null;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid selection");
            return null;
        }
    }

    private Long selectSupplier() {
        List<Supplier> suppliers = supplierRepository.findAllActive();
        if (suppliers.isEmpty()) {
            console.printWarning("No active suppliers found.");
            String create = console.readLine("Would you like to create a supplier now? (Y/n): ");
            boolean doCreate = create == null || create.trim().isEmpty() || create.trim().toLowerCase().startsWith("y");
            if (doCreate) {
                Long newId = createSupplierInline();
                if (newId != null) {
                    return newId;
                }
            }
            console.printError("Please add suppliers first and try again.");
            return null;
        }

        console.println("\nAvailable Suppliers:");
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier supplier = suppliers.get(i);
            console.println((i + 1) + ". " + supplier.getSupplierName());
        }

        String choice = console.readLine("Select Supplier (1-" + suppliers.size() + "): ");
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < suppliers.size()) {
                return suppliers.get(index).getId();
            } else {
                console.printError("Invalid selection");
                return null;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid selection");
            return null;
        }
    }

    private UnitOfMeasure selectUnitOfMeasure() {
        UnitOfMeasure[] units = UnitOfMeasure.values();
        console.println("\nAvailable Units of Measure:");
        for (int i = 0; i < units.length; i++) {
            console.println((i + 1) + ". " + units[i]);
        }

        String choice = console.readLine("Select Unit of Measure (1-" + units.length + "): ");
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < units.length) {
                return units[index];
            } else {
                console.printError("Invalid selection");
                return null;
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid selection");
            return null;
        }
    }

    private BigDecimal readPositiveDecimal(String prompt) {
        String input = console.readLine(prompt);
        try {
            BigDecimal value = new BigDecimal(input);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                console.printError("Value must be positive");
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            console.printError("Invalid decimal format");
            return null;
        }
    }

    // Inline creation helpers to remove hard precondition roadblocks
    private Long createBrandInline() {
        try {
            console.println("\n➕ Create Brand");
            String code = console.readLine("Enter Brand Code (e.g., COCA): ");
            String name = console.readLine("Enter Brand Name: ");
            String desc = console.readLine("Enter Description (optional): ");
            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                console.printError("Brand code and name are required");
                return null;
            }
            Brand brand = Brand.create(code.trim(), name.trim(), desc == null ? "" : desc.trim());
            Brand saved = brandRepository.save(brand);
            console.printSuccess("Brand '" + saved.getBrandName() + "' created.");
            return saved.getId();
        } catch (Exception ex) {
            logger.error("Failed to create brand inline", ex);
            console.printError("Failed to create brand: " + ex.getMessage());
            return null;
        }
    }

    private Long createCategoryInline() {
        try {
            console.println("\n➕ Create Category");
            String code = console.readLine("Enter Category Code (e.g., BEV): ");
            String name = console.readLine("Enter Category Name: ");
            String desc = console.readLine("Enter Description (optional): ");
            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                console.printError("Category code and name are required");
                return null;
            }
            Category category = Category.createRootCategory(code.trim(), name.trim(), desc == null ? "" : desc.trim(), 0);
            Category saved = categoryRepository.save(category);
            console.printSuccess("Category '" + saved.getCategoryName() + "' created.");
            return saved.getId();
        } catch (Exception ex) {
            logger.error("Failed to create category inline", ex);
            console.printError("Failed to create category: " + ex.getMessage());
            return null;
        }
    }

    private Long createSupplierInline() {
        try {
            console.println("\n➕ Create Supplier");
            String code = console.readLine("Enter Supplier Code (e.g., SUP001): ");
            String name = console.readLine("Enter Supplier Name: ");
            String phone = console.readLine("Enter Supplier Phone: ");
            String email = console.readLine("Enter Supplier Email (optional): ");
            String address = console.readLine("Enter Supplier Address (optional): ");
            String contact = console.readLine("Enter Contact Person (optional): ");
            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                console.printError("Supplier code, name, and phone are required");
                return null;
            }
            Supplier supplier = Supplier.create(
                code.trim(), name.trim(), phone.trim(),
                email == null ? "" : email.trim(),
                address == null ? "" : address.trim(),
                contact == null ? "" : contact.trim()
            );
            Supplier saved = supplierRepository.save(supplier);
            console.printSuccess("Supplier '" + saved.getSupplierName() + "' created.");
            return saved.getId();
        } catch (Exception ex) {
            logger.error("Failed to create supplier inline", ex);
            console.printError("Failed to create supplier: " + ex.getMessage());
            return null;
        }
    }
}
