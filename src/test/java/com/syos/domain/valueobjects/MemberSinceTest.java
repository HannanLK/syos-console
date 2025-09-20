package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MemberSince Value Object")
class MemberSinceTest {

    @Test
    @DisplayName("creation from LocalDateTime and CreatedAt with equality")
    void creationAndEquality() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 12, 0);
        MemberSince ms1 = MemberSince.of(dt);
        CreatedAt ca = CreatedAt.of(dt);
        MemberSince ms2 = MemberSince.fromCreatedAt(ca);

        assertThat(ms1.getValue()).isEqualTo(dt);
        assertThat(ms1).isEqualTo(ms2);
        assertThat(ms1.hashCode()).isEqualTo(ms2.hashCode());
        assertThat(ms1.toString()).contains("2024-01-01T12:00");
    }

    @Test
    @DisplayName("now() should create approximately current time and not throw")
    void nowCreation() {
        assertThatNoException().isThrownBy(MemberSince::now);
    }

    @Test
    @DisplayName("should reject null inputs")
    void rejectsNull() {
        assertThatThrownBy(() -> MemberSince.of(null)).isInstanceOf(NullPointerException.class);
    }
}
