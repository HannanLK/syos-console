package com.syos.domain.entities;

import com.syos.domain.valueobjects.SynexPointsConfiguration;
import com.syos.domain.valueobjects.UserID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Entity representing the system configuration for Synex Points
 * Follows DDD principles and encapsulates business logic
 * 
 * Requirement Addressed: Admin configuration management for loyalty points
 * Design Patterns: Entity Pattern, Builder Pattern
 * SOLID Principles: SRP, OCP - Single responsibility, open for extension
 */
@Getter
@AllArgsConstructor
public class SystemConfiguration {
    private Long configurationId;
    private String configurationKey;
    private SynexPointsConfiguration pointsConfiguration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserID lastModifiedBy;
    private boolean isActive;
    
    // Configuration keys constants
    public static final String SYNEX_POINTS_CONFIG_KEY = "SYNEX_POINTS_CONFIGURATION";
    
    /**
     * Private constructor for builder pattern
     */
    private SystemConfiguration(Builder builder) {
        this.configurationId = builder.configurationId;
        this.configurationKey = builder.configurationKey;
        this.pointsConfiguration = builder.pointsConfiguration;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();
        this.lastModifiedBy = builder.lastModifiedBy;
        this.isActive = builder.isActive;
        
        validateEntity();
    }
    
    /**
     * Update the points configuration
     * Business rule: Only active configurations can be updated
     * Business rule: Must track who made the change
     */
    public SystemConfiguration updatePointsConfiguration(SynexPointsConfiguration newConfig, UserID modifiedBy) {
        if (!this.isActive) {
            throw new IllegalStateException("Cannot update inactive configuration");
        }
        
        if (newConfig == null) {
            throw new IllegalArgumentException("New configuration cannot be null");
        }
        
        if (modifiedBy == null) {
            throw new IllegalArgumentException("Modified by user cannot be null");
        }
        
        return new Builder()
            .configurationId(this.configurationId)
            .configurationKey(this.configurationKey)
            .pointsConfiguration(newConfig)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .lastModifiedBy(modifiedBy)
            .isActive(this.isActive)
            .build();
    }
    
    /**
     * Activate or deactivate the configuration
     */
    public SystemConfiguration setActiveStatus(boolean active, UserID modifiedBy) {
        if (modifiedBy == null) {
            throw new IllegalArgumentException("Modified by user cannot be null");
        }
        
        return new Builder()
            .configurationId(this.configurationId)
            .configurationKey(this.configurationKey)
            .pointsConfiguration(this.pointsConfiguration)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .lastModifiedBy(modifiedBy)
            .isActive(active)
            .build();
    }
    
    /**
     * Check if this configuration is for Synex Points
     */
    public boolean isSynexPointsConfiguration() {
        return SYNEX_POINTS_CONFIG_KEY.equals(this.configurationKey);
    }
    
    /**
     * Get configuration summary for reporting
     */
    public String getConfigurationSummary() {
        if (pointsConfiguration != null) {
            return String.format(
                "Synex Points Configuration: Rate=%s, Min Threshold=%.2f LKR, Max Points=%.2f, Active=%s",
                pointsConfiguration.getPointsRateAsPercentage(),
                pointsConfiguration.getMinimumSpendingThreshold(),
                pointsConfiguration.getMaximumPointsPerTransaction(),
                pointsConfiguration.isActive()
            );
        }
        return "No points configuration available";
    }
    
    private void validateEntity() {
        if (configurationKey == null || configurationKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration key cannot be null or empty");
        }
        
        if (lastModifiedBy == null) {
            throw new IllegalArgumentException("Last modified by cannot be null");
        }
        
        if (SYNEX_POINTS_CONFIG_KEY.equals(configurationKey) && pointsConfiguration == null) {
            throw new IllegalArgumentException("Points configuration cannot be null for Synex Points configuration");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemConfiguration that = (SystemConfiguration) o;
        return Objects.equals(configurationId, that.configurationId) &&
               Objects.equals(configurationKey, that.configurationKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(configurationId, configurationKey);
    }
    
    /**
     * Builder class for SystemConfiguration
     * Implements Builder Pattern for clean object construction
     */
    public static class Builder {
        private Long configurationId;
        private String configurationKey;
        private SynexPointsConfiguration pointsConfiguration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private UserID lastModifiedBy;
        private boolean isActive = true;
        
        public Builder configurationId(Long configurationId) {
            this.configurationId = configurationId;
            return this;
        }
        
        public Builder configurationKey(String configurationKey) {
            this.configurationKey = configurationKey;
            return this;
        }
        
        public Builder pointsConfiguration(SynexPointsConfiguration pointsConfiguration) {
            this.pointsConfiguration = pointsConfiguration;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder lastModifiedBy(UserID lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public SystemConfiguration build() {
            return new SystemConfiguration(this);
        }
    }
    
    /**
     * Factory method for creating new Synex Points configuration
     */
    public static SystemConfiguration createSynexPointsConfiguration(SynexPointsConfiguration pointsConfig, 
                                                                   UserID createdBy) {
        return new Builder()
            .configurationKey(SYNEX_POINTS_CONFIG_KEY)
            .pointsConfiguration(pointsConfig)
            .lastModifiedBy(createdBy)
            .build();
    }
}
