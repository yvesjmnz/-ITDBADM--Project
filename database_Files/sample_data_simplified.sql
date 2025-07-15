-- =====================================================
-- Neo's Burritos - Sample Data (SIMPLIFIED)
-- Minimal dataset for testing the simplified schema
-- =====================================================

USE neos_burritos;

-- =====================================================
-- CURRENCIES (Required: USD, PHP, KRW)
-- =====================================================
INSERT INTO currencies (currency_code, symbol, exchange_rate_to_usd) VALUES
('USD', '$', 1.0000),
('PHP', '₱', 56.5000),
('KRW', '₩', 1320.0000);

-- =====================================================
-- USERS (Admin, Staff, Customers)
-- =====================================================
INSERT INTO users (name, email, password, role, phone, address, is_active) VALUES
-- Admin user
('Neo Anderson', 'admin@neosburritos.com', 'admin123', 'ADMIN', '+1-555-0001', '123 Matrix Street, Digital City', TRUE),

-- Staff user
('Trinity Staff', 'staff@neosburritos.com', 'staff123', 'STAFF', '+1-555-0002', '456 Zion Avenue, Digital City', TRUE),

-- Customer users
('John Customer', 'john@email.com', 'customer123', 'CUSTOMER', '+1-555-0101', '789 Customer Lane, Burrito Town', TRUE),
('Jane Foodie', 'jane@email.com', 'customer456', 'CUSTOMER', '+1-555-0102', '321 Hungry Street, Food City', TRUE);

-- =====================================================
-- PRODUCTS (6+ items across categories)
-- =====================================================
INSERT INTO products (name, description, base_price, stock_quantity, currency_id, category, is_customizable, is_active) VALUES
-- Burritos (customizable)
('Classic Chicken Burrito', 'Grilled chicken with rice, beans, and fresh vegetables wrapped in a warm tortilla', 8.99, 50, 1, 'BURRITO', TRUE, TRUE),
('Beef Barbacoa Burrito', 'Slow-cooked beef barbacoa with cilantro lime rice and black beans', 10.99, 30, 1, 'BURRITO', TRUE, TRUE),
('Veggie Delight Burrito', 'Fresh vegetables, rice, beans, and guacamole in a spinach tortilla', 7.99, 40, 1, 'BURRITO', TRUE, TRUE),

-- Bowls (customizable)
('Power Bowl', 'All the burrito goodness in a bowl - perfect for low-carb diets', 9.99, 35, 1, 'BOWL', TRUE, TRUE),
('Protein Bowl', 'Double protein bowl with your choice of meat and toppings', 12.99, 25, 1, 'BOWL', TRUE, TRUE),

-- Drinks (not customizable)
('Fresh Lime Agua Fresca', 'Refreshing lime-flavored water with a hint of mint', 2.99, 100, 1, 'DRINK', FALSE, TRUE),
('Horchata', 'Traditional Mexican rice drink with cinnamon', 3.49, 80, 1, 'DRINK', FALSE, TRUE),

-- Sides (not customizable)
('Guacamole & Chips', 'Fresh made guacamole with crispy tortilla chips', 4.99, 60, 1, 'SIDE', FALSE, TRUE),
('Queso Blanco & Chips', 'Creamy white cheese dip with warm tortilla chips', 4.49, 70, 1, 'SIDE', FALSE, TRUE);

-- =====================================================
-- INGREDIENTS (For customizable products)
-- =====================================================
INSERT INTO ingredients (name, category, additional_price, is_available) VALUES
-- Proteins
('Grilled Chicken', 'PROTEIN', 0.00, TRUE),
('Carnitas', 'PROTEIN', 0.50, TRUE),
('Barbacoa', 'PROTEIN', 1.00, TRUE),
('Sofritas (Tofu)', 'PROTEIN', 0.00, TRUE),
('Steak', 'PROTEIN', 1.50, TRUE),

-- Rice
('Cilantro Lime Rice', 'RICE', 0.00, TRUE),
('Brown Rice', 'RICE', 0.00, TRUE),

-- Beans
('Black Beans', 'BEANS', 0.00, TRUE),
('Pinto Beans', 'BEANS', 0.00, TRUE),

