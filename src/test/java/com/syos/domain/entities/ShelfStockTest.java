package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ShelfStock Entity")
class ShelfStockTest {

    private ShelfStock buildSample(double qty, double price, LocalDateTime expiry) {
        return new ShelfStock.Builder()
                .itemCode(ItemCode.of("ITM001"))
                .itemId(1L)
                .batchId(10L)
                .shelfCode("A1")
                .quantityOnShelf(Quantity.of(BigDecimal.valueOf(qty)))
                .expiryDate(expiry)
                .placedBy(UserID.of(100L))
                .unitPrice(new Money(BigDecimal.valueOf(price)))
                .lastUpdatedBy(UserID.of(100L))
                .build();
    }

    @Test
    @DisplayName("sellStock reduces quantity and updates lastUpdatedBy")
    void sellStockReducesQuantity() {
        ShelfStock ss = buildSample(10, 250.0, LocalDateTime.now().plusDays(30));
        ShelfStock after = ss.sellStock(Quantity.of(BigDecimal.valueOf(3)), UserID.of(200L));
        assertThat(after.getQuantityOnShelf().toBigDecimal()).isEqualByComparingTo("7");
        assertThat(after.getLastUpdatedBy()).isEqualTo(UserID.of(200L));
    }

    @Test
    @DisplayName("selling more than available throws")
    void cannotOversell() {
        ShelfStock ss = buildSample(2, 100, LocalDateTime.now().plusDays(2));
        assertThatThrownBy(() -> ss.sellStock(Quantity.of(BigDecimal.valueOf(3)), UserID.of(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot sell more than available");
    }

    @Test
    @DisplayName("restock increases quantity and enforces max level when set")
    void restockAndMaxLevel() {
        ShelfStock ss = new ShelfStock.Builder(buildSample(2, 100, LocalDateTime.now().plusDays(10)))
                .maximumStockLevel(Quantity.of(BigDecimal.valueOf(5)))
                .lastUpdatedBy(UserID.of(1L))
                .build();
        ShelfStock after = ss.restockShelf(Quantity.of(BigDecimal.valueOf(3)), UserID.of(2L));
        assertThat(after.getQuantityOnShelf().toBigDecimal()).isEqualByComparingTo("5");
        assertThatThrownBy(() -> after.restockShelf(Quantity.of(BigDecimal.ONE), UserID.of(3L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceed maximum");
    }

    @Test
    @DisplayName("updatePrice validates positive and updates value")
    void updatePrice() {
        ShelfStock ss = buildSample(1, 50, LocalDateTime.now().plusDays(1));
        ShelfStock after = ss.updatePrice(new Money(BigDecimal.valueOf(75)), UserID.of(9L));
        assertThat(after.getUnitPrice().toBigDecimal()).isEqualByComparingTo("75");
        assertThatThrownBy(() -> ss.updatePrice(Money.zero(), UserID.of(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("expiry flags and availability")
    void expiryAndAvailability() {
        ShelfStock soon = buildSample(2, 100, LocalDateTime.now().plusDays(2));
        ShelfStock expired = buildSample(2, 100, LocalDateTime.now().minusHours(1));
        ShelfStock noQty = buildSample(0, 100, LocalDateTime.now().plusDays(10));
        assertThat(soon.isExpiringSoon()).isTrue();
        assertThat(expired.isExpired()).isTrue();
        assertThat(expired.isAvailableForSale()).isFalse();
        assertThat(noQty.isAvailableForSale()).isFalse();
    }

    @Test
    @DisplayName("total value = unitPrice * qty")
    void totalValue() {
        ShelfStock ss = buildSample(3, 199.99, LocalDateTime.now().plusDays(5));
        assertThat(ss.getTotalValue().toBigDecimal())
                .isEqualByComparingTo(BigDecimal.valueOf(199.99).multiply(BigDecimal.valueOf(3)));
    }
}
