package com.syos.application.strategies.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FIFOWithExpiryStrategy
 * Tests FIFO logic with expiry date prioritization
 */
@DisplayName("FIFO With Expiry Strategy Comprehensive Tests")
class FIFOWithExpiryStrategyComprehensiveTest {
    
    private FIFOWithExpiryStrategy strategy;
    
    @BeforeEach
    void setUp() {
        strategy = new FIFOWithExpiryStrategy();
    }
    
    @Nested
    @DisplayName("Basic FIFO Logic Tests")
    class BasicFIFOLogicTests {
        
        @Test
        @DisplayName("Should select oldest batch when all have same expiry date")
        void shouldSelectOldestBatchWhenAllHaveSameExpiryDate() {
            // Given
            LocalDate commonExpiryDate = LocalDate.now().plusMonths(6);
            
            BatchInfo batch1 = BatchInfo.of(1L, BigDecimal.valueOf(100), LocalDate.now().minusDays(10), commonExpiryDate);
            BatchInfo batch2 = BatchInfo.of(2L, BigDecimal.valueOf(150), LocalDate.now().minusDays(5), commonExpiryDate);
            BatchInfo batch3 = BatchInfo.of(3L, BigDecimal.valueOf(200), LocalDate.now().minusDays(15), commonExpiryDate);
            
            List<BatchInfo> availableBatches = Arrays.asList(batch2, batch1, batch3); // Not in order
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(250));
            
            // Then
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            // First allocation should be from oldest batch (batch3)
            BatchAllocation firstAllocation = allocations.get(0);
            assertEquals(3L, firstAllocation.getBatchId());
            assertEquals(0, BigDecimal.valueOf(200).compareTo(firstAllocation.getAllocatedQuantity()));
            
            // Second allocation should be from second oldest batch (batch1)
            BatchAllocation secondAllocation = allocations.get(1);
            assertEquals(1L, secondAllocation.getBatchId());
            assertEquals(0, BigDecimal.valueOf(50).compareTo(secondAllocation.getAllocatedQuantity()));
        }
        
