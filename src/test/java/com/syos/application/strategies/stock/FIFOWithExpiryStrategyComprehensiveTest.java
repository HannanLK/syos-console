package com.syos.application.strategies.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FIFOWithExpiryStrategy
 * Tests FIFO logic with expiry date prioritization
 * 
 * Target: 100% line coverage for stock selection strategy
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
            
            BatchInfo batch1 = new BatchInfo("BATCH001", 100, LocalDate.now().minusDays(10), commonExpiryDate);
            BatchInfo batch2 = new BatchInfo("BATCH002", 150, LocalDate.now().minusDays(5), commonExpiryDate);
            BatchInfo batch3 = new BatchInfo("BATCH003", 200, LocalDate.now().minusDays(15), commonExpiryDate);
            
            List<BatchInfo> availableBatches = Arrays.asList(batch2, batch1, batch3); // Not in order
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 250);
            
            // Then
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            // First allocation should be from oldest batch (batch3)
            BatchAllocation firstAllocation = allocations.get(0);
            assertEquals("BATCH003", firstAllocation.getBatchId());
            assertEquals(200, firstAllocation.getQuantity());
            
            // Second allocation should be from second oldest batch (batch1)
            BatchAllocation secondAllocation = allocations.get(1);
            assertEquals("BATCH001", secondAllocation.getBatchId());
            assertEquals(50, secondAllocation.getQuantity());
        }
        
        @Test
        @DisplayName("Should select all from single batch when quantity is sufficient")
        void shouldSelectAllFromSingleBatchWhenQuantityIsSufficient() {
            // Given
            BatchInfo batch1 = new BatchInfo("BATCH001", 500, LocalDate.now().minusDays(5), LocalDate.now().plusMonths(3));
            BatchInfo batch2 = new BatchInfo("BATCH002", 300, LocalDate.now().minusDays(3), LocalDate.now().plusMonths(4));
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 200);
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            
            BatchAllocation allocation = allocations.get(0);
            assertEquals("BATCH001", allocation.getBatchId());
            assertEquals(200, allocation.getQuantity());
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
            BatchInfo olderBatch = new BatchInfo("BATCH001", 200, today.minusDays(10), today.plusMonths(6));
            
            // Newer batch but expires sooner
            BatchInfo newerBatch = new BatchInfo("BATCH002", 150, today.minusDays(5), today.plusDays(30));
            
            List<BatchInfo> availableBatches = Arrays.asList(olderBatch, newerBatch);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 100);
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            
            // Should select from newer batch due to earlier expiry
            BatchAllocation allocation = allocations.get(0);
            assertEquals("BATCH002", allocation.getBatchId());
            assertEquals(100, allocation.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle batches expiring today with highest priority")
        void shouldHandleBatchesExpiringTodayWithHighestPriority() {
            // Given
            LocalDate today = LocalDate.now();
            
            BatchInfo expiringToday = new BatchInfo("EXPIRING", 50, today.minusDays(15), today);
            BatchInfo expiringSoon = new BatchInfo("SOON", 100, today.minusDays(10), today.plusDays(3));
            BatchInfo expiringLater = new BatchInfo("LATER", 200, today.minusDays(5), today.plusMonths(1));
            
            List<BatchInfo> availableBatches = Arrays.asList(expiringLater, expiringSoon, expiringToday);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 120);
            
            // Then
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            // Should use expiring batch first
            assertEquals("EXPIRING", allocations.get(0).getBatchId());
            assertEquals(50, allocations.get(0).getQuantity());
            
            // Then use soon-expiring batch
            assertEquals("SOON", allocations.get(1).getBatchId());
            assertEquals(70, allocations.get(1).getQuantity());
        }
        
        @Test
        @DisplayName("Should handle multiple batches with same expiry date using FIFO")
        void shouldHandleMultipleBatchesWithSameExpiryDateUsingFIFO() {
            // Given
            LocalDate sameExpiryDate = LocalDate.now().plusDays(15);
            
            BatchInfo batch1 = new BatchInfo("BATCH001", 100, LocalDate.now().minusDays(20), sameExpiryDate);
            BatchInfo batch2 = new BatchInfo("BATCH002", 150, LocalDate.now().minusDays(18), sameExpiryDate);
            BatchInfo batch3 = new BatchInfo("BATCH003", 200, LocalDate.now().minusDays(22), sameExpiryDate);
            
            List<BatchInfo> availableBatches = Arrays.asList(batch2, batch1, batch3);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 300);
            
            // Then - Should use FIFO order when expiry dates are same
            assertEquals("BATCH003", allocations.get(0).getBatchId()); // Oldest
            assertEquals("BATCH001", allocations.get(1).getBatchId()); // Second oldest
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
            List<BatchAllocation> allocations = strategy.allocateStock(emptyBatches, 100);
            
            // Then
            assertNotNull(allocations);
            assertTrue(allocations.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle zero quantity request")
        void shouldHandleZeroQuantityRequest() {
            // Given
            BatchInfo batch = new BatchInfo("BATCH001", 100, LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            List<BatchInfo> availableBatches = Arrays.asList(batch);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 0);
            
            // Then
            assertNotNull(allocations);
            assertTrue(allocations.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle insufficient total stock")
        void shouldHandleInsufficientTotalStock() {
            // Given
            BatchInfo batch1 = new BatchInfo("BATCH001", 50, LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            BatchInfo batch2 = new BatchInfo("BATCH002", 30, LocalDate.now().minusDays(3), LocalDate.now().plusDays(25));
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 100); // More than available
            
            // Then
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            // Should allocate all available stock
            int totalAllocated = allocations.stream()
                .mapToInt(BatchAllocation::getQuantity)
                .sum();
            assertEquals(80, totalAllocated); // 50 + 30
        }
        
        @Test
        @DisplayName("Should handle batches with zero quantity")
        void shouldHandleBatchesWithZeroQuantity() {
            // Given
            BatchInfo zeroBatch = new BatchInfo("ZERO", 0, LocalDate.now().minusDays(10), LocalDate.now().plusDays(30));
            BatchInfo normalBatch = new BatchInfo("NORMAL", 100, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25));
            
            List<BatchInfo> availableBatches = Arrays.asList(zeroBatch, normalBatch);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 50);
            
            // Then
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            assertEquals("NORMAL", allocations.get(0).getBatchId());
            assertEquals(50, allocations.get(0).getQuantity());
        }
        
        @Test
        @DisplayName("Should handle expired batches")
        void shouldHandleExpiredBatches() {
            // Given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            BatchInfo expiredBatch = new BatchInfo("EXPIRED", 100, LocalDate.now().minusDays(10), yesterday);
            BatchInfo validBatch = new BatchInfo("VALID", 150, LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            
            List<BatchInfo> availableBatches = Arrays.asList(expiredBatch, validBatch);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 80);
            
            // Then - Should prioritize expired batch to clear it out
            assertNotNull(allocations);
            assertEquals(1, allocations.size());
            assertEquals("EXPIRED", allocations.get(0).getBatchId());
            assertEquals(80, allocations.get(0).getQuantity());
        }
        
        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowExceptionForNegativeQuantity() {
            // Given
            BatchInfo batch = new BatchInfo("BATCH001", 100, LocalDate.now().minusDays(5), LocalDate.now().plusDays(30));
            List<BatchInfo> availableBatches = Arrays.asList(batch);
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                strategy.allocateStock(availableBatches, -10);
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null batch list")
        void shouldThrowExceptionForNullBatchList() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                strategy.allocateStock(null, 100);
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
            
            BatchInfo batch1 = new BatchInfo("B1", 100, today.minusDays(20), today.plusDays(60)); // Old, long shelf life
            BatchInfo batch2 = new BatchInfo("B2", 150, today.minusDays(15), today.plusDays(10)); // Medium age, short shelf life
            BatchInfo batch3 = new BatchInfo("B3", 200, today.minusDays(10), today.plusDays(50)); // Newer, medium shelf life
            BatchInfo batch4 = new BatchInfo("B4", 80, today.minusDays(25), today.plusDays(5)); // Oldest, very short shelf life
            
            List<BatchInfo> availableBatches = Arrays.asList(batch1, batch2, batch3, batch4);
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 350);
            
            // Then - Should prioritize by expiry date first, then FIFO
            assertNotNull(allocations);
            
            // First should be batch4 (expires in 5 days)
            assertEquals("B4", allocations.get(0).getBatchId());
            assertEquals(80, allocations.get(0).getQuantity());
            
            // Second should be batch2 (expires in 10 days)
            assertEquals("B2", allocations.get(1).getBatchId());
            assertEquals(150, allocations.get(1).getQuantity());
            
            // Third should be batch3 (expires in 50 days, but newer than batch1)
            assertEquals("B3", allocations.get(2).getBatchId());
            // Should only need 120 more (350 - 80 - 150 = 120)
            assertEquals(120, allocations.get(2).getQuantity());
        }
        
        @Test
        @DisplayName("Should optimize allocation for minimal waste")
        void shouldOptimizeAllocationForMinimalWaste() {
            // Given - Multiple batches with different quantities
            LocalDate futureDate = LocalDate.now().plusMonths(3);
            
            BatchInfo smallBatch = new BatchInfo("SMALL", 25, LocalDate.now().minusDays(10), futureDate);
            BatchInfo mediumBatch = new BatchInfo("MEDIUM", 100, LocalDate.now().minusDays(8), futureDate);
            BatchInfo largeBatch = new BatchInfo("LARGE", 500, LocalDate.now().minusDays(6), futureDate);
            
            List<BatchInfo> availableBatches = Arrays.asList(smallBatch, mediumBatch, largeBatch);
            
            // When - Request amount that can be satisfied by older batches
            List<BatchAllocation> allocations = strategy.allocateStock(availableBatches, 125);
            
            // Then - Should use all of small and medium batches (FIFO order)
            assertNotNull(allocations);
            assertEquals(2, allocations.size());
            
            assertEquals("SMALL", allocations.get(0).getBatchId());
            assertEquals(25, allocations.get(0).getQuantity());
            
            assertEquals("MEDIUM", allocations.get(1).getBatchId());
            assertEquals(100, allocations.get(1).getQuantity());
            
            // Large batch should be untouched for now
            int totalAllocated = allocations.stream()
                .mapToInt(BatchAllocation::getQuantity)
                .sum();
            assertEquals(125, totalAllocated);
        }
        
        @Test
        @DisplayName("Should handle large number of batches efficiently")
        void shouldHandleLargeNumberOfBatchesEfficiently() {
            // Given - Create 100 batches with random dates
            List<BatchInfo> largeBatchList = new java.util.ArrayList<>();
            LocalDate baseDate = LocalDate.now();
            
            for (int i = 0; i < 100; i++) {
                BatchInfo batch = new BatchInfo(
                    "BATCH" + String.format("%03d", i),
                    50 + (i % 50), // Varying quantities
                    baseDate.minusDays(100 - i), // Varying received dates
                    baseDate.plusDays(30 + (i % 60)) // Varying expiry dates
                );
                largeBatchList.add(batch);
            }
            
            long startTime = System.currentTimeMillis();
            
            // When
            List<BatchAllocation> allocations = strategy.allocateStock(largeBatchList, 2000);
            
            long endTime = System.currentTimeMillis();
            
            // Then - Should complete in reasonable time and produce valid allocations
            assertNotNull(allocations);
            assertFalse(allocations.isEmpty());
            
            // Verify performance (should complete within 1 second for 100 batches)
            assertTrue(endTime - startTime < 1000, "Allocation took too long: " + (endTime - startTime) + "ms");
            
            // Verify total allocated quantity
            int totalAllocated = allocations.stream()
                .mapToInt(BatchAllocation::getQuantity)
                .sum();
            assertTrue(totalAllocated <= 2000);
            
            // Verify all allocations are positive
            allocations.forEach(allocation -> {
                assertTrue(allocation.getQuantity() > 0);
                assertNotNull(allocation.getBatchId());
            });
        }
        
        @Test
        @DisplayName("Should maintain allocation order consistency")
        void shouldMaintainAllocationOrderConsistency() {
            // Given
            LocalDate today = LocalDate.now();
            
            BatchInfo batch1 = new BatchInfo("A", 100, today.minusDays(10), today.plusDays(30));
            BatchInfo batch2 = new BatchInfo("B", 100, today.minusDays(8), today.plusDays(25));
            BatchInfo batch3 = new BatchInfo("C", 100, today.minusDays(6), today.plusDays(35));
            
            List<BatchInfo> batches = Arrays.asList(batch1, batch2, batch3);
            
            // When - Multiple calls with same data should produce same result
            List<BatchAllocation> allocations1 = strategy.allocateStock(batches, 150);
            List<BatchAllocation> allocations2 = strategy.allocateStock(batches, 150);
            
            // Then - Results should be identical
            assertEquals(allocations1.size(), allocations2.size());
            
            for (int i = 0; i < allocations1.size(); i++) {
                assertEquals(allocations1.get(i).getBatchId(), allocations2.get(i).getBatchId());
                assertEquals(allocations1.get(i).getQuantity(), allocations2.get(i).getQuantity());
            }
        }
    }
    
    @Nested
    @DisplayName("Strategy Interface Compliance Tests")
    class StrategyInterfaceComplianceTests {
        
        @Test
        @DisplayName("Should implement StockSelectionStrategy interface correctly")
        void shouldImplementStockSelectionStrategyInterfaceCorrectly() {
            assertTrue(strategy instanceof StockSelectionStrategy);
        }
        
        @Test
        @DisplayName("Should provide meaningful strategy name")
        void shouldProvideMeaningfulStrategyName() {
            String strategyName = strategy.getStrategyName();
            assertNotNull(strategyName);
            assertFalse(strategyName.trim().isEmpty());
            assertTrue(strategyName.contains("FIFO") || strategyName.toLowerCase().contains("expiry"));
        }
        
        @Test
        @DisplayName("Should provide strategy description")
        void shouldProvideStrategyDescription() {
            String description = strategy.getDescription();
            assertNotNull(description);
            assertFalse(description.trim().isEmpty());
            assertTrue(description.length() > 20); // Should be meaningful description
        }
    }
}
