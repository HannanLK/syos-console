package com.syos.shared.constants;

/**
 * Business-specific constants for SYOS operations
 * 
 * Design Pattern: Constants Pattern
 * Responsibility: Business rule constants and domain-specific values
 * Clean Architecture: Shared Kernel layer
 * 
 * @author Hannanlk
 * @version 1.0
 */
public final class BusinessConstants {
    
    private BusinessConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    // ==================== INVENTORY MANAGEMENT ====================
    
    /** FIFO stock selection priority */
    public static final String STOCK_SELECTION_FIFO = "FIFO";
    
    /** Expiry date priority for stock selection */
    public static final String STOCK_SELECTION_EXPIRY = "EXPIRY_PRIORITY";
    
    /** Maximum days to consider for expiry priority override */
    public static final int EXPIRY_PRIORITY_THRESHOLD_DAYS = 7;
    
    /** Stock transfer batch size for optimization */
    public static final int STOCK_TRANSFER_BATCH_SIZE = 100;
    
    /** Minimum stock level for automatic reorder trigger */
    public static final int AUTO_REORDER_TRIGGER_LEVEL = 10;
    
    /** Maximum items that can be transferred in a single operation */
    public static final int MAX_TRANSFER_QUANTITY = 1000;
    
    // ==================== TRANSACTION PROCESSING ====================
    
    /** Maximum time (in seconds) to hold a transaction before timeout */
    public static final int TRANSACTION_TIMEOUT_SECONDS = 300; // 5 minutes
    
    /** Maximum number of items in a single transaction */
    public static final int MAX_TRANSACTION_ITEMS = 50;
    
    /** Minimum quantity for bulk discount eligibility */
    public static final int BULK_DISCOUNT_MIN_QUANTITY = 10;
    
    /** Card payment processing timeout in milliseconds */
    public static final int CARD_PAYMENT_TIMEOUT_MS = 30000; // 30 seconds
    
    /** Cash payment maximum denomination handling */
    public static final String[] ACCEPTED_DENOMINATIONS = {
        "5000", "1000", "500", "100", "50", "20", "10", "5", "1"
    };
    
    // ==================== FIFO IMPLEMENTATION ====================
    
    /** Priority score for oldest batch in FIFO */
    public static final int FIFO_OLDEST_PRIORITY = 100;
    
    /** Priority score increment per day of age */
    public static final int FIFO_AGE_PRIORITY_INCREMENT = 1;
    
    /** Expiry override priority boost */
    public static final int EXPIRY_OVERRIDE_PRIORITY_BOOST = 1000;
    
    /** Days before expiry to trigger priority boost */
    public static final int EXPIRY_PRIORITY_TRIGGER_DAYS = 14;
    
    // ==================== BUSINESS VALIDATION ====================
    
    /** Minimum shelf life required for products (in days) */
    public static final int MIN_REMAINING_SHELF_LIFE_DAYS = 30;
    
    /** Maximum allowed price variance percentage for price updates */
    public static final double MAX_PRICE_VARIANCE_PERCENTAGE = 20.0;
    
    /** Minimum profit margin percentage */
    public static final double MIN_PROFIT_MARGIN_PERCENTAGE = 5.0;
    
    /** Maximum discount percentage that can be applied */
    public static final double MAX_DISCOUNT_PERCENTAGE = 50.0;
    
    // ==================== LOYALTY PROGRAM ====================
    
    /** Points multiplier for premium customers */
    public static final double PREMIUM_CUSTOMER_POINTS_MULTIPLIER = 1.5;
    
    /** Minimum purchase amount to earn points */
    public static final double MIN_PURCHASE_FOR_POINTS = 100.0;
    
    /** Maximum points that can be redeemed in a single transaction */
    public static final int MAX_POINTS_REDEMPTION_PER_TRANSACTION = 1000;
    
    /** Points to currency conversion rate (points per LKR) */
    public static final double POINTS_TO_LKR_RATE = 0.01; // 1 point = 0.01 LKR
    
    // ==================== STOCK LOCATION PRIORITIES ====================
    
    /** Priority for shelf stock in customer transactions */
    public static final int SHELF_STOCK_PRIORITY = 1;
    
    /** Priority for warehouse stock in stock transfers */
    public static final int WAREHOUSE_STOCK_PRIORITY = 2;
    
    /** Priority for web inventory in online transactions */
    public static final int WEB_INVENTORY_PRIORITY = 1;
    
    // ==================== NOTIFICATION THRESHOLDS ====================
    
    /** Stock level percentage that triggers low stock notification */
    public static final double LOW_STOCK_THRESHOLD_PERCENTAGE = 20.0;
    
    /** Stock level percentage that triggers critical stock notification */
    public static final double CRITICAL_STOCK_THRESHOLD_PERCENTAGE = 5.0;
    
    /** Days before expiry to send expiry warning */
    public static final int EXPIRY_WARNING_THRESHOLD_DAYS = 30;
    
    /** Days before expiry for critical expiry notification */
    public static final int CRITICAL_EXPIRY_THRESHOLD_DAYS = 7;
    
    // ==================== RETAIL INSIGHTS ====================
    
    /** Minimum transactions per hour to be considered peak hour */
    public static final int PEAK_HOUR_MIN_TRANSACTIONS = 10;
    
    /** Number of days to calculate inventory turnover */
    public static final int INVENTORY_TURNOVER_PERIOD_DAYS = 30;
    
