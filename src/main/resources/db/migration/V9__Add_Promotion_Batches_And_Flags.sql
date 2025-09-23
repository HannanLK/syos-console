-- V8: Batch-specific promotions (Option A)
-- Adds promotion_batches junction and an is_batch_specific flag to promotions

-- Create junction table for promotion to batches mapping
CREATE TABLE IF NOT EXISTS promotion_batches (
    promotion_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (promotion_id, batch_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE
);

-- Optional flag to indicate batch specificity on a promotion
ALTER TABLE promotions
    ADD COLUMN IF NOT EXISTS is_batch_specific BOOLEAN DEFAULT FALSE;
