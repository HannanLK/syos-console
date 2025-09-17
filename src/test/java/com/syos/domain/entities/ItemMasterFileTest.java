package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.ProductStatus;
import com.syos.shared.enums.UnitOfMeasure;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemMasterFile domain entity.
 * 
 * Testing Strategy:
 * - Test domain business rules
 * - Test value object validation
 * - Test entity state transitions
 * - No external dependencies (pure domain testing)
 */
class ItemMasterFileTest {

    @Test
    void shouldCreateValidItemMasterFile() {
        // Arrange
        ItemCode itemCode = ItemCode.of("TEST_001");
        String itemName = "Test Product";
        String description = "Test Description";
        BrandId brandId = BrandId.of(1L);
        CategoryId categoryId = CategoryId.of(1L);
        SupplierId supplierId = SupplierId.of(1L);
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.EACH;
        PackSize packSize = PackSize.of(1);
        Money costPrice = Money.of(BigDecimal.valueOf(100));
        Money sellingPrice = Money.of(BigDecimal.valueOf(150));
        ReorderPoint reorderPoint = ReorderPoint.of(50);
        UserID createdBy = UserID.of(1L);

        // Act
        ItemMasterFile item = ItemMasterFile.createNew(
            itemCode, itemName, description, brandId, categoryId, supplierId,
            unitOfMeasure, packSize, costPrice, sellingPrice, reorderPoint,
            false, createdBy
        );

        // Assert
        assertNotNull(item);
        assertEquals(itemCode, item.getItemCode());
        assertEquals(itemName, item.getItemName());
        assertEquals(description, item.getDescription());
        assertEquals(brandId, item.getBrandId());
        assertEquals(categoryId, item.getCategoryId());
        assertEquals(supplierId, item.getSupplierId());
        assertEquals(unitOfMeasure, item.getUnitOfMeasure());
        assertEquals(packSize, item.getPackSize());
        assertEquals(costPrice, item.getCostPrice());
        assertEquals(sellingPrice, item.getSellingPrice());
        assertEquals(reorderPoint, item.getReorderPoint());
        assertFalse(item.isPerishable());
        assertEquals(ProductStatus.ACTIVE, item.getStatus());
        assertTrue(item.isActive());
        assertEquals(createdBy, item.getCreatedBy());
    }

    @Test
    void shouldThrowExceptionWhenSellingPriceLessThanCostPrice() {
        // Arrange
        Money costPrice = Money.of(BigDecimal.valueOf(150));
        Money sellingPrice = Money.of(BigDecimal.valueOf(100)); // Less than cost price

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ItemMasterFile.createNew(
                ItemCode.of("TEST_001"),
                "Test Product",
                "Description",
                BrandId.of(1L),
                CategoryId.of(1L),
                SupplierId.of(1L),
                UnitOfMeasure.EACH,
                PackSize.of(1),
                costPrice,
                sellingPrice,
                ReorderPoint.of(50),
                false,
                UserID.of(1L)
            );
        });

        assertTrue(exception.getMessage().contains("Selling price must be greater than or equal to cost price"));
    }

    @Test
    void shouldThrowExceptionWhenItemNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ItemMasterFile.createNew(
                ItemCode.of("TEST_001"),
                "", // Empty name
                "Description",
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
        });

        assertTrue(exception.getMessage().contains("Item name cannot be null or empty"));
    }

    @Test
    void shouldUpdateSellingPrice() {
        // Arrange
        ItemMasterFile originalItem = createValidItem();
        Money newSellingPrice = Money.of(BigDecimal.valueOf(200));
        UserID updatedBy = UserID.of(2L);

        // Act
        ItemMasterFile updatedItem = originalItem.updateSellingPrice(newSellingPrice, updatedBy);

        // Assert
        assertEquals(newSellingPrice, updatedItem.getSellingPrice());
        assertEquals(updatedBy, updatedItem.getUpdatedBy());
        assertNotEquals(originalItem.getLastUpdated(), updatedItem.getLastUpdated());
    }

    @Test
    void shouldMarkAsFeatured() {
        // Arrange
        ItemMasterFile originalItem = createValidItem();
        UserID updatedBy = UserID.of(2L);

        // Act
        ItemMasterFile featuredItem = originalItem.markAsFeatured(updatedBy);

        // Assert
        assertTrue(featuredItem.isFeatured());
        assertEquals(updatedBy, featuredItem.getUpdatedBy());
        assertFalse(originalItem.isFeatured()); // Original should be unchanged
    }

    @Test
    void shouldDeactivateItem() {
        // Arrange
        ItemMasterFile originalItem = createValidItem();
        UserID updatedBy = UserID.of(2L);

        // Act
        ItemMasterFile deactivatedItem = originalItem.deactivate(updatedBy);

        // Assert
        assertEquals(ProductStatus.INACTIVE, deactivatedItem.getStatus());
        assertFalse(deactivatedItem.isActive());
        assertEquals(updatedBy, deactivatedItem.getUpdatedBy());
        assertTrue(originalItem.isActive()); // Original should be unchanged
    }

    @Test
    void shouldTestEquality() {
        // Arrange
        ItemCode itemCode = ItemCode.of("TEST_001");
        ItemMasterFile item1 = createValidItemWithCode(itemCode);
        ItemMasterFile item2 = createValidItemWithCode(itemCode);
        ItemMasterFile item3 = createValidItemWithCode(ItemCode.of("TEST_002"));

        // Assert
        assertEquals(item1, item2); // Same item code should be equal
        assertNotEquals(item1, item3); // Different item codes should not be equal
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void shouldCreateItemWithBuilder() {
        // Arrange & Act
        ItemMasterFile item = new ItemMasterFile.Builder()
            .itemCode(ItemCode.of("BUILDER_001"))
            .itemName("Builder Test")
            .description("Built with builder pattern")
            .brandId(BrandId.of(1L))
            .categoryId(CategoryId.of(1L))
            .supplierId(SupplierId.of(1L))
            .unitOfMeasure(UnitOfMeasure.KG)
            .packSize(PackSize.of(5.0))
            .costPrice(Money.of(BigDecimal.valueOf(50)))
            .sellingPrice(Money.of(BigDecimal.valueOf(75)))
            .reorderPoint(ReorderPoint.of(25))
            .isPerishable(true)
            .isFeatured(true)
            .createdBy(UserID.of(1L))
            .build();

        // Assert
        assertNotNull(item);
        assertEquals("BUILDER_001", item.getItemCode().getValue());
        assertEquals("Builder Test", item.getItemName());
        assertEquals(UnitOfMeasure.KG, item.getUnitOfMeasure());
        assertEquals(5.0, item.getPackSize().getDoubleValue());
        assertTrue(item.isPerishable());
        assertTrue(item.isFeatured());
    }

    // Helper methods
    private ItemMasterFile createValidItem() {
        return createValidItemWithCode(ItemCode.of("TEST_001"));
    }

    private ItemMasterFile createValidItemWithCode(ItemCode itemCode) {
        return ItemMasterFile.createNew(
            itemCode,
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
