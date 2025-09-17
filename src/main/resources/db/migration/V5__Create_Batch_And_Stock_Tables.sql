-- =============================================================================
-- SYOS Database Schema - V5__Create_Batch_And_Stock_Tables.sql
-- Stock management with FIFO support and batch tracking
-- =============================================================================

-- -----------------------------------------------------------------------------
-- BATCHES TABLE (Tracks incoming stock with expiry dates)
-- -----------------------------------------------------------------------------
CREATE TABLE batches (
    id BIGSERIAL PRIMARY KEY,
    
    -- Product Reference
    item_id BIGINT NOT NULL,
    
    -- Batch Information
    batch_number VARCHAR(100) NOT NULL,
    manufacturer_batch_number VARCHAR(100),
    
    -- Quantity Information
    quantity_received INTEGER NOT NULL CHECK (quantity_received > 0),
    quantity_available INTEGER NOT NULL CHECK (quantity_available >= 0),
    
    -- Date Information for FIFO and Expiry
    manufacture_date DATE,
    expiry_date DATE,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Audit Information
    received_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_quantity_available_le_received 
        CHECK (quantity_available <= quantity_received),
    CONSTRAINT chk_batch_number_not_empty 
        CHECK (TRIM(batch_number) != ''),
    CONSTRAINT chk_expiry_after_manufacture 
        CHECK (expiry_date IS NULL OR manufacture_date IS NULL OR expiry_date > manufacture_date),
    
    -- Foreign Keys
    CONSTRAINT fk_batch_item FOREIGN KEY (item_id) REFERENCES item_master_file(id),
    CONSTRAINT fk_batch_received_by FOREIGN KEY (received_by) REFERENCES users(id),
    
    -- Unique constraint for batch tracking
    CONSTRAINT uk_item_batch_number UNIQUE (item_id, batch_number)
);

-- -----------------------------------------------------------------------------
-- LOCATIONS TABLE (Physical locations for stock)
-- -----------------------------------------------------------------------------
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    location_code VARCHAR(20) UNIQUE NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    location_type VARCHAR(50) NOT NULL, -- WAREHOUSE, SHELF, WEB
    description TEXT,
    capacity INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_location_code_format 
        CHECK (location_code ~ '^[A-Z0-9_-]+$'),
    CONSTRAINT chk_capacity_positive 
        CHECK (capacity IS NULL OR capacity > 0)
);

-- -----------------------------------------------------------------------------
-- STOCK TABLE (Current stock levels at different locations)
-- -----------------------------------------------------------------------------
CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,
    
    -- References
    item_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    batch_id BIGINT,
    
    -- Stock Information
    current_stock INTEGER NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    reserved_stock INTEGER NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    available_stock INTEGER GENERATED ALWAYS AS (current_stock - reserved_stock) STORED,
    
    -- Audit Information
    last_movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_reserved_le_current 
        CHECK (reserved_stock <= current_stock),
    
    -- Foreign Keys
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES item_master_file(id),
    CONSTRAINT fk_stock_location FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT fk_stock_batch FOREIGN KEY (batch_id) REFERENCES batches(id),
    
    -- Unique constraint to prevent duplicate stock entries
    CONSTRAINT uk_item_location_batch UNIQUE (item_id, location_id, batch_id)
);

-- -----------------------------------------------------------------------------
-- STOCK_MOVEMENTS TABLE (Audit trail for all stock changes)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    
    -- References
    stock_id BIGINT NOT NULL,
    
    -- Movement Information
    movement_type VARCHAR(50) NOT NULL, -- IN, OUT, TRANSFER, ADJUSTMENT
    quantity_change INTEGER NOT NULL, -- positive for IN, negative for OUT
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    
    -- Movement Details
    reason VARCHAR(200),
    reference_number VARCHAR(100), -- PO number, transfer ID, etc.
    
    -- Audit Information
    performed_by BIGINT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_movement_type_valid 
        CHECK (movement_type IN ('IN', 'OUT', 'TRANSFER', 'ADJUSTMENT')),
    CONSTRAINT chk_quantities_consistent 
        CHECK (new_quantity = previous_quantity + quantity_change),
    CONSTRAINT chk_new_quantity_non_negative 
        CHECK (new_quantity >= 0),
    
    -- Foreign Keys
    CONSTRAINT fk_movement_stock FOREIGN KEY (stock_id) REFERENCES stock(id),
    CONSTRAINT fk_movement_performed_by FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- -----------------------------------------------------------------------------
-- PERFORMANCE INDEXES
-- -----------------------------------------------------------------------------

-- Batch Management
CREATE INDEX idx_batches_item_expiry ON batches (item_id, expiry_date) 
    WHERE quantity_available > 0;
    
CREATE INDEX idx_batches_item_purchase_date ON batches (item_id, purchase_date) 
    WHERE quantity_available > 0;

CREATE INDEX idx_batches_expiry_soon ON batches (expiry_date) 
    WHERE expiry_date IS NOT NULL 
    AND quantity_available > 0 
    AND expiry_date <= CURRENT_DATE + INTERVAL '30 days';

-- Stock Management
CREATE INDEX idx_stock_item_location ON stock (item_id, location_id);
CREATE INDEX idx_stock_available ON stock (available_stock) WHERE available_stock > 0;
CREATE INDEX idx_stock_low_levels ON stock (item_id, available_stock);

-- Location Management
CREATE INDEX idx_locations_active ON locations (location_type, is_active) 
    WHERE is_active = true;

-- Stock Movement Audit
CREATE INDEX idx_movements_stock_date ON stock_movements (stock_id, performed_at);
CREATE INDEX idx_movements_reference ON stock_movements (reference_number) 
    WHERE reference_number IS NOT NULL;

