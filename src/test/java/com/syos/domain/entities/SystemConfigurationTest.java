package com.syos.domain.entities;

import com.syos.domain.valueobjects.SynexPointsConfiguration;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigurationTest {

    @Test
    void create_update_toggle_andSummary() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.defaultConfiguration();
        SystemConfiguration sc = SystemConfiguration.createSynexPointsConfiguration(cfg, UserID.of(1L));
        assertTrue(sc.isActive());
        assertTrue(sc.isSynexPointsConfiguration());
        assertTrue(sc.getConfigurationSummary().contains("SynexPointsConfiguration"));

        var newCfg = cfg.withRate(new BigDecimal("0.05"), "ADMIN");
        SystemConfiguration updated = sc.updatePointsConfiguration(newCfg, UserID.of(2L));
        assertNotEquals(sc, updated);
        assertEquals(new BigDecimal("0.0500"), updated.getPointsConfiguration().getPointsPercentageRate());

        SystemConfiguration inactive = updated.setActiveStatus(false, UserID.of(2L));
        assertFalse(inactive.isActive());
        assertTrue(inactive.getConfigurationSummary().contains("inactive"));
    }
}
