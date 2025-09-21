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
    private final com.syos.application.usecases.inventory.CompleteProductManagementUseCase productUseCase;

    public AddProductCommand(ConsoleIO console,
                             AddProductUseCase addProductUseCase,
                             BrandRepository brandRepository,
                             CategoryRepository categoryRepository,
                             SupplierRepository supplierRepository,
                             SessionManager sessionManager,
                             com.syos.application.usecases.inventory.CompleteProductManagementUseCase productUseCase) {
        this.console = console;
        this.addProductUseCase = addProductUseCase;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.sessionManager = sessionManager;
        this.productUseCase = productUseCase;
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

            // Build a single ProductRequest to add item AND receive initial stock atomically
            var productReq = new com.syos.application.dto.requests.ProductRequest();
            productReq.setItemCode(request.getItemCode());
            productReq.setItemName(request.getItemName());
            productReq.setDescription(request.getDescription());
            productReq.setBrandId(request.getBrandId());
            productReq.setCategoryId(request.getCategoryId());
            productReq.setSupplierId(request.getSupplierId());
            productReq.setUnitOfMeasure(request.getUnitOfMeasure().name());
            productReq.setPackSize(request.getPackSize().doubleValue());
            productReq.setCostPrice(request.getCostPrice().doubleValue());
            productReq.setSellingPrice(request.getSellingPrice().doubleValue());
            productReq.setReorderPoint(request.getReorderPoint());
            productReq.setPerishable(request.isPerishable());

            // Collect initial stock in the same flow (streamlined, no duplicate cost prompt)
            String batchNumber = console.readLine("Enter Batch Number (leave blank to auto-generate): ");
            if (batchNumber == null || batchNumber.trim().isEmpty()) {
                String codePart = request.getItemCode().trim().toUpperCase();
                String ts = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(java.time.LocalDateTime.now());
                batchNumber = codePart + "-" + ts;
                console.printInfo("Auto-generated batch number: " + batchNumber);
            }
            productReq.setBatchNumber(batchNumber.trim());

            java.math.BigDecimal qty;
            while (true) {
                String qtyStr = console.readLine("Enter Initial Quantity (default 1): ");
                try {
                    if (qtyStr == null || qtyStr.trim().isEmpty()) {
                        qty = java.math.BigDecimal.ONE;
                        break;
                    }
                    qty = new java.math.BigDecimal(qtyStr.trim());
                    if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) { console.printError("Quantity must be positive"); continue; }
                    break;
                } catch (NumberFormatException ex) {
                    console.printError("Invalid quantity");
                }
            }
            productReq.setInitialQuantity(qty.doubleValue());

            // Optional perishable dates
            String perishableAns = console.readLine("Does this batch have expiry? (y/N): ");
            java.time.LocalDate mfg = null;
            java.time.LocalDateTime exp = null;
            if (perishableAns != null && perishableAns.trim().toLowerCase().startsWith("y")) {
                String mfgStr = console.readLine("Manufacture Date (yyyy-MM-dd, optional): ");
                if (mfgStr != null && !mfgStr.trim().isEmpty()) {
                    try { mfg = java.time.LocalDate.parse(mfgStr.trim()); } catch (java.time.format.DateTimeParseException e) { console.printError("Invalid manufacture date. Skipping."); }
                }
                String expStr = console.readLine("Expiry Date (yyyy-MM-dd, optional): ");
                if (expStr != null && !expStr.trim().isEmpty()) {
                    try { exp = java.time.LocalDate.parse(expStr.trim()).atStartOfDay(); } catch (java.time.format.DateTimeParseException e) { console.printError("Invalid expiry date. Skipping."); }
                }
            }
            if (mfg != null) productReq.setManufactureDate(mfg);
            if (exp != null) productReq.setExpiryDate(exp);

            String location = console.readLine("Warehouse Location (default MAIN-WAREHOUSE): ");
            if (location == null || location.trim().isEmpty()) location = "MAIN-WAREHOUSE";
            productReq.setWarehouseLocation(location.trim());

            // IMPORTANT: Use the product's cost price for this batch as well (no duplicate prompt)
            productReq.setCostPrice(request.getCostPrice().doubleValue());

            // Execute atomic add + initial stock
            var productResp = productUseCase.addProductWithInitialStock(productReq, com.syos.domain.valueobjects.UserID.of(session.getUserId()));

            if (productResp.isSuccess()) {
                console.printSuccess("✅ Product added successfully with initial stock!");
                console.println("Item Code: " + productResp.getItemCode());
                console.println(productResp.getMessage());
                console.println("Hint: View it under Warehouse Stock Management → View Warehouse Stock.");
            } else {
                console.printError("❌ Failed to add product: " + productResp.getError());
            }

        } catch (Exception e) {
            logger.error("Error adding product", e);
            console.printError("An error occurred while adding the product: " + e.getMessage());
        }

        console.println("\nPress Enter to continue...");
        console.readLine();
    }

    private void receiveInitialWarehouseStock(String itemCode) {
        try {
            if (sessionManager.getCurrentSession() == null) {
                console.printError("Authentication required");
                return;
            }
            // Collect minimal stock details
            String batchNumber;
            // Allow auto-generation if left blank
            batchNumber = console.readLine("Enter Batch Number (leave blank to auto-generate): ");
            if (batchNumber == null || batchNumber.trim().isEmpty()) {
                String codePart = itemCode != null && !itemCode.trim().isEmpty() ? itemCode.trim().toUpperCase() : "BATCH";
                String ts = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(java.time.LocalDateTime.now());
                batchNumber = codePart + "-" + ts;
                console.printInfo("Auto-generated batch number: " + batchNumber);
            }

            java.math.BigDecimal qty;
            while (true) {
                String qtyStr = console.readLine("Enter Initial Quantity (default 1): ");
                try {
                    if (qtyStr == null || qtyStr.trim().isEmpty()) {
                        qty = java.math.BigDecimal.ONE;
                        break;
                    }
                    qty = new java.math.BigDecimal(qtyStr.trim());
                    if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) { console.printError("Quantity must be positive"); continue; }
                    break;
                } catch (NumberFormatException ex) {
                    console.printError("Invalid quantity");
                }
            }

            // Optional dates
            String perishableAns = console.readLine("Does this batch have expiry? (y/N): ");
            java.time.LocalDate mfg = null;
            java.time.LocalDateTime exp = null;
            if (perishableAns != null && perishableAns.trim().toLowerCase().startsWith("y")) {
                String mfgStr = console.readLine("Manufacture Date (yyyy-MM-dd, optional): ");
                if (mfgStr != null && !mfgStr.trim().isEmpty()) {
                    try { mfg = java.time.LocalDate.parse(mfgStr.trim()); } catch (java.time.format.DateTimeParseException e) { console.printError("Invalid manufacture date. Skipping."); }
                }
                String expStr = console.readLine("Expiry Date (yyyy-MM-dd, optional): ");
                if (expStr != null && !expStr.trim().isEmpty()) {
                    try { exp = java.time.LocalDate.parse(expStr.trim()).atStartOfDay(); } catch (java.time.format.DateTimeParseException e) { console.printError("Invalid expiry date. Skipping."); }
                }
            }

            String location = console.readLine("Warehouse Location (default MAIN-WAREHOUSE): ");
            if (location == null || location.trim().isEmpty()) location = "MAIN-WAREHOUSE";

            var req = new com.syos.application.dto.requests.ProductRequest();
            req.setBatchNumber(batchNumber.trim());
            req.setInitialQuantity(qty.doubleValue());
            if (mfg != null) req.setManufactureDate(mfg);
            if (exp != null) req.setExpiryDate(exp);
            req.setWarehouseLocation(location.trim());

            // Cost per unit for this batch (must be positive)
            java.math.BigDecimal batchCost;
            while (true) {
                String cp = console.readLine("Cost per unit for this batch (LKR): ");
                try {
                    if (cp == null || cp.trim().isEmpty()) {
                        console.printError("Cost per unit is required and must be positive.");
                        continue;
                    }
                    batchCost = new java.math.BigDecimal(cp.trim());
                    if (batchCost.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        console.printError("Cost per unit must be positive.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    console.printError("Invalid amount. Please enter a number like 350 or 115.75");
                }
            }
            req.setCostPrice(batchCost.doubleValue());

            var resp = productUseCase.receiveStock(itemCode, req, com.syos.domain.valueobjects.UserID.of(sessionManager.getCurrentUserId()));
            if (resp.isSuccess()) {
                console.printSuccess("Initial stock received into warehouse.");
            } else {
                console.printError("Failed to receive stock: " + resp.getError());
            }
        } catch (Exception ex) {
            logger.error("Error receiving initial warehouse stock", ex);
            console.printError("Unexpected error: " + ex.getMessage());
        }
    }

    private AddProductUseCase.AddProductRequest collectProductInformation() {
        try {
            AddProductUseCase.AddProductRequest request = new AddProductUseCase.AddProductRequest();

            // Item Code (loop until non-empty)
            String itemCode;
            while (true) {
                itemCode = console.readLine("Enter Item Code: ");
                if (itemCode != null && !itemCode.trim().isEmpty()) break;
                console.printError("Item code is required");
            }
            request.itemCode(itemCode.trim());

            // Item Name (loop until non-empty)
            String itemName;
            while (true) {
                itemName = console.readLine("Enter Item Name: ");
                if (itemName != null && !itemName.trim().isEmpty()) break;
                console.printError("Item name is required");
            }
            request.itemName(itemName.trim());

            // Description
            String description = console.readLine("Enter Description (optional): ");
            request.description(description != null ? description.trim() : "");

            // Brand Selection (loop inside method)
            Long brandId = selectBrand();
            request.brandId(brandId);

            // Category Selection (loop inside method)
            Long categoryId = selectCategory();
            request.categoryId(categoryId);

            // Supplier Selection (loop inside method)
            Long supplierId = selectSupplier();
            request.supplierId(supplierId);

            // Unit of Measure (loops inside method)
            UnitOfMeasure unitOfMeasure = selectUnitOfMeasure();
            request.unitOfMeasure(unitOfMeasure);

            // Pack Size (loop until valid)
            BigDecimal packSize = readPositiveDecimal("Enter Pack Size: ");
            request.packSize(packSize);

            // Cost Price (loop until valid)
            BigDecimal costPrice = readPositiveDecimal("Enter Cost Price (LKR): ");
            request.costPrice(costPrice);

            // Selling Price (loop until >= cost price)
            BigDecimal sellingPrice;
            while (true) {
                sellingPrice = readPositiveDecimal("Enter Selling Price (LKR): ");
                if (sellingPrice.compareTo(costPrice) < 0) {
                    console.printError("Selling price must be greater than or equal to cost price");
                    continue;
                }
                break;
            }
            request.sellingPrice(sellingPrice);

            // Reorder Point (loop until integer or blank)
            while (true) {
                String reorderStr = console.readLine("Enter Reorder Point (default: 50): ");
                if (reorderStr == null || reorderStr.trim().isEmpty()) {
                    request.reorderPoint(50);
                    break;
                }
                try {
                    int reorderPoint = Integer.parseInt(reorderStr.trim());
                    request.reorderPoint(reorderPoint);
                    break;
                } catch (NumberFormatException nf) {
                    console.printError("Invalid number format. Please enter a whole number or leave blank for 50.");
                }
            }

            // Perishable
            String perishableStr = console.readLine("Is this product perishable? (y/N): ");
            boolean isPerishable = perishableStr != null && perishableStr.toLowerCase().startsWith("y");
            request.isPerishable(isPerishable);

            return request;

        } catch (Exception e) {
            console.printError("Error collecting product information: " + e.getMessage());
            return null;
        }
    }

    private Long selectBrand() {
        while (true) {
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
                // loop again to show prompt after potential creation failure
                continue;
            }

            console.println("\nAvailable Brands:");
            for (Brand brand : brands) {
                console.println(String.format("ID: %d | Code: %s | Name: %s",
                        brand.getId(), brand.getBrandCode(), brand.getBrandName()));
            }

            String addNew = console.readLine("Brand not listed? Add new (y/n): ");
            if (addNew != null && addNew.trim().toLowerCase().startsWith("y")) {
                Long newId = createBrandInline();
                if (newId != null) {
                    return newId;
                }
                // If creation failed, continue loop to list again
                continue;
            }

            String idInput = console.readLine("Enter Brand ID: ");
            try {
                Long id = Long.parseLong(idInput.trim());
                if (brandRepository.findById(id).isPresent()) {
                    return id;
                } else {
                    console.printError("Invalid brand ID");
                    continue; // prompt again
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid brand ID");
                continue; // prompt again
            }
        }
    }

    private Long selectCategory() {
        while (true) {
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
                continue;
            }

            console.println("\nAvailable Categories:");
            for (Category category : categories) {
                console.println(String.format("ID: %d | Code: %s | Name: %s",
                        category.getId(), category.getCategoryCode(), category.getCategoryName()));
            }

            String addNew = console.readLine("Category not listed? Add new (y/n): ");
            if (addNew != null && addNew.trim().toLowerCase().startsWith("y")) {
                Long newId = createCategoryInline();
                if (newId != null) {
                    return newId;
                }
                continue;
            }

            String idInput = console.readLine("Enter Category ID: ");
            try {
                Long id = Long.parseLong(idInput.trim());
                if (categoryRepository.findById(id).isPresent()) {
                    return id;
                } else {
                    console.printError("Invalid category ID");
                    continue; // prompt again
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid category ID");
                continue; // prompt again
            }
        }
    }

    private Long selectSupplier() {
        while (true) {
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
                continue;
            }

            console.println("\nAvailable Suppliers:");
            for (Supplier supplier : suppliers) {
                console.println(String.format("ID: %d | Code: %s | Name: %s | Contact: %s",
                        supplier.getId(), supplier.getSupplierCode(), supplier.getSupplierName(),
                        supplier.getContactPerson()));
            }

            String addNew = console.readLine("Supplier not listed? Add new (y/n): ");
            if (addNew != null && addNew.trim().toLowerCase().startsWith("y")) {
                Long newId = createSupplierInline();
                if (newId != null) {
                    return newId;
                }
                continue;
            }

            String idInput = console.readLine("Enter Supplier ID: ");
            try {
                Long id = Long.parseLong(idInput.trim());
                if (supplierRepository.findById(id).isPresent()) {
                    return id;
                } else {
                    console.printError("Invalid supplier ID");
                    continue; // prompt again
                }
            } catch (NumberFormatException e) {
                console.printError("Invalid supplier ID");
                continue; // prompt again
            }
        }
    }

    private UnitOfMeasure selectUnitOfMeasure() {
        UnitOfMeasure[] units = UnitOfMeasure.values();
        while (true) {
            console.println("\nAvailable Units of Measure:");
            for (int i = 0; i < units.length; i++) {
                console.println((i + 1) + ". " + units[i]);
            }
            String choice = console.readLine("Select Unit of Measure (1-" + units.length + "): ");
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < units.length) {
                    return units[index];
                }
            } catch (NumberFormatException e) {
                // fall through to error
            }
            console.printError("Invalid selection. Please enter a number between 1 and " + units.length + ".");
        }
    }

    private BigDecimal readPositiveDecimal(String prompt) {
        while (true) {
            String input = console.readLine(prompt);
            try {
                BigDecimal value = new BigDecimal(input);
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    console.printError("Value must be positive");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                console.printError("Invalid decimal format");
            }
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
