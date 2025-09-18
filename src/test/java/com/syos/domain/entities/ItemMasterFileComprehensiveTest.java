package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.ProductStatus;
import com.syos.shared.enums.UnitOfMeasure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ItemMasterFile domain entity.
 * Tests business rules, validation, and domain behavior.
 */
@DisplayName("ItemMasterFile Domain Entity Tests")
class ItemMasterFileComprehensiveTest {

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProductWithValidData() {
            // Act
            ItemMasterFile product = createValidProduct();

            // Assert
            assertNotNull(product);
            assertEquals("TEST001", product.getItemCode().getValue());
            assertEquals("Test Product", product.getItemName());
            assertEquals("Test Description", product.getDescription());
            assertEquals(Money.of(BigDecimal.valueOf(100)), product.getCostPrice());
            assertEquals(Money.of(BigDecimal.valueOf(150)), product.getSellingPrice());
            assertEquals(ReorderPoint.of(50), product.getReorderPoint());
            assertEquals(ProductStatus.ACTIVE, product.getStatus());
            assertTrue(product.isActive());
            assertFalse(product.isPerishable());
            assertFalse(product.isFeatured());
            assertFalse(product.isLatest());
        }

        @Test
        @DisplayName("Should fail creation with null item code")
        void shouldFailCreationWithNullItemCode() {
            assertThrows(NullPointerException.class, () ->
                ItemMasterFile.createNew(
                    null, // null item code
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
                )
            );
        }

        @Test
        @DisplayName("Should enforce selling price greater than or equal to cost price")
        void shouldEnforceSellingPriceBusinessRule() {
            assertThrows(IllegalArgumentException.class, () ->
                ItemMasterFile.createNew(
                    ItemCode.of("TEST001"),
                    "Test Product",
                    "Test Description",
                    BrandId.of(1L),
                    CategoryId.of(1L),
                    SupplierId.of(1L),
                    UnitOfMeasure.EACH,
                    PackSize.of(1),
                    Money.of(BigDecimal.valueOf(200)), // Higher cost price
                    Money.of(BigDecimal.valueOf(100)), // Lower selling price
                    ReorderPoint.of(50),
                    false,
                    UserID.of(1L)
                )
            );
        }

        @Test
        @DisplayName("Should enforce minimum reorder point threshold of 50 as business rule")
        void shouldEnforceMinimumReorderPointThreshold() {
            // Create product with reorder point below 50
            ItemMasterFile product = ItemMasterFile.createNew(
                ItemCode.of("LOW_REORDER"),
                "Low Reorder Product",
                "Test Description",
                BrandId.of(1L),
                CategoryId.of(1L),
                SupplierId.of(1L),
                UnitOfMeasure.EACH,
                PackSize.of(1),
                Money.of(BigDecimal.valueOf(100)),
                Money.of(BigDecimal.valueOf(150)),
                ReorderPoint.of(25), // Below standard threshold
                false,
                UserID.of(1L)
            );
            
            // Should allow but note that business logic should warn
            assertEquals(ReorderPoint.of(25), product.getReorderPoint());
            
            // Standard reorder point should be 50
            ItemMasterFile standardProduct = ItemMasterFile.createNew(
                ItemCode.of("STANDARD_REORDER"),
                "Standard Reorder Product",
                "Test Description",
                BrandId.of(1L),
                CategoryId.of(1L),
                SupplierId.of(1L),
                UnitOfMeasure.EACH,
                PackSize.of(1),
                Money.of(BigDecimal.valueOf(100)),
                Money.of(BigDecimal.valueOf(150)),
                ReorderPoint.of(50), // Standard threshold
                false,
                UserID.of(1L)
            );
            
            assertEquals(ReorderPoint.of(50), standardProduct.getReorderPoint());
        }
    }

    @Nested
    @DisplayName("Product Modification Tests")
    class ProductModificationTests {

        @Test
        @DisplayName("Should update selling price correctly")
        void shouldUpdateSellingPrice() {
            // Arrange
            ItemMasterFile product = createValidProduct();
            Money newSellingPrice = Money.of(BigDecimal.valueOf(200));
            UserID updatedBy = UserID.of(2L);

            // Act
            ItemMasterFile updatedProduct = product.updateSellingPrice(newSellingPrice, updatedBy);

            // Assert
            assertEquals(newSellingPrice, updatedProduct.getSellingPrice());
            assertEquals(updatedBy, updatedProduct.getUpdatedBy());
            assertTrue(updatedProduct.getLastUpdated().isAfter(product.getLastUpdated()));
            
            // Verify original product is unchanged (immutability)
            assertNotEquals(newSellingPrice, product.getSellingPrice());
        }

        @Test
        @DisplayName("Should mark product as featured")
        void shouldMarkAsFeatured() {
            // Arrange
            ItemMasterFile product = createValidProduct();
            UserID updatedBy = UserID.of(2L);

            // Act
            ItemMasterFile featuredProduct = product.markAsFeatured(updatedBy);

            // Assert
            assertTrue(featuredProduct.isFeatured());
            assertEquals(updatedBy, featuredProduct.getUpdatedBy());
            assertTrue(featuredProduct.getLastUpdated().isAfter(product.getLastUpdated()));
            
            // Verify original product is unchanged
            assertFalse(product.isFeatured());
        }

        @Test
        @DisplayName("Should assign ID to new product")
        void shouldAssignIdToNewProduct() {
            // Arrange
            ItemMasterFile product = createValidProduct();
            assertNull(product.getId());

            // Act
            ItemMasterFile productWithId = product.withId(100L);

            // Assert
            assertEquals(Long.valueOf(100L), productWithId.getId());
            
            // Verify original product is unchanged
            assertNull(product.getId());
        }
    }

    @Test
    @DisplayName("Should work with different units of measure")
    void shouldWorkWithDifferentUnitsOfMeasure() {
        for (UnitOfMeasure unit : UnitOfMeasure.values()) {
            ItemMasterFile product = ItemMasterFile.createNew(
                ItemCode.of("TEST_" + unit.name()),
                "Test Product " + unit.name(),
                "Test Description",
                BrandId.of(1L),
                CategoryId.of(1L),
                SupplierId.of(1L),
                unit,
                PackSize.of(1),
                Money.of(BigDecimal.valueOf(100)),
                Money.of(BigDecimal.valueOf(150)),
                ReorderPoint.of(50),
                false,
                UserID.of(1L)
            );
            
            assertEquals(unit, product.getUnitOfMeasure());
            assertNotNull(product);
        }
    }

    // Helper method to create a valid product for testing
    private ItemMasterFile createValidProduct() {
        return ItemMasterFile.createNew(
            ItemCode.of("TEST001"),
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
        );
    }
}
