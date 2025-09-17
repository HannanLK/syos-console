-- =============================================================================
-- SYOS Database Schema - V4__Create_Item_Master_File_Table.sql
-- Item Master File with complete product information and relationships
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ITEM_MASTER_FILE TABLE (Main Product Catalog)
-- -----------------------------------------------------------------------------
CREATE TABLE item_master_file (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Core Product Identity
    item_code VARCHAR(50) UNIQUE NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    description TEXT,
    
    -- Foreign Key Relationships
    brand_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    
    -- Product Specifications
    unit_of_measure unit_of_measure NOT NULL DEFAULT 'EACH',
    pack_size DECIMAL(10,3) DEFAULT 1.0,
    
    -- Pricing Information
    cost_price DECIMAL(12,4) NOT NULL CHECK (cost_price >= 0),
    selling_price DECIMAL(12,4) NOT NULL CHECK (selling_price >= 0),
    
    -- Inventory Management
    reorder_point INTEGER DEFAULT 50 CHECK (reorder_point >= 0),
    
    -- Product Classification
    is_perishable BOOLEAN DEFAULT false,
    status product_status DEFAULT 'ACTIVE',
    
    -- Featured and Latest Product Flags
    is_featured BOOLEAN DEFAULT false,
    is_latest BOOLEAN DEFAULT false,
    
    -- Audit Fields
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Business Logic Constraints
    CONSTRAINT chk_cost_price_positive CHECK (cost_price > 0),
    CONSTRAINT chk_selling_price_positive CHECK (selling_price > 0),
    CONSTRAINT chk_selling_price_ge_cost CHECK (selling_price >= cost_price),
    CONSTRAINT chk_item_name_not_empty CHECK (TRIM(item_name) != ''),
    CONSTRAINT chk_pack_size_positive CHECK (pack_size > 0),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_item_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_item_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_item_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_item_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- -----------------------------------------------------------------------------
-- PERFORMANCE INDEXES
-- -----------------------------------------------------------------------------

-- Primary lookup indexes for business operations
CREATE INDEX idx_item_code_active ON item_master_file (item_code) 
    WHERE status = 'ACTIVE';

CREATE INDEX idx_item_name_search ON item_master_file 
    USING gin(to_tsvector('english', item_name));

-- Category-based browsing (major use case)
CREATE INDEX idx_item_category_active ON item_master_file (category_id, status) 
    WHERE status = 'ACTIVE';

-- Brand-based filtering
CREATE INDEX idx_item_brand_active ON item_master_file (brand_id, status) 
    WHERE status = 'ACTIVE';

-- Featured products for front-end display
CREATE INDEX idx_item_featured ON item_master_file (is_featured, status) 
    WHERE is_featured = true AND status = 'ACTIVE';

-- Latest products for front-end display
CREATE INDEX idx_item_latest ON item_master_file (is_latest, date_added DESC) 
    WHERE is_latest = true AND status = 'ACTIVE';

-- Supplier management
CREATE INDEX idx_item_supplier ON item_master_file (supplier_id);

-- Reorder management for inventory
CREATE INDEX idx_item_reorder_analysis ON item_master_file (reorder_point, status);

-- Perishable items for expiry management
CREATE INDEX idx_item_perishable ON item_master_file (is_perishable) 
    WHERE is_perishable = true AND status = 'ACTIVE';

-- Price range queries for reporting
CREATE INDEX idx_item_pricing ON item_master_file (selling_price, cost_price);

-- -----------------------------------------------------------------------------
-- TRIGGER FOR AUTOMATIC TIMESTAMP UPDATE
-- -----------------------------------------------------------------------------
CREATE TRIGGER update_item_master_file_updated_at 
    BEFORE UPDATE ON item_master_file
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- VIEWS FOR COMMON QUERIES
-- -----------------------------------------------------------------------------

-- Active items with full details (most common query)
CREATE VIEW active_items_view AS
SELECT 
    imf.*,
    b.brand_name,
    c.category_name,
    s.supplier_name,
    c.parent_category_id,
    CASE WHEN c.parent_category_id IS NULL THEN c.category_name
         ELSE (SELECT category_name FROM categories WHERE id = c.parent_category_id) || ' > ' || c.category_name
    END as full_category_path
FROM item_master_file imf
    JOIN brands b ON imf.brand_id = b.id
    JOIN categories c ON imf.category_id = c.id
    JOIN suppliers s ON imf.supplier_id = s.id
WHERE imf.status = 'ACTIVE' 
    AND b.is_active = true 
    AND c.is_active = true 
    AND s.is_active = true;

-- Featured products view for front-end
CREATE VIEW featured_products_view AS
SELECT * FROM active_items_view 
WHERE is_featured = true
ORDER BY date_added DESC;

-- Latest products view for front-end
CREATE VIEW latest_products_view AS
SELECT * FROM active_items_view 
WHERE is_latest = true
ORDER BY date_added DESC;

-- -----------------------------------------------------------------------------
-- COMMENTS AND DOCUMENTATION
-- -----------------------------------------------------------------------------
COMMENT ON TABLE item_master_file IS 'Master catalog of all products in SYOS inventory system';
COMMENT ON COLUMN item_master_file.item_code IS 'Unique identifier for the product (used in POS and inventory)';
COMMENT ON COLUMN item_master_file.reorder_point IS 'Minimum stock level to trigger reorder notification (default 50)';
COMMENT ON COLUMN item_master_file.is_perishable IS 'Flag indicating if product has expiry date considerations';
COMMENT ON COLUMN item_master_file.is_featured IS 'Flag for featuring product in store front displays';
COMMENT ON COLUMN item_master_file.is_latest IS 'Flag for showing product in latest arrivals section';
COMMENT ON COLUMN item_master_file.pack_size IS 'Size of the package (e.g., 500g, 2L, etc.)';

-- -----------------------------------------------------------------------------
-- VERIFICATION
-- -----------------------------------------------------------------------------
DO
$do$
    BEGIN
        -- Verify table creation
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'item_master_file') THEN
            RAISE NOTICE 'Item Master File table created successfully!';
        END IF;
        
        -- Verify views
        IF EXISTS (SELECT 1 FROM information_schema.views WHERE table_name = 'active_items_view') THEN
            RAISE NOTICE 'Active Items View created successfully!';
        END IF;
    END
$do$;
