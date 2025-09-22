package com.syos.domain.entities;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Batch Entity")
class BatchTest {

    private Batch batch(LocalDateTime expiry, double qty, String number) {
        return new Batch.Builder()
                .itemId(1L)
                .batchNumber(number)
                .quantityReceived(Quantity.of(BigDecimal.valueOf(qty)))
                .quantityAvailable(Quantity.of(BigDecimal.valueOf(qty)))
                .manufactureDate(LocalDate.now().minusDays(30))
                .expiryDate(expiry)
                .receivedDate(LocalDate.now().minusDays(15))
                .receivedBy(UserID.of(1L))
                .costPerUnit(Money.of(new BigDecimal("100.00")))
                .createdAt(LocalDateTime.now().minusDays(15))
                .updatedAt(LocalDateTime.now().minusDays(15))
                .build();
    }

    @Test
    @DisplayName("reduceQuantity and availability")
    void reduceQuantity() {
        Batch b = batch(LocalDateTime.now().plusDays(10), 10, "B1");
        Batch after = b.reduceQuantity(Quantity.of(BigDecimal.valueOf(3)));
        assertThat(after.getQuantityAvailable().toBigDecimal()).isEqualByComparingTo("7");
        assertThat(after.hasAvailableStock()).isTrue();
        Batch empty = after.reduceQuantity(Quantity.of(BigDecimal.valueOf(7)));
        assertThat(empty.getQuantityAvailable().toBigDecimal()).isEqualByComparingTo("0");
        assertThat(empty.hasAvailableStock()).isFalse();
        assertThatThrownBy(() -> empty.reduceQuantity(Quantity.of(BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("expiry and selection rule: expiry override beats FIFO when sooner")
    void selectionRuleExpiryOverride() {
        // Older batch A (older received), later expiry
        Batch A = new Batch.Builder(batch(LocalDateTime.now().plusDays(30), 5, "A")).createdAt(LocalDateTime.now().minusDays(20)).build();
        // Newer batch B (newer received), but expiring sooner -> should be selected first
        Batch B = new Batch.Builder(batch(LocalDateTime.now().plusDays(5), 5, "B")).createdAt(LocalDateTime.now().minusDays(10)).build();

        assertThat(B.shouldBeSelectedBefore(A)).isTrue();
        assertThat(A.shouldBeSelectedBefore(B)).isFalse();
    }
}
