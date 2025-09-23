package com.syos.domain.entities;

import com.syos.domain.valueobjects.SynexPointsConfiguration;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigurationNegativeTest {

    @Test
    void update_whenInactive_shouldThrow() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.defaultConfiguration();
        SystemConfiguration sc = SystemConfiguration.createSynexPointsConfiguration(cfg, UserID.of(1L));
        SystemConfiguration inactive = sc.setActiveStatus(false, UserID.of(2L));

        SynexPointsConfiguration newCfg = cfg.withRate(new BigDecimal("0.02"), "ADMIN");
        assertThrows(IllegalStateException.class, () -> inactive.updatePointsConfiguration(newCfg, UserID.of(3L)));
    }

    @Test
    void builder_validationFailures() {
        // Last modified by cannot be null
        assertThrows(IllegalArgumentException.class, () -> new SystemConfiguration.Builder()
                .configurationKey(SystemConfiguration.SYNEX_POINTS_CONFIG_KEY)
                .pointsConfiguration(SynexPointsConfiguration.defaultConfiguration())
                .lastModifiedBy(null)
                .build());

        // Points configuration cannot be null when key is Synex Points
        assertThrows(IllegalArgumentException.class, () -> new SystemConfiguration.Builder()
                .configurationKey(SystemConfiguration.SYNEX_POINTS_CONFIG_KEY)
                .pointsConfiguration(null)
                .lastModifiedBy(UserID.of(1L))
                .build());
    }
}
