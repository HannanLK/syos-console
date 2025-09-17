package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.ItemMasterFileRepository;
import com.syos.application.ports.out.BrandRepository;
import com.syos.application.ports.out.CategoryRepository;
import com.syos.application.ports.out.SupplierRepository;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.DomainException;
import com.syos.shared.enums.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddProductUseCase.
 * 
 * Testing Strategy:
 * - Mock all repository dependencies
 * - Test successful product creation
 * - Test validation scenarios
 * - Test business rule enforcement
 */
class AddProductUseCaseTest {

    @Mock
    private ItemMasterFileRepository itemRepository;
    
    @Mock
    private BrandRepository brandRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private SupplierRepository supplierRepository;
    
    private AddProductUseCase addProductUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        addProductUseCase = new AddProductUseCase(
            itemRepository, brandRepository, categoryRepository, supplierRepository);
    }

    @Test
    void shouldAddProductSuccessfully() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidRequest();
        ItemMasterFile mockSavedItem = createMockItemWithId();
        
        // Mock repository responses
        when(itemRepository.existsByItemCode(any(ItemCode.class))).thenReturn(false);
        when(brandRepository.existsById(1L)).thenReturn(true);
        when(brandRepository.isActive(1L)).thenReturn(true);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.isActive(1L)).thenReturn(true);
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.isActive(1L)).thenReturn(true);
        when(itemRepository.save(any(ItemMasterFile.class))).thenReturn(mockSavedItem);

        // Act
        AddProductUseCase.AddProductResponse response = addProductUseCase.execute(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(100L, response.getItemId());
        assertEquals("TEST_001", response.getItemCode());
        assertEquals("Product added successfully", response.getMessage());
        
        // Verify interactions
        verify(itemRepository).existsByItemCode(any(ItemCode.class));
        verify(itemRepository).save(any(ItemMasterFile.class));
        verify(brandRepository).existsById(1L);
        verify(brandRepository).isActive(1L);
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).isActive(1L);
        verify(supplierRepository).existsById(1L);
        verify(supplierRepository).isActive(1L);
    }

    @Test
    void shouldFailWhenItemCodeAlreadyExists() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidRequest();
        when(itemRepository.existsByItemCode(any(ItemCode.class))).thenReturn(true);

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, () -> {
            addProductUseCase.execute(request);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(itemRepository).existsByItemCode(any(ItemCode.class));
        verify(itemRepository, never()).save(any(ItemMasterFile.class));
    }

    @Test
    void shouldFailWhenBrandDoesNotExist() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidRequest();
        when(itemRepository.existsByItemCode(any(ItemCode.class))).thenReturn(false);
        when(brandRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, () -> {
            addProductUseCase.execute(request);
        });

        assertTrue(exception.getMessage().contains("Brand with ID 1 does not exist"));
        verify(itemRepository, never()).save(any(ItemMasterFile.class));
    }

    @Test
    void shouldFailWhenSellingPriceLessThanCostPrice() {
        // Arrange
        AddProductUseCase.AddProductRequest request = createValidRequest();
        request.costPrice(BigDecimal.valueOf(150));
        request.sellingPrice(BigDecimal.valueOf(100)); // Less than cost price

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            addProductUseCase.execute(request);
        });

        assertTrue(exception.getMessage().contains("Selling price must be greater than or equal to cost price"));
    }

    // Helper methods
    private AddProductUseCase.AddProductRequest createValidRequest() {
        return new AddProductUseCase.AddProductRequest()
            .itemCode("TEST_001")
            .itemName("Test Product")
            .description("Test Description")
            .brandId(1L)
            .categoryId(1L)
            .supplierId(1L)
            .unitOfMeasure(UnitOfMeasure.EACH)
            .packSize(BigDecimal.ONE)
            .costPrice(BigDecimal.valueOf(100))
            .sellingPrice(BigDecimal.valueOf(150))
            .reorderPoint(50)
            .isPerishable(false)
            .createdBy(1L);
    }

    private ItemMasterFile createMockItemWithId() {
        return ItemMasterFile.createNew(
            ItemCode.of("TEST_001"),
            "Test Product",
            "Test Description",
            BrandId.of(1L),
            CategoryId.of(1L),
            SupplierId.of(1L),
            UnitOfMeasure.EACH,
            PackSize.of(1),
            Money.of(BigDecimal.valueOf(100)),
            Money.of(BigDecimal.valueOf(150)),
            ReorderPoint.of(50),
            false,
            UserID.of(1L)
        ).withId(100L);
    }
}
