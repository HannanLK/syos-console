package com.syos.shared.enums;

/**
 * Enum representing units of measure for products in SYOS.
 * Supports various measurement types for different product categories.
 */
public enum UnitOfMeasure {
    EACH("Each", "Individual items", false),
    PACK("Pack", "Package or bundle", false),
    KG("Kg", "Kilogram", true),
    G("g", "Gram", true),
    L("L", "Liter", true),
    ML("mL", "Milliliter", true),
    BOX("Box", "Box packaging", false);

    private final String displayName;
    private final String description;
    private final boolean isWeight;

    UnitOfMeasure(String displayName, String description, boolean isWeight) {
        this.displayName = displayName;
        this.description = description;
        this.isWeight = isWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isWeightMeasure() {
        return isWeight;
    }

    public boolean isVolumeMeasure() {
        return this == L || this == ML;
    }

    public boolean isCountMeasure() {
        return this == EACH || this == PACK || this == BOX;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
