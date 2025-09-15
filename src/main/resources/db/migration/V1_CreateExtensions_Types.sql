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
DO $ BEGIN
    CREATE TYPE user_role AS ENUM ('CUSTOMER', 'EMPLOYEE', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $;

-- Verification
DO
$do$
    BEGIN
        RAISE NOTICE 'Database extensions and types setup completed successfully!';
    END
$do$;