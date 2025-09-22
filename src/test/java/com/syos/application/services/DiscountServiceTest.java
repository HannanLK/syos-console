package com.syos.application.services;

import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionEntity;
import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionType;
import com.syos.infrastructure.persistence.repositories.JpaPromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscountService Unit Tests")
class DiscountServiceTest {

    @Mock
    private JpaPromotionRepository promoRepo;

    private DiscountService service;

    @BeforeEach
    void setUp() {
        service = new DiscountService(promoRepo);
    }

    private PromotionEntity buildPromotion(PromotionType type, BigDecimal value) {
        PromotionEntity p = new PromotionEntity();
        p.setPromotionType(type);
        p.setDiscountValue(value);
        // Other fields (dates/flags) are enforced by repository; mocked method bypasses that filtering
        return p;
    }

    @Test
    @DisplayName("Percentage promotion applies correct discount")
    void percentageDiscount() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.of(buildPromotion(PromotionType.PERCENTAGE, new BigDecimal("10")))); // 10%

        BigDecimal unitPrice = new BigDecimal("250.00");
        double qty = 3; // gross = 750.00, 10% = 75.00
        BigDecimal discount = service.calculateBatchDiscount(1L, 100L, unitPrice, qty);
        assertEquals(new BigDecimal("75.00"), discount);
    }

    @Test
    @DisplayName("Fixed amount promotion applies per-unit discount")
    void fixedAmountPerUnitDiscount() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.of(buildPromotion(PromotionType.FIXED_AMOUNT, new BigDecimal("15")))); // 15 per unit

        BigDecimal unitPrice = new BigDecimal("200.00");
        double qty = 4; // discount = 60.00
        BigDecimal discount = service.calculateBatchDiscount(2L, 200L, unitPrice, qty);
        assertEquals(new BigDecimal("60.00"), discount);
    }

    @Test
    @DisplayName("No active promotion yields zero discount")
    void noPromotion() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.empty());

        BigDecimal discount = service.calculateBatchDiscount(3L, 300L, new BigDecimal("99.99"), 2);
        // Use compareTo to ignore scale differences (0 vs 0.00)
        assertEquals(0, discount.compareTo(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("Unsupported promotion type yields zero discount")
    void unsupportedType() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.of(buildPromotion(PromotionType.BUY_X_GET_Y, new BigDecimal("0"))));

        BigDecimal discount = service.calculateBatchDiscount(4L, 400L, new BigDecimal("50.00"), 5);
        assertEquals(new BigDecimal("0.00"), discount);
    }

    @Test
    @DisplayName("Discount never exceeds line gross amount (clamped)")
    void discountClampedToGross() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.of(buildPromotion(PromotionType.FIXED_AMOUNT, new BigDecimal("1000")))); // absurd per-unit

        BigDecimal discount = service.calculateBatchDiscount(5L, 500L, new BigDecimal("20.00"), 2); // gross=40
        assertEquals(new BigDecimal("40.00"), discount);
    }

    @Test
    @DisplayName("Negative discount values are treated as zero")
    void negativeDiscountHandledAsZero() {
        when(promoRepo.findActiveBatchPromotionForItemAndBatch(any(), any(), any()))
                .thenReturn(Optional.of(buildPromotion(PromotionType.PERCENTAGE, new BigDecimal("-5"))));

        BigDecimal discount = service.calculateBatchDiscount(6L, 600L, new BigDecimal("100.00"), 1);
        assertEquals(new BigDecimal("0.00"), discount);
    }
}
