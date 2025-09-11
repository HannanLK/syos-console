package com.syos.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Central application configuration loader.

 * Loads properties from the classpath: config/application.properties and allows overriding
 * with System properties and Environment variables.

 * Environment variable override rules:
 *  - an Exact property key is first checked as a System property (-Dkey=value).
 *  - Then checks an uppercased, underscore version in ENV (e.g., datasource.url -> DATASOURCE_URL).
 *  - Falls back to value from application.properties.
 */
public final class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final String CLASSPATH_CONFIG = "/config/application.properties";

    private static final ApplicationConfig INSTANCE = new ApplicationConfig();

    private final Properties props = new Properties();

    private ApplicationConfig() {
        try (InputStream in = ApplicationConfig.class.getResourceAsStream(CLASSPATH_CONFIG)) {
            if (in != null) {
                props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            } else {
                LoggerFactory.getLogger(ApplicationConfig.class).warn("application.properties not found at {}. Proceeding with environment and system properties only.", CLASSPATH_CONFIG);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application properties", e);
        }

        // Log a minimal, safe snapshot
        log.info("Configuration loaded. Environment overrides and -D system properties will take precedence.");
    }

    public static ApplicationConfig get() {
        return INSTANCE;
    }

    public Properties asProperties() {
        // Provide a defensive copy so callers cannot mutate internal state
        Properties copy = new Properties();
        copy.putAll(this.props);
        return copy;
    }

    public Optional<String> find(String key) {
        Objects.requireNonNull(key, "key");
        // 1) JVM system property
        String fromSys = System.getProperty(key);
        if (fromSys != null && !fromSys.isBlank()) return Optional.of(fromSys);

        // 2) ENV with upper-case + underscores
        String envKey = key.replace('.', '_').toUpperCase(Locale.ROOT);
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) return Optional.of(fromEnv);

        // 3) application.properties
        String fromFile = props.getProperty(key);
        return Optional.ofNullable(fromFile);
    }

    public String get(String key, String defaultValue) {
        return find(key).orElse(defaultValue);
    }

    public String require(String key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing required config key: " + key));
    }

    public int getInt(String key, int defaultValue) {
        String v = find(key).orElse(null);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid int for key " + key + ": " + v);
        }
    }

    public long getLong(String key, long defaultValue) {
        String v = find(key).orElse(null);
        if (v == null) return defaultValue;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long for key " + key + ": " + v);
        }
    }

    public boolean getBool(String key, boolean defaultValue) {
        String v = find(key).orElse(null);
        if (v == null) return defaultValue;
        return Boolean.parseBoolean(v.trim());
    }

    public ZoneId getZoneId(String key, ZoneId defaultZone) {
        String v = find(key).orElse(null);
        if (v == null || v.isBlank()) return defaultZone;
        return ZoneId.of(v.trim());
    }
}
