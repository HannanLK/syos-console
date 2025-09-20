package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SupplierId Value Object")
class SupplierIdTest {

    @Test
    void creationAndEquality() {
        SupplierId id1 = SupplierId.of(1L);
        SupplierId id2 = SupplierId.of(1L);
        SupplierId id3 = SupplierId.of(2L);

        assertThat(id1.getValue()).isEqualTo(1L);
        assertThat(id1).isEqualTo(id2).isNotEqualTo(id3);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1.toString()).contains("SupplierId{");
    }

    @Test
    void validation() {
        assertThatThrownBy(() -> SupplierId.of(0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SupplierId.of(-1L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SupplierId.of(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
