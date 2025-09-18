-- =====================================================
-- SYOS Database Setup Script
-- Run this script as PostgreSQL superuser to create database and user
-- =====================================================

-- Create database if it doesn't exist
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'syosdb') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE syosdb');
   END IF;
END
$do$;

-- Alternative method (run this if the above doesn't work)
-- CREATE DATABASE syosdb
--     WITH 
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'English_United States.1252'
--     LC_CTYPE = 'English_United States.1252'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

-- Connect to the database
\c syosdb;

-- Ensure proper permissions
GRANT ALL PRIVILEGES ON DATABASE syosdb TO postgres;
GRANT ALL ON SCHEMA public TO postgres;

-- Set timezone
SET timezone = 'Asia/Colombo';

-- Create backup of migrations directory
COMMENT ON DATABASE syosdb IS 'SYOS Retail Management System Database - Version 1.0';

-- Verification
SELECT 'Database syosdb created and ready for migrations!' as status;
