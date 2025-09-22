package com.syos.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Brand Entity")
class BrandTest {

    @Test
    @DisplayName("create and status transitions")
    void lifecycle() {
        Brand b = Brand.create("COCA", "Coca-Cola", "Soft drinks brand");
        assertThat(b.isActive()).isTrue();
        Brand deactivated = b.deactivate();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.activate().isActive()).isTrue();
    }

    @Test
    @DisplayName("validation of code and name")
    void validation() {
        assertThatThrownBy(() -> Brand.create(" ", "Name", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Brand.create("CODE", " ", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
