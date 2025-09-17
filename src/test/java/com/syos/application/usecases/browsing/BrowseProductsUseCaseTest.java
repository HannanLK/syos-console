package com.syos.application.usecases.browsing;

import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.domain.entities.Brand;
import com.syos.domain.entities.Category;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrowseProductsUseCase")
class BrowseProductsUseCaseTest {

    @Mock
    private ItemMasterFileRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    private BrowseProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BrowseProductsUseCase(itemRepository, categoryRepository, brandRepository);
    }

    @Test
    @DisplayName("getAllProducts: returns mapped DTOs and success message")
    void getAllProducts_returnsSuccess() {
        // Arrange
        List<ItemMasterFile> items = List.of(
            testItem(1L, "ITM001", "Coke", 150),
            testItem(2L, "ITM002", "Pepsi", 140)
        );
        when(itemRepository.findAllActive()).thenReturn(items);

        // Act
        BrowseProductsUseCase.BrowseProductsResponse res = useCase.getAllProducts();

        // Assert
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(2);
        assertThat(res.getProducts().get(0).getItemCode()).isEqualTo("ITM001");
        assertThat(res.getProducts().get(0).getItemName()).isEqualTo("Coke");
        assertThat(res.getProducts().get(0).getPrice()).isEqualTo("150");
        assertThat(res.getMessage()).contains("All products");
        verify(itemRepository).findAllActive();
    }

    @Test
    @DisplayName("getProductsByCategory: returns failure if category missing")
    void getProductsByCategory_missingCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        BrowseProductsUseCase.BrowseProductsResponse res = useCase.getProductsByCategory(99L);

        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(99L);
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("getProductsByCategory: returns products when category exists")
    void getProductsByCategory_success() {
        Category beverages = Category.reconstruct(1L, null, "BEV", "Beverages", "", 1, true, null, null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(beverages));
        when(itemRepository.findByCategory(any(CategoryId.class)))
            .thenReturn(List.of(testItem(1L, "ITM001", "Coke", 150)));

        BrowseProductsUseCase.BrowseProductsResponse res = useCase.getProductsByCategory(1L);

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(1);
        assertThat(res.getMessage()).contains("Beverages");
        verify(itemRepository).findByCategory(CategoryId.of(1L));
    }

    @Test
    @DisplayName("getProductsByBrand: returns failure if brand missing")
    void getProductsByBrand_missingBrand() {
        when(brandRepository.findById(77L)).thenReturn(Optional.empty());

        BrowseProductsUseCase.BrowseProductsResponse res = useCase.getProductsByBrand(77L);

        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Brand not found");
        verify(brandRepository).findById(77L);
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("getProductsByBrand: returns products when brand exists")
    void getProductsByBrand_success() {
        Brand coke = Brand.reconstruct(5L, "COK", "Coca-Cola", "", true, null, null);
        when(brandRepository.findById(5L)).thenReturn(Optional.of(coke));
        when(itemRepository.findByBrand(any(BrandId.class)))
            .thenReturn(List.of(testItem(1L, "ITM001", "Coke", 150)));

        BrowseProductsUseCase.BrowseProductsResponse res = useCase.getProductsByBrand(5L);

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(1);
        assertThat(res.getMessage()).contains("Coca-Cola");
        verify(itemRepository).findByBrand(BrandId.of(5L));
    }

    @Test
    @DisplayName("getFeaturedProducts: maps items to DTOs")
    void getFeaturedProducts_maps() {
        when(itemRepository.findFeaturedItems()).thenReturn(List.of(testItem(1L, "ITM001", "Coke", 150, true, false)));

        var res = useCase.getFeaturedProducts();
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(1);
        assertThat(res.getProducts().get(0).isFeatured()).isTrue();
        verify(itemRepository).findFeaturedItems();
    }

    @Test
    @DisplayName("getLatestProducts: maps items to DTOs")
    void getLatestProducts_maps() {
        when(itemRepository.findLatestItems()).thenReturn(List.of(testItem(2L, "ITM002", "Sprite", 130, false, true)));

        var res = useCase.getLatestProducts();
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(1);
        assertThat(res.getProducts().get(0).isLatest()).isTrue();
        verify(itemRepository).findLatestItems();
    }

    @Test
    @DisplayName("searchProducts: returns failure for empty term")
    void searchProducts_emptyTerm() {
        var res1 = useCase.searchProducts(null);
        var res2 = useCase.searchProducts("   ");
        assertThat(res1.isSuccess()).isFalse();
        assertThat(res2.isSuccess()).isFalse();
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("searchProducts: trims and searches, mapping results")
    void searchProducts_success() {
        when(itemRepository.searchByName("coke")).thenReturn(List.of(testItem(1L, "ITM001", "Coke", 150)));

        var res = useCase.searchProducts("  coke  ");
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getProducts()).hasSize(1);
        assertThat(res.getProducts().get(0).getItemName()).isEqualTo("Coke");
        verify(itemRepository).searchByName("coke");
    }

    @Test
    @DisplayName("getCategories: returns root categories mapped to DTOs")
    void getCategories_success() {
        when(categoryRepository.findRootCategories()).thenReturn(List.of(
            Category.reconstruct(1L, null, "BEV", "Beverages", "", 1, true, null, null),
            Category.reconstruct(2L, null, "SNK", "Snacks", "", 2, true, null, null)
        ));

        var res = useCase.getCategories();
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getCategories()).hasSize(2);
        assertThat(res.getCategories().get(0).isRootCategory()).isTrue();
        verify(categoryRepository).findRootCategories();
    }

    @Test
    @DisplayName("getSubCategories: returns subcategories mapped to DTOs")
    void getSubCategories_success() {
        when(categoryRepository.findByParentCategoryId(1L)).thenReturn(List.of(
            Category.reconstruct(10L, 1L, "SFT", "Soft Drinks", "", 1, true, null, null)
        ));

        var res = useCase.getSubCategories(1L);
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getCategories()).hasSize(1);
        assertThat(res.getCategories().get(0).getCategoryName()).isEqualTo("Soft Drinks");
        verify(categoryRepository).findByParentCategoryId(1L);
    }

    @Test
    @DisplayName("getBrands: returns active brands mapped to DTOs")
    void getBrands_success() {
        when(brandRepository.findAllActive()).thenReturn(List.of(
            Brand.reconstruct(5L, "COK", "Coca-Cola", "", true, null, null),
            Brand.reconstruct(6L, "PEP", "Pepsi", "", true, null, null)
        ));

        var res = useCase.getBrands();
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getBrands()).hasSize(2);
        assertThat(res.getBrands().get(1).getBrandName()).isEqualTo("Pepsi");
        verify(brandRepository).findAllActive();
    }

    // Helpers
    private ItemMasterFile testItem(Long id, String code, String name, double price) {
        return testItem(id, code, name, price, false, false);
    }

    private ItemMasterFile testItem(Long id, String code, String name, double price, boolean featured, boolean latest) {
        ItemMasterFile item = ItemMasterFile.createNew(
            ItemCode.of(code),
            name,
            "desc",
            BrandId.of(1L),
            CategoryId.of(1L),
            SupplierId.of(1L),
            UnitOfMeasure.EACH,
            PackSize.of(1),
            Money.of(BigDecimal.valueOf(price - 50)),
            Money.of(BigDecimal.valueOf(price)),
            ReorderPoint.of(50),
            false,
            UserID.of(1L)
        );
        item = featured ? item.markAsFeatured(UserID.of(1L)) : item;
        item = latest ? item.markAsLatest(UserID.of(1L)) : item;
        return id != null ? item.withId(id) : item;
    }
}