        @Test
        @DisplayName("Should select all from single batch when quantity is sufficient")
        void shouldSelectAllFromSingleBatchWhenQuantityIsSufficient() {
            // Given
            BatchInfo batch1 = BatchInfo.of(1L, BigDecimal.valueOf(500), LocalDate.now().minusDays(5), LocalDate.now().plusMonths(3));
            BatchInfo batch2 = BatchInfo.of(2L, BigDecimal.valueOf(300), LocalDate.now().minusDays(3), LocalDate.now().plusMonths(4));
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2);
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(200));
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            
            BatchAllocation allocation = allocations.get(0);
            assertEquals(1L, allocation.getBatchId());
            assertEquals(0, BigDecimal.valueOf(200).compareTo(allocation.getAllocatedQuantity()));
        }
    }
    
    @Nested
    @DisplayName("Expiry Date Priority Tests")
    class ExpiryDatePriorityTests {
        
        @Test
        @DisplayName("Should prioritize batch with closer expiry date over FIFO order")
        void shouldPrioritizeBatchWithCloserExpiryDateOverFIFOOrder() {
            // Given
            LocalDate today = LocalDate.now();
            
            // Older batch but expires later
            BatchInfo olderBatch = BatchInfo.of(1L, BigDecimal.valueOf(200), today.minusDays(10), today.plusMonths(6));
            
            // Newer batch but expires sooner
            BatchInfo newerBatch = BatchInfo.of(2L, BigDecimal.valueOf(150), today.minusDays(5), today.plusDays(30));
            
            List<BatchInfo> availableBatches = Arrays.asList(olderBatch, newerBatch);
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(100));
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            
            // Should select from newer batch due to earlier expiry
            BatchAllocation allocation = allocations.get(0);
            assertEquals(2L, allocation.getBatchId());
            assertEquals(0, BigDecimal.valueOf(100).compareTo(allocation.getAllocatedQuantity()));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesAndValidationTests {
        
        @Test
        @DisplayName("Should handle empty batch list")
        void shouldHandleEmptyBatchList() {
            // Given
            List<BatchInfo> emptyBatches = Collections.emptyList();
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(emptyBatches, BigDecimal.valueOf(100));
            
            // Then
            assertNotNull(allocations);
            assertTrue(allocations.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle insufficient total stock")
        void shouldHandleInsufficientTotalStock() {
            // Given
            BatchInfo batch1 = BatchInfo.of(1L, BigDecimal.valueOf(50), LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            BatchInfo batch2 = BatchInfo.of(2L, BigDecimal.valueOf(30), LocalDate.now().minusDays(3), LocalDate.now().plusDays(25));
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2);
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(100)); // More than available
            
            // Then
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            // Should allocate all available stock
            BigDecimal totalAllocated = allocations.stream()
                .map(BatchAllocation::getAllocatedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(0, BigDecimal.valueOf(80).compareTo(totalAllocated)); // 50 + 30
        }
        
        @Test
        @DisplayName("Should handle batches with zero quantity")
        void shouldHandleBatchesWithZeroQuantity() {
            // Given
            BatchInfo zeroBatch = BatchInfo.of(1L, BigDecimal.ZERO, LocalDate.now().minusDays(10), LocalDate.now().plusDays(30));
            BatchInfo normalBatch = BatchInfo.of(2L, BigDecimal.valueOf(100), LocalDate.now().minusDays(5), LocalDate.now().plusDays(25));
            
            List<BatchInfo> availableBatches = Arrays.asList(zeroBatch, normalBatch);
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(50));
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            assertEquals(2L, allocations.get(0).getBatchId());
            assertEquals(0, BigDecimal.valueOf(50).compareTo(allocations.get(0).getAllocatedQuantity()));
        }
        
        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowExceptionForNegativeQuantity() {
            // Given
            BatchInfo batch = BatchInfo.of(1L, BigDecimal.valueOf(100), LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            List<BatchInfo> availableBatches = Arrays.asList(batch);
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(-10));
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null batch list")
        void shouldThrowExceptionForNullBatchList() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                strategy.selectBatchesForDispatch(null, BigDecimal.valueOf(100));
            });
        }
    }
    
    @Nested
    @DisplayName("Complex Allocation Scenarios")
    class ComplexAllocationScenarios {
        
        @Test
        @DisplayName("Should handle mixed expiry dates and FIFO correctly")
        void shouldHandleMixedExpiryDatesAndFIFOCorrectly() {
            // Given
            LocalDate today = LocalDate.now();
            
            BatchInfo batch1 = BatchInfo.of(1L, BigDecimal.valueOf(100), today.minusDays(20), today.plusDays(60)); // Old, long shelf life
            BatchInfo batch2 = BatchInfo.of(2L, BigDecimal.valueOf(150), today.minusDays(15), today.plusDays(10)); // Medium age, short shelf life
            BatchInfo batch3 = BatchInfo.of(3L, BigDecimal.valueOf(200), today.minusDays(10), today.plusDays(50)); // Newer, medium shelf life
            BatchInfo batch4 = BatchInfo.of(4L, BigDecimal.valueOf(80), today.minusDays(25), today.plusDays(5)); // Oldest, very short shelf life
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2, batch3, batch4);
            
            // When
            List<BatchAllocation> allocations = strategy.selectBatchesForDispatch(availableBatches, BigDecimal.valueOf(350));
            
            // Then - Should prioritize by expiry date first, then FIFO
            assertNotNull(allocations);
            
            // First should be batch4 (expires in 5 days)
            assertEquals(4L, allocations.get(0).getBatchId());
            assertEquals(0, BigDecimal.valueOf(80).compareTo(allocations.get(0).getAllocatedQuantity()));
            
            // Second should be batch2 (expires in 10 days)
            assertEquals(2L, allocations.get(1).getBatchId());
            assertEquals(0, BigDecimal.valueOf(150).compareTo(allocations.get(1).getAllocatedQuantity()));
        }
        
        @Test
        @DisplayName("Should maintain allocation order consistency")
        void shouldMaintainAllocationOrderConsistency() {
            // Given
            LocalDate today = LocalDate.now();
            
            BatchInfo batch1 = BatchInfo.of(1L, BigDecimal.valueOf(100), today.minusDays(10), today.plusDays(30));
            BatchInfo batch2 = BatchInfo.of(2L, BigDecimal.valueOf(100), today.minusDays(8), today.plusDays(25));
            BatchInfo batch3 = BatchInfo.of(3L, BigDecimal.valueOf(100), today.minusDays(6), today.plusDays(35));
            
            List<BatchInfo> batches = Arrays.asList(batch1, batch2, batch3);
            
            // When - Multiple calls with same data should produce same result
            List<BatchAllocation> allocations1 = strategy.selectBatchesForDispatch(batches, BigDecimal.valueOf(150));
            List<BatchAllocation> allocations2 = strategy.selectBatchesForDispatch(batches, BigDecimal.valueOf(150));
            
            // Then - Results should be identical
            assertEquals(allocations1.size(), allocations2.size());
            
            for (int i = 0; i < allocations1.size(); i++) {
                assertEquals(allocations1.get(i).getBatchId(), allocations2.get(i).getBatchId());
                assertEquals(0, allocations1.get(i).getAllocatedQuantity().compareTo(allocations2.get(i).getAllocatedQuantity()));
            }
        }
    }
}
