USE neos_burritos;

DELIMITER //

-- =====================================================
-- AUDIT TRIGGERS
-- =====================================================

-- Log order status changes
CREATE TRIGGER tr_order_status_log
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO transaction_log (
            order_id, payment_status, amount, currency_id, transaction_reference, processed_at
        )
        VALUES (
            NEW.order_id, NEW.status, NEW.total_amount, OLD.currency_id,
            CONCAT('STATUS_CHANGE_', OLD.status, '_TO_', NEW.status),
            CURRENT_TIMESTAMP
        );
    END IF;
END //

-- Log user role changes
DROP TRIGGER IF EXISTS tr_log_user_role_change;
CREATE TRIGGER tr_log_user_role_change
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.role != NEW.role THEN
        INSERT INTO transaction_log (
            order_id, payment_status, amount, currency_id, transaction_reference, processed_at
        )
        VALUES (
            NULL, 'COMPLETED', 0.00, NULL,
            CONCAT('ROLE_CHANGE_USER_', NEW.user_id, '_FROM_', OLD.role, '_TO_', NEW.role),
            CURRENT_TIMESTAMP
        );
    END IF;
END //

-- Log currency rate changes
DROP TRIGGER IF EXISTS tr_currency_rate_change;
CREATE TRIGGER tr_currency_rate_change
AFTER UPDATE ON currencies
FOR EACH ROW
BEGIN
    IF OLD.exchange_rate_to_usd != NEW.exchange_rate_to_usd THEN
        INSERT INTO transaction_log (
            order_id, payment_status, amount, currency_id, transaction_reference, processed_at
        )
        VALUES (
            NULL, 'COMPLETED', NEW.exchange_rate_to_usd, NEW.currency_id,
            CONCAT('RATE_CHANGE_', NEW.currency_code, '_FROM_', OLD.exchange_rate_to_usd, '_TO_', NEW.exchange_rate_to_usd),
            CURRENT_TIMESTAMP
        );
    END IF;
END //

-- =====================================================
-- DATA INTEGRITY TRIGGERS
-- =====================================================

-- Validate stock before adding to cart
CREATE TRIGGER tr_validate_cart_stock
BEFORE INSERT ON cart_items
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;

    SELECT stock_quantity INTO v_stock
    FROM products
    WHERE product_id = NEW.product_id AND is_active = TRUE;

    IF v_stock IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product not found or inactive';
    ELSEIF v_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient stock available';
    END IF;
END //

-- Validate stock before placing order
CREATE TRIGGER tr_validate_order_stock
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;

    SELECT stock_quantity INTO v_stock
    FROM products
    WHERE product_id = NEW.product_id AND is_active = TRUE;

    IF v_stock IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product not found or inactive';
    ELSEIF v_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient stock for order';
    END IF;
END //

-- Update stock after order confirmation
CREATE TRIGGER tr_update_stock_on_order
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status = 'PENDING' AND NEW.status = 'CONFIRMED' THEN
        UPDATE products p
        JOIN order_items oi ON p.product_id = oi.product_id
        SET p.stock_quantity = p.stock_quantity - oi.quantity
        WHERE oi.order_id = NEW.order_id;
    END IF;
END //

-- Restore stock on order cancellation
CREATE TRIGGER tr_restore_stock_on_cancel
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status IN ('CONFIRMED', 'PENDING') AND NEW.status = 'CANCELLED' THEN
        UPDATE products p
        JOIN order_items oi ON p.product_id = oi.product_id
        SET p.stock_quantity = p.stock_quantity + oi.quantity
        WHERE oi.order_id = NEW.order_id;
    END IF;
END //

-- =====================================================
-- BUSINESS LOGIC TRIGGERS
-- =====================================================

-- Auto-generate customizations hash for cart items
CREATE TRIGGER tr_generate_customizations_hash
BEFORE INSERT ON cart_items
FOR EACH ROW
BEGIN
    SET NEW.customizations_hash = SHA2(IFNULL(NEW.customizations, ''), 256);
END //

CREATE TRIGGER tr_update_customizations_hash
BEFORE UPDATE ON cart_items
FOR EACH ROW
BEGIN
    SET NEW.customizations_hash = SHA2(IFNULL(NEW.customizations, ''), 256);
END //

-- Validate order total
CREATE TRIGGER tr_validate_order_total
BEFORE INSERT ON orders
FOR EACH ROW
BEGIN
    IF NEW.total_amount <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order total must be greater than zero';
    END IF;
END //

-- Prevent updates on completed or cancelled orders
CREATE TRIGGER tr_protect_completed_orders
BEFORE UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status IN ('COMPLETED', 'CANCELLED') AND NEW.status != OLD.status THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot modify completed or cancelled orders';
    END IF;
END //

-- Prevent deletion of orders with items
CREATE TRIGGER tr_prevent_order_deletion
BEFORE DELETE ON orders
FOR EACH ROW
BEGIN
    DECLARE v_item_count INT;

    SELECT COUNT(*) INTO v_item_count
    FROM order_items
    WHERE order_id = OLD.order_id;

    IF v_item_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete orders with items. Cancel the order instead.';
    END IF;
END //

DELIMITER ;
