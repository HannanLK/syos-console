package com.syos.shared.enums;

/**
 * Enum representing product status in the SYOS system.
 * Controls product lifecycle and availability.
 */
public enum ProductStatus {
    ACTIVE("Active", "Product is active and available for sale"),
    INACTIVE("Inactive", "Product is temporarily inactive"),
    DISCONTINUED("Discontinued", "Product has been permanently discontinued");

    private final String displayName;
    private final String description;

    ProductStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailableForSale() {
        return this == ACTIVE;
    }

    public boolean canBeReordered() {
        return this == ACTIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
