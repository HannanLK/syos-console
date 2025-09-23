-- Align warehouse_stock and related tables with Entity mappings and triggers
-- This migration adds missing columns expected by JPA entities and DB triggers, and backfills data

-- 1) Add missing columns on warehouse_stock (used by BEFORE UPDATE trigger update_updated_at_column)
ALTER TABLE warehouse_stock
    ADD COLUMN IF NOT EXISTS item_code VARCHAR(50) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS quantity_received DECIMAL(12,3) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS quantity_available DECIMAL(12,3) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS expiry_date TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS received_by BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS location VARCHAR(50),
    ADD COLUMN IF NOT EXISTS is_reserved BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS reserved_by BIGINT,
    ADD COLUMN IF NOT EXISTS reserved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_updated_by BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;  -- required by trigger

-- 1b) Ensure web_inventory also has updated_at for the same trigger
ALTER TABLE IF EXISTS web_inventory
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;  -- required by trigger

-- 2) Backfill item_code from item_master_file
UPDATE warehouse_stock ws
SET item_code = im.item_code
FROM item_master_file im
WHERE ws.item_id = im.id AND (ws.item_code IS NULL OR ws.item_code = '');

-- 3) Backfill quantities from existing columns
UPDATE warehouse_stock ws
SET 
    quantity_received = COALESCE(ws.quantity, 0),
    quantity_available = COALESCE(ws.quantity, 0) - COALESCE(ws.reserved_quantity, 0);

-- 4) Backfill location from locations table
UPDATE warehouse_stock ws
SET location = COALESCE(l.location_name, 'MAIN-WAREHOUSE')
FROM locations l
WHERE ws.location_id = l.id AND (ws.location IS NULL OR ws.location = '');

-- 5) Backfill received_by and last_updated_by from created_by
UPDATE warehouse_stock ws
SET received_by = COALESCE(ws.created_by, 0)
WHERE COALESCE(ws.received_by, 0) = 0;

UPDATE warehouse_stock ws
SET last_updated_by = COALESCE(ws.created_by, 0)
WHERE COALESCE(ws.last_updated_by, 0) = 0;

-- 6) Backfill expiry_date from batches table
UPDATE warehouse_stock ws
SET expiry_date = b.expiry_date
FROM batches b
WHERE ws.batch_id = b.id AND ws.expiry_date IS NULL;

-- Optional: simple index to speed up lookups by item_code
CREATE INDEX IF NOT EXISTS idx_warehouse_stock_item_code ON warehouse_stock(item_code);
