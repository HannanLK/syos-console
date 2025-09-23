-- =============================================================================
-- V15__Sync_Batches_Cost_Columns.sql
-- Purpose: Resolve runtime violation where batches.cost_price is NOT NULL while
--          application inserts only cost_per_unit. We backfill and keep columns
--          synchronized via trigger so either column can satisfy constraints.
-- =============================================================================

-- Only proceed if the batches table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'batches') THEN
        RETURN; -- nothing to do
    END IF;
END$$;

-- 1) Ensure both columns exist with DECIMAL(12,4)
ALTER TABLE IF EXISTS batches
    ADD COLUMN IF NOT EXISTS cost_per_unit DECIMAL(12,4);

-- If cost_price exists, normalize its type
DO $$
DECLARE
    has_cost_price BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'batches' AND column_name = 'cost_price'
    ) INTO has_cost_price;

    IF has_cost_price THEN
        -- Try to align type; using USING cast to avoid errors if already correct
        BEGIN
            ALTER TABLE batches 
                ALTER COLUMN cost_price TYPE DECIMAL(12,4) USING cost_price::DECIMAL(12,4);
        EXCEPTION WHEN others THEN
            -- Ignore if type already compatible
            NULL;
        END;

        -- 2) Backfill existing rows so cost_price is populated if null
        UPDATE batches 
        SET cost_price = COALESCE(cost_price, cost_per_unit)
        WHERE cost_price IS NULL;

        -- 3) Create sync trigger to keep both columns aligned on insert/update
        -- Drop objects if they already exist to keep migration idempotent
        -- Drop trigger and function if they already exist (idempotent)
        DROP TRIGGER IF EXISTS trg_sync_batch_cost_columns ON batches;
        DROP FUNCTION IF EXISTS sync_batch_cost_columns();

        CREATE FUNCTION sync_batch_cost_columns()
        RETURNS trigger AS $FUNC$
        BEGIN
            -- If one is null and the other set, mirror the value
            IF NEW.cost_price IS NULL AND NEW.cost_per_unit IS NOT NULL THEN
                NEW.cost_price := NEW.cost_per_unit;
            ELSIF NEW.cost_per_unit IS NULL AND NEW.cost_price IS NOT NULL THEN
                NEW.cost_per_unit := NEW.cost_price;
            END IF;
            RETURN NEW;
        END;
        $FUNC$ LANGUAGE plpgsql;

        CREATE TRIGGER trg_sync_batch_cost_columns
        BEFORE INSERT OR UPDATE ON batches
        FOR EACH ROW
        EXECUTE FUNCTION sync_batch_cost_columns();
    END IF;
END$$;

-- Notes:
--  - If batches.cost_price does not exist in this database, nothing beyond adding
--    cost_per_unit is required, and this migration is effectively a no-op.
--  - If batches.cost_price exists and is NOT NULL, the trigger ensures that
--    inserts providing only cost_per_unit will have cost_price populated too.
