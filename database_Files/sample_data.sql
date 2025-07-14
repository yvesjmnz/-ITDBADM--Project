-- =====================================================
-- Neo's Burritos - Sample Data
-- Initial setup with currencies, users, products, and ingredients
-- =====================================================

USE neos_burritos;

-- =====================================================
-- CURRENCIES (3 required currencies)
-- =====================================================
INSERT INTO currencies (currency_code, symbol, exchange_rate_to_usd) VALUES
('USD', '$', 1.0000),
('PHP', '₱', 56.5000),  -- 1 USD = 56.50 PHP (approximate)
('KRW', '₩', 1320.0000); -- 1 USD = 1320 KRW (approximate)

-- =====================================================
-- SAMPLE USERS (All roles represented)
-- =====================================================
-- Admin user (password: admin123)
INSERT INTO users (name, email, password, role, phone, address) VALUES
('System Administrator', 'admin@neosburritos.com', 'admin123', 'ADMIN', '+1-555-0001', '123 Admin Street, Business District'),

-- Staff users (password: staff123)
('Maria Santos', 'maria@neosburritos.com', 'staff123', 'STAFF', '+63-917-1234567', '456 Staff Avenue, Manila'),
('John Kim', 'john@neosburritos.com', 'staff123', 'STAFF', '+82-10-1234-5678', '789 Staff Road, Seoul'),

-- Customer users (password: customer123)
('Alice Johnson', 'alice@email.com', 'customer123', 'CUSTOMER', '+1-555-0101', '321 Customer Lane, Suburbia'),
('Pedro Dela Cruz', 'pedro@email.com', 'customer123', 'CUSTOMER', '+63-917-9876543', '654 Customer Street, Quezon City'),
('Kim Min-jun', 'kimmin@email.com', 'customer123', 'CUSTOMER', '+82-10-9876-5432', '987 Customer Boulevard, Gangnam');

-- =====================================================
-- INGREDIENTS (For customizable products)
-- =====================================================
INSERT INTO ingredients (name, category, additional_price) VALUES
-- Proteins
('Grilled Chicken', 'PROTEIN', 0.00),
('Beef Barbacoa', 'PROTEIN', 1.50),
('Carnitas Pork', 'PROTEIN', 1.00),
('Black Bean', 'PROTEIN', 0.00),
('Tofu', 'PROTEIN', 0.50),

-- Rice
('Cilantro Lime Rice', 'RICE', 0.00),
('Brown Rice', 'RICE', 0.50),
('Spanish Rice', 'RICE', 0.00),

-- Beans
('Black Beans', 'BEANS', 0.00),
('Pinto Beans', 'BEANS', 0.00),
('Refried Beans', 'BEANS', 0.25),

-- Vegetables
('Lettuce', 'VEGETABLES', 0.00),
('Tomatoes', 'VEGETABLES', 0.00),
('Onions', 'VEGETABLES', 0.00),
('Bell Peppers', 'VEGETABLES', 0.25),
('Corn', 'VEGETABLES', 0.25),
('Jalapeños', 'VEGETABLES', 0.00),

-- Sauces
('Mild Salsa', 'SAUCE', 0.00),
('Medium Salsa', 'SAUCE', 0.00),
('Hot Salsa', 'SAUCE', 0.00),
('Guacamole', 'SAUCE', 1.00),
('Sour Cream', 'SAUCE', 0.50),
('Cheese Sauce', 'SAUCE', 0.75),

-- Extras
('Shredded Cheese', 'EXTRAS', 0.50),
('Extra Meat', 'EXTRAS', 2.00),
('Avocado Slices', 'EXTRAS', 1.25);

-- =====================================================
-- PRODUCTS (6+ products as required)
-- =====================================================
-- Get currency IDs for reference
SET @usd_id = (SELECT currency_id FROM currencies WHERE currency_code = 'USD');
SET @php_id = (SELECT currency_id FROM currencies WHERE currency_code = 'PHP');
SET @krw_id = (SELECT currency_id FROM currencies WHERE currency_code = 'KRW');

-- Burritos (customizable)
INSERT INTO products (name, description, base_price, currency_id, category, is_customizable, stock_quantity) VALUES
('Classic Chicken Burrito', 'Grilled chicken with cilantro lime rice, black beans, lettuce, tomatoes, and mild salsa wrapped in a warm flour tortilla', 8.99, @usd_id, 'BURRITO', TRUE, 100),
('Beef Barbacoa Burrito', 'Tender beef barbacoa with Spanish rice, pinto beans, onions, and hot salsa', 10.99, @usd_id, 'BURRITO', TRUE, 80),
('Veggie Delight Burrito', 'Black bean and tofu with brown rice, corn, bell peppers, guacamole, and cheese', 7.99, @usd_id, 'BURRITO', TRUE, 120),

