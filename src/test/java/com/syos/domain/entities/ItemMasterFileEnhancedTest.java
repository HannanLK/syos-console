package com.syos.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import com.syos.domain.valueobjects.*;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.shared.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ItemMasterFile entity
 * Tests all business logic, validation, and edge cases
 * 
 * Target: 100% line coverage for ItemMasterFile
 */
@DisplayName("ItemMasterFile Entity Tests")
class ItemMasterFileComprehensiveTest {
    
    private ItemMasterFile itemMasterFile;
    private ItemCode validItemCode;
    private Name validName;
    private BrandId validBrandId;
    private CategoryId validCategoryId;
    private Money validCostPrice;
    private Money validSellingPrice;
    private ReorderPoint validReorderPoint;
    private SupplierId validSupplierId;
    
    @BeforeEach
    void setUp() {
        validItemCode = new ItemCode("IT001");
        validName = new Name("Test Product");
        validBrandId = new BrandId(1L);
        validCategoryId = new CategoryId(1L);
        validCostPrice = new Money(BigDecimal.valueOf(10.00));
        validSellingPrice = new Money(BigDecimal.valueOf(15.00));
        validReorderPoint = new ReorderPoint(50);
        validSupplierId = new SupplierId(1L);
        
        itemMasterFile = new ItemMasterFile(
            validItemCode,
            validName,
            validBrandId,
            validCategoryId,
            "Test Description",
            UnitOfMeasure.EACH,
            new PackSize(1),
            validCostPrice,
            validSellingPrice,
            validReorderPoint,
            validSupplierId
        );
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create ItemMasterFile with all valid parameters")
        void shouldCreateItemMasterFileWithAllValidParameters() {
            assertNotNull(itemMasterFile);
            assertEquals(validItemCode, itemMasterFile.getItemCode());
            assertEquals(validName, itemMasterFile.getName());
            assertEquals(validBrandId, itemMasterFile.getBrandId());
            assertEquals(validCategoryId, itemMasterFile.getCategoryId());
            assertEquals("Test Description", itemMasterFile.getDescription());
            assertEquals(UnitOfMeasure.EACH, itemMasterFile.getUnitOfMeasure());
            assertEquals(validCostPrice, itemMasterFile.getCostPrice());
            assertEquals(validSellingPrice, itemMasterFile.getSellingPrice());
            assertEquals(validReorderPoint, itemMasterFile.getReorderPoint());
            assertEquals(validSupplierId, itemMasterFile.getSupplierId());
            assertEquals(ProductStatus.ACTIVE, itemMasterFile.getStatus());
            assertNotNull(itemMasterFile.getDateAdded());
            assertNotNull(itemMasterFile.getLastUpdated());
        }
        
        @Test
        @DisplayName("Should throw exception when itemCode is null")
        void shouldThrowExceptionWhenItemCodeIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ItemMasterFile(
                    null,
                    validName,
                    validBrandId,
                    validCategoryId,
                    "Test Description",
                    UnitOfMeasure.EACH,
                    new PackSize(1),
                    validCostPrice,
                    validSellingPrice,
                    validReorderPoint,
                    validSupplierId
                );
            });
        }
        
        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ItemMasterFile(
                    validItemCode,
                    null,
                    validBrandId,
                    validCategoryId,
                    "Test Description",
                    UnitOfMeasure.EACH,
                    new PackSize(1),
                    validCostPrice,
                    validSellingPrice,
                    validReorderPoint,
                    validSupplierId
                );
            });
        }
        
        @Test
        @DisplayName("Should throw exception when cost price is higher than selling price")
        void shouldThrowExceptionWhenCostPriceIsHigherThanSellingPrice() {
            Money higherCostPrice = new Money(BigDecimal.valueOf(20.00));
            Money lowerSellingPrice = new Money(BigDecimal.valueOf(15.00));
            
            assertThrows(IllegalArgumentException.class, () -> {
                new ItemMasterFile(
                    validItemCode,
                    validName,
                    validBrandId,
                    validCategoryId,
                    "Test Description",
                    UnitOfMeasure.EACH,
                    new PackSize(1),
                    higherCostPrice,
                    lowerSellingPrice,
                    validReorderPoint,
                    validSupplierId
                );
            });
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should calculate profit margin correctly")
        void shouldCalculateProfitMarginCorrectly() {
            // Cost: $10, Selling: $15 => Margin: $5, Percentage: 33.33%
            Money expectedMargin = new Money(BigDecimal.valueOf(5.00));
            assertEquals(expectedMargin, itemMasterFile.getProfitMargin());
            
            BigDecimal expectedPercentage = BigDecimal.valueOf(33.33);
            assertEquals(0, expectedPercentage.compareTo(itemMasterFile.getProfitMarginPercentage()));
        }
        
        @Test
        @DisplayName("Should update selling price and recalculate margin")
        void shouldUpdateSellingPriceAndRecalculateMargin() {
            Money newSellingPrice = new Money(BigDecimal.valueOf(20.00));
            itemMasterFile.updateSellingPrice(newSellingPrice);
            
            assertEquals(newSellingPrice, itemMasterFile.getSellingPrice());
            Money expectedMargin = new Money(BigDecimal.valueOf(10.00));
            assertEquals(expectedMargin, itemMasterFile.getProfitMargin());
        }
        
        @Test
        @DisplayName("Should throw exception when updating selling price below cost price")
        void shouldThrowExceptionWhenUpdatingSellingPriceBelowCostPrice() {
            Money belowCostPrice = new Money(BigDecimal.valueOf(5.00));
            
            assertThrows(IllegalArgumentException.class, () -> {
                itemMasterFile.updateSellingPrice(belowCostPrice);
            });
        }
        
        @Test
        @DisplayName("Should deactivate product")
        void shouldDeactivateProduct() {
            itemMasterFile.deactivate();
            assertEquals(ProductStatus.INACTIVE, itemMasterFile.getStatus());
        }
        
        @Test
        @DisplayName("Should reactivate product")
        void shouldReactivateProduct() {
            itemMasterFile.deactivate();
            itemMasterFile.reactivate();
            assertEquals(ProductStatus.ACTIVE, itemMasterFile.getStatus());
        }
        
        @Test
        @DisplayName("Should check if product is active")
        void shouldCheckIfProductIsActive() {
            assertTrue(itemMasterFile.isActive());
            itemMasterFile.deactivate();
            assertFalse(itemMasterFile.isActive());
        }
        
        @Test
        @DisplayName("Should update reorder point")
        void shouldUpdateReorderPoint() {
            ReorderPoint newReorderPoint = new ReorderPoint(100);
            itemMasterFile.updateReorderPoint(newReorderPoint);
            assertEquals(newReorderPoint, itemMasterFile.getReorderPoint());
        }
        
        @Test
        @DisplayName("Should update description")
        void shouldUpdateDescription() {
            String newDescription = "Updated description";
            itemMasterFile.updateDescription(newDescription);
            assertEquals(newDescription, itemMasterFile.getDescription());
        }
        
        @Test
        @DisplayName("Should update last updated timestamp when modified")
        void shouldUpdateLastUpdatedTimestampWhenModified() {
            LocalDateTime originalTimestamp = itemMasterFile.getLastUpdated();
            
            // Wait a small amount to ensure timestamp difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            itemMasterFile.updateDescription("New description");
            assertTrue(itemMasterFile.getLastUpdated().isAfter(originalTimestamp));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesAndValidationTests {
        
        @Test
        @DisplayName("Should handle zero profit margin")
        void shouldHandleZeroProfitMargin() {
            Money sameCostAndSellingPrice = new Money(BigDecimal.valueOf(10.00));
            ItemMasterFile zeroMarginItem = new ItemMasterFile(
                new ItemCode("IT002"),
                new Name("Zero Margin Product"),
                validBrandId,
                validCategoryId,
                "Zero margin description",
                UnitOfMeasure.EACH,
                new PackSize(1),
                sameCostAndSellingPrice,
                sameCostAndSellingPrice,
                validReorderPoint,
                validSupplierId
            );
            
            assertEquals(Money.ZERO, zeroMarginItem.getProfitMargin());
            assertEquals(BigDecimal.ZERO, zeroMarginItem.getProfitMarginPercentage());
        }
        
        @Test
        @DisplayName("Should handle different unit of measures")
        void shouldHandleDifferentUnitOfMeasures() {
            for (UnitOfMeasure unit : UnitOfMeasure.values()) {
                ItemMasterFile item = new ItemMasterFile(
                    new ItemCode("IT" + unit.ordinal()),
                    new Name("Product " + unit.name()),
                    validBrandId,
                    validCategoryId,
                    "Description for " + unit,
                    unit,
                    new PackSize(1),
                    validCostPrice,
                    validSellingPrice,
                    validReorderPoint,
                    validSupplierId
                );
                
                assertEquals(unit, item.getUnitOfMeasure());
            }
        }
        
        @Test
        @DisplayName("Should handle large pack sizes")
        void shouldHandleLargePackSizes() {
            PackSize largePackSize = new PackSize(1000);
            ItemMasterFile bulkItem = new ItemMasterFile(
                new ItemCode("BULK001"),
                new Name("Bulk Product"),
                validBrandId,
                validCategoryId,
                "Bulk description",
                UnitOfMeasure.PACK,
                largePackSize,
                validCostPrice,
                validSellingPrice,
                validReorderPoint,
                validSupplierId
            );
            
            assertEquals(largePackSize, bulkItem.getPackSize());
        }
        
        @Test
        @DisplayName("Should validate null description handling")
        void shouldValidateNullDescriptionHandling() {
            ItemMasterFile itemWithNullDescription = new ItemMasterFile(
                new ItemCode("NULL001"),
                new Name("Null Description Product"),
                validBrandId,
                validCategoryId,
                null,
                UnitOfMeasure.EACH,
                new PackSize(1),
                validCostPrice,
                validSellingPrice,
                validReorderPoint,
                validSupplierId
            );
            
            assertNull(itemWithNullDescription.getDescription());
            
            // Should be able to update to non-null description
            itemWithNullDescription.updateDescription("Now has description");
            assertEquals("Now has description", itemWithNullDescription.getDescription());
        }
    }
    
    @Nested
    @DisplayName("Equality and Hash Code Tests")
    class EqualityAndHashCodeTests {
        
        @Test
        @DisplayName("Should be equal when item codes are the same")
        void shouldBeEqualWhenItemCodesAreTheSame() {
            ItemMasterFile anotherItem = new ItemMasterFile(
                validItemCode, // Same item code
                new Name("Different Name"),
                validBrandId,
                validCategoryId,
                "Different description",
                UnitOfMeasure.PACK,
                new PackSize(2),
                validCostPrice,
                validSellingPrice,
                validReorderPoint,
                validSupplierId
            );
            
            assertEquals(itemMasterFile, anotherItem);
            assertEquals(itemMasterFile.hashCode(), anotherItem.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when item codes are different")
        void shouldNotBeEqualWhenItemCodesAreDifferent() {
            ItemMasterFile differentItem = new ItemMasterFile(
                new ItemCode("DIFFERENT"),
                validName,
                validBrandId,
                validCategoryId,
                "Test Description",
                UnitOfMeasure.EACH,
                new PackSize(1),
                validCostPrice,
                validSellingPrice,
                validReorderPoint,
                validSupplierId
            );
            
            assertNotEquals(itemMasterFile, differentItem);
        }
        
        @Test
        @DisplayName("Should not be equal to null or different class")
        void shouldNotBeEqualToNullOrDifferentClass() {
            assertNotEquals(itemMasterFile, null);
            assertNotEquals(itemMasterFile, "string");
            assertNotEquals(itemMasterFile, 123);
        }
    }
    
    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should provide meaningful string representation")
        void shouldProvideMeaningfulStringRepresentation() {
            String toString = itemMasterFile.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("ItemMasterFile"));
            assertTrue(toString.contains("IT001"));
            assertTrue(toString.contains("Test Product"));
        }
    }
}
