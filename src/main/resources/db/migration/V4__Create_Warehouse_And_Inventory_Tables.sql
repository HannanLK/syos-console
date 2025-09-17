-- =============================================================================
-- SYOS Database Schema - V4__Create_Warehouse_And_Inventory_Tables.sql
-- Warehouse, Inventory Management, and Stock Transfer Tables
-- =============================================================================

-- -----------------------------------------------------------------------------
-- LOCATIONS TABLE (For warehouses, shelves, web inventory)
-- -----------------------------------------------------------------------------
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    location_code VARCHAR(20) UNIQUE NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    location_type VARCHAR(50) NOT NULL CHECK (location_type IN ('WAREHOUSE', 'SHELF', 'WEB_INVENTORY')),
    parent_location_id BIGINT,
    capacity INT DEFAULT 1000,
    current_occupancy INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_occupancy CHECK (current_occupancy >= 0 AND current_occupancy <= capacity)
);

-- -----------------------------------------------------------------------------
-- ITEM_MASTER_FILE TABLE (Product catalog)
-- -----------------------------------------------------------------------------
CREATE TABLE item_master_file (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(20) UNIQUE NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    brand_id BIGINT,
    category_id BIGINT NOT NULL,
    supplier_id BIGINT,
    description TEXT,
    unit_of_measure unit_of_measure NOT NULL DEFAULT 'EACH',
    pack_size DECIMAL(10, 3) DEFAULT 1,
    pack_size_unit VARCHAR(10),
    cost_price DECIMAL(10, 2) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    reorder_point INT NOT NULL DEFAULT 50,
    max_stock_level INT DEFAULT 1000,
    status product_status DEFAULT 'ACTIVE',
    barcode VARCHAR(50),
    is_perishable BOOLEAN DEFAULT false,
    is_featured BOOLEAN DEFAULT false,
    is_web_available BOOLEAN DEFAULT true,
    date_added DATE DEFAULT CURRENT_DATE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_prices CHECK (cost_price >= 0 AND selling_price >= 0),
    CONSTRAINT chk_price_margin CHECK (selling_price >= cost_price),
    CONSTRAINT chk_reorder CHECK (reorder_point >= 0),
    CONSTRAINT chk_pack_size CHECK (pack_size > 0)
);

-- -----------------------------------------------------------------------------
-- BATCHES TABLE (Track product batches with expiry)
-- -----------------------------------------------------------------------------
CREATE TABLE batches (
    id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(50) NOT NULL,
    item_id BIGINT NOT NULL,
    supplier_batch_number VARCHAR(50),
    quantity_received DECIMAL(10, 3) NOT NULL,
    quantity_available DECIMAL(10, 3) NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL,
    manufacture_date DATE,
    expiry_date DATE,
    received_date DATE DEFAULT CURRENT_DATE,
    is_expired BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_quantities CHECK (quantity_available >= 0 AND quantity_available <= quantity_received),
    CONSTRAINT chk_dates CHECK (expiry_date IS NULL OR expiry_date > manufacture_date),
    UNIQUE(batch_number, item_id)
);

-- -----------------------------------------------------------------------------
-- WAREHOUSE_STOCK TABLE (Warehouse inventory with batch tracking)
-- -----------------------------------------------------------------------------
CREATE TABLE warehouse_stock (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(100) UNIQUE NOT NULL,
    item_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    reserved_quantity DECIMAL(10, 3) DEFAULT 0,
    received_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE RESTRICT,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE RESTRICT,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_warehouse_quantities CHECK (quantity >= 0 AND reserved_quantity >= 0 AND reserved_quantity <= quantity)
);

-- Create trigger to validate location type for warehouse_stock
CREATE OR REPLACE FUNCTION validate_warehouse_location()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM locations 
        WHERE id = NEW.location_id AND location_type = 'WAREHOUSE'
    ) THEN
        RAISE EXCEPTION 'Location must be of type WAREHOUSE';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_warehouse_location
BEFORE INSERT OR UPDATE ON warehouse_stock
FOR EACH ROW EXECUTE FUNCTION validate_warehouse_location();

-- -----------------------------------------------------------------------------
-- SHELF_STOCK TABLE (Store shelf inventory)
-- -----------------------------------------------------------------------------
CREATE TABLE shelf_stock (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    batch_id BIGINT,
    location_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL DEFAULT 0,
    min_stock_level DECIMAL(10, 3) DEFAULT 10,
    max_stock_level DECIMAL(10, 3) DEFAULT 100,
    last_restocked TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
    CONSTRAINT chk_shelf_stock CHECK (quantity >= 0),
    UNIQUE(item_id, batch_id, location_id)
);

-- Create trigger to validate location type for shelf_stock
CREATE OR REPLACE FUNCTION validate_shelf_location()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM locations 
        WHERE id = NEW.location_id AND location_type = 'SHELF'
    ) THEN
        RAISE EXCEPTION 'Location must be of type SHELF';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_shelf_location
BEFORE INSERT OR UPDATE ON shelf_stock
FOR EACH ROW EXECUTE FUNCTION validate_shelf_location();

-- -----------------------------------------------------------------------------
-- WEB_INVENTORY TABLE (Web/Online inventory)
-- -----------------------------------------------------------------------------
CREATE TABLE web_inventory (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    batch_id BIGINT,
    location_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(10, 3) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
    CONSTRAINT chk_web_quantities CHECK (quantity >= 0 AND reserved_quantity >= 0 AND reserved_quantity <= quantity),
    UNIQUE(item_id, batch_id, location_id)
);