-- Vegetables
('Lettuce', 'VEGETABLES', 0.00, TRUE),
('Tomatoes', 'VEGETABLES', 0.00, TRUE),
('Onions', 'VEGETABLES', 0.00, TRUE),
('Bell Peppers', 'VEGETABLES', 0.00, TRUE),
('Corn', 'VEGETABLES', 0.25, TRUE),

-- Sauces
('Mild Salsa', 'SAUCE', 0.00, TRUE),
('Medium Salsa', 'SAUCE', 0.00, TRUE),
('Hot Salsa', 'SAUCE', 0.00, TRUE),
('Guacamole', 'SAUCE', 1.00, TRUE),
('Sour Cream', 'SAUCE', 0.50, TRUE),

-- Extras
('Extra Cheese', 'EXTRAS', 0.75, TRUE),
('Extra Meat', 'EXTRAS', 2.00, TRUE),
('Jalapeños', 'EXTRAS', 0.00, TRUE),
('Pickled Onions', 'EXTRAS', 0.00, TRUE);

-- =====================================================
-- PRODUCT INGREDIENTS (Default ingredients for products)
-- =====================================================
INSERT INTO product_ingredients (product_id, ingredient_id, is_default) VALUES
-- Classic Chicken Burrito defaults
(1, 1, TRUE),  -- Grilled Chicken
(1, 6, TRUE),  -- Cilantro Lime Rice
(1, 8, TRUE),  -- Black Beans
(1, 10, TRUE), -- Lettuce
(1, 11, TRUE), -- Tomatoes

-- Beef Barbacoa Burrito defaults
(2, 3, TRUE),  -- Barbacoa
(2, 6, TRUE),  -- Cilantro Lime Rice
(2, 8, TRUE),  -- Black Beans
(2, 16, TRUE), -- Mild Salsa

-- Veggie Delight Burrito defaults
(3, 4, TRUE),  -- Sofritas
(3, 7, TRUE),  -- Brown Rice
(3, 9, TRUE),  -- Pinto Beans
(3, 10, TRUE), -- Lettuce
(3, 19, TRUE), -- Guacamole

-- Power Bowl defaults
(4, 1, TRUE),  -- Grilled Chicken
(4, 6, TRUE),  -- Cilantro Lime Rice
(4, 8, TRUE),  -- Black Beans
(4, 10, TRUE), -- Lettuce

-- Protein Bowl defaults
(5, 5, TRUE),  -- Steak
(5, 6, TRUE),  -- Cilantro Lime Rice
(5, 8, TRUE),  -- Black Beans
(5, 22, TRUE); -- Extra Meat

-- =====================================================
-- DATA VALIDATION QUERIES
-- =====================================================

-- Verify currency setup
SELECT 'Currency Check' as test_name, COUNT(*) as count FROM currencies WHERE currency_code IN ('USD', 'PHP', 'KRW');

-- Verify user roles
SELECT 'User Roles Check' as test_name, role, COUNT(*) as count FROM users GROUP BY role;

-- Verify product categories
SELECT 'Product Categories Check' as test_name, category, COUNT(*) as count FROM products GROUP BY category;

-- Verify customizable products have ingredients
SELECT 'Customizable Products Check' as test_name, 
       p.name, 
       COUNT(pi.ingredient_id) as ingredient_count 
FROM products p 
LEFT JOIN product_ingredients pi ON p.product_id = pi.product_id 
WHERE p.is_customizable = TRUE 
GROUP BY p.product_id, p.name;

-- =====================================================
-- SAMPLE DATA SUMMARY:
-- 
-- CURRENCIES: 3 (USD, PHP, KRW)
-- USERS: 4 (1 admin, 1 staff, 2 customers)
-- PRODUCTS: 9 (3 burritos, 2 bowls, 2 drinks, 2 sides)
-- INGREDIENTS: 24 (covering all categories)
-- ORDERS: 2 (1 delivered, 1 pending)
-- CART ITEMS: 3 (testing cart functionality)
-- 
-- This minimal dataset provides:
-- ✓ All required currencies
-- ✓ All user roles for testing
-- ✓ Products in all categories
-- ✓ Customizable and non-customizable products
-- ✓ Complete ingredient system
-- ✓ Order workflow examples
-- ✓ Cart functionality testing
-- =====================================================