-- =============================================================================
-- SYOS Database Schema - V14__Alter_Batches_Add_Received_By_And_Align_Expiry_Type.sql
-- Fixes for batches table to match JPA entity and runtime usage
-- - Adds missing received_by column if it does not exist
-- - Ensures expiry_date is TIMESTAMP (align with LocalDateTime in JPA)
-- =============================================================================

-- Add received_by column if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'batches' AND column_name = 'received_by'
    ) THEN
        ALTER TABLE batches ADD COLUMN received_by BIGINT;
    END IF;
END$$;


-- Align expiry_date column to TIMESTAMP if currently DATE
-- Handle dependency: fifo_batch_selection view depends on batches.expiry_date
-- Drop the view temporarily, alter the column, then recreate the view.
DO $$
DECLARE
    col_type TEXT;
    has_fifo BOOLEAN;
    has_expsoon BOOLEAN;
BEGIN
    -- Check existing type of expiry_date
    SELECT data_type INTO col_type
    FROM information_schema.columns
    WHERE table_name = 'batches' AND column_name = 'expiry_date';

    -- Check if dependent views exist
    SELECT EXISTS (
        SELECT 1 FROM information_schema.views 
        WHERE table_name = 'fifo_batch_selection'
    ) INTO has_fifo;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.views 
        WHERE table_name = 'batches_expiring_soon'
    ) INTO has_expsoon;

    IF col_type = 'date' THEN
        -- Drop dependent views if present to allow type change
        IF has_fifo THEN
            EXECUTE 'DROP VIEW fifo_batch_selection';
        END IF;
        IF has_expsoon THEN
            EXECUTE 'DROP VIEW batches_expiring_soon';
        END IF;

        -- Perform the type change
        ALTER TABLE batches 
            ALTER COLUMN expiry_date TYPE TIMESTAMP 
            USING CASE 
                WHEN expiry_date IS NULL THEN NULL 
                ELSE expiry_date::timestamp 
            END;

        -- Recreate fifo_batch_selection with TIMESTAMP-aware logic
        EXECUTE $VIEW$
            CREATE VIEW fifo_batch_selection AS
            SELECT 
                b.*, 
                i.item_code,
                i.item_name,
                i.is_perishable,
                l.location_code,
                l.location_name,
                s.available_stock,
                ROW_NUMBER() OVER (
                    PARTITION BY b.item_id, l.id 
                    ORDER BY 
                        CASE WHEN i.is_perishable AND b.expiry_date IS NOT NULL 
                             THEN b.expiry_date 
                             ELSE (CURRENT_TIMESTAMP + INTERVAL '10 years') 
                        END ASC,
                        b.received_date ASC,
                        b.id ASC
                ) AS fifo_priority
            FROM batches b
                JOIN item_master_file i ON b.item_id = i.id
                JOIN stock s ON b.id = s.batch_id
                JOIN locations l ON s.location_id = l.id
            WHERE b.quantity_available > 0 
                AND s.available_stock > 0
                AND i.status = 'ACTIVE';
        $VIEW$;

        -- Recreate batches_expiring_soon view with TIMESTAMP-aware logic
        EXECUTE $VIEW2$
            CREATE VIEW batches_expiring_soon AS
            SELECT 
                b.*
            FROM batches b
            WHERE b.expiry_date IS NOT NULL
              AND b.expiry_date > CURRENT_TIMESTAMP
              AND b.expiry_date <= CURRENT_TIMESTAMP + INTERVAL '30 days';
        $VIEW2$;
    END IF;
END$$;

-- Optional: Ensure cost_per_unit exists (idempotent safety in case V8 missed it)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'batches' AND column_name = 'cost_per_unit'
    ) THEN
        ALTER TABLE batches ADD COLUMN cost_per_unit DECIMAL(12,4);
    END IF;
END$$;


