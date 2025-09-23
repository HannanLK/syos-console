package com.syos.domain.entities;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.SynexPoints;
import com.syos.domain.valueobjects.SynexPointsConfiguration;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigurationTest {

    @Test
    void create_update_setActive_summary_and_validation() {
        SynexPointsConfiguration cfg = SynexPointsConfiguration.of(new BigDecimal("0.01"), new BigDecimal("100"), new BigDecimal("1000"), true, "admin");
        SystemConfiguration sc = SystemConfiguration.createSynexPointsConfiguration(cfg, UserID.of(1L));

        assertTrue(sc.isSynexPointsConfiguration());
        assertNotNull(sc.getConfigurationSummary());

        // update points configuration
        SynexPointsConfiguration newCfg = cfg.withRate(new BigDecimal("0.02"), "boss");
        SystemConfiguration updated = sc.updatePointsConfiguration(newCfg, UserID.of(2L));
        assertTrue(updated.getConfigurationSummary().contains("2.00%"));

        // set active status toggles on underlying points configuration to reflect in summary
        SynexPointsConfiguration inactiveCfg = newCfg.withActiveStatus(false, "boss2");
        SystemConfiguration updatedInactive = sc.updatePointsConfiguration(inactiveCfg, UserID.of(3L));
        assertTrue(updatedInactive.getConfigurationSummary().toLowerCase().contains("active=false"));

        // qualifiesForPoints indirectly through cfg
        SynexPoints points = newCfg.calculatePoints(Money.of(new BigDecimal("1000.00")));
        assertTrue(points.getValue().compareTo(new BigDecimal("0.00")) > 0);

        // validation: null config not allowed
        assertThrows(IllegalArgumentException.class, () -> sc.updatePointsConfiguration(null, UserID.of(1L)));
    }
}
