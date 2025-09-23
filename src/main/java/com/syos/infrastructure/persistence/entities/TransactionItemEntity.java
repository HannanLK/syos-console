package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Transaction Items
 * Represents individual items within a transaction
 * 
 * Design Pattern: Entity Pattern
 * Clean Architecture: Infrastructure Layer
 */
@Entity
@Table(name = "transaction_items", indexes = {
    @Index(name = "idx_transaction_item_transaction", columnList = "transaction_id"),
    @Index(name = "idx_transaction_item_item", columnList = "item_id")
})
public class TransactionItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_item_id")
    private Long transactionItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemMasterFileEntity item;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    // Align with DB column name
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountApplied;

    // Required by DB schema (NOT NULL)
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TransactionItemEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TransactionItemEntity(ItemMasterFileEntity item, Integer quantity, BigDecimal unitPrice) {
        this();
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        recalcTotal();
    }
    
    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        recalcTotal();
    }
    
    // Getters and Setters
    public Long getTransactionItemId() { return transactionItemId; }
    public void setTransactionItemId(Long transactionItemId) { this.transactionItemId = transactionItemId; }
    
    public TransactionEntity getTransaction() { return transaction; }
    public void setTransaction(TransactionEntity transaction) { this.transaction = transaction; }
    
    public ItemMasterFileEntity getItem() { return item; }
    public void setItem(ItemMasterFileEntity item) { this.item = item; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        recalculateSubtotal();
        recalcTotal();
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        recalculateSubtotal();
        recalcTotal();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; recalcTotal(); }
    
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; recalcTotal(); }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Business logic methods
    private void recalculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private void recalcTotal() {
        BigDecimal discount = discountApplied != null ? discountApplied : BigDecimal.ZERO;
        BigDecimal base = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal total = base.subtract(discount);
        // prevent negative totals
        if (total.signum() < 0) total = BigDecimal.ZERO;
        this.totalPrice = total;
    }
    
    public BigDecimal getDiscountedSubtotal() {
        BigDecimal discount = discountApplied != null ? discountApplied : BigDecimal.ZERO;
        return subtotal.subtract(discount);
    }
    
    @Override
    public String toString() {
        return String.format("TransactionItemEntity{id=%d, quantity=%d, unitPrice=%s, subtotal=%s}", 
                           transactionItemId, quantity, unitPrice, subtotal);
    }
}