-- Bowls (customizable)
('Power Bowl', 'Build your own bowl with your choice of protein, rice, beans, and toppings', 9.49, @usd_id, 'BOWL', TRUE, 150),
('Korean Fusion Bowl', 'Korean-inspired bowl with special seasonings - priced in KRW', 12000, @krw_id, 'BOWL', TRUE, 90),

-- Drinks (non-customizable)
('Fresh Lime Agua Fresca', 'Refreshing lime-flavored water with a hint of mint', 2.99, @usd_id, 'DRINK', FALSE, 200),
('Horchata', 'Traditional rice and cinnamon drink', 3.49, @usd_id, 'DRINK', FALSE, 150),
('Filipino Buko Juice', 'Fresh coconut water - priced in PHP', 85.00, @php_id, 'DRINK', FALSE, 100),

-- Sides (non-customizable)
('Chips and Guacamole', 'Fresh tortilla chips served with our signature guacamole', 4.99, @usd_id, 'SIDE', FALSE, 180),
('Elote (Mexican Street Corn)', 'Grilled corn with mayo, cheese, chili powder, and lime', 3.99, @usd_id, 'SIDE', FALSE, 120),
('Churros (3 pieces)', 'Crispy fried dough pastry rolled in cinnamon sugar', 4.49, @usd_id, 'SIDE', FALSE, 160);

-- =====================================================
-- PRODUCT-INGREDIENT RELATIONSHIPS
-- (Default ingredients for customizable products)
-- =====================================================

-- Classic Chicken Burrito defaults
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
(1, 1, TRUE),  -- Grilled Chicken
(1, 6, TRUE),  -- Cilantro Lime Rice
(1, 9, TRUE),  -- Black Beans
(1, 13, TRUE), -- Lettuce
(1, 14, TRUE), -- Tomatoes
(1, 19, TRUE); -- Mild Salsa

-- Beef Barbacoa Burrito defaults
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
(2, 2, TRUE),  -- Beef Barbacoa
(2, 8, TRUE),  -- Spanish Rice
(2, 10, TRUE), -- Pinto Beans
(2, 15, TRUE), -- Onions
(2, 21, TRUE); -- Hot Salsa

-- Veggie Delight Burrito defaults
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
(3, 4, TRUE),  -- Black Bean
(3, 5, TRUE),  -- Tofu
(3, 7, TRUE),  -- Brown Rice
(3, 17, TRUE), -- Corn
(3, 16, TRUE), -- Bell Peppers
(3, 22, TRUE), -- Guacamole
(3, 25, TRUE); -- Shredded Cheese

-- Power Bowl defaults (minimal - meant to be customized)
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
(4, 1, TRUE),  -- Grilled Chicken (default protein)
(4, 6, TRUE),  -- Cilantro Lime Rice
(4, 9, TRUE);  -- Black Beans

-- Korean Fusion Bowl defaults
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
(5, 1, TRUE),  -- Grilled Chicken
(5, 6, TRUE),  -- Cilantro Lime Rice
(5, 17, TRUE), -- Corn
(5, 16, TRUE), -- Bell Peppers
(5, 23, TRUE); -- Sour Cream

-- =====================================================
-- SAMPLE TRANSACTION LOG ENTRIES
-- (For demonstration of audit trail)
-- =====================================================
INSERT INTO transaction_log (order_id, payment_method, payment_status, amount, transaction_reference, processed_at) VALUES
(0, 'CASH', 'COMPLETED', 0, 'SYSTEM_INITIALIZATION', CURRENT_TIMESTAMP),
(0, 'CASH', 'COMPLETED', 1.0000, 'RATE_SETUP_USD', CURRENT_TIMESTAMP),
(0, 'CASH', 'COMPLETED', 56.5000, 'RATE_SETUP_PHP', CURRENT_TIMESTAMP),
(0, 'CASH', 'COMPLETED', 1320.0000, 'RATE_SETUP_KRW', CURRENT_TIMESTAMP);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify currency setup
SELECT 'Currency Setup' as verification_type, currency_code, symbol, exchange_rate_to_usd 
FROM currencies 
ORDER BY currency_code;

-- Verify user roles
SELECT 'User Roles' as verification_type, role, COUNT(*) as user_count 
FROM users 
GROUP BY role 
ORDER BY role;

-- Verify product categories and pricing
SELECT 'Product Catalog' as verification_type, 
       p.name, 
       p.category, 
       p.base_price, 
       c.currency_code,
       p.is_customizable,
       p.stock_quantity
FROM products p 
JOIN currencies c ON p.currency_id = c.currency_id 
ORDER BY p.category, p.name;

-- Verify ingredient categories
SELECT 'Ingredient Categories' as verification_type, 
       category, 
       COUNT(*) as ingredient_count,
       AVG(additional_price) as avg_additional_price
FROM ingredients 
GROUP BY category 
ORDER BY category;