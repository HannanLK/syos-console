-- =============================================================================
-- SYOS Database Schema - V1__Create_Extensions_And_Types.sql
-- PostgreSQL Extensions and Custom Types Creation
-- =============================================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS public;

-- Set timezone
SET timezone = 'Asia/Colombo';

-- =============================================================================
-- EXTENSIONS
-- =============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create the user_role enum type
DO $$
BEGIN
    CREATE TYPE user_role AS ENUM ('CUSTOMER', 'EMPLOYEE', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

-- Create other required enum types referenced by later migrations
DO $$
BEGIN
    CREATE TYPE unit_of_measure AS ENUM ('EACH','PACK','KG','G','L','ML','BOX');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE product_status AS ENUM ('ACTIVE','INACTIVE','DISCONTINUED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE transfer_type AS ENUM (
        'WAREHOUSE_TO_SHELF',
        'WAREHOUSE_TO_WEB',
        'SHELF_TO_WAREHOUSE',
        'WEB_TO_WAREHOUSE',
        'SHELF_TO_SHELF',
        'WEB_TO_WEB'
    );
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE transfer_status AS ENUM ('PENDING','APPROVED','COMPLETED','REJECTED','CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE notification_type AS ENUM ('REORDER','EXPIRY','LOW_STOCK','SYSTEM','TRANSFER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE promotion_type AS ENUM ('PERCENTAGE','FIXED_AMOUNT','BUY_X_GET_Y');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE transaction_type AS ENUM ('POS','WEB');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE payment_method AS ENUM ('CASH','CARD');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE transaction_status AS ENUM ('PENDING','COMPLETED','VOIDED','REFUNDED','CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

-- Generic trigger function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Verification
DO
$do$
    BEGIN
        RAISE NOTICE 'Database extensions and types setup completed successfully!';
    END
$do$;