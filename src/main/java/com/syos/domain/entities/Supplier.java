package com.syos.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a supplier in the SYOS system.
 * Manages supplier information and relationships with brands.
 */
public class Supplier {
    private final Long id;
    private final String supplierCode;
    private final String supplierName;
    private final String supplierPhone;
    private final String supplierEmail;
    private final String supplierAddress;
    private final String contactPerson;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Supplier(Long id, String supplierCode, String supplierName, String supplierPhone,
                    String supplierEmail, String supplierAddress, String contactPerson,
                    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.supplierCode = validateSupplierCode(supplierCode);
        this.supplierName = validateSupplierName(supplierName);
        this.supplierPhone = validatePhone(supplierPhone);
        this.supplierEmail = supplierEmail; // Optional field
        this.supplierAddress = supplierAddress;
        this.contactPerson = contactPerson;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    private String validateSupplierCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier code cannot be null or empty");
        }
        return code.trim().toUpperCase();
    }

    private String validateSupplierName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        return name.trim();
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier phone cannot be null or empty");
        }
        return phone.trim();
    }

    /**
     * Factory method to create new supplier
     */
    public static Supplier create(String supplierCode, String supplierName, String supplierPhone,
                                 String supplierEmail, String supplierAddress, String contactPerson) {
        return new Supplier(null, supplierCode, supplierName, supplierPhone,
                           supplierEmail, supplierAddress, contactPerson, true, null, null);
    }

    /**
     * Factory method for repository reconstruction
     */
    public static Supplier reconstruct(Long id, String supplierCode, String supplierName,
                                     String supplierPhone, String supplierEmail, String supplierAddress,
                                     String contactPerson, boolean isActive,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Supplier(id, supplierCode, supplierName, supplierPhone, supplierEmail,
                           supplierAddress, contactPerson, isActive, createdAt, updatedAt);
    }

    public Supplier withId(Long id) {
        return new Supplier(id, supplierCode, supplierName, supplierPhone, supplierEmail,
                           supplierAddress, contactPerson, isActive, createdAt, updatedAt);
    }

    public Supplier updateContactInfo(String newPhone, String newEmail, String newAddress, String newContactPerson) {
        return new Supplier(id, supplierCode, supplierName, newPhone, newEmail,
                           newAddress, newContactPerson, isActive, createdAt, LocalDateTime.now());
    }

    public Supplier deactivate() {
        return new Supplier(id, supplierCode, supplierName, supplierPhone, supplierEmail,
                           supplierAddress, contactPerson, false, createdAt, LocalDateTime.now());
    }

    public Supplier activate() {
        return new Supplier(id, supplierCode, supplierName, supplierPhone, supplierEmail,
                           supplierAddress, contactPerson, true, createdAt, LocalDateTime.now());
    }

    // Getters
    public Long getId() { return id; }
    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierPhone() { return supplierPhone; }
    public String getSupplierEmail() { return supplierEmail; }
    public String getSupplierAddress() { return supplierAddress; }
    public String getContactPerson() { return contactPerson; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(supplierCode, supplier.supplierCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierCode);
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierCode='" + supplierCode + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
