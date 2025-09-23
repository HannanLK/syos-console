-- =============================================================================
-- SYOS Database Schema - V7__Create_FIFO_Functions_And_Procedures.sql
-- FIFO with Expiry Priority Functions and Stock Management Procedures
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Function: Get next available batch using FIFO with expiry priority
-- Returns batches ordered by: 1) Expiry date (nearest first), 2) Received date (oldest first)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_fifo_batches(
    p_item_id BIGINT,
    p_location_id BIGINT,
    p_location_type VARCHAR(50),
    p_quantity DECIMAL
)
RETURNS TABLE (
    batch_id BIGINT,
    available_quantity DECIMAL,
    expiry_date DATE,
    quantity_to_use DECIMAL
) AS $$
DECLARE
    remaining_quantity DECIMAL := p_quantity;
    current_quantity DECIMAL;
BEGIN
    -- Different logic based on location type
    IF p_location_type = 'WAREHOUSE' THEN
        FOR batch_id, available_quantity, expiry_date IN
            SELECT ws.batch_id, ws.quantity - ws.reserved_quantity, b.expiry_date
            FROM warehouse_stock ws
            JOIN batches b ON ws.batch_id = b.id
            WHERE ws.item_id = p_item_id 
              AND ws.location_id = p_location_id
              AND ws.quantity > ws.reserved_quantity
              AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE)
            ORDER BY 
                CASE WHEN b.expiry_date IS NOT NULL 
                     THEN b.expiry_date 
                     ELSE DATE '9999-12-31' 
                END ASC,
                ws.received_date ASC,
                ws.id ASC
        LOOP
            IF remaining_quantity <= 0 THEN
                EXIT;
            END IF;
            
            current_quantity := LEAST(available_quantity, remaining_quantity);
            quantity_to_use := current_quantity;
            remaining_quantity := remaining_quantity - current_quantity;
            
            RETURN NEXT;
        END LOOP;
    ELSIF p_location_type = 'SHELF' THEN
        FOR batch_id, available_quantity, expiry_date IN
            SELECT ss.batch_id, ss.quantity, b.expiry_date
            FROM shelf_stock ss
            LEFT JOIN batches b ON ss.batch_id = b.id
            WHERE ss.item_id = p_item_id 
              AND ss.location_id = p_location_id
              AND ss.quantity > 0
              AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE)
            ORDER BY 
                CASE WHEN b.expiry_date IS NOT NULL 
                     THEN b.expiry_date 
                     ELSE DATE '9999-12-31' 
                END ASC,
                ss.last_restocked ASC,
                ss.id ASC
        LOOP
            IF remaining_quantity <= 0 THEN
                EXIT;
            END IF;
            
            current_quantity := LEAST(available_quantity, remaining_quantity);
            quantity_to_use := current_quantity;
            remaining_quantity := remaining_quantity - current_quantity;
            
            RETURN NEXT;
        END LOOP;
    ELSIF p_location_type = 'WEB_INVENTORY' THEN
        FOR batch_id, available_quantity, expiry_date IN
            SELECT wi.batch_id, wi.quantity - wi.reserved_quantity, b.expiry_date
            FROM web_inventory wi
            LEFT JOIN batches b ON wi.batch_id = b.id
            WHERE wi.item_id = p_item_id 
              AND wi.location_id = p_location_id
              AND wi.quantity > wi.reserved_quantity
              AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE)
            ORDER BY 
                CASE WHEN b.expiry_date IS NOT NULL 
                     THEN b.expiry_date 
                     ELSE DATE '9999-12-31' 
                END ASC,
                wi.last_updated ASC,
                wi.id ASC
        LOOP
            IF remaining_quantity <= 0 THEN
                EXIT;
            END IF;
            
            current_quantity := LEAST(available_quantity, remaining_quantity);
            quantity_to_use := current_quantity;
            remaining_quantity := remaining_quantity - current_quantity;
            
            RETURN NEXT;
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------------------
-- Function: Check if sufficient stock available
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION check_stock_availability(
    p_item_id BIGINT,
    p_location_id BIGINT,
    p_location_type VARCHAR(50),
    p_required_quantity DECIMAL
)
RETURNS BOOLEAN AS $$
DECLARE
    available_quantity DECIMAL;
