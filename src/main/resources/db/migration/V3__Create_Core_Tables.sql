-- =============================================================================
-- SYOS Database Schema - V3__Create_Core_Tables.sql
-- Core tables: Categories, Brands, Suppliers
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 2 SUPPLIERS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(20) UNIQUE NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    supplier_phone VARCHAR(20) NOT NULL,
    supplier_email VARCHAR(100),
    supplier_address TEXT,
    contact_person VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_supplier_email CHECK (
        supplier_email IS NULL OR 
        supplier_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    )
);

-- -----------------------------------------------------------------------------
-- 3 BRANDS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    brand_code VARCHAR(20) UNIQUE NOT NULL,
    brand_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 4 BRAND_SUPPLIER JUNCTION TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE brand_suppliers (
    brand_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (brand_id, supplier_id),
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 5 CATEGORIES TABLE (Self-referencing for hierarchy)
-- -----------------------------------------------------------------------------
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    parent_category_id BIGINT,
    category_code VARCHAR(20) UNIQUE NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- Create indexes
-- -----------------------------------------------------------------------------
CREATE INDEX idx_suppliers_active ON suppliers(is_active) WHERE is_active = true;
CREATE INDEX idx_brands_active ON brands(is_active) WHERE is_active = true;
CREATE INDEX idx_categories_parent ON categories(parent_category_id);
CREATE INDEX idx_categories_active ON categories(is_active) WHERE is_active = true;

-- -----------------------------------------------------------------------------
-- Apply update timestamp triggers
-- -----------------------------------------------------------------------------
CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON suppliers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_brands_updated_at BEFORE UPDATE ON brands
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- Comments
-- -----------------------------------------------------------------------------
COMMENT ON TABLE suppliers IS 'Supplier information for product procurement';
COMMENT ON TABLE brands IS 'Product brands available in SYOS';
COMMENT ON TABLE categories IS 'Hierarchical product categories with parent-child relationships';
COMMENT ON TABLE brand_suppliers IS 'Many-to-many relationship between brands and suppliers';