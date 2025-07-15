-- =====================================================
-- Neo's Burritos Database Schema (SIMPLIFIED VERSION)
-- Removed unnecessary payment method complexity
-- =====================================================

DROP DATABASE IF EXISTS neos_burritos;
CREATE DATABASE neos_burritos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE neos_burritos;

-- =====================================================
-- TABLE: currencies
-- PURPOSE: Stores currency information for multi-currency support
-- =====================================================
CREATE TABLE currencies (
    currency_id INT PRIMARY KEY AUTO_INCREMENT,
    currency_code VARCHAR(3) NOT NULL UNIQUE COMMENT 'ISO 4217 currency code (USD, PHP, KRW)',
    symbol VARCHAR(5) NOT NULL COMMENT 'Currency symbol ($, ₱, ₩)',
    exchange_rate_to_usd DECIMAL(10,4) NOT NULL DEFAULT 1.0000 COMMENT 'Exchange rate relative to USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: users
-- PURPOSE: Stores user account information for all user types
-- =====================================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'Full name of the user',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Login email, must be unique',
    password VARCHAR(255) NOT NULL COMMENT 'Hashed password',
    role ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER' COMMENT 'User role for access control',
    phone VARCHAR(20) COMMENT 'Contact phone number',
    address TEXT COMMENT 'Delivery address for customers',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Account status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: products
-- PURPOSE: Stores product/service information
-- =====================================================
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'Product name',
    description TEXT COMMENT 'Product description',
    base_price DECIMAL(10,2) NOT NULL COMMENT 'Base price before customizations',
    stock_quantity INT NOT NULL DEFAULT 100 COMMENT 'Available stock',
    currency_id INT NOT NULL COMMENT 'Reference to currency table',
    category ENUM('BURRITO', 'BOWL', 'DRINK', 'SIDE') NOT NULL DEFAULT 'BURRITO' COMMENT 'Product category',
    is_customizable BOOLEAN DEFAULT FALSE COMMENT 'Whether product allows customizations',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Product availability status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (currency_id) REFERENCES currencies(currency_id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: ingredients
-- PURPOSE: Stores available ingredients for customizable products
-- =====================================================
CREATE TABLE ingredients (
    ingredient_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT 'Ingredient name',
    category ENUM('PROTEIN', 'RICE', 'BEANS', 'VEGETABLES', 'SAUCE', 'EXTRAS') NOT NULL COMMENT 'Ingredient category',
    additional_price DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Extra cost for this ingredient',
    is_available BOOLEAN DEFAULT TRUE COMMENT 'Ingredient availability',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: product_ingredients
-- PURPOSE: Many-to-many relationship between products and their default ingredients
-- =====================================================
CREATE TABLE product_ingredients (
    product_id INT,
    ingredient_id INT,
    is_default BOOLEAN DEFAULT TRUE COMMENT 'Whether this ingredient comes by default',
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: orders
-- PURPOSE: Stores order header information
-- =====================================================
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'Customer who placed the order',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When order was placed',
    total_amount DECIMAL(10,2) NOT NULL COMMENT 'Total order amount',
    currency_id INT NOT NULL COMMENT 'Currency used for this order',
    status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' COMMENT 'Order status',
    delivery_address TEXT COMMENT 'Delivery address for this order',
    notes TEXT COMMENT 'Special instructions',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (currency_id) REFERENCES currencies(currency_id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: order_items
-- PURPOSE: Stores individual items within each order
-- =====================================================
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL COMMENT 'Reference to parent order',
    product_id INT NOT NULL COMMENT 'Product being ordered',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity of this product',
    unit_price DECIMAL(10,2) NOT NULL COMMENT 'Price per unit at time of order',
    customizations TEXT COMMENT 'Custom ingredients as JSON string',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: transaction_log (SIMPLIFIED)
-- PURPOSE: Logs payment transactions for auditing
-- SIMPLIFIED: Removed payment_method complexity
-- =====================================================
DROP TABLE IF EXISTS transaction_log;
CREATE TABLE transaction_log (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL COMMENT 'Reference to the order being paid',
    payment_status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' COMMENT 'Payment status',
    amount DECIMAL(10,2) NOT NULL COMMENT 'Transaction amount',
    transaction_reference VARCHAR(100) COMMENT 'Contains Transaction ID or Status Change (If changed by admin)',
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When transaction was processed',
    currency_id INT COMMENT 'Reference to the currency selected by the user',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE RESTRICT,
	FOREIGN KEY (currency_id) REFERENCES orders(currency_id)
);

-- =====================================================
-- TABLE: cart_items
-- PURPOSE: Stores temporary cart items for logged-in users
-- =====================================================
CREATE TABLE cart_items (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'User who owns this cart item',
    product_id INT NOT NULL COMMENT 'Product in cart',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity in cart',
    customizations TEXT COMMENT 'Custom ingredients as JSON string',
    customizations_hash VARCHAR(64) COMMENT 'SHA256 hash of customizations for uniqueness',
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When item was added to cart',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product_custom (user_id, product_id, customizations_hash)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_transaction_status ON transaction_log(payment_status);
CREATE INDEX idx_cart_user ON cart_items(user_id);
CREATE INDEX idx_cart_hash ON cart_items(customizations_hash);

-- =====================================================
-- SIMPLIFICATION BENEFITS:
-- 
-- 1. REDUCED COMPLEXITY: Removed payment method enum and validation
-- 2. CLEANER UI: No payment method selection dropdown
-- 3. SIMPLER BUSINESS LOGIC: Focus on core ordering functionality
-- 4. EASIER MAINTENANCE: Less code to maintain and test
-- 5. YAGNI COMPLIANCE: Removed features not needed for core functionality
-- 
-- The system still supports:
-- - Order placement and tracking
-- - Payment processing (simplified)
-- - Transaction logging for auditing
-- - Multi-currency support
-- - User management and roles
-- =====================================================