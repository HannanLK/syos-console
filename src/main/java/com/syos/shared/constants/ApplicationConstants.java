package com.syos.shared.constants;

import java.math.BigDecimal;

/**
 * Application-wide constants for SYOS system
 * 
 * Design Pattern: Constants Pattern
 * Responsibility: Centralized constant management
 * Clean Architecture: Shared Kernel layer
 * 
 * @author Hannanlk
 * @version 1.0
 */
public final class ApplicationConstants {
    
    // Prevent instantiation
    private ApplicationConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    // ==================== APPLICATION METADATA ====================
    public static final String APPLICATION_NAME = "Synex Outlet Store";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String DEVELOPER_NAME = "Hannanlk";
    public static final String UNIVERSITY = "Staffordshire University";
    public static final String COURSE = "COMP63038 - Clean Coding and Concurrent Programming";
    
    // ==================== STORE INFORMATION ====================
    public static final String STORE_NAME = "Synex Outlet Store";
    public static final String STORE_ACRONYM = "SYOS";
    public static final String STORE_ADDRESS = "77 Hortan Pl, Colombo 07";
    public static final String STORE_COUNTRY = "Sri Lanka";
    public static final String STORE_TIMEZONE = "Asia/Colombo";
    public static final String STORE_CURRENCY = "LKR";
    
    // ==================== BUSINESS RULES ====================
    public static final BigDecimal DEFAULT_REORDER_LEVEL = new BigDecimal("50.00");
    public static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("500000.00"); // LKR 500,000
    public static final BigDecimal MAX_EMPLOYEE_PURCHASE_AMOUNT = new BigDecimal("10000.00"); // LKR 10,000
    public static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    
    // ==================== SYNEX POINTS SYSTEM ====================
    public static final BigDecimal SYNEX_POINTS_RATE = new BigDecimal("0.01"); // 1% of purchase amount
    public static final BigDecimal SYNEX_POINTS_PER_100_LKR = new BigDecimal("1.00"); // 1 point per 100 LKR
    public static final BigDecimal MIN_PURCHASE_FOR_POINTS = new BigDecimal("100.00"); // Minimum LKR 100
    public static final BigDecimal MAX_POINTS_PER_TRANSACTION = new BigDecimal("5000.00"); // Max 5000 points
    
    // ==================== VALIDATION LIMITS ====================
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MIN_ITEM_CODE_LENGTH = 3;
    public static final int MAX_ITEM_CODE_LENGTH = 20;
    public static final int MIN_ITEM_NAME_LENGTH = 2;
    public static final int MAX_ITEM_NAME_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    
    // ==================== STOCK MANAGEMENT ====================
    public static final int DEFAULT_SHELF_CAPACITY = 1000;
    public static final int DEFAULT_WAREHOUSE_CAPACITY = 10000;
    public static final int DEFAULT_WEB_INVENTORY_CAPACITY = 5000;
    public static final int MIN_STOCK_QUANTITY = 0;
    public static final int MAX_STOCK_QUANTITY = 999999;
    public static final int STOCK_WARNING_THRESHOLD = 10;
    public static final int CRITICAL_STOCK_THRESHOLD = 5;
    
    // ==================== BATCH MANAGEMENT ====================
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int MAX_BATCH_SIZE = 10000;
    public static final int MIN_SHELF_LIFE_DAYS = 1;
    public static final int DEFAULT_SHELF_LIFE_DAYS = 365;
    public static final int MAX_SHELF_LIFE_DAYS = 3650; // 10 years
    public static final int EXPIRY_WARNING_DAYS = 30;
    public static final int CRITICAL_EXPIRY_DAYS = 7;
    
    // ==================== PAYMENT CONSTANTS ====================
    public static final String FAILED_CARD_NUMBER = "0767600730204128";
    public static final int CARD_NUMBER_LENGTH = 16;
    public static final BigDecimal MAX_CASH_PAYMENT = new BigDecimal("100000.00"); // LKR 100,000
    public static final BigDecimal MAX_CARD_PAYMENT = new BigDecimal("1000000.00"); // LKR 1,000,000
    
    // ==================== BILL GENERATION ====================
    public static final String BILL_PREFIX = "SYOS";
    public static final String EMPLOYEE_BILL_PREFIX = "EMP";
    public static final int BILL_NUMBER_LENGTH = 10;
    public static final int MAX_ITEMS_PER_BILL = 100;
    public static final String BILL_FOOTER = "Thank you for shopping at Synex Outlet Store!";
    
    // ==================== SESSION MANAGEMENT ====================
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int LOCKOUT_DURATION_MINUTES = 15;
    public static final String SESSION_KEY_PREFIX = "SYOS_SESSION_";
    
    // ==================== REPORTING ====================
    public static final int MAX_REPORT_RECORDS = 10000;
    public static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String REPORT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String EXPORT_CSV_DELIMITER = ",";
    public static final String EXPORT_PDF_MARGIN = "20";
    
    // ==================== FILE SYSTEM ====================
    public static final String REPORTS_DIRECTORY = "reports";
    public static final String BILLS_DIRECTORY = "bills";
    public static final String LOGS_DIRECTORY = "logs";
    public static final String CONFIG_DIRECTORY = "config";
    public static final String DATA_DIRECTORY = "data";
    public static final String TEMP_DIRECTORY = "temp";
    
