package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Transaction management
 * Represents both POS and Web transactions
 * 
 * Design Pattern: Entity Pattern
 * Clean Architecture: Infrastructure Layer
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user", columnList = "user_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type")
})
public class TransactionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "synex_points_awarded")
    private Integer synexPointsAwarded;
    
    @Column(name = "cash_tendered", precision = 10, scale = 2)
    private BigDecimal cashTendered;
    
    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;
    
    @Column(name = "bill_serial_number")
    private String billSerialNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.COMPLETED;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionItemEntity> items = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum TransactionType {
        POS, WEB
    }
    
    public enum PaymentMethod {
        CASH, CARD
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, VOIDED, RETURNED
    }
    
    // Constructors
    public TransactionEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.transactionDate = LocalDateTime.now();
    }
    
    public TransactionEntity(Long userId, TransactionType transactionType, BigDecimal totalAmount, PaymentMethod paymentMethod) {
        this();
        this.userId = userId;
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }
    
    // PreUpdate callback
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public Integer getSynexPointsAwarded() { return synexPointsAwarded; }
    public void setSynexPointsAwarded(Integer synexPointsAwarded) { this.synexPointsAwarded = synexPointsAwarded; }
    
    public BigDecimal getCashTendered() { return cashTendered; }
    public void setCashTendered(BigDecimal cashTendered) { this.cashTendered = cashTendered; }
    
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    
    public String getBillSerialNumber() { return billSerialNumber; }
    public void setBillSerialNumber(String billSerialNumber) { this.billSerialNumber = billSerialNumber; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public List<TransactionItemEntity> getItems() { return items; }
    public void setItems(List<TransactionItemEntity> items) { this.items = items; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods for business logic
    public void addItem(TransactionItemEntity item) {
        items.add(item);
        item.setTransaction(this);
    }
    
    public void removeItem(TransactionItemEntity item) {
        items.remove(item);
        item.setTransaction(null);
    }
    
    public BigDecimal calculateTotal() {
        return items.stream()
            .map(TransactionItemEntity::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public String toString() {
        return String.format("TransactionEntity{id=%d, type=%s, amount=%s, date=%s}", 
                           transactionId, transactionType, totalAmount, transactionDate);
    }
}
