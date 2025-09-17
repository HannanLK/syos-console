package com.syos.infrastructure.config;

import com.syos.application.ports.out.*;
import com.syos.application.usecases.inventory.AddProductUseCase;
import com.syos.application.usecases.browsing.BrowseProductsUseCase;
import com.syos.infrastructure.persistence.repositories.*;
import com.syos.adapter.in.cli.controllers.ProductController;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.io.StandardConsoleIO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Configuration class for dependency injection without Spring.
 * 
 * Infrastructure Layer:
 * - Wires together all dependencies
 * - Creates and manages EntityManager
 * - Provides factory methods for components
 */
public class ApplicationConfiguration {
    
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    
    // Repositories
    private ItemMasterFileRepository itemMasterFileRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;
    private SupplierRepository supplierRepository;
    
    // Use Cases
    private AddProductUseCase addProductUseCase;
    private BrowseProductsUseCase browseProductsUseCase;
    
    // Controllers
    private ProductController productController;
    
    // IO
    private ConsoleIO consoleIO;

    public ApplicationConfiguration() {
        initialize();
    }

    private void initialize() {
        // Initialize JPA
        initializeJPA();
        
        // Initialize repositories
        initializeRepositories();
        
        // Initialize use cases
        initializeUseCases();
        
        // Initialize controllers
        initializeControllers();
    }

    private void initializeJPA() {
        try {
            // Create EntityManagerFactory using persistence.xml configuration
            this.entityManagerFactory = Persistence.createEntityManagerFactory("syos-persistence-unit");
            this.entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            // Fallback for testing without database
            System.err.println("Warning: Could not initialize JPA EntityManager: " + e.getMessage());
            System.err.println("Running in test mode without database connection.");
            this.entityManager = null;
        }
    }

    private void initializeRepositories() {
        if (entityManager != null) {
            this.itemMasterFileRepository = new JpaItemMasterFileRepository(entityManager);
            this.brandRepository = new JpaBrandRepository(entityManager);
            this.categoryRepository = new JpaCategoryRepository(entityManager);
            this.supplierRepository = new JpaSupplierRepository(entityManager);
        } else {
            // Create mock repositories for testing
            this.itemMasterFileRepository = new MockItemMasterFileRepository();
            this.brandRepository = new MockBrandRepository();
            this.categoryRepository = new MockCategoryRepository();
            this.supplierRepository = new MockSupplierRepository();
        }
    }

    private void initializeUseCases() {
        this.addProductUseCase = new AddProductUseCase(
            itemMasterFileRepository, brandRepository, categoryRepository, supplierRepository);
        
        this.browseProductsUseCase = new BrowseProductsUseCase(
            itemMasterFileRepository, categoryRepository, brandRepository);
    }

    private void initializeControllers() {
        this.consoleIO = new StandardConsoleIO();
        this.productController = new ProductController(consoleIO, addProductUseCase, browseProductsUseCase);
    }

    // Public getters for accessing configured components
    public ProductController getProductController() {
        return productController;
    }
    
    public AddProductUseCase getAddProductUseCase() {
        return addProductUseCase;
    }
    
    public BrowseProductsUseCase getBrowseProductsUseCase() {
        return browseProductsUseCase;
    }
    
    public ConsoleIO getConsoleIO() {
        return consoleIO;
    }

    public void shutdown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
    
    // Simple mock implementations for testing without database
    private static class MockItemMasterFileRepository implements ItemMasterFileRepository {
        @Override
        public com.syos.domain.entities.ItemMasterFile save(com.syos.domain.entities.ItemMasterFile item) {
            // Return item with mock ID
            return item.withId(1L);
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.ItemMasterFile> findById(Long id) {
            return java.util.Optional.empty();
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.ItemMasterFile> findByItemCode(com.syos.domain.valueobjects.ItemCode itemCode) {
            return java.util.Optional.empty();
        }
        
        @Override
        public boolean existsByItemCode(com.syos.domain.valueobjects.ItemCode itemCode) {
            return false; // Always allow new items for testing
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findAllActive() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findByCategory(com.syos.domain.valueobjects.CategoryId categoryId) {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findByBrand(com.syos.domain.valueobjects.BrandId brandId) {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findFeaturedItems() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findLatestItems() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> findItemsRequiringReorder() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.ItemMasterFile> searchByName(String searchTerm) {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public long countActiveItems() {
            return 0;
        }
        
        @Override
        public void deleteById(Long id) {
            // Mock implementation
        }
        
        @Override
        public boolean isActive(Long id) {
            return true;
        }
    }
    
    private static class MockBrandRepository implements BrandRepository {
        @Override
        public com.syos.domain.entities.Brand save(com.syos.domain.entities.Brand brand) {
            return brand.withId(1L);
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Brand> findById(Long id) {
            return java.util.Optional.empty();
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Brand> findByBrandCode(String brandCode) {
            return java.util.Optional.empty();
        }
        
        @Override
        public boolean existsById(Long id) {
            return true; // Always exist for testing
        }
        
        @Override
        public boolean existsByBrandCode(String brandCode) {
            return false;
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Brand> findAllActive() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Brand> findAll() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public boolean isActive(Long id) {
            return true; // Always active for testing
        }
        
        @Override
        public long countActiveBrands() {
            return 0;
        }
        
        @Override
        public void deleteById(Long id) {
            // Mock implementation
        }
    }
    
    private static class MockCategoryRepository implements CategoryRepository {
        @Override
        public com.syos.domain.entities.Category save(com.syos.domain.entities.Category category) {
            return category.withId(1L);
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Category> findById(Long id) {
            return java.util.Optional.empty();
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Category> findByCategoryCode(String categoryCode) {
            return java.util.Optional.empty();
        }
        
        @Override
        public boolean existsById(Long id) {
            return true;
        }
        
        @Override
        public boolean existsByCategoryCode(String categoryCode) {
            return false;
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Category> findAllActive() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Category> findRootCategories() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Category> findByParentCategoryId(Long parentId) {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Category> findAll() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public boolean isActive(Long id) {
            return true;
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Category> getCategoryHierarchy() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public long countActiveCategories() {
            return 0;
        }
        
        @Override
        public void deleteById(Long id) {
            // Mock implementation
        }
    }
    
    private static class MockSupplierRepository implements SupplierRepository {
        @Override
        public com.syos.domain.entities.Supplier save(com.syos.domain.entities.Supplier supplier) {
            return supplier.withId(1L);
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Supplier> findById(Long id) {
            return java.util.Optional.empty();
        }
        
        @Override
        public java.util.Optional<com.syos.domain.entities.Supplier> findBySupplierCode(String supplierCode) {
            return java.util.Optional.empty();
        }
        
        @Override
        public boolean existsById(Long id) {
            return true;
        }
        
        @Override
        public boolean existsBySupplierCode(String supplierCode) {
            return false;
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> findAllActive() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> findAll() {
            return java.util.Collections.emptyList();
        }
        
        @Override
        public boolean isActive(Long id) {
            return true;
        }
        
        @Override
        public long countActiveSuppliers() {
            return 0;
        }
        
        @Override
        public void deleteById(Long id) {
            // Mock implementation
        }
        
        @Override
        public java.util.List<com.syos.domain.entities.Supplier> searchByName(String searchTerm) {
            return java.util.Collections.emptyList();
        }
    }
}
