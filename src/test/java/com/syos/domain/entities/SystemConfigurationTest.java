package com.syos.domain.entities;

import com.syos.domain.valueobjects.SynexPointsConfiguration;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SystemConfiguration Entity")
class SystemConfigurationTest {

    @Test
    @DisplayName("create, update, activate/deactivate")
    void createUpdateToggle() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.defaultConfiguration();
        SystemConfiguration sys = SystemConfiguration.createSynexPointsConfiguration(cfg, UserID.of(1L));
        assertThat(sys.isSynexPointsConfiguration()).isTrue();
        assertThat(sys.isActive()).isTrue();
        assertThat(sys.getConfigurationSummary()).contains("Synex Points Configuration");

        SynexPointsConfiguration newCfg = cfg.withRate(new BigDecimal("0.02"), "admin");
        SystemConfiguration updated = sys.updatePointsConfiguration(newCfg, UserID.of(2L));
        assertThat(updated.getPointsConfiguration().getPointsRateAsPercentage()).isEqualTo("2.00%");
        assertThat(updated.getLastModifiedBy()).isEqualTo(UserID.of(2L));

        SystemConfiguration deactivated = updated.setActiveStatus(false, UserID.of(3L));
        assertThat(deactivated.isActive()).isFalse();
        
        // Cannot update an inactive configuration
        assertThatThrownBy(() -> deactivated.updatePointsConfiguration(newCfg, UserID.of(1L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("validation: key and lastModifiedBy are required; points config required for SP key")
    void validation() {
        assertThatThrownBy(() -> new SystemConfiguration.Builder()
                .configurationKey(SystemConfiguration.SYNEX_POINTS_CONFIG_KEY)
                .lastModifiedBy(UserID.of(1L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points configuration cannot be null");

        assertThatThrownBy(() -> new SystemConfiguration.Builder()
                .pointsConfiguration(SynexPointsConfiguration.defaultConfiguration())
                .lastModifiedBy(UserID.of(1L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Configuration key");

        assertThatThrownBy(() -> new SystemConfiguration.Builder()
                .configurationKey(SystemConfiguration.SYNEX_POINTS_CONFIG_KEY)
                .pointsConfiguration(SynexPointsConfiguration.defaultConfiguration())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Last modified by");
    }
}
