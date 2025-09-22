package com.syos.application.services;

import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionEntity;
import com.syos.infrastructure.persistence.entities.PromotionEntities.PromotionType;
import com.syos.infrastructure.persistence.repositories.JpaPromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Batch-aware discount calculator using Promotion + promotion_batches (Option A).
 */
public class DiscountService {
    private final JpaPromotionRepository promoRepo;

    public DiscountService(JpaPromotionRepository promoRepo) {
        this.promoRepo = promoRepo;
    }

    /**
     * Calculate discount amount for a line fulfilled by a single batch.
     * @param itemId item id
     * @param batchId batch id
     * @param unitPrice price per unit
     * @param quantity quantity as double (matches POSCommand usage)
     * @return discount amount (>=0)
     */
    public BigDecimal calculateBatchDiscount(Long itemId, Long batchId, BigDecimal unitPrice, double quantity) {
        Optional<PromotionEntity> promoOpt = promoRepo.findActiveBatchPromotionForItemAndBatch(itemId, batchId, LocalDateTime.now());
        if (promoOpt.isEmpty()) return BigDecimal.ZERO;
        PromotionEntity p = promoOpt.get();
        BigDecimal qty = BigDecimal.valueOf(quantity);
        BigDecimal gross = unitPrice.multiply(qty);
        BigDecimal discount;
        if (p.getPromotionType() == PromotionType.PERCENTAGE) {
            // discount_value is percentage (e.g., 10 for 10%)
            discount = gross.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (p.getPromotionType() == PromotionType.FIXED_AMOUNT) {
            // interpret as fixed amount per unit
            BigDecimal perUnit = p.getDiscountValue();
            discount = perUnit.multiply(qty);
        } else {
            // Unsupported types treated as no discount for now
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(gross) > 0) discount = gross; // clamp
        if (discount.signum() < 0) discount = BigDecimal.ZERO;
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
