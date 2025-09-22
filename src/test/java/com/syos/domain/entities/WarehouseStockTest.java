package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WarehouseStock Entity")
class WarehouseStockTest {

    private WarehouseStock sample(double received, double available, LocalDateTime expiry) {
        return WarehouseStock.builder()
                .itemCode(ItemCode.of("ITM-WS-1"))
                .itemId(1L)
                .batchId(9L)
                .quantityReceived(Quantity.of(BigDecimal.valueOf(received)))
                .quantityAvailable(Quantity.of(BigDecimal.valueOf(available)))
                .receivedDate(LocalDateTime.now().minusDays(1))
                .expiryDate(expiry)
                .receivedBy(UserID.of(1L))
                .lastUpdatedBy(UserID.of(1L))
                .build();
    }

    @Test
    @DisplayName("reserve and cancel reservation")
    void reserveAndCancel() {
        WarehouseStock ws = sample(10, 10, LocalDateTime.now().plusDays(20));
        WarehouseStock reserved = ws.reserve(Quantity.of(BigDecimal.valueOf(5)), UserID.of(5L));
        assertThat(reserved.isReserved()).isTrue();
        assertThat(reserved.getReservedBy()).isEqualTo(UserID.of(5L));

        WarehouseStock cancelled = reserved.cancelReservation(UserID.of(6L));
        assertThat(cancelled.isReserved()).isFalse();
        assertThat(cancelled.getReservedBy()).isNull();
    }

    @Test
    @DisplayName("transfer reduces available and clears reservation")
    void transfer() {
        WarehouseStock ws = sample(10, 10, LocalDateTime.now().plusDays(20)).reserve(Quantity.of(BigDecimal.valueOf(4)), UserID.of(2L));
        WarehouseStock after = ws.transfer(Quantity.of(BigDecimal.valueOf(3)), UserID.of(3L));
        assertThat(after.getQuantityAvailable().toBigDecimal()).isEqualByComparingTo("7");
        assertThat(after.isReserved()).isFalse();
    }

    @Test
    @DisplayName("availability and expiry flags")
    void flags() {
        WarehouseStock ws = sample(1, 1, LocalDateTime.now().plusDays(3));
        WarehouseStock expired = sample(1, 1, LocalDateTime.now().minusHours(1));
        WarehouseStock zero = sample(1, 0, LocalDateTime.now().plusDays(10));
        assertThat(ws.isExpiringSoon()).isTrue();
        assertThat(expired.isExpired()).isTrue();
        assertThat(zero.isAvailableForTransfer()).isFalse();
    }

    @Test
    @DisplayName("validation: available cannot exceed received")
    void validation() {
        assertThatThrownBy(() -> sample(5, 6, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }
}