-- -----------------------------------------------------------------------------
-- TRIGGERS
-- -----------------------------------------------------------------------------

-- Update timestamp triggers
CREATE TRIGGER update_batches_updated_at 
    BEFORE UPDATE ON batches
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_locations_updated_at 
    BEFORE UPDATE ON locations
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stock_updated_at 
    BEFORE UPDATE ON stock
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Stock movement logging trigger
CREATE OR REPLACE FUNCTION log_stock_movement()
RETURNS TRIGGER AS $$
BEGIN
    -- Only log if current_stock changes
    IF OLD.current_stock != NEW.current_stock THEN
        INSERT INTO stock_movements (
            stock_id, 
            movement_type, 
            quantity_change,
            previous_quantity,
            new_quantity,
            reason,
            performed_by
        ) VALUES (
            NEW.id,
            CASE 
                WHEN NEW.current_stock > OLD.current_stock THEN 'IN'
                ELSE 'OUT'
            END,
            NEW.current_stock - OLD.current_stock,
            OLD.current_stock,
            NEW.current_stock,
            'Automatic stock update',
            1 -- System user, should be configurable
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_stock_movement
    AFTER UPDATE ON stock
    FOR EACH ROW
    EXECUTE FUNCTION log_stock_movement();

-- -----------------------------------------------------------------------------
-- VIEWS FOR BUSINESS OPERATIONS
-- -----------------------------------------------------------------------------

-- Current stock levels with item information
CREATE VIEW current_stock_levels AS
SELECT 
    i.id as item_id,
    i.item_code,
    i.item_name,
    l.location_code,
    l.location_name,
    l.location_type,
    COALESCE(SUM(s.current_stock), 0) as total_current_stock,
    COALESCE(SUM(s.reserved_stock), 0) as total_reserved_stock,
    COALESCE(SUM(s.available_stock), 0) as total_available_stock,
    i.reorder_point,
    CASE 
        WHEN COALESCE(SUM(s.available_stock), 0) <= i.reorder_point THEN true
        ELSE false
    END as needs_reorder
FROM item_master_file i
    CROSS JOIN locations l
    LEFT JOIN stock s ON i.id = s.item_id AND l.id = s.location_id
WHERE i.status = 'ACTIVE' AND l.is_active = true
GROUP BY i.id, i.item_code, i.item_name, i.reorder_point, 
         l.id, l.location_code, l.location_name, l.location_type;

-- FIFO batch selection view (for stock allocation)
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
                 ELSE CURRENT_DATE + INTERVAL '10 years' 
            END ASC,
            b.purchase_date ASC,
            b.id ASC
    ) as fifo_priority
FROM batches b
    JOIN item_master_file i ON b.item_id = i.id
    JOIN stock s ON b.id = s.batch_id
    JOIN locations l ON s.location_id = l.id
WHERE b.quantity_available > 0 
    AND s.available_stock > 0
    AND i.status = 'ACTIVE';

-- Items requiring reorder
CREATE VIEW items_requiring_reorder AS
SELECT DISTINCT
    i.id,
    i.item_code,
    i.item_name,
    i.reorder_point,
    COALESCE(total_stock.available_stock, 0) as current_available_stock,
    b.brand_name,
    s.supplier_name
FROM item_master_file i
    JOIN brands b ON i.brand_id = b.id
    JOIN suppliers s ON i.supplier_id = s.id
    LEFT JOIN (
        SELECT 
            item_id,
            SUM(available_stock) as available_stock
        FROM stock st
            JOIN locations l ON st.location_id = l.id
        WHERE l.is_active = true
        GROUP BY item_id
    ) total_stock ON i.id = total_stock.item_id
WHERE i.status = 'ACTIVE'
    AND COALESCE(total_stock.available_stock, 0) <= i.reorder_point;

-- -----------------------------------------------------------------------------
-- SAMPLE DATA INSERTION (Default Locations)
-- -----------------------------------------------------------------------------
INSERT INTO locations (location_code, location_name, location_type, description) VALUES 
('MAIN_WH', 'Main Warehouse', 'WAREHOUSE', 'Primary storage facility'),
('SHELF_01', 'Store Shelf Section 1', 'SHELF', 'Front store display shelf'),
('SHELF_02', 'Store Shelf Section 2', 'SHELF', 'Side store display shelf'),
('WEB_INV', 'Web Inventory', 'WEB', 'Online store inventory');

-- -----------------------------------------------------------------------------
-- COMMENTS
-- -----------------------------------------------------------------------------
COMMENT ON TABLE batches IS 'Tracks received stock batches with expiry dates for FIFO processing';
COMMENT ON TABLE locations IS 'Physical and logical locations where stock can be stored';
COMMENT ON TABLE stock IS 'Current stock levels at each location for each item and batch';
COMMENT ON TABLE stock_movements IS 'Audit trail for all stock quantity changes';

COMMENT ON COLUMN batches.quantity_available IS 'Available quantity from this batch (decreases with sales)';
COMMENT ON COLUMN stock.reserved_stock IS 'Stock reserved for pending transactions';
COMMENT ON COLUMN stock.available_stock IS 'Current stock minus reserved stock (computed column)';

-- -----------------------------------------------------------------------------
-- VERIFICATION
-- -----------------------------------------------------------------------------
DO
$do$
    BEGIN
        RAISE NOTICE 'Batch and Stock management tables created successfully!';
        RAISE NOTICE 'Default locations inserted: MAIN_WH, SHELF_01, SHELF_02, WEB_INV';
        RAISE NOTICE 'FIFO and stock level views created for business operations';
    END
$do$;
