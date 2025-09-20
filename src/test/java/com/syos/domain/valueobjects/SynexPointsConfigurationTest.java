package com.syos.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SynexPointsConfiguration Value Object")
class SynexPointsConfigurationTest {

    @Test
    @DisplayName("default configuration should be active and calculate 1% with threshold")
    void defaultConfig() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.defaultConfiguration();
        // Below threshold (100.00) -> 0 points
        assertThat(cfg.qualifiesForPoints(Money.of(new BigDecimal("99.99")))).isFalse();
        assertThat(cfg.calculatePoints(Money.of(new BigDecimal("99.99"))).getValue()).isEqualByComparingTo("0.00");

        // At threshold -> earns points
        SynexPoints pts = cfg.calculatePoints(Money.of(new BigDecimal("100.00")));
        assertThat(pts.getValue()).isEqualByComparingTo("1.00");
        assertThat(cfg.getPointsRateAsPercentage()).isEqualTo("1.00%");
    }

    @Test
    @DisplayName("custom configuration enforces maximum points cap")
    void maxCapApplied() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.of(
                new BigDecimal("0.05"), // 5%
                new BigDecimal("100.00"),
                new BigDecimal("10.00"), // cap at 10 points
                true,
                "tester");

        SynexPoints pts = cfg.calculatePoints(Money.of(new BigDecimal("1000.00"))); // would be 50, but cap => 10
        assertThat(pts.getValue()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("inactive configuration should always return zero points and not qualify")
    void inactiveConfig() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.of(
                new BigDecimal("0.02"), new BigDecimal("100.00"), new BigDecimal("500.00"), false, "tester");

        assertThat(cfg.qualifiesForPoints(Money.of(new BigDecimal("1000.00")))).isFalse();
        assertThat(cfg.calculatePoints(Money.of(new BigDecimal("1000.00"))).getValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("withRate and withThreshold should create updated immutable instances")
    void immutabilityUpdates() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.defaultConfiguration();
        SynexPointsConfiguration cfg2 = cfg.withRate(new BigDecimal("0.02"), "admin");
        SynexPointsConfiguration cfg3 = cfg2.withThreshold(new BigDecimal("200.00"), "admin");

        assertThat(cfg2.getPointsPercentageRate()).isEqualByComparingTo("0.0200");
        assertThat(cfg3.getMinimumSpendingThreshold()).isEqualByComparingTo("200.00");
        // original unchanged
        assertThat(cfg.getPointsPercentageRate()).isEqualByComparingTo("0.0100");
        assertThat(cfg.getMinimumSpendingThreshold()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("validation rules should reject invalid inputs")
    void validation() {
        // Rate negative
        assertThatThrownBy(() -> SynexPointsConfiguration.of(
                new BigDecimal("-0.01"), new BigDecimal("100.00"), new BigDecimal("10.00"), true, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        // Rate > 100%
        assertThatThrownBy(() -> SynexPointsConfiguration.of(
                new BigDecimal("1.01"), new BigDecimal("100.00"), new BigDecimal("10.00"), true, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        // Threshold negative
        assertThatThrownBy(() -> SynexPointsConfiguration.of(
                new BigDecimal("0.01"), new BigDecimal("-1.00"), new BigDecimal("10.00"), true, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        // Max points non-positive
        assertThatThrownBy(() -> SynexPointsConfiguration.of(
                new BigDecimal("0.01"), new BigDecimal("100.00"), new BigDecimal("0.00"), true, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
