-- =============================================================================
-- SYOS Database Schema - V001__Create_Extensions_And_Types.sql
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