BEGIN
    IF p_location_type = 'WAREHOUSE' THEN
        SELECT COALESCE(SUM(ws.quantity - ws.reserved_quantity), 0)
        INTO available_quantity
        FROM warehouse_stock ws
        JOIN batches b ON ws.batch_id = b.id
        WHERE ws.item_id = p_item_id 
          AND ws.location_id = p_location_id
          AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE);
    ELSIF p_location_type = 'SHELF' THEN
        SELECT COALESCE(SUM(ss.quantity), 0)
        INTO available_quantity
        FROM shelf_stock ss
        LEFT JOIN batches b ON ss.batch_id = b.id
        WHERE ss.item_id = p_item_id 
          AND ss.location_id = p_location_id
          AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE);
    ELSIF p_location_type = 'WEB_INVENTORY' THEN
        SELECT COALESCE(SUM(wi.quantity - wi.reserved_quantity), 0)
        INTO available_quantity
        FROM web_inventory wi
        LEFT JOIN batches b ON wi.batch_id = b.id
        WHERE wi.item_id = p_item_id 
          AND wi.location_id = p_location_id
          AND (b.expiry_date IS NULL OR b.expiry_date > CURRENT_DATE);
    END IF;
    
    RETURN available_quantity >= p_required_quantity;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------------------
