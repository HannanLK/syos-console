package com.syos.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for WarehouseStock
 * Maps to warehouse_stock table in database
 */
@Entity
@Table(name = "warehouse_stock")
public class WarehouseStockEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Some databases created earlier migrations with a warehouse_code column (NOT NULL)
    // We include it here to satisfy constraints if present.
    @Column(name = "warehouse_code")
    private String warehouseCode;
    
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    // Original normalized FK to locations table (required by trigger)
    @Column(name = "location_id")
    private Long locationId;
    
    @Column(name = "quantity_received", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityReceived;
    
    @Column(name = "quantity_available", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityAvailable;
    
    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "received_by", nullable = false)
    private Long receivedBy;

    // Convenience denormalized location name (if the column exists)
    @Column(name = "location", length = 50)
    private String location;
    
    @Column(name = "is_reserved", nullable = false)
    private Boolean isReserved = false;
    
    @Column(name = "reserved_by")
    private Long reservedBy;
    
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "last_updated_by", nullable = false)
    private Long lastUpdatedBy;

    // Constructors
    public WarehouseStockEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(BigDecimal quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public LocalDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDateTime receivedDate) { this.receivedDate = receivedDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Long getReceivedBy() { return receivedBy; }
    public void setReceivedBy(Long receivedBy) { this.receivedBy = receivedBy; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsReserved() { return isReserved; }
    public void setIsReserved(Boolean isReserved) { this.isReserved = isReserved; }

    public Long getReservedBy() { return reservedBy; }
    public void setReservedBy(Long reservedBy) { this.reservedBy = reservedBy; }

    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Long getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(Long lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
