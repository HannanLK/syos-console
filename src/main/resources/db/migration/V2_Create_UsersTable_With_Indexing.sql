-- =====================================================
-- SYOS Users Table Migration - V2
-- Purpose: Create users table for SYOS retail management system
-- Supports: Authentication, Authorization, Loyalty Points, Audit Trail
-- =====================================================

-- =====================================================
-- 1 USERS TABLE CREATION
-- =====================================================
CREATE TABLE users (
    -- Primary identification
    id BIGSERIAL PRIMARY KEY,

    -- Authentication credentials
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Authorization and role management
    role user_role NOT NULL DEFAULT 'CUSTOMER',

    -- Personal information
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,

    -- SYOS Loyalty System (1% per 100 LKR spent)
    synex_points DECIMAL(10, 2) DEFAULT 0.00,

    -- Account status and session management
    is_active BOOLEAN DEFAULT true,

    -- Audit trail and timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT, -- References users.id for audit

    -- Member since tracking for customer reports
    member_since TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Business rule constraints
    CONSTRAINT chk_synex_points CHECK (synex_points >= 0),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_name_length CHECK (LENGTH(TRIM(name)) >= 2),

    -- Self-referential foreign key for audit trail
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- =====================================================
-- PERFORMANCE INDEXES
-- =====================================================

-- Primary authentication index (most frequent operation)
CREATE UNIQUE INDEX idx_users_username_active
    ON users (username)
    WHERE is_active = true;

-- Email lookup index for registration and password recovery
CREATE UNIQUE INDEX idx_users_email_active
    ON users (email)
    WHERE is_active = true;

-- Role-based access control index
CREATE INDEX idx_users_role_active
    ON users (role, is_active);

-- Customer loyalty analysis
CREATE INDEX idx_users_loyalty_points
    ON users (synex_points DESC, role)
    WHERE role = 'CUSTOMER' AND is_active = true;

-- =====================================================
-- TRIGGER FOR UPDATED_AT MAINTENANCE
-- =====================================================

-- Function to update timestamp on row modification
CREATE OR REPLACE FUNCTION update_users_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update updated_at field
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_users_updated_at();

-- =====================================================
-- COMMENTS AND DOCUMENTATION
-- =====================================================

COMMENT ON TABLE users IS 'Core user management table supporting multi-role authentication, loyalty points, and comprehensive audit trail for SYOS retail system';
COMMENT ON COLUMN users.id IS 'Primary key - auto-incrementing user identifier';
COMMENT ON COLUMN users.username IS 'Unique username for authentication (3-50 characters)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password for secure authentication';
COMMENT ON COLUMN users.role IS 'User role determining system access: CUSTOMER, EMPLOYEE, ADMIN';
COMMENT ON COLUMN users.synex_points IS 'Loyalty points earned (1% per 100 LKR spent)';
COMMENT ON COLUMN users.member_since IS 'Customer registration timestamp for analytics';
COMMENT ON COLUMN users.created_by IS 'Audit trail - references user who created this account';

-- =====================================================
-- VERIFICATION
-- =====================================================

DO
$do$
    BEGIN
        RAISE NOTICE 'Users table created successfully! Application will initialize default users.';
    END
$do$;
