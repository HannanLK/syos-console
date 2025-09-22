package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Minimal JPA entities for Promotions with batch-specific mapping (Option A).
 * These map to V7 promotions/promotion_items and V8 promotion_batches tables.
 */
public final class PromotionEntities {

    public enum PromotionType { PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y }

    @Entity
    @Table(name = "promotions")
    public static class PromotionEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "promo_code", nullable = false, unique = true, length = 20)
        private String promoCode;

        @Column(name = "promo_name", nullable = false)
        private String promoName;

        @Enumerated(EnumType.STRING)
        @Column(name = "promotion_type", nullable = false)
        private PromotionType promotionType;

        @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
        private BigDecimal discountValue;

        @Column(name = "start_date", nullable = false)
        private LocalDateTime startDate;

        @Column(name = "end_date", nullable = false)
        private LocalDateTime endDate;

        @Column(name = "is_active")
        private boolean active = true;

        @Column(name = "is_batch_specific")
        private boolean batchSpecific = false;

        @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PromotionItemEntity> items = new HashSet<>();

        @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PromotionBatchEntity> batches = new HashSet<>();

        public Long getId() { return id; }
        public String getPromoCode() { return promoCode; }
        public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
        public String getPromoName() { return promoName; }
        public void setPromoName(String promoName) { this.promoName = promoName; }
        public PromotionType getPromotionType() { return promotionType; }
        public void setPromotionType(PromotionType promotionType) { this.promotionType = promotionType; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isBatchSpecific() { return batchSpecific; }
        public void setBatchSpecific(boolean batchSpecific) { this.batchSpecific = batchSpecific; }
    }

    @Entity
    @Table(name = "promotion_items")
    public static class PromotionItemEntity {
        @EmbeddedId
        private PromotionItemPK id = new PromotionItemPK();

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("promotionId")
        @JoinColumn(name = "promotion_id")
        private PromotionEntity promotion;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        public PromotionItemEntity() {}
        public PromotionItemEntity(PromotionEntity promotion, Long itemId) {
            this.promotion = promotion;
            this.id = new PromotionItemPK(promotion.getId(), itemId);
        }
        public PromotionItemPK getId() { return id; }
        public PromotionEntity getPromotion() { return promotion; }
        public void setPromotion(PromotionEntity promotion) { this.promotion = promotion; }
    }

    @Embeddable
    public static class PromotionItemPK implements java.io.Serializable {
        @Column(name = "promotion_id")
        private Long promotionId;
        @Column(name = "item_id")
        private Long itemId;
        public PromotionItemPK() {}
        public PromotionItemPK(Long promotionId, Long itemId) { this.promotionId = promotionId; this.itemId = itemId; }
        public Long getPromotionId() { return promotionId; }
        public Long getItemId() { return itemId; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PromotionItemPK)) return false; PromotionItemPK that = (PromotionItemPK) o; return java.util.Objects.equals(promotionId, that.promotionId) && java.util.Objects.equals(itemId, that.itemId); }
        @Override public int hashCode() { return java.util.Objects.hash(promotionId, itemId); }
    }

    @Entity
    @Table(name = "promotion_batches")
    public static class PromotionBatchEntity {
        @EmbeddedId
        private PromotionBatchPK id = new PromotionBatchPK();

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("promotionId")
        @JoinColumn(name = "promotion_id")
        private PromotionEntity promotion;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        public PromotionBatchEntity() {}
        public PromotionBatchEntity(PromotionEntity promotion, Long batchId) {
            this.promotion = promotion;
            this.id = new PromotionBatchPK(promotion.getId(), batchId);
        }
        public PromotionBatchPK getId() { return id; }
        public PromotionEntity getPromotion() { return promotion; }
        public void setPromotion(PromotionEntity promotion) { this.promotion = promotion; }
    }

    @Embeddable
    public static class PromotionBatchPK implements java.io.Serializable {
        @Column(name = "promotion_id")
        private Long promotionId;
        @Column(name = "batch_id")
        private Long batchId;
        public PromotionBatchPK() {}
        public PromotionBatchPK(Long promotionId, Long batchId) { this.promotionId = promotionId; this.batchId = batchId; }
        public Long getPromotionId() { return promotionId; }
        public Long getBatchId() { return batchId; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PromotionBatchPK)) return false; PromotionBatchPK that = (PromotionBatchPK) o; return java.util.Objects.equals(promotionId, that.promotionId) && java.util.Objects.equals(batchId, that.batchId); }
        @Override public int hashCode() { return java.util.Objects.hash(promotionId, batchId); }
    }
}
