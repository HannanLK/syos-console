-- =============================================================================
-- V10__Alter_Batches_Add_Cost_Per_Unit.sql
-- Purpose: Align database schema with domain/JPA by adding cost_per_unit column
-- Context: Runtime error indicated missing column "cost_per_unit" on table "batches"
-- =============================================================================

-- Add column only if it does not already exist
ALTER TABLE IF EXISTS batches
    ADD COLUMN IF NOT EXISTS cost_per_unit DECIMAL(12,4);

-- Optional: comment for documentation
COMMENT ON COLUMN batches.cost_per_unit IS 'Per-unit procurement cost for this batch (nullable)';
