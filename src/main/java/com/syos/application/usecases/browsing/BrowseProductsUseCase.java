package com.syos.application.usecases.browsing;

import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.BrandRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.Category;
import com.syos.domain.entities.Brand;
import com.syos.domain.valueobjects.CategoryId;
import com.syos.domain.valueobjects.BrandId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for browsing products in the SYOS system.
 * 
 * Addresses Scenario Requirements:
 * - Browse Products functionality
 * - View all products (categorized dynamically)
 * - View by category with sub-categories
 * - Featured section display
 * - Latest products display
 * 
 * Clean Architecture:
 * - Orchestrates domain entities through repository interfaces
 * - Contains business logic for product browsing workflows
 */
public class BrowseProductsUseCase {
    
    private final ItemMasterFileRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public BrowseProductsUseCase(ItemMasterFileRepository itemRepository,
                                CategoryRepository categoryRepository,
                                BrandRepository brandRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    /**
     * Get all active products with their details
     */
    public BrowseProductsResponse getAllProducts() {
        List<ItemMasterFile> items = itemRepository.findAllActive();
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, "All products retrieved successfully");
    }

    /**
     * Get products by category (including subcategories)
     */
    public BrowseProductsResponse getProductsByCategory(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            return BrowseProductsResponse.failure("Category not found");
        }

