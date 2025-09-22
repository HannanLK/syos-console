package com.syos.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Supplier Entity")
class SupplierTest {

    @Test
    @DisplayName("create, update contact info, deactivate/activate")
    void lifecycle() {
        Supplier s = Supplier.create("SUP01", "Ceylon Supplies", "+94-11-1234567", "info@cs.lk", "Colombo", "Amal");
        assertThat(s.isActive()).isTrue();
        assertThat(s.getSupplierCode()).isEqualTo("SUP01");

        Supplier updated = s.updateContactInfo("011-7654321", "sales@cs.lk", "Kandy", "Kamal");
        assertThat(updated.getSupplierPhone()).contains("7654");
        assertThat(updated.getSupplierAddress()).isEqualTo("Kandy");

        Supplier deactivated = updated.deactivate();
        assertThat(deactivated.isActive()).isFalse();
        Supplier activated = deactivated.activate();
        assertThat(activated.isActive()).isTrue();
    }

    @Test
    @DisplayName("validation: code/name/phone required")
    void validation() {
        assertThatThrownBy(() -> Supplier.create(" ", "Name", "011", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Supplier.create("SUP02", " ", "011", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Supplier.create("SUP02", "Name", " ", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
