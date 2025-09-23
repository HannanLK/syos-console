-- =============================================================================
-- V11__Alter_payment_method_to_varchar.sql
-- Purpose: Align transactions.payment_method with JPA @Enumerated(EnumType.STRING)
-- Problem: Column is PostgreSQL ENUM (payment_method), Hibernate binds VARCHAR -> type mismatch
-- Fix: Change column type to VARCHAR while preserving data
-- =============================================================================

BEGIN;

-- Only alter if current type is the ENUM 'payment_method'
DO $$
DECLARE
    current_type TEXT;
BEGIN
    SELECT data_type INTO current_type
    FROM information_schema.columns
    WHERE table_name = 'transactions' AND column_name = 'payment_method';

    -- information_schema reports 'USER-DEFINED' for enums; double-check underlying enum type
    IF current_type = 'USER-DEFINED' THEN
        -- Perform safe cast from enum to text, then to varchar
        EXECUTE 'ALTER TABLE transactions ALTER COLUMN payment_method TYPE VARCHAR(16) USING payment_method::text';
    END IF;
END $$;

-- Optional: keep enum values around for other tables; we don't drop the type here

COMMIT;