    // ==================== LOGGING ====================
    public static final String LOG_FILE_PATTERN = "syos_%d{yyyy-MM-dd}.log";
    public static final String LOG_LEVEL_DEFAULT = "INFO";
    public static final int LOG_MAX_FILE_SIZE_MB = 10;
    public static final int LOG_MAX_HISTORY_DAYS = 30;
    
    // ==================== PERFORMANCE ====================
    public static final int DATABASE_CONNECTION_POOL_SIZE = 10;
    public static final int DATABASE_CONNECTION_TIMEOUT_MS = 30000;
    public static final int CACHE_SIZE = 1000;
    public static final int CACHE_TTL_MINUTES = 60;
    
    // ==================== SECURITY ====================
    public static final int BCRYPT_STRENGTH = 12;
    public static final String JWT_SECRET_KEY = "syos_jwt_secret_key_change_in_production";
    public static final int JWT_EXPIRY_HOURS = 24;
    public static final String ADMIN_DEFAULT_USERNAME = "admin";
    public static final String ADMIN_DEFAULT_EMAIL = "admin@syos.com";
    
    // ==================== RETAIL INSIGHTS ====================
    public static final int PEAK_HOURS_THRESHOLD_TRANSACTIONS = 10;
    public static final int INVENTORY_TURNOVER_DAYS = 30;
    public static final BigDecimal HIGH_PERFORMANCE_THRESHOLD = new BigDecimal("0.8"); // 80%
    public static final BigDecimal LOW_PERFORMANCE_THRESHOLD = new BigDecimal("0.2"); // 20%
    
    // ==================== WEB SIMULATION ====================
    public static final int WEB_SIMULATION_DELAY_MS = 2000; // 2 seconds
    public static final int WEB_TIMEOUT_MS = 10000; // 10 seconds
    public static final String USER_AGENT = "SYOS-WebClient/1.0";
    
    // ==================== ERROR HANDLING ====================
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 1000;
    public static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred. Please try again.";
    
    // ==================== FEATURE FLAGS ====================
    public static final boolean ENABLE_LOYALTY_POINTS = true;
    public static final boolean ENABLE_EMPLOYEE_PURCHASES = true;
    public static final boolean ENABLE_WEB_TRANSACTIONS = true;
    public static final boolean ENABLE_RETURNS = true;
    public static final boolean ENABLE_DISCOUNTS = true;
    public static final boolean ENABLE_REPORTS = true;
    public static final boolean ENABLE_PDF_GENERATION = true;
    public static final boolean ENABLE_EMAIL_NOTIFICATIONS = false; // Future feature
    public static final boolean ENABLE_SMS_NOTIFICATIONS = false; // Future feature
    
    // ==================== MENU SYSTEM ====================
    public static final String MENU_SEPARATOR = "═";
    public static final String MENU_BORDER = "║";
    public static final int MENU_WIDTH = 60;
    public static final String MENU_TITLE_PREFIX = ">>> ";
    public static final String MENU_ITEM_PREFIX = "  ";
    public static final String BACK_OPTION = "Go Back";
    public static final String EXIT_OPTION = "Exit";
    public static final String MAIN_MENU_OPTION = "Main Menu";
    
    // ==================== INPUT VALIDATION PATTERNS ====================
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    public static final String PHONE_REGEX = "^[0-9+\\-\\s()]{10,15}$";
    public static final String ITEM_CODE_REGEX = "^[A-Z0-9]{3,20}$";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,50}$";
    public static final String CARD_NUMBER_REGEX = "^[0-9]{16}$";
    
    // ==================== SUCCESS MESSAGES ====================
    public static final String LOGIN_SUCCESS = "Login successful! Welcome back.";
    public static final String REGISTRATION_SUCCESS = "Registration successful! You can now login.";
    public static final String PRODUCT_ADDED_SUCCESS = "Product added successfully!";
    public static final String STOCK_TRANSFERRED_SUCCESS = "Stock transferred successfully!";
    public static final String TRANSACTION_SUCCESS = "Transaction completed successfully!";
    public static final String REPORT_GENERATED_SUCCESS = "Report generated successfully!";
    
    // ==================== DEFAULT VALUES ====================
    public static final String DEFAULT_CATEGORY = "General";
    public static final String DEFAULT_BRAND = "Generic";
    public static final String DEFAULT_SUPPLIER = "Default Supplier";
    public static final String DEFAULT_UNIT_OF_MEASURE = "EACH";
    public static final BigDecimal DEFAULT_PACK_SIZE = new BigDecimal("1.00");
    public static final String DEFAULT_LOCATION = "Main Store";
    
    // ==================== SYSTEM STATUS ====================
    public static final String SYSTEM_STATUS_ACTIVE = "ACTIVE";
    public static final String SYSTEM_STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String SYSTEM_STATUS_OFFLINE = "OFFLINE";
    
    // ==================== NOTIFICATION TYPES ====================
    public static final String NOTIFICATION_REORDER = "REORDER_ALERT";
    public static final String NOTIFICATION_EXPIRY = "EXPIRY_WARNING";
    public static final String NOTIFICATION_LOW_STOCK = "LOW_STOCK";
    public static final String NOTIFICATION_SYSTEM = "SYSTEM_MESSAGE";
    public static final String NOTIFICATION_TRANSACTION = "TRANSACTION_ALERT";
}