-- Procedure: Add product to warehouse (initial receipt)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE add_product_to_warehouse(
    p_item_id BIGINT,
    p_batch_number VARCHAR(50),
    p_quantity DECIMAL,
    p_warehouse_location_id BIGINT,
    p_manufacture_date DATE,
    p_expiry_date DATE,
    p_cost_price DECIMAL,
    p_supplier_batch_number VARCHAR(50),
    p_user_id BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_batch_id BIGINT;
    v_warehouse_code VARCHAR(100);
BEGIN
    -- Create or update batch
    INSERT INTO batches (
        batch_number, item_id, supplier_batch_number, 
        quantity_received, quantity_available, cost_price,
        manufacture_date, expiry_date, created_by
    ) VALUES (
        p_batch_number, p_item_id, p_supplier_batch_number,
        p_quantity, p_quantity, p_cost_price,
        p_manufacture_date, p_expiry_date, p_user_id
    )
    ON CONFLICT (batch_number, item_id) 
    DO UPDATE SET
        quantity_received = batches.quantity_received + p_quantity,
        quantity_available = batches.quantity_available + p_quantity
    RETURNING id INTO v_batch_id;
    
    -- Generate warehouse code
    v_warehouse_code := 'WH-' || p_item_id || '-' || v_batch_id || '-' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS');
    
    -- Add to warehouse stock
    INSERT INTO warehouse_stock (
        warehouse_code, item_id, batch_id, location_id, 
        quantity, created_by
    ) VALUES (
        v_warehouse_code, p_item_id, v_batch_id, p_warehouse_location_id,
        p_quantity, p_user_id
    );
    
    -- Record stock movement
    INSERT INTO stock_movements (
        item_id, batch_id, to_location_id, movement_type,
        quantity, reference_type, reason, performed_by
    ) VALUES (
        p_item_id, v_batch_id, p_warehouse_location_id, 'RECEIPT',
        p_quantity, 'WAREHOUSE_RECEIPT', 'Initial product receipt', p_user_id
    );
    
    -- Check for reorder notification
    CALL check_reorder_level(p_item_id);
END;
$$;

-- -----------------------------------------------------------------------------
-- Procedure: Transfer stock from warehouse to shelf/web
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE transfer_stock(
    p_item_id BIGINT,
    p_from_location_id BIGINT,
    p_to_location_id BIGINT,
    p_quantity DECIMAL,
    p_user_id BIGINT,
    p_reason TEXT DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_from_location_type VARCHAR(50);
    v_to_location_type VARCHAR(50);
    v_transfer_code VARCHAR(100);
    v_transfer_type transfer_type;
    v_batch_record RECORD;
BEGIN
    -- Get location types
    SELECT location_type INTO v_from_location_type 
    FROM locations WHERE id = p_from_location_id;
    
    SELECT location_type INTO v_to_location_type 
    FROM locations WHERE id = p_to_location_id;
    
    -- Determine transfer type
    IF v_from_location_type = 'WAREHOUSE' AND v_to_location_type = 'SHELF' THEN
        v_transfer_type := 'WAREHOUSE_TO_SHELF';
    ELSIF v_from_location_type = 'WAREHOUSE' AND v_to_location_type = 'WEB_INVENTORY' THEN
        v_transfer_type := 'WAREHOUSE_TO_WEB';
    ELSE
        RAISE EXCEPTION 'Invalid transfer type: % to %', v_from_location_type, v_to_location_type;
    END IF;
    
    -- Check availability
    IF NOT check_stock_availability(p_item_id, p_from_location_id, v_from_location_type, p_quantity) THEN
        RAISE EXCEPTION 'Insufficient stock for transfer';
    END IF;
    
    -- Process transfer using FIFO
    FOR v_batch_record IN 
        SELECT * FROM get_fifo_batches(p_item_id, p_from_location_id, v_from_location_type, p_quantity)
    LOOP
        -- Generate transfer code
        v_transfer_code := 'TR-' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS') || '-' || v_batch_record.batch_id;
        
        -- Create transfer record
        INSERT INTO stock_transfers (
            transfer_code, item_id, batch_id, from_location_id, to_location_id,
            quantity, transfer_type, transfer_status, transfer_reason,
            initiated_by, completed_by, completed_at
        ) VALUES (
            v_transfer_code, p_item_id, v_batch_record.batch_id, p_from_location_id, p_to_location_id,
            v_batch_record.quantity_to_use, v_transfer_type, 'COMPLETED', p_reason,
            p_user_id, p_user_id, NOW()
        );
        
        -- Update source inventory
        UPDATE warehouse_stock 
        SET quantity = quantity - v_batch_record.quantity_to_use
        WHERE item_id = p_item_id 
          AND batch_id = v_batch_record.batch_id 
          AND location_id = p_from_location_id;
        
        -- Update destination inventory
        IF v_to_location_type = 'SHELF' THEN
            INSERT INTO shelf_stock (item_id, batch_id, location_id, quantity, last_restocked)
            VALUES (p_item_id, v_batch_record.batch_id, p_to_location_id, v_batch_record.quantity_to_use, NOW())
            ON CONFLICT (item_id, batch_id, location_id) 
            DO UPDATE SET 
                quantity = shelf_stock.quantity + v_batch_record.quantity_to_use,
                last_restocked = NOW();
        ELSIF v_to_location_type = 'WEB_INVENTORY' THEN
            INSERT INTO web_inventory (item_id, batch_id, location_id, quantity)
            VALUES (p_item_id, v_batch_record.batch_id, p_to_location_id, v_batch_record.quantity_to_use)
            ON CONFLICT (item_id, batch_id, location_id) 
            DO UPDATE SET 
                quantity = web_inventory.quantity + v_batch_record.quantity_to_use;
        END IF;
        
        -- Record movement
        INSERT INTO stock_movements (
            item_id, batch_id, from_location_id, to_location_id,
            movement_type, quantity, reference_type, reason, performed_by
        ) VALUES (
            p_item_id, v_batch_record.batch_id, p_from_location_id, p_to_location_id,
            'TRANSFER', v_batch_record.quantity_to_use, 'STOCK_TRANSFER', p_reason, p_user_id
        );
    END LOOP;
END;
$$;

-- -----------------------------------------------------------------------------
-- Procedure: Check and create reorder notifications
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE check_reorder_level(p_item_id BIGINT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_stock DECIMAL;
    v_reorder_point INT;
    v_item_name VARCHAR(200);
BEGIN
    -- Get item details
    SELECT item_name, reorder_point 
    INTO v_item_name, v_reorder_point
    FROM item_master_file 
    WHERE id = p_item_id;
    
    -- Calculate total stock across all locations
    SELECT COALESCE(SUM(total_quantity), 0) INTO v_total_stock
    FROM (
        SELECT SUM(quantity - reserved_quantity) as total_quantity 
        FROM warehouse_stock WHERE item_id = p_item_id
        UNION ALL
        SELECT SUM(quantity) FROM shelf_stock WHERE item_id = p_item_id
        UNION ALL
        SELECT SUM(quantity - reserved_quantity) FROM web_inventory WHERE item_id = p_item_id
    ) as stock_totals;
    
    -- Check if below reorder point
    IF v_total_stock <= v_reorder_point THEN
        -- Create notification for admins and employees (avoiding duplicates)
        INSERT INTO notifications (user_id, notification_type, title, message, related_entity_type, related_entity_id)
        SELECT id, 'REORDER', 'Reorder Alert: ' || v_item_name,
               'Stock for ' || v_item_name || ' (ID: ' || p_item_id || ') is below reorder point. Current: ' || 
               v_total_stock || ', Reorder Point: ' || v_reorder_point,
               'ITEM', p_item_id
        FROM users 
        WHERE role IN ('ADMIN', 'EMPLOYEE') 
          AND is_active = true
          AND NOT EXISTS (
              SELECT 1 FROM notifications 
              WHERE related_entity_id = p_item_id 
                AND related_entity_type = 'ITEM'
                AND notification_type = 'REORDER'
                AND is_read = false
                AND created_at > NOW() - INTERVAL '24 hours'
          );
    END IF;
END;
$$;

-- -----------------------------------------------------------------------------
-- Function: Get inventory summary for reporting
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_inventory_summary(p_item_id BIGINT DEFAULT NULL)
RETURNS TABLE (
    item_id BIGINT,
    item_code VARCHAR(20),
    item_name VARCHAR(200),
    warehouse_stock DECIMAL,
    shelf_stock DECIMAL,
    web_stock DECIMAL,
    total_stock DECIMAL,
    reorder_point INT,
    status VARCHAR(20)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        imf.id,
        imf.item_code,
        imf.item_name,
        COALESCE(ws.quantity, 0) as warehouse_stock,
        COALESCE(ss.quantity, 0) as shelf_stock,
        COALESCE(wi.quantity, 0) as web_stock,
        COALESCE(ws.quantity, 0) + COALESCE(ss.quantity, 0) + COALESCE(wi.quantity, 0) as total_stock,
        imf.reorder_point,
        CASE 
            WHEN COALESCE(ws.quantity, 0) + COALESCE(ss.quantity, 0) + COALESCE(wi.quantity, 0) = 0 THEN 'OUT_OF_STOCK'
            WHEN COALESCE(ws.quantity, 0) + COALESCE(ss.quantity, 0) + COALESCE(wi.quantity, 0) <= imf.reorder_point THEN 'LOW_STOCK'
            ELSE 'IN_STOCK'
        END as status
    FROM item_master_file imf
    LEFT JOIN (
        SELECT item_id, SUM(quantity - reserved_quantity) as quantity
        FROM warehouse_stock
        GROUP BY item_id
    ) ws ON imf.id = ws.item_id
    LEFT JOIN (
        SELECT item_id, SUM(quantity) as quantity
        FROM shelf_stock
        GROUP BY item_id
    ) ss ON imf.id = ss.item_id
    LEFT JOIN (
        SELECT item_id, SUM(quantity - reserved_quantity) as quantity
        FROM web_inventory
        GROUP BY item_id
    ) wi ON imf.id = wi.item_id
    WHERE imf.status = 'ACTIVE'
      AND (p_item_id IS NULL OR imf.id = p_item_id);
END;
$$ LANGUAGE plpgsql;