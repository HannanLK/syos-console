package com.syos.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration using HikariCP. All values are read from ApplicationConfig
 * (config/application.properties with ENV/-D overrides). No hard-coded credentials.
 */
public final class DatabaseConfig implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private final ApplicationConfig cfg = ApplicationConfig.get();
    private HikariDataSource dataSource;

    public DatabaseConfig() {
        this.dataSource = createDataSource();
    }

    private HikariDataSource createDataSource() {
        HikariConfig hc = new HikariConfig();

        String url = cfg.get("datasource.url", null);
        String user = cfg.get("datasource.username", null);
        String pass = cfg.get("datasource.password", null);
        String driver = cfg.get("datasource.driver", "org.postgresql.Driver");

        if (url == null || user == null) {
            throw new IllegalStateException("Database configuration incomplete. Please set datasource.url and datasource.username");
        }

        hc.setJdbcUrl(url);
        hc.setUsername(user);
        hc.setPassword(pass);
        hc.setDriverClassName(driver);
        hc.setPoolName(cfg.get("datasource.hikari.pool-name", "SYOSHikariPool"));
        hc.setMinimumIdle(cfg.getInt("datasource.hikari.minimum-idle", 5));
        hc.setMaximumPoolSize(cfg.getInt("datasource.hikari.maximum-pool-size", 20));
        hc.setIdleTimeout(cfg.getLong("datasource.hikari.idle-timeout", 300_000));
        hc.setMaxLifetime(cfg.getLong("datasource.hikari.max-lifetime", 1_200_000));
        hc.setConnectionTimeout(cfg.getLong("datasource.hikari.connection-timeout", 20_000));
        hc.setLeakDetectionThreshold(cfg.getLong("datasource.hikari.leak-detection-threshold", 60_000));
        hc.setAutoCommit(cfg.getBool("datasource.hikari.auto-commit", false));

        // Some common prepared statement caching options (driver-specific)
        Properties dsProps = new Properties();
        dsProps.setProperty("cachePrepStmts", "true");
        dsProps.setProperty("prepStmtCacheSize", "250");
        dsProps.setProperty("prepStmtCacheSqlLimit", "2048");
        dsProps.setProperty("reWriteBatchedInserts", "true");
        hc.setDataSourceProperties(dsProps);

        // Safe logging (never log passwords)
        log.info("Initializing HikariCP DataSource: url='{}', user='{}', driver='{}', pool='{}'",
                url, user, driver, hc.getPoolName());

        return new HikariDataSource(hc);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Provide a Properties object containing Hibernate settings resolved from ApplicationConfig.
     * Can be used to bootstrap a SessionFactory programmatically if desired.
     */
    public Properties getHibernateProperties() {
        Properties p = new Properties();
        // Core connection props (Hibernate will pull from DataSource if set via programmatic bootstrap)
        p.setProperty("hibernate.default_schema", cfg.get("datasource.schema", "public"));

        // HikariCP props mirrored for hibernate-c3p0 compatibility when using hibernate.cfg.xml
        p.setProperty("hibernate.hikari.minimumIdle", String.valueOf(cfg.getInt("datasource.hikari.minimum-idle", 5)));
        p.setProperty("hibernate.hikari.maximumPoolSize", String.valueOf(cfg.getInt("datasource.hikari.maximum-pool-size", 20)));
        p.setProperty("hibernate.hikari.idleTimeout", String.valueOf(cfg.getLong("datasource.hikari.idle-timeout", 300_000)));
        p.setProperty("hibernate.hikari.connectionTimeout", String.valueOf(cfg.getLong("datasource.hikari.connection-timeout", 20_000)));
        p.setProperty("hibernate.hikari.maxLifetime", String.valueOf(cfg.getLong("datasource.hikari.max-lifetime", 1_200_000)));
        p.setProperty("hibernate.hikari.poolName", cfg.get("datasource.hikari.pool-name", "SYOSHikariPool"));
        p.setProperty("hibernate.hikari.leakDetectionThreshold", String.valueOf(cfg.getLong("datasource.hikari.leak-detection-threshold", 60_000)));
        p.setProperty("hibernate.hikari.autoCommit", String.valueOf(cfg.getBool("datasource.hikari.auto-commit", false)));

        // Hibernate tuning
        p.setProperty("hibernate.dialect", cfg.get("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        p.setProperty("hibernate.hbm2ddl.auto", cfg.get("hibernate.hbm2ddl.auto", "validate"));
        p.setProperty("hibernate.show_sql", String.valueOf(cfg.getBool("hibernate.show_sql", false)));
        p.setProperty("hibernate.format_sql", String.valueOf(cfg.getBool("hibernate.format_sql", true)));
        p.setProperty("hibernate.use_sql_comments", String.valueOf(cfg.getBool("hibernate.use_sql_comments", false)));
        p.setProperty("hibernate.generate_statistics", String.valueOf(cfg.getBool("hibernate.generate_statistics", true)));
        p.setProperty("hibernate.jdbc.batch_size", String.valueOf(cfg.getInt("hibernate.jdbc.batch_size", 25)));
        p.setProperty("hibernate.jdbc.fetch_size", String.valueOf(cfg.getInt("hibernate.jdbc.fetch_size", 50)));
        p.setProperty("hibernate.default_batch_fetch_size", String.valueOf(cfg.getInt("hibernate.default_batch_fetch_size", 16)));
        p.setProperty("hibernate.order_inserts", String.valueOf(cfg.getBool("hibernate.order_inserts", true)));
        p.setProperty("hibernate.order_updates", String.valueOf(cfg.getBool("hibernate.order_updates", true)));
        p.setProperty("hibernate.jdbc.time_zone", cfg.get("hibernate.jdbc.time_zone", "UTC"));

        return p;
    }

    @Override
    public void close() {
        if (dataSource != null) {
            try {
                dataSource.close();
                log.info("HikariCP DataSource closed.");
            } catch (Exception e) {
                log.warn("Error while closing DataSource: {}", e.getMessage());
            }
        }
    }
}
