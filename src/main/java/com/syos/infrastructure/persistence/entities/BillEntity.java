package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Bill management
 * Represents generated bills with PDF storage capability
 * 
 * Design Pattern: Entity Pattern
 * Clean Architecture: Infrastructure Layer
 */
@Entity
@Table(name = "bills", indexes = {
    @Index(name = "idx_bill_serial_number", columnList = "bill_serial_number", unique = true),
    @Index(name = "idx_bill_transaction", columnList = "transaction_id"),
    @Index(name = "idx_bill_date", columnList = "bill_date")
})
public class BillEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;
    
    @Column(name = "bill_serial_number", nullable = false, unique = true)
    private String billSerialNumber;
    
    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;
    
    @Column(name = "bill_date", nullable = false)
    private LocalDateTime billDate;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "cash_tendered", precision = 10, scale = 2)
    private BigDecimal cashTendered;
    
    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;
    
    @Column(name = "synex_points_awarded")
    private Integer synexPointsAwarded;
    
    @Lob
    @Column(name = "pdf_content", columnDefinition = "BYTEA")
    private byte[] pdfContent;
    
    @Column(name = "pdf_file_path")
    private String pdfFilePath;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public BillEntity() {
        this.createdAt = LocalDateTime.now();
        this.billDate = LocalDateTime.now();
    }
    
    public BillEntity(String billSerialNumber, TransactionEntity transaction) {
        this();
        this.billSerialNumber = billSerialNumber;
        this.transaction = transaction;
        this.totalAmount = transaction.getTotalAmount();
        this.discountAmount = transaction.getDiscountAmount();
        this.cashTendered = transaction.getCashTendered();
        this.changeAmount = transaction.getChangeAmount();
        this.synexPointsAwarded = transaction.getSynexPointsAwarded();
    }
    
    // Getters and Setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    
    public String getBillSerialNumber() { return billSerialNumber; }
    public void setBillSerialNumber(String billSerialNumber) { this.billSerialNumber = billSerialNumber; }
    
    public TransactionEntity getTransaction() { return transaction; }
    public void setTransaction(TransactionEntity transaction) { this.transaction = transaction; }
    
    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public BigDecimal getCashTendered() { return cashTendered; }
    public void setCashTendered(BigDecimal cashTendered) { this.cashTendered = cashTendered; }
    
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    
    public Integer getSynexPointsAwarded() { return synexPointsAwarded; }
    public void setSynexPointsAwarded(Integer synexPointsAwarded) { this.synexPointsAwarded = synexPointsAwarded; }
    
    public byte[] getPdfContent() { return pdfContent; }
    public void setPdfContent(byte[] pdfContent) { this.pdfContent = pdfContent; }
    
    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Business logic methods
    public BigDecimal getFinalAmount() {
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        return totalAmount.subtract(discount);
    }
    
    public boolean hasPdfStored() {
        return pdfContent != null && pdfContent.length > 0;
    }
    
    @Override
    public String toString() {
        return String.format("BillEntity{billId=%d, serialNumber='%s', totalAmount=%s, date=%s}", 
                           billId, billSerialNumber, totalAmount, billDate);
    }
}
