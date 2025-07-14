-- =====================================================
-- Neo's Burritos - Stored Procedures
-- Following SOLID principles and single responsibility
-- =====================================================

USE neos_burritos;

DELIMITER //

-- =====================================================
-- USER AUTHENTICATION & MANAGEMENT PROCEDURES
-- =====================================================

-- Register new user with role validation
CREATE PROCEDURE sp_register_user(
    IN p_name VARCHAR(100),
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    IN p_role ENUM('ADMIN', 'STAFF', 'CUSTOMER'),
    IN p_phone VARCHAR(20),
    IN p_address TEXT,
    OUT p_user_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Registration failed due to database error';
        SET p_user_id = NULL;
    END;

    START TRANSACTION;
    
    -- Check if email already exists
    IF EXISTS (SELECT 1 FROM users WHERE email = p_email) THEN
        SET p_success = FALSE;
        SET p_message = 'Email already registered';
        SET p_user_id = NULL;
        ROLLBACK;
    ELSE
        INSERT INTO users (name, email, password, role, phone, address)
        VALUES (p_name, p_email, p_password, p_role, p_phone, p_address);
        
        SET p_user_id = LAST_INSERT_ID();
        SET p_success = TRUE;
        SET p_message = 'User registered successfully';
        COMMIT;
    END IF;
END //

-- Authenticate user login
CREATE PROCEDURE sp_authenticate_user(
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    OUT p_user_id INT,
    OUT p_role ENUM('ADMIN', 'STAFF', 'CUSTOMER'),
    OUT p_name VARCHAR(100),
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_stored_password VARCHAR(255);
    DECLARE v_is_active BOOLEAN;
    
    -- Get user details
    SELECT user_id, password, role, name, is_active
    INTO p_user_id, v_stored_password, p_role, p_name, v_is_active
    FROM users 
    WHERE email = p_email;
    
    IF p_user_id IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'Invalid email or password';
    ELSEIF NOT v_is_active THEN
        SET p_success = FALSE;
        SET p_message = 'Account is deactivated';
    ELSEIF v_stored_password = p_password THEN
        SET p_success = TRUE;
        SET p_message = 'Login successful';
    ELSE
        SET p_success = FALSE;
        SET p_message = 'Invalid email or password';
    END IF;
END //

-- Update user profile
CREATE PROCEDURE sp_update_user_profile(
    IN p_user_id INT,
    IN p_name VARCHAR(100),
    IN p_phone VARCHAR(20),
    IN p_address TEXT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_success = FALSE;
        SET p_message = 'Profile update failed';
    END;

    UPDATE users 
    SET name = p_name, phone = p_phone, address = p_address, updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;
    
    IF ROW_COUNT() > 0 THEN
        SET p_success = TRUE;
        SET p_message = 'Profile updated successfully';
    ELSE
        SET p_success = FALSE;
        SET p_message = 'User not found';
    END IF;
END //

-- =====================================================
-- CURRENCY MANAGEMENT PROCEDURES
-- =====================================================

-- Get current exchange rate for currency conversion
CREATE PROCEDURE sp_get_exchange_rate(
    IN p_from_currency VARCHAR(3),
    IN p_to_currency VARCHAR(3),
    OUT p_rate DECIMAL(10,4),
    OUT p_success BOOLEAN
)
BEGIN
    DECLARE v_from_rate DECIMAL(10,4);
    DECLARE v_to_rate DECIMAL(10,4);
    
    -- Get exchange rates (all rates are relative to USD)
    SELECT exchange_rate_to_usd INTO v_from_rate 
    FROM currencies WHERE currency_code = p_from_currency;
    
    SELECT exchange_rate_to_usd INTO v_to_rate 
    FROM currencies WHERE currency_code = p_to_currency;
    
    IF v_from_rate IS NULL OR v_to_rate IS NULL THEN
        SET p_success = FALSE;
        SET p_rate = NULL;
    ELSE
        -- Convert: amount_in_from * (1/from_rate) * to_rate
        SET p_rate = v_to_rate / v_from_rate;
        SET p_success = TRUE;
    END IF;
END //

-- Convert price between currencies
CREATE PROCEDURE sp_convert_price(
    IN p_amount DECIMAL(10,2),
    IN p_from_currency VARCHAR(3),
    IN p_to_currency VARCHAR(3),
    OUT p_converted_amount DECIMAL(10,2),
    OUT p_success BOOLEAN
)
BEGIN
    DECLARE v_rate DECIMAL(10,4);
    DECLARE v_rate_success BOOLEAN;
    
    CALL sp_get_exchange_rate(p_from_currency, p_to_currency, v_rate, v_rate_success);
    
    IF v_rate_success THEN
        SET p_converted_amount = ROUND(p_amount * v_rate, 2);
        SET p_success = TRUE;
    ELSE
        SET p_converted_amount = NULL;
        SET p_success = FALSE;
    END IF;
END //

-- =====================================================
-- PRODUCT MANAGEMENT PROCEDURES
-- =====================================================

-- Get products with currency conversion
CREATE PROCEDURE sp_get_products_by_currency(
    IN p_currency_code VARCHAR(3),
    IN p_category ENUM('BURRITO', 'BOWL', 'DRINK', 'SIDE')
)
BEGIN
    SELECT 
        p.product_id,
        p.name,
        p.description,
        CASE 
            WHEN c.currency_code = p_currency_code THEN p.base_price
            ELSE ROUND(p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd), 2)
        END as converted_price,
        p_currency_code as display_currency,
        target_c.symbol as currency_symbol,
        p.stock_quantity,
        p.category,
        p.is_customizable,
        p.is_active
    FROM products p
    JOIN currencies c ON p.currency_id = c.currency_id
    JOIN currencies target_c ON target_c.currency_code = p_currency_code
    WHERE p.is_active = TRUE
    AND (p_category IS NULL OR p.category = p_category)
    ORDER BY p.category, p.name;
END //

-- Add new product (Admin only)
CREATE PROCEDURE sp_add_product(
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_base_price DECIMAL(10,2),
    IN p_currency_code VARCHAR(3),
    IN p_category ENUM('BURRITO', 'BOWL', 'DRINK', 'SIDE'),
    IN p_is_customizable BOOLEAN,
    IN p_stock_quantity INT,
    OUT p_product_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_currency_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Failed to add product';
        SET p_product_id = NULL;
    END;

    START TRANSACTION;
    
    -- Get currency ID
    SELECT currency_id INTO v_currency_id 
    FROM currencies WHERE currency_code = p_currency_code;
    
    IF v_currency_id IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'Invalid currency code';
        SET p_product_id = NULL;
        ROLLBACK;
    ELSE
        INSERT INTO products (name, description, base_price, currency_id, category, is_customizable, stock_quantity)
        VALUES (p_name, p_description, p_base_price, v_currency_id, p_category, p_is_customizable, p_stock_quantity);
        
        SET p_product_id = LAST_INSERT_ID();
        SET p_success = TRUE;
        SET p_message = 'Product added successfully';
        COMMIT;
    END IF;
END //

-- Update product (Admin only)
CREATE PROCEDURE sp_update_product(
    IN p_product_id INT,
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_base_price DECIMAL(10,2),
    IN p_stock_quantity INT,
    IN p_is_active BOOLEAN,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_success = FALSE;
        SET p_message = 'Failed to update product';
    END;

    UPDATE products 
    SET name = p_name, 
        description = p_description, 
        base_price = p_base_price, 
        stock_quantity = p_stock_quantity,
        is_active = p_is_active,
        updated_at = CURRENT_TIMESTAMP
    WHERE product_id = p_product_id;
    
    IF ROW_COUNT() > 0 THEN
        SET p_success = TRUE;
        SET p_message = 'Product updated successfully';
    ELSE
        SET p_success = FALSE;
        SET p_message = 'Product not found';
    END IF;
END //

-- =====================================================
-- CART MANAGEMENT PROCEDURES
-- =====================================================

-- Add item to cart with customization support
CREATE PROCEDURE sp_add_to_cart(
    IN p_user_id INT,
    IN p_product_id INT,
    IN p_quantity INT,
    IN p_customizations TEXT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_customizations_hash VARCHAR(64);
    DECLARE v_existing_quantity INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Failed to add item to cart';
    END;

    START TRANSACTION;
    
    -- Generate hash for customizations
    SET v_customizations_hash = SHA2(IFNULL(p_customizations, ''), 256);
    
    -- Check if item with same customizations already exists
    SELECT quantity INTO v_existing_quantity
    FROM cart_items 
    WHERE user_id = p_user_id 
    AND product_id = p_product_id 
    AND customizations_hash = v_customizations_hash;
    
    IF v_existing_quantity > 0 THEN
        -- Update existing cart item
        UPDATE cart_items 
        SET quantity = quantity + p_quantity
        WHERE user_id = p_user_id 
        AND product_id = p_product_id 
        AND customizations_hash = v_customizations_hash;
    ELSE
        -- Insert new cart item
        INSERT INTO cart_items (user_id, product_id, quantity, customizations, customizations_hash)
        VALUES (p_user_id, p_product_id, p_quantity, p_customizations, v_customizations_hash);
    END IF;
    
    SET p_success = TRUE;
    SET p_message = 'Item added to cart successfully';
    COMMIT;
END //

-- Get cart items for user
CREATE PROCEDURE sp_get_cart_items(
    IN p_user_id INT,
    IN p_currency_code VARCHAR(3)
)
BEGIN
    SELECT 
        ci.cart_id,
        ci.product_id,
        p.name as product_name,
        ci.quantity,
        ci.customizations,
        CASE 
            WHEN c.currency_code = p_currency_code THEN p.base_price
            ELSE ROUND(p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd), 2)
        END as unit_price,
        CASE 
            WHEN c.currency_code = p_currency_code THEN (p.base_price * ci.quantity)
            ELSE ROUND((p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd)) * ci.quantity, 2)
        END as total_price,
        target_c.symbol as currency_symbol,
        ci.added_at
    FROM cart_items ci
    JOIN products p ON ci.product_id = p.product_id
    JOIN currencies c ON p.currency_id = c.currency_id
    JOIN currencies target_c ON target_c.currency_code = p_currency_code
    WHERE ci.user_id = p_user_id
    ORDER BY ci.added_at DESC;
END //

-- Clear user cart
CREATE PROCEDURE sp_clear_cart(
    IN p_user_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DELETE FROM cart_items WHERE user_id = p_user_id;
    
    SET p_success = TRUE;
    SET p_message = 'Cart cleared successfully';
END //

-- =====================================================
-- ORDER MANAGEMENT PROCEDURES
-- =====================================================

-- Place order from cart
CREATE PROCEDURE sp_place_order(
    IN p_user_id INT,
    IN p_currency_code VARCHAR(3),
    IN p_delivery_address TEXT,
    IN p_notes TEXT,
    OUT p_order_id INT,
    OUT p_total_amount DECIMAL(10,2),
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_currency_id INT;
    DECLARE v_cart_total DECIMAL(10,2) DEFAULT 0;
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_cart_id INT;
    DECLARE v_product_id INT;
    DECLARE v_quantity INT;
    DECLARE v_customizations TEXT;
    DECLARE v_unit_price DECIMAL(10,2);
    
    DECLARE cart_cursor CURSOR FOR
        SELECT 
            ci.cart_id,
            ci.product_id,
            ci.quantity,
            ci.customizations,
            CASE 
                WHEN c.currency_code = p_currency_code THEN p.base_price
                ELSE ROUND(p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd), 2)
            END as unit_price
        FROM cart_items ci
        JOIN products p ON ci.product_id = p.product_id
        JOIN currencies c ON p.currency_id = c.currency_id
        JOIN currencies target_c ON target_c.currency_code = p_currency_code
        WHERE ci.user_id = p_user_id;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Failed to place order';
        SET p_order_id = NULL;
        SET p_total_amount = NULL;
    END;

    START TRANSACTION;
    
    -- Get currency ID
    SELECT currency_id INTO v_currency_id 
    FROM currencies WHERE currency_code = p_currency_code;
    
    IF v_currency_id IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'Invalid currency';
        ROLLBACK;
    ELSE
        -- Calculate total from cart
        SELECT SUM(
            CASE 
                WHEN c.currency_code = p_currency_code THEN (p.base_price * ci.quantity)
                ELSE ROUND((p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd)) * ci.quantity, 2)
            END
        ) INTO v_cart_total
        FROM cart_items ci
        JOIN products p ON ci.product_id = p.product_id
        JOIN currencies c ON p.currency_id = c.currency_id
        JOIN currencies target_c ON target_c.currency_code = p_currency_code
        WHERE ci.user_id = p_user_id;
        
        IF v_cart_total IS NULL OR v_cart_total = 0 THEN
            SET p_success = FALSE;
            SET p_message = 'Cart is empty';
            ROLLBACK;
        ELSE
            -- Create order
            INSERT INTO orders (user_id, total_amount, currency_id, delivery_address, notes)
            VALUES (p_user_id, v_cart_total, v_currency_id, p_delivery_address, p_notes);
            
            SET p_order_id = LAST_INSERT_ID();
            SET p_total_amount = v_cart_total;
            
            -- Move cart items to order items
            OPEN cart_cursor;
            read_loop: LOOP
                FETCH cart_cursor INTO v_cart_id, v_product_id, v_quantity, v_customizations, v_unit_price;
                IF done THEN
                    LEAVE read_loop;
                END IF;
                
                INSERT INTO order_items (order_id, product_id, quantity, unit_price, customizations)
                VALUES (p_order_id, v_product_id, v_quantity, v_unit_price, v_customizations);
            END LOOP;
            CLOSE cart_cursor;
            
            -- Clear cart
            DELETE FROM cart_items WHERE user_id = p_user_id;
            
            SET p_success = TRUE;
            SET p_message = 'Order placed successfully';
            COMMIT;
        END IF;
    END IF;
END //

-- Get order history for user
CREATE PROCEDURE sp_get_order_history(
    IN p_user_id INT,
    IN p_limit INT
)
BEGIN
    SELECT 
        o.order_id,
        o.order_date,
        o.total_amount,
        c.currency_code,
        c.symbol as currency_symbol,
        o.status,
        o.delivery_address,
        o.notes,
        COUNT(oi.order_item_id) as item_count
    FROM orders o
    JOIN currencies c ON o.currency_id = c.currency_id
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.user_id = p_user_id
    GROUP BY o.order_id
    ORDER BY o.order_date DESC
    LIMIT p_limit;
END //

-- Get order details with items
CREATE PROCEDURE sp_get_order_details(
    IN p_order_id INT,
    IN p_user_id INT
)
BEGIN
    -- Order header
    SELECT 
        o.order_id,
        o.order_date,
        o.total_amount,
        c.currency_code,
        c.symbol as currency_symbol,
        o.status,
        o.delivery_address,
        o.notes
    FROM orders o
    JOIN currencies c ON o.currency_id = c.currency_id
    WHERE o.order_id = p_order_id AND o.user_id = p_user_id;
    
    -- Order items
    SELECT 
        oi.order_item_id,
        oi.product_id,
        p.name as product_name,
        oi.quantity,
        oi.unit_price,
        (oi.quantity * oi.unit_price) as total_price,
        oi.customizations
    FROM order_items oi
    JOIN products p ON oi.product_id = p.product_id
    JOIN orders o ON oi.order_id = o.order_id
    WHERE oi.order_id = p_order_id AND o.user_id = p_user_id;
END //

-- Update order status (Staff/Admin only)
CREATE PROCEDURE sp_update_order_status(
    IN p_order_id INT,
    IN p_status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED'),
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_success = FALSE;
        SET p_message = 'Failed to update order status';
    END;

    UPDATE orders 
    SET status = p_status, updated_at = CURRENT_TIMESTAMP
    WHERE order_id = p_order_id;
    
    IF ROW_COUNT() > 0 THEN
        SET p_success = TRUE;
        SET p_message = 'Order status updated successfully';
    ELSE
        SET p_success = FALSE;
        SET p_message = 'Order not found';
    END IF;
END //

DELIMITER ;