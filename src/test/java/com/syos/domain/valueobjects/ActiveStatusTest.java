package com.syos.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ActiveStatusTest {

    @Test
    void basicBehavior() {
        ActiveStatus a = ActiveStatus.active();
        ActiveStatus b = ActiveStatus.inactive();
        assertThat(a.isActive()).isTrue();
        assertThat(b.isActive()).isFalse();
        assertThat(ActiveStatus.of(true)).isEqualTo(a);
        assertThat(ActiveStatus.of(false)).isEqualTo(b);
        assertThat(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
        assertThat(a.toString()).isEqualTo("true");
    }
}