    /** High performance threshold for category performance */
    public static final double HIGH_PERFORMANCE_CATEGORY_THRESHOLD = 80.0;
    
    /** Low performance threshold for category performance */
    public static final double LOW_PERFORMANCE_CATEGORY_THRESHOLD = 20.0;
    
    /** Minimum sales amount to be included in performance analysis */
    public static final double MIN_SALES_FOR_ANALYSIS = 1000.0;
    
    // ==================== OPERATIONAL HOURS ====================
    
    /** Store opening hour (24-hour format) */
    public static final int STORE_OPENING_HOUR = 8; // 8 AM
    
    /** Store closing hour (24-hour format) */
    public static final int STORE_CLOSING_HOUR = 22; // 10 PM
    
    /** Peak hours start */
    public static final int PEAK_HOURS_START = 17; // 5 PM
    
    /** Peak hours end */
    public static final int PEAK_HOURS_END = 20; // 8 PM
    
    /** Lunch hour peak start */
    public static final int LUNCH_PEAK_START = 12; // 12 PM
    
    /** Lunch hour peak end */
    public static final int LUNCH_PEAK_END = 14; // 2 PM
    
    // ==================== WEB TRANSACTION SIMULATION ====================
    
    /** Success rate for web transactions (percentage) */
    public static final double WEB_TRANSACTION_SUCCESS_RATE = 85.0;
    
    /** Average web transaction processing time (milliseconds) */
    public static final int WEB_TRANSACTION_PROCESSING_TIME_MS = 3000;
    
    /** Web inventory update frequency (minutes) */
    public static final int WEB_INVENTORY_UPDATE_FREQUENCY_MINUTES = 15;
    
    // ==================== QUALITY CONTROL ====================
    
    /** Minimum quality score for products to be transferred to shelf */
    public static final int MIN_QUALITY_SCORE_FOR_SHELF = 80;
    
    /** Quality check frequency (days) */
    public static final int QUALITY_CHECK_FREQUENCY_DAYS = 7;
    
    /** Maximum acceptable damage percentage */
    public static final double MAX_ACCEPTABLE_DAMAGE_PERCENTAGE = 5.0;
    
    // ==================== SUPPLIER MANAGEMENT ====================
    
    /** Minimum supplier rating for new orders */
    public static final int MIN_SUPPLIER_RATING = 3; // Out of 5
    
    /** Days to wait for supplier response */
    public static final int SUPPLIER_RESPONSE_TIMEOUT_DAYS = 7;
    
    /** Maximum number of suppliers per product category */
    public static final int MAX_SUPPLIERS_PER_CATEGORY = 5;
    
    // ==================== EMPLOYEE PURCHASE RESTRICTIONS ====================
    
    /** Maximum employee purchase amount per day */
    public static final double EMPLOYEE_MAX_DAILY_PURCHASE = 5000.0;
    
    /** Maximum employee purchase amount per transaction */
    public static final double EMPLOYEE_MAX_TRANSACTION_PURCHASE = 2000.0;
    
    /** Employee discount percentage */
    public static final double EMPLOYEE_DISCOUNT_PERCENTAGE = 5.0;
    
    /** Restricted product categories for employees */
    public static final String[] EMPLOYEE_RESTRICTED_CATEGORIES = {
        "ALCOHOL", "TOBACCO", "PHARMACY", "GIFT_CARDS"
    };
    
    // ==================== RETURN POLICY ====================
    
    /** Maximum days allowed for returns */
    public static final int MAX_RETURN_DAYS = 30;
    
    /** Minimum condition percentage for returns */
    public static final double MIN_RETURN_CONDITION_PERCENTAGE = 90.0;
    
    /** Return processing fee percentage */
    public static final double RETURN_PROCESSING_FEE_PERCENTAGE = 2.0;
    
    /** Categories eligible for returns */
    public static final String[] RETURNABLE_CATEGORIES = {
        "ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GOODS"
    };
    
    // ==================== BUSINESS INTELLIGENCE ====================
    
    /** Minimum data points required for trend analysis */
    public static final int MIN_DATA_POINTS_FOR_TRENDS = 10;
    
    /** Forecast accuracy threshold percentage */
    public static final double FORECAST_ACCURACY_THRESHOLD = 75.0;
    
    /** Days to consider for seasonal analysis */
    public static final int SEASONAL_ANALYSIS_DAYS = 365;
    
    // ==================== AUDIT AND COMPLIANCE ====================
    
    /** Audit log retention days */
    public static final int AUDIT_LOG_RETENTION_DAYS = 2555; // 7 years
    
    /** Transaction log retention days */
    public static final int TRANSACTION_LOG_RETENTION_DAYS = 1825; // 5 years
    
    /** Financial record retention days */
    public static final int FINANCIAL_RECORD_RETENTION_DAYS = 2555; // 7 years
    
    /** Mandatory audit frequency (days) */
    public static final int MANDATORY_AUDIT_FREQUENCY_DAYS = 30;
    
    // ==================== PERFORMANCE BENCHMARKS ====================
    
    /** Maximum acceptable response time (milliseconds) */
    public static final int MAX_RESPONSE_TIME_MS = 3000;
    
    /** Maximum acceptable database query time (milliseconds) */
    public static final int MAX_DB_QUERY_TIME_MS = 1000;
    
    /** Target transaction processing rate (per minute) */
    public static final int TARGET_TRANSACTION_RATE_PER_MINUTE = 20;
    
    /** Maximum concurrent users supported */
    public static final int MAX_CONCURRENT_USERS = 100;
}
