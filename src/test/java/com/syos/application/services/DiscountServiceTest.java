package com.syos.application.services;

import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionEntity;
import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionType;
import com.syos.infrastructure.persistence.repositories.JpaPromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class DiscountServiceTest {

    JpaPromotionRepository repo;
    DiscountService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(JpaPromotionRepository.class);
        service = new DiscountService(repo);
    }

    private static PromotionEntity mkPromo(PromotionType type, String value) {
        PromotionEntity p = new PromotionEntity();
        p.setPromotionType(type);
        p.setDiscountValue(new BigDecimal(value));
        p.setStartDate(LocalDateTime.now().minusDays(1));
        p.setEndDate(LocalDateTime.now().plusDays(1));
        p.setActive(true);
        p.setBatchSpecific(true);
        return p;
    }

    @Test
    void calculateBatchDiscount_percentage_and_fixed_and_none_and_clamp() {
        // percentage 10% on 100x2 -> 20.00
        when(repo.findActiveBatchPromotionForItemAndBatch(anyLong(), anyLong(), any())).thenReturn(Optional.of(mkPromo(PromotionType.PERCENTAGE, "10")));
        BigDecimal d1 = service.calculateBatchDiscount(1L, 1L, new BigDecimal("100.00"), 2);
        assertEquals(new BigDecimal("20.00"), d1);

        // fixed 5 per unit on 3 units -> 15.00
        when(repo.findActiveBatchPromotionForItemAndBatch(anyLong(), anyLong(), any())).thenReturn(Optional.of(mkPromo(PromotionType.FIXED_AMOUNT, "5.00")));
        BigDecimal d2 = service.calculateBatchDiscount(1L, 1L, new BigDecimal("100.00"), 3);
        assertEquals(new BigDecimal("15.00"), d2);

        // unsupported type -> zero
        PromotionEntity unsupported = mkPromo(PromotionType.BUY_X_GET_Y, "0");
        when(repo.findActiveBatchPromotionForItemAndBatch(anyLong(), anyLong(), any())).thenReturn(Optional.of(unsupported));
        BigDecimal d3 = service.calculateBatchDiscount(1L, 1L, new BigDecimal("50.00"), 1);
        assertEquals(0, d3.compareTo(new BigDecimal("0.00")));

        // none -> zero
        when(repo.findActiveBatchPromotionForItemAndBatch(anyLong(), anyLong(), any())).thenReturn(Optional.empty());
        BigDecimal d4 = service.calculateBatchDiscount(1L, 1L, new BigDecimal("50.00"), 1);
        assertEquals(0, d4.compareTo(new BigDecimal("0.00")));

        // clamp: discount larger than gross
        PromotionEntity percent200 = mkPromo(PromotionType.PERCENTAGE, "200"); // 200% -> clamp to gross
        when(repo.findActiveBatchPromotionForItemAndBatch(anyLong(), anyLong(), any())).thenReturn(Optional.of(percent200));
        BigDecimal d5 = service.calculateBatchDiscount(1L, 1L, new BigDecimal("10.00"), 1);
        assertEquals(0, d5.compareTo(new BigDecimal("10.00")));
    }
}
