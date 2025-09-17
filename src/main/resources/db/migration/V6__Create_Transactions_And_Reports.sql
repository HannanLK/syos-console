-- =============================================================================
-- SYOS Database Schema - V6__Create_Transactions_And_Reports.sql
-- Transaction Tables, Bills, and Reporting Views
-- =============================================================================

-- -----------------------------------------------------------------------------
-- PROMOTIONS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    promo_code VARCHAR(20) UNIQUE NOT NULL,
    promo_name VARCHAR(100) NOT NULL,
    description TEXT,
    promotion_type promotion_type NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_purchase_amount DECIMAL(10, 2),
    max_discount_amount DECIMAL(10, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    usage_limit INT,
    used_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_promo_dates CHECK (end_date > start_date),
    CONSTRAINT chk_discount_value CHECK (discount_value > 0)
);

-- -----------------------------------------------------------------------------
-- PROMOTION_ITEMS JUNCTION TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE promotion_items (
    promotion_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (promotion_id, item_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- TRANSACTIONS TABLE (Supports both POS and WEB transactions)
-- -----------------------------------------------------------------------------
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT,
    cashier_id BIGINT,
    transaction_type transaction_type NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal_amount DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_method payment_method NOT NULL,
    cash_tendered DECIMAL(12, 2),
    change_given DECIMAL(10, 2),
    card_last_four VARCHAR(4),
    card_transaction_id VARCHAR(100),
    synex_points_earned DECIMAL(10, 2) DEFAULT 0,
    synex_points_redeemed DECIMAL(10, 2) DEFAULT 0,
    status transaction_status DEFAULT 'COMPLETED',
    promotion_id BIGINT,
    source_location_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE SET NULL,
    FOREIGN KEY (source_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT chk_amounts CHECK (total_amount >= 0 AND subtotal_amount >= 0),
    CONSTRAINT chk_change CHECK (change_given >= 0 OR change_given IS NULL),
    CONSTRAINT chk_pos_cashier CHECK (
        (transaction_type = 'POS' AND cashier_id IS NOT NULL) OR
        (transaction_type = 'WEB' AND cashier_id IS NULL)
    )
);

-- -----------------------------------------------------------------------------
-- TRANSACTION_ITEMS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE transaction_items (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    batch_id BIGINT,
    quantity DECIMAL(10, 3) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_price DECIMAL(12, 2) NOT NULL,
    source_location_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE RESTRICT,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE SET NULL,
    FOREIGN KEY (source_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_item_prices CHECK (unit_price >= 0 AND total_price >= 0)
);

-- -----------------------------------------------------------------------------
-- BILLS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    bill_number VARCHAR(50) UNIQUE NOT NULL,
    transaction_id BIGINT NOT NULL,
    bill_date DATE DEFAULT CURRENT_DATE,
    bill_time TIME DEFAULT CURRENT_TIME,
    pdf_path VARCHAR(500),
    is_printed BOOLEAN DEFAULT false,
    printed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- RETURNS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) UNIQUE NOT NULL,
    original_transaction_id BIGINT NOT NULL,
    return_transaction_id BIGINT,
    return_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_reason TEXT NOT NULL,
    approved_by BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (original_transaction_id) REFERENCES transactions(id) ON DELETE RESTRICT,
    FOREIGN KEY (return_transaction_id) REFERENCES transactions(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- RETURN_ITEMS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL,
    restocking_fee DECIMAL(10, 2) DEFAULT 0,
    condition VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (return_id) REFERENCES returns(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE RESTRICT,
    CONSTRAINT chk_return_quantity CHECK (quantity > 0),
    CONSTRAINT chk_refund CHECK (refund_amount >= 0)
);

-- -----------------------------------------------------------------------------
-- CARTS TABLE (For web transactions)
-- -----------------------------------------------------------------------------
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- CART_ITEMS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item_master_file(id) ON DELETE CASCADE,
    UNIQUE(cart_id, item_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

-- -----------------------------------------------------------------------------
-- LOYALTY_TRANSACTIONS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_id BIGINT,
    points_type VARCHAR(20) NOT NULL CHECK (points_type IN ('EARNED', 'REDEEMED', 'EXPIRED', 'ADJUSTED')),
    points_amount DECIMAL(10, 2) NOT NULL,
    balance_after DECIMAL(10, 2) NOT NULL,
    description TEXT,
    expires_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- Create indexes
-- -----------------------------------------------------------------------------
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_cashier ON transactions(cashier_id);

CREATE INDEX idx_transaction_items_transaction ON transaction_items(transaction_id);
CREATE INDEX idx_transaction_items_item ON transaction_items(item_id);

CREATE INDEX idx_bills_transaction ON bills(transaction_id);
CREATE INDEX idx_bills_number ON bills(bill_number);
CREATE INDEX idx_bills_date ON bills(bill_date);

CREATE INDEX idx_returns_original ON returns(original_transaction_id);
CREATE INDEX idx_returns_date ON returns(return_date);

CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_carts_active ON carts(is_active) WHERE is_active = true;

CREATE INDEX idx_loyalty_user ON loyalty_transactions(user_id);
CREATE INDEX idx_loyalty_transaction ON loyalty_transactions(transaction_id);

-- -----------------------------------------------------------------------------
-- Apply update timestamp triggers
-- -----------------------------------------------------------------------------
CREATE TRIGGER update_promotions_updated_at BEFORE UPDATE ON promotions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_returns_updated_at BEFORE UPDATE ON returns
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();