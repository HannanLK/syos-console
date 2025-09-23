-- =============================================================================
-- V16__Alter_transaction_enums_to_varchar.sql
-- Purpose: Align transactions.status and transactions.transaction_type with JPA @Enumerated(EnumType.STRING)
-- Problem: Columns are PostgreSQL ENUMs (transaction_status, transaction_type),
--          while Hibernate binds VARCHAR when persisting enums as STRING.
-- Fix: Change both columns to VARCHAR while preserving data using safe cast.
--       Also handles dependent CHECK constraints that reference enum literals.
-- =============================================================================

BEGIN;

-- Drop dependent CHECK constraint that compares enum values to avoid cast errors during type change
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'transactions' AND constraint_name = 'chk_pos_cashier'
    ) THEN
        EXECUTE 'ALTER TABLE transactions DROP CONSTRAINT chk_pos_cashier';
    END IF;
END $$;

-- Only alter if current type is ENUM for status
DO $$
DECLARE
    current_type TEXT;
BEGIN
    SELECT data_type INTO current_type
    FROM information_schema.columns
    WHERE table_name = 'transactions' AND column_name = 'status';

    IF current_type = 'USER-DEFINED' THEN
        EXECUTE 'ALTER TABLE transactions ALTER COLUMN status TYPE VARCHAR(20) USING status::text';
    END IF;
END $$;

-- Only alter if current type is ENUM for transaction_type
DO $$
DECLARE
    current_type_tt TEXT;
BEGIN
    SELECT data_type INTO current_type_tt
    FROM information_schema.columns
    WHERE table_name = 'transactions' AND column_name = 'transaction_type';

    IF current_type_tt = 'USER-DEFINED' THEN
        EXECUTE 'ALTER TABLE transactions ALTER COLUMN transaction_type TYPE VARCHAR(20) USING transaction_type::text';
    END IF;
END $$;

-- Recreate the CHECK constraint using VARCHAR comparisons
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'transactions' AND constraint_name = 'chk_pos_cashier'
    ) THEN
        EXECUTE 'ALTER TABLE transactions ADD CONSTRAINT chk_pos_cashier CHECK ((transaction_type = ''POS'' AND cashier_id IS NOT NULL) OR (transaction_type = ''WEB'' AND cashier_id IS NULL))';
    END IF;
END $$;

COMMIT;