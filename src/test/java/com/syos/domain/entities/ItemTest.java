package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Item Entity")
class ItemTest {

    @Test
    @DisplayName("create with valid prices and perishable flag")
    void createValid() {
        Item item = Item.create(
                ItemCode.of("ITM777"),
                "Sugar 1kg",
                Money.of(new BigDecimal("200.00")),
                Money.of(new BigDecimal("250.00")),
                true,
                50
        );
        assertThat(item.getCode().getValue()).isEqualTo("ITM777");
        assertThat(item.getSellingPrice().isGreaterThan(item.getCostPrice())).isTrue();
    }

    @Test
    @DisplayName("selling price must be >= cost price")
    void invalidPrices() {
        assertThatThrownBy(() -> Item.create(
                ItemCode.of("ITM888"),
                "Oil 1L",
                Money.of(new BigDecimal("500.00")),
                Money.of(new BigDecimal("499.99")),
                false,
                10
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
