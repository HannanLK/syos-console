package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WebInventory Entity")
class WebInventoryTest {

    private WebInventory buildSample(double qty, double price, LocalDateTime expiry) {
        return new WebInventory.Builder()
                .itemCode(ItemCode.of("WEB100"))
                .itemId(1L)
                .batchId(2L)
                .quantityAvailable(Quantity.of(BigDecimal.valueOf(qty)))
                .expiryDate(expiry)
                .addedBy(UserID.of(1L))
                .webPrice(new Money(BigDecimal.valueOf(price)))
                .lastUpdatedBy(UserID.of(1L))
                .build();
    }

    @Test
    @DisplayName("sellStock reduces quantity; cannot oversell")
    void sellStock() {
        WebInventory wi = buildSample(5, 1000, LocalDateTime.now().plusDays(7));
        WebInventory after = wi.sellStock(Quantity.of(BigDecimal.valueOf(2)), UserID.of(7L));
        assertThat(after.getQuantityAvailable().toBigDecimal()).isEqualByComparingTo("3");
        assertThatThrownBy(() -> after.sellStock(Quantity.of(BigDecimal.valueOf(4)), UserID.of(7L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("restock and flags")
    void restockAndFlags() {
        WebInventory wi = buildSample(0, 500, LocalDateTime.now().plusDays(1));
        WebInventory restocked = wi.restockWeb(Quantity.of(BigDecimal.valueOf(10)), UserID.of(2L));
        assertThat(restocked.getQuantityAvailable().toBigDecimal()).isEqualByComparingTo("10");
        assertThat(restocked.isLowStock()).isFalse();
        assertThat(restocked.isAvailableForPurchase()).isTrue();
    }

    @Test
    @DisplayName("expiry behavior and total value")
    void expiryAndTotal() {
        WebInventory near = buildSample(1, 250.50, LocalDateTime.now().plusHours(24));
        WebInventory expired = buildSample(1, 100, LocalDateTime.now().minusMinutes(1));
        assertThat(near.isExpiringSoon()).isTrue();
        assertThat(expired.isExpired()).isTrue();
        assertThat(near.getTotalValue().toBigDecimal())
                .isEqualByComparingTo(new BigDecimal("250.50"));
    }
}
