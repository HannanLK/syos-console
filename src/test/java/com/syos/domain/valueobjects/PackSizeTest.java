package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PackSize Value Object")
class PackSizeTest {

    @Test
    @DisplayName("creation and accessors")
    void creation() {
        PackSize p1 = PackSize.of(6);
        PackSize p2 = PackSize.of(1.5);
        PackSize p3 = PackSize.of(new BigDecimal("2.25"));

        assertThat(p1.getValue()).isEqualByComparingTo("6");
        assertThat(p2.getDoubleValue()).isEqualTo(1.5);
        assertThat(p3.toString()).isEqualTo("2.25");
    }

    @Test
    @DisplayName("comparisons and equality")
    void comparisons() {
        PackSize small = PackSize.of(1);
        PackSize large = PackSize.of(10);
        assertThat(large.isGreaterThan(small)).isTrue();
        assertThat(small.isLessThan(large)).isTrue();
        assertThat(PackSize.of(1)).isEqualTo(small);
        assertThat(PackSize.of(1).hashCode()).isEqualTo(small.hashCode());
    }

    @Test
    @DisplayName("validation of positive values")
    void validation() {
        assertThatThrownBy(() -> PackSize.of(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PackSize.of(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PackSize.of((BigDecimal) null)).isInstanceOf(IllegalArgumentException.class);
    }
}
