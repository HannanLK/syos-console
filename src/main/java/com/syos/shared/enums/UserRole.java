package com.syos.shared.enums;

/**
 * User roles within the SYOS system.
 * Only three roles are supported as per requirements: CUSTOMER, EMPLOYEE, ADMIN.
 */
public enum UserRole {
    CUSTOMER,
    EMPLOYEE,
    ADMIN;

    public static UserRole fromString(String value) {
        if (value == null) throw new IllegalArgumentException("UserRole cannot be null");
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown user role: " + value);
        }
    }
}
