-- =============================================================================
-- SYOS Database Schema - V8__Update_Transaction_Entities_For_JPA.sql
-- Updates transaction tables to match JPA entity definitions
-- =============================================================================

-- Ensure transaction_type enum includes our values
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
        CREATE TYPE transaction_type AS ENUM ('POS', 'WEB');
    END IF;
END$$;

-- Ensure payment_method enum includes our values
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_method') THEN
        CREATE TYPE payment_method AS ENUM ('CASH', 'CARD');
    END IF;
END$$;

-- Ensure transaction_status enum includes our values
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_status') THEN
        CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'VOIDED', 'RETURNED');
    END IF;
END$$;

-- Update transactions table to match TransactionEntity
-- Note: Only add columns if they don't exist to avoid conflicts
DO $$
BEGIN
    -- Check if transaction_id column exists, if not add it
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'transactions' AND column_name = 'transaction_id'
    ) THEN
        ALTER TABLE transactions RENAME COLUMN id TO transaction_id;
    END IF;
    
    -- Ensure bill_serial_number column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'transactions' AND column_name = 'bill_serial_number'
    ) THEN
        ALTER TABLE transactions ADD COLUMN bill_serial_number VARCHAR(50);
    END IF;
END$$;

-- Update transaction_items table to match TransactionItemEntity
DO $$
BEGIN
    -- Check if transaction_item_id column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'transaction_items' AND column_name = 'transaction_item_id'
    ) THEN
        ALTER TABLE transaction_items RENAME COLUMN id TO transaction_item_id;
    END IF;
    
    -- Ensure subtotal column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'transaction_items' AND column_name = 'subtotal'
    ) THEN
        ALTER TABLE transaction_items ADD COLUMN subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0;
    END IF;
    
    -- Update existing records to calculate subtotal
    UPDATE transaction_items 
    SET subtotal = COALESCE(total_price, unit_price * quantity) 
    WHERE subtotal = 0 OR subtotal IS NULL;
END$$;

-- Update bills table to match BillEntity
DO $$
BEGIN
    -- Check if bill_id column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'bill_id'
    ) THEN
        ALTER TABLE bills RENAME COLUMN id TO bill_id;
    END IF;
    
    -- Ensure customer_name column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'customer_name'
    ) THEN
        ALTER TABLE bills ADD COLUMN customer_name VARCHAR(255);
    END IF;
    
    -- Ensure pdf_content column exists for storing PDF binary data
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'pdf_content'
    ) THEN
        ALTER TABLE bills ADD COLUMN pdf_content BYTEA;
    END IF;
    
    -- Rename pdf_path to pdf_file_path for consistency
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'pdf_file_path'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'pdf_path'
    ) THEN
        ALTER TABLE bills RENAME COLUMN pdf_path TO pdf_file_path;
    END IF;
    
    -- Add amount columns from transaction
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bills' AND column_name = 'total_amount'
    ) THEN
        ALTER TABLE bills ADD COLUMN total_amount DECIMAL(10, 2);
        ALTER TABLE bills ADD COLUMN discount_amount DECIMAL(10, 2);
        ALTER TABLE bills ADD COLUMN cash_tendered DECIMAL(10, 2);
        ALTER TABLE bills ADD COLUMN change_amount DECIMAL(10, 2);
        ALTER TABLE bills ADD COLUMN synex_points_awarded INTEGER;
    END IF;
END$$;

-- Update existing bill records with transaction data
UPDATE bills 
SET 
    total_amount = t.total_amount,
    discount_amount = t.discount_amount,
    cash_tendered = t.cash_tendered,
    change_amount = t.change_given,
    synex_points_awarded = CAST(t.synex_points_earned as INTEGER)
FROM transactions t 
WHERE bills.transaction_id = t.transaction_id 
AND bills.total_amount IS NULL;

-- Create indexes for new columns
DO $$
BEGIN
    -- Index for bill serial number
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace 
        WHERE c.relname = 'idx_bills_serial_number'
    ) THEN
        CREATE INDEX idx_bills_serial_number ON bills(bill_serial_number);
    END IF;
    
    -- Index for transaction items subtotal
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace 
        WHERE c.relname = 'idx_transaction_items_subtotal'
    ) THEN
        CREATE INDEX idx_transaction_items_subtotal ON transaction_items(subtotal);
    END IF;
END$$;

-- Add comments to document the JPA entity mapping
COMMENT ON TABLE transactions IS 'Mapped to TransactionEntity.java - supports both POS and WEB transactions';
COMMENT ON TABLE transaction_items IS 'Mapped to TransactionItemEntity.java - individual items within transactions';
COMMENT ON TABLE bills IS 'Mapped to BillEntity.java - generated bills with PDF storage capability';

-- Ensure data consistency
DO $$
BEGIN
    -- Update any NULL transaction types to default
    UPDATE transactions SET transaction_type = 'POS' WHERE transaction_type IS NULL;
    
    -- Update any NULL payment methods to default
    UPDATE transactions SET payment_method = 'CASH' WHERE payment_method IS NULL;
    
    -- Update any NULL transaction status to default
    UPDATE transactions SET status = 'COMPLETED' WHERE status IS NULL;
END$$;
