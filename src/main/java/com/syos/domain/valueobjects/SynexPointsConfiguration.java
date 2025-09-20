package com.syos.domain.valueobjects;

import lombok.Value;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Value Object representing Synex Points configuration
 * Immutable and contains business rules for point calculations
 * 
 * Requirement Addressed: Admin menu configuration for Synex Points system
 * Design Patterns: Value Object Pattern
 * SOLID Principle: SRP - Single responsibility for points configuration
 */
@Value
public class SynexPointsConfiguration {
    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.01"); // 1% default
    private static final BigDecimal MIN_SPENDING_THRESHOLD = new BigDecimal("100.00"); // 100 LKR minimum
    private static final BigDecimal MAX_POINTS_PER_TRANSACTION = new BigDecimal("1000.00"); // Max 1000 points per transaction
    
    BigDecimal pointsPercentageRate; // e.g., 0.01 for 1%
    BigDecimal minimumSpendingThreshold; // minimum LKR amount to earn points
    BigDecimal maximumPointsPerTransaction; // maximum points earnable per transaction
    boolean isActive;
    LocalDateTime lastModified;
    String modifiedBy;
    
    /**
     * Creates a new Synex Points configuration with validation
     */
    private SynexPointsConfiguration(BigDecimal pointsPercentageRate, 
                                   BigDecimal minimumSpendingThreshold,
                                   BigDecimal maximumPointsPerTransaction,
                                   boolean isActive,
                                   LocalDateTime lastModified,
                                   String modifiedBy) {
        validateConfiguration(pointsPercentageRate, minimumSpendingThreshold, maximumPointsPerTransaction);
        this.pointsPercentageRate = pointsPercentageRate.setScale(4, RoundingMode.HALF_UP);
        this.minimumSpendingThreshold = minimumSpendingThreshold.setScale(2, RoundingMode.HALF_UP);
        this.maximumPointsPerTransaction = maximumPointsPerTransaction.setScale(2, RoundingMode.HALF_UP);
        this.isActive = isActive;
        this.lastModified = lastModified != null ? lastModified : LocalDateTime.now();
        this.modifiedBy = modifiedBy != null ? modifiedBy : "SYSTEM";
    }
    
    /**
     * Factory method to create default configuration
     */
    public static SynexPointsConfiguration defaultConfiguration() {
        return new SynexPointsConfiguration(
            DEFAULT_RATE,
            MIN_SPENDING_THRESHOLD,
            MAX_POINTS_PER_TRANSACTION,
            true,
            LocalDateTime.now(),
            "SYSTEM"
        );
    }
    
    /**
     * Factory method to create custom configuration
     */
    public static SynexPointsConfiguration of(BigDecimal pointsRate, 
                                            BigDecimal minThreshold, 
                                            BigDecimal maxPoints,
                                            boolean isActive,
                                            String modifiedBy) {
        return new SynexPointsConfiguration(
            pointsRate,
            minThreshold,
            maxPoints,
            isActive,
            LocalDateTime.now(),
            modifiedBy
        );
    }
    
    /**
     * Calculate points earned for a given spending amount
     */
    public SynexPoints calculatePoints(Money spendingAmount) {
        if (!isActive || spendingAmount.getAmount().compareTo(minimumSpendingThreshold) < 0) {
            return SynexPoints.of(BigDecimal.ZERO);
        }
        
        BigDecimal pointsEarned = spendingAmount.getAmount()
            .multiply(pointsPercentageRate)
            .setScale(2, RoundingMode.HALF_UP);
            
        // Apply maximum points per transaction limit
        if (pointsEarned.compareTo(maximumPointsPerTransaction) > 0) {
            pointsEarned = maximumPointsPerTransaction;
        }
        
        return SynexPoints.of(pointsEarned);
    }
    
    /**
     * Check if spending amount qualifies for points
     */
    public boolean qualifiesForPoints(Money spendingAmount) {
        return isActive && spendingAmount.getAmount().compareTo(minimumSpendingThreshold) >= 0;
    }
    
    /**
     * Create updated configuration with new rate
     */
    public SynexPointsConfiguration withRate(BigDecimal newRate, String modifiedBy) {
        return new SynexPointsConfiguration(
            newRate,
            this.minimumSpendingThreshold,
            this.maximumPointsPerTransaction,
            this.isActive,
            LocalDateTime.now(),
            modifiedBy
        );
    }
    
    /**
     * Create updated configuration with new threshold
     */
    public SynexPointsConfiguration withThreshold(BigDecimal newThreshold, String modifiedBy) {
        return new SynexPointsConfiguration(
            this.pointsPercentageRate,
            newThreshold,
            this.maximumPointsPerTransaction,
            this.isActive,
            LocalDateTime.now(),
            modifiedBy
        );
    }
    
    /**
     * Create updated configuration with new active status
     */
    public SynexPointsConfiguration withActiveStatus(boolean newStatus, String modifiedBy) {
        return new SynexPointsConfiguration(
            this.pointsPercentageRate,
            this.minimumSpendingThreshold,
            this.maximumPointsPerTransaction,
            newStatus,
            LocalDateTime.now(),
            modifiedBy
        );
    }
    
    /**
     * Get points rate as percentage (e.g., 0.01 -> "1.00%")
     */
    public String getPointsRateAsPercentage() {
        return pointsPercentageRate.multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP) + "%";
    }
    
    /**
     * Getter methods required by SystemConfiguration
     */
    public BigDecimal getMinimumSpendingThreshold() {
        return minimumSpendingThreshold;
    }
    
    public BigDecimal getMaximumPointsPerTransaction() {
        return maximumPointsPerTransaction;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    private void validateConfiguration(BigDecimal pointsRate, 
                                     BigDecimal minThreshold, 
                                     BigDecimal maxPoints) {
        if (pointsRate == null || pointsRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Points rate cannot be null or negative");
        }
        
        if (pointsRate.compareTo(new BigDecimal("1.0")) > 0) {
            throw new IllegalArgumentException("Points rate cannot exceed 100%");
        }
        
        if (minThreshold == null || minThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum spending threshold cannot be null or negative");
        }
        
        if (maxPoints == null || maxPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Maximum points per transaction must be positive");
        }
        
        if (minThreshold.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException("Minimum spending threshold too high (max 100,000 LKR)");
        }
    }
}
