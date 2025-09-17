package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import java.util.Objects;

/**
 * Domain entity representing a product in the catalog (Item Master).
 * Minimal fields to support warehouse-first intake and transfers.
 */
public class Item {
    private final Long id; // nullable for new item
    private final ItemCode code;
    private final String name;
    private final Money costPrice;
    private final Money sellingPrice;
    private final boolean perishable;
    private final int reorderPoint;

    private Item(Long id, ItemCode code, String name, Money costPrice, Money sellingPrice,
                 boolean perishable, int reorderPoint) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.costPrice = Objects.requireNonNull(costPrice);
        this.sellingPrice = Objects.requireNonNull(sellingPrice);
        this.perishable = perishable;
        this.reorderPoint = Math.max(0, reorderPoint);
    }

    public static Item create(ItemCode code, String name, Money costPrice, Money sellingPrice,
                              boolean perishable, int reorderPoint) {
        if (name.isBlank()) throw new IllegalArgumentException("Item name cannot be blank");
        if (sellingPrice.toBigDecimal().compareTo(costPrice.toBigDecimal()) < 0) {
            throw new IllegalArgumentException("Selling price must be >= cost price");
        }
        return new Item(null, code, name.trim(), costPrice, sellingPrice, perishable, reorderPoint);
    }

    public Item withId(Long id) { return new Item(id, code, name, costPrice, sellingPrice, perishable, reorderPoint); }

    public Long getId() { return id; }
    public ItemCode getCode() { return code; }
    public String getName() { return name; }
    public Money getCostPrice() { return costPrice; }
    public Money getSellingPrice() { return sellingPrice; }
    public boolean isPerishable() { return perishable; }
    public int getReorderPoint() { return reorderPoint; }
}
