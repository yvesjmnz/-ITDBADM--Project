-- =====================================================
-- Neo's Burritos User Roles and Privileges
-- PURPOSE: Define database roles and grant appropriate privileges
-- FOLLOWS: Principle of least privilege and role-based access control
-- =====================================================

USE neos_burritos;

-- =====================================================
-- ROLE DEFINITIONS
-- PURPOSE: Create distinct roles for different user types
-- PRINCIPLE: Single responsibility - each role has specific access patterns
-- =====================================================

-- Drop existing roles if they exist (MySQL 8.0+ syntax)
DROP ROLE IF EXISTS 'neos_admin'@'%';
DROP ROLE IF EXISTS 'neos_staff'@'%';
DROP ROLE IF EXISTS 'neos_customer'@'%';

-- Create roles
CREATE ROLE 'neos_admin'@'%';
CREATE ROLE 'neos_staff'@'%';
CREATE ROLE 'neos_customer'@'%';

-- =====================================================
-- ADMIN PRIVILEGES
-- PURPOSE: Full system access for administrative tasks
-- SCOPE: Complete CRUD operations on all tables
-- JUSTIFICATION: Admins need full control for system management
-- =====================================================

-- Grant full privileges on all tables to admin role
GRANT ALL PRIVILEGES ON neos_burritos.* TO 'neos_admin'@'%';


-- =====================================================
-- STAFF PRIVILEGES
-- PURPOSE: Operational access for day-to-day business functions
-- SCOPE: Read/write access to operational data, limited admin functions
-- PRINCIPLE: Staff can manage orders, products, and customer service
-- =====================================================

-- Core operational tables - full access
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.orders TO 'neos_staff'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.order_items TO 'neos_staff'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.transaction_log TO 'neos_staff'@'%';

-- Product management - full access
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.products TO 'neos_staff'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.ingredients TO 'neos_staff'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.product_ingredients TO 'neos_staff'@'%';

-- User management - limited access (no password changes, no admin creation)
GRANT SELECT, UPDATE ON neos_burritos.users TO 'neos_staff'@'%';

-- Currency management - read only (staff shouldn't modify exchange rates)
GRANT SELECT ON neos_burritos.currencies TO 'neos_staff'@'%';

-- Cart management - full access for customer service
GRANT SELECT, INSERT, UPDATE, DELETE ON neos_burritos.cart_items TO 'neos_staff'@'%';

-- =====================================================
-- CUSTOMER PRIVILEGES
-- PURPOSE: Self-service access for customer operations
-- SCOPE: Limited to own data and read-only product information
-- PRINCIPLE: Customers can only access their own orders and cart
-- =====================================================

-- Product catalog - read only
GRANT SELECT ON neos_burritos.products TO 'neos_customer'@'%';
GRANT SELECT ON neos_burritos.ingredients TO 'neos_customer'@'%';
GRANT SELECT ON neos_burritos.product_ingredients TO 'neos_customer'@'%';
GRANT SELECT ON neos_burritos.currencies TO 'neos_customer'@'%';