-- Create trigger to validate location type for web_inventory
CREATE OR REPLACE FUNCTION validate_web_location()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM locations 
        WHERE id = NEW.location_id AND location_type = 'WEB_INVENTORY'
    ) THEN
        RAISE EXCEPTION 'Location must be of type WEB_INVENTORY';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_web_location
BEFORE INSERT OR UPDATE ON web_inventory
FOR EACH ROW EXECUTE FUNCTION validate_web_location();

-- -----------------------------------------------------------------------------
-- STOCK_TRANSFERS TABLE (Track movements between locations)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_code VARCHAR(100) UNIQUE NOT NULL,
    item_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    from_location_id BIGINT NOT NULL,
    to_location_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    transfer_type transfer_type NOT NULL,
    transfer_status transfer_status DEFAULT 'PENDING',
    transfer_reason TEXT,
    initiated_by BIGINT NOT NULL,
    approved_by BIGINT,
    completed_by BIGINT,
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE RESTRICT,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE RESTRICT,
    FOREIGN KEY (from_location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    FOREIGN KEY (to_location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    FOREIGN KEY (initiated_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_transfer_quantity CHECK (quantity > 0),
    CONSTRAINT chk_different_locations CHECK (from_location_id != to_location_id)
);

-- -----------------------------------------------------------------------------
-- STOCK_MOVEMENTS TABLE (Audit trail for all stock changes)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    item_id BIGINT NOT NULL,
    batch_id BIGINT,
    from_location_id BIGINT,
    to_location_id BIGINT,
    movement_type VARCHAR(50) NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reason TEXT,
    performed_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE SET NULL,
    FOREIGN KEY (from_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    FOREIGN KEY (to_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_movement_quantity CHECK (quantity != 0)
);

-- -----------------------------------------------------------------------------
-- NOTIFICATIONS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    notification_type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    is_dismissed BOOLEAN DEFAULT false,
    dismissed_at TIMESTAMP,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- Create indexes for performance
-- -----------------------------------------------------------------------------
CREATE INDEX idx_warehouse_stock_item ON warehouse_stock(item_id);
CREATE INDEX idx_warehouse_stock_batch ON warehouse_stock(batch_id);
CREATE INDEX idx_warehouse_stock_location ON warehouse_stock(location_id);
CREATE INDEX idx_warehouse_stock_available ON warehouse_stock(item_id, quantity) WHERE quantity > 0;

CREATE INDEX idx_shelf_stock_item ON shelf_stock(item_id);
CREATE INDEX idx_shelf_stock_batch ON shelf_stock(batch_id);
CREATE INDEX idx_shelf_stock_location ON shelf_stock(location_id);

CREATE INDEX idx_web_inventory_item ON web_inventory(item_id);
CREATE INDEX idx_web_inventory_batch ON web_inventory(batch_id);
CREATE INDEX idx_web_inventory_available ON web_inventory(item_id, quantity) WHERE quantity > reserved_quantity;

CREATE INDEX idx_stock_transfers_item ON stock_transfers(item_id);
CREATE INDEX idx_stock_transfers_status ON stock_transfers(transfer_status);
CREATE INDEX idx_stock_transfers_dates ON stock_transfers(initiated_at, completed_at);

CREATE INDEX idx_stock_movements_item ON stock_movements(item_id);
CREATE INDEX idx_stock_movements_date ON stock_movements(movement_date);

CREATE INDEX idx_batches_item ON batches(item_id);
CREATE INDEX idx_batches_expiry ON batches(expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX idx_batches_available ON batches(id) WHERE quantity_available > 0;

CREATE INDEX idx_locations_type ON locations(location_type);
CREATE INDEX idx_locations_active ON locations(is_active) WHERE is_active = true;

CREATE INDEX idx_item_master_code ON item_master_file(item_code);
CREATE INDEX idx_item_master_category ON item_master_file(category_id);
CREATE INDEX idx_item_master_status ON item_master_file(status) WHERE status = 'ACTIVE';

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = false;
CREATE INDEX idx_notifications_type ON notifications(notification_type);

-- -----------------------------------------------------------------------------
-- Apply update timestamp triggers
-- -----------------------------------------------------------------------------
CREATE TRIGGER update_warehouse_stock_updated_at BEFORE UPDATE ON warehouse_stock
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shelf_stock_updated_at BEFORE UPDATE ON shelf_stock
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_web_inventory_updated_at BEFORE UPDATE ON web_inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stock_transfers_updated_at BEFORE UPDATE ON stock_transfers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_batches_updated_at BEFORE UPDATE ON batches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_locations_updated_at BEFORE UPDATE ON locations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_item_master_updated_at BEFORE UPDATE ON item_master_file
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- Comments
-- -----------------------------------------------------------------------------
COMMENT ON TABLE locations IS 'Physical and logical locations for inventory (warehouse, shelf, web)';
COMMENT ON TABLE item_master_file IS 'Product catalog with all item details';
COMMENT ON TABLE batches IS 'Product batches with expiry tracking for FIFO implementation';
COMMENT ON TABLE warehouse_stock IS 'Warehouse inventory tracking';
COMMENT ON TABLE shelf_stock IS 'Store shelf inventory tracking';
COMMENT ON TABLE web_inventory IS 'Web/online inventory tracking';
COMMENT ON TABLE stock_transfers IS 'Stock movement tracking between locations';
COMMENT ON TABLE stock_movements IS 'Audit trail for all inventory changes';
COMMENT ON TABLE notifications IS 'System notifications for reorder, expiry, and other alerts';