        List<ItemMasterFile> items = itemRepository.findByCategory(CategoryId.of(categoryId));
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, 
            "Products in category '" + category.get().getCategoryName() + "' retrieved successfully");
    }

    /**
     * Get products by brand
     */
    public BrowseProductsResponse getProductsByBrand(Long brandId) {
        Optional<Brand> brand = brandRepository.findById(brandId);
        if (brand.isEmpty()) {
            return BrowseProductsResponse.failure("Brand not found");
        }

        List<ItemMasterFile> items = itemRepository.findByBrand(BrandId.of(brandId));
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, 
            "Products for brand '" + brand.get().getBrandName() + "' retrieved successfully");
    }

    /**
     * Get featured products
     */
    public BrowseProductsResponse getFeaturedProducts() {
        List<ItemMasterFile> items = itemRepository.findFeaturedItems();
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, "Featured products retrieved successfully");
    }

    /**
     * Get latest products
     */
    public BrowseProductsResponse getLatestProducts() {
        List<ItemMasterFile> items = itemRepository.findLatestItems();
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, "Latest products retrieved successfully");
    }

    /**
     * Search products by name
     */
    public BrowseProductsResponse searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return BrowseProductsResponse.failure("Search term cannot be empty");
        }

        List<ItemMasterFile> items = itemRepository.searchByName(searchTerm.trim());
        List<ProductDisplayDto> products = items.stream()
            .map(this::mapToDisplayDto)
            .collect(Collectors.toList());

        return BrowseProductsResponse.success(products, 
            "Search results for '" + searchTerm + "' retrieved successfully");
    }

    /**
     * Get all categories for navigation
     */
    public CategoryNavigationResponse getCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        List<CategoryDisplayDto> categoryDtos = rootCategories.stream()
            .map(this::mapToCategoryDto)
            .collect(Collectors.toList());

        return CategoryNavigationResponse.success(categoryDtos);
    }

    /**
     * Get subcategories for a parent category
     */
    public CategoryNavigationResponse getSubCategories(Long parentCategoryId) {
        List<Category> subCategories = categoryRepository.findByParentCategoryId(parentCategoryId);
        List<CategoryDisplayDto> categoryDtos = subCategories.stream()
            .map(this::mapToCategoryDto)
            .collect(Collectors.toList());

        return CategoryNavigationResponse.success(categoryDtos);
    }

    /**
     * Get all brands for filtering
     */
    public BrandNavigationResponse getBrands() {
        List<Brand> brands = brandRepository.findAllActive();
        List<BrandDisplayDto> brandDtos = brands.stream()
            .map(this::mapToBrandDto)
            .collect(Collectors.toList());

        return BrandNavigationResponse.success(brandDtos);
    }

    // Private helper methods for mapping
    private ProductDisplayDto mapToDisplayDto(ItemMasterFile item) {
        return new ProductDisplayDto(
            item.getId(),
            item.getItemCode().getValue(),
            item.getItemName(),
            item.getDescription(),
            item.getSellingPrice().toDisplayString(),
            item.getUnitOfMeasure().getDisplayName(),
            item.getPackSize().getDoubleValue(),
            item.isFeatured(),
            item.isLatest(),
            item.isPerishable()
        );
    }

    private CategoryDisplayDto mapToCategoryDto(Category category) {
        return new CategoryDisplayDto(
            category.getId(),
            category.getCategoryCode(),
            category.getCategoryName(),
            category.getDescription(),
            category.getParentCategoryId(),
            category.isRootCategory()
        );
    }

    private BrandDisplayDto mapToBrandDto(Brand brand) {
        return new BrandDisplayDto(
            brand.getId(),
            brand.getBrandCode(),
            brand.getBrandName(),
            brand.getDescription()
        );
    }

    // Response DTOs
    public static class BrowseProductsResponse {
        private final boolean success;
        private final List<ProductDisplayDto> products;
        private final String message;

        private BrowseProductsResponse(boolean success, List<ProductDisplayDto> products, String message) {
            this.success = success;
            this.products = products;
            this.message = message;
        }

        public static BrowseProductsResponse success(List<ProductDisplayDto> products, String message) {
            return new BrowseProductsResponse(true, products, message);
        }

        public static BrowseProductsResponse failure(String message) {
            return new BrowseProductsResponse(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public List<ProductDisplayDto> getProducts() { return products; }
        public String getMessage() { return message; }
        public int getCount() { return products != null ? products.size() : 0; }
    }

    public static class CategoryNavigationResponse {
        private final boolean success;
        private final List<CategoryDisplayDto> categories;
        private final String message;

        private CategoryNavigationResponse(boolean success, List<CategoryDisplayDto> categories, String message) {
            this.success = success;
            this.categories = categories;
            this.message = message;
        }

        public static CategoryNavigationResponse success(List<CategoryDisplayDto> categories) {
            return new CategoryNavigationResponse(true, categories, "Categories retrieved successfully");
        }

        public static CategoryNavigationResponse failure(String message) {
            return new CategoryNavigationResponse(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public List<CategoryDisplayDto> getCategories() { return categories; }
        public String getMessage() { return message; }
    }

    public static class BrandNavigationResponse {
        private final boolean success;
        private final List<BrandDisplayDto> brands;
        private final String message;

        private BrandNavigationResponse(boolean success, List<BrandDisplayDto> brands, String message) {
            this.success = success;
            this.brands = brands;
            this.message = message;
        }

        public static BrandNavigationResponse success(List<BrandDisplayDto> brands) {
            return new BrandNavigationResponse(true, brands, "Brands retrieved successfully");
        }

        public static BrandNavigationResponse failure(String message) {
            return new BrandNavigationResponse(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public List<BrandDisplayDto> getBrands() { return brands; }
        public String getMessage() { return message; }
    }

    // Display DTOs
    public static class ProductDisplayDto {
        private final Long id;
        private final String itemCode;
        private final String itemName;
        private final String description;
        private final String price;
        private final String unitOfMeasure;
        private final double packSize;
        private final boolean isFeatured;
        private final boolean isLatest;
        private final boolean isPerishable;

        public ProductDisplayDto(Long id, String itemCode, String itemName, String description,
                                String price, String unitOfMeasure, double packSize,
                                boolean isFeatured, boolean isLatest, boolean isPerishable) {
            this.id = id;
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.description = description;
            this.price = price;
            this.unitOfMeasure = unitOfMeasure;
            this.packSize = packSize;
            this.isFeatured = isFeatured;
            this.isLatest = isLatest;
            this.isPerishable = isPerishable;
        }

        // Getters
        public Long getId() { return id; }
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getDescription() { return description; }
        public String getPrice() { return price; }
        public String getUnitOfMeasure() { return unitOfMeasure; }
        public double getPackSize() { return packSize; }
        public boolean isFeatured() { return isFeatured; }
        public boolean isLatest() { return isLatest; }
        public boolean isPerishable() { return isPerishable; }
    }

    public static class CategoryDisplayDto {
        private final Long id;
        private final String categoryCode;
        private final String categoryName;
        private final String description;
        private final Long parentCategoryId;
        private final boolean isRootCategory;

        public CategoryDisplayDto(Long id, String categoryCode, String categoryName,
                                 String description, Long parentCategoryId, boolean isRootCategory) {
            this.id = id;
            this.categoryCode = categoryCode;
            this.categoryName = categoryName;
            this.description = description;
            this.parentCategoryId = parentCategoryId;
            this.isRootCategory = isRootCategory;
        }

        // Getters
        public Long getId() { return id; }
        public String getCategoryCode() { return categoryCode; }
        public String getCategoryName() { return categoryName; }
        public String getDescription() { return description; }
        public Long getParentCategoryId() { return parentCategoryId; }
        public boolean isRootCategory() { return isRootCategory; }
    }

    public static class BrandDisplayDto {
        private final Long id;
        private final String brandCode;
        private final String brandName;
        private final String description;

        public BrandDisplayDto(Long id, String brandCode, String brandName, String description) {
            this.id = id;
            this.brandCode = brandCode;
            this.brandName = brandName;
            this.description = description;
        }

        // Getters
        public Long getId() { return id; }
        public String getBrandCode() { return brandCode; }
        public String getBrandName() { return brandName; }
        public String getDescription() { return description; }
    }
}
