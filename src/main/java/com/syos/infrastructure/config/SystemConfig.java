package com.syos.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Applies system-level configuration such as default timezone, locale, and charset
 * based on application properties.
 */
public final class SystemConfig {
    private static final Logger log = LoggerFactory.getLogger(SystemConfig.class);

    private final ApplicationConfig cfg = ApplicationConfig.get();

    public void apply() {
        // Timezone
        ZoneId zoneId = cfg.getZoneId("hibernate.jdbc.time_zone", ZoneId.of("UTC"));
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));

        // Locale (optional, default to a system if not specified)
        String langTag = cfg.get("system.locale", "");
        if (!langTag.isBlank()) {
            Locale locale = Locale.forLanguageTag(langTag);
            Locale.setDefault(locale);
        }

        // Charset (optional)
        Charset cs = StandardCharsets.UTF_8; // default to UTF-8
        String charsetName = cfg.get("system.charset", "UTF-8");
        try {
            cs = Charset.forName(charsetName);
        } catch (Exception ignored) {
            // Keep UTF-8 if invalid
        }
        System.setProperty("file.encoding", cs.name());

        // Expose all relevant config values as System properties for frameworks that read from them
        bridgeToSystemProperties();

        // Safe logging of applied settings
        log.info("System configuration applied: timezone={}, locale={}, charset={}",
                zoneId, Locale.getDefault(), cs);

        // Bridge LOG_LEVEL to Logback if provided in properties
        String logLevel = cfg.get("logging.level.root", null);
        if (logLevel != null && !logLevel.isBlank()) {
            System.setProperty("LOG_LEVEL", logLevel);
        }
        // Bridge LOG_HOME if configured
        String logHome = cfg.get("logging.home", null);
        if (logHome != null && !logHome.isBlank()) {
            System.setProperty("LOG_HOME", logHome);
        }
    }

    private void bridgeToSystemProperties() {
        // Datasource
        setIfPresent("datasource.url");
        setIfPresent("datasource.username");
        setIfPresent("datasource.password");
        setIfPresent("datasource.driver");
        setIfPresent("datasource.schema");
        // Hikari
        setIfPresent("datasource.hikari.minimum-idle");
        setIfPresent("datasource.hikari.maximum-pool-size");
        setIfPresent("datasource.hikari.idle-timeout");
        setIfPresent("datasource.hikari.connection-timeout");
        setIfPresent("datasource.hikari.max-lifetime");
        setIfPresent("datasource.hikari.pool-name");
        setIfPresent("datasource.hikari.leak-detection-threshold");
        setIfPresent("datasource.hikari.auto-commit");
        // Hibernate
        setIfPresent("hibernate.dialect");
        setIfPresent("hibernate.hbm2ddl.auto");
        setIfPresent("hibernate.show_sql");
        setIfPresent("hibernate.format_sql");
        setIfPresent("hibernate.use_sql_comments");
        setIfPresent("hibernate.generate_statistics");
        setIfPresent("hibernate.jdbc.batch_size");
        setIfPresent("hibernate.jdbc.fetch_size");
        setIfPresent("hibernate.default_batch_fetch_size");
        setIfPresent("hibernate.order_inserts");
        setIfPresent("hibernate.order_updates");
        setIfPresent("hibernate.jdbc.time_zone");
    }

    private void setIfPresent(String key) {
        String val = cfg.find(key).orElse(null);
        if (val != null && !val.isBlank()) {
            System.setProperty(key, val);
        }
    }
}
