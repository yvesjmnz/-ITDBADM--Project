package com.neosburritos.dao;

import com.neosburritos.model.CartItem;
import com.neosburritos.util.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object for Cart operations
 * Handles cart persistence and management
 */
public class CartDAO {
    
    /**
     * Add item to cart or update quantity if exists
     */
    public boolean addToCart(int userId, int productId, int quantity, String customizations) {
        String sql = "{CALL sp_add_to_cart(?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setString(4, customizations);
            stmt.registerOutParameter(5, Types.BOOLEAN);
            stmt.registerOutParameter(6, Types.VARCHAR);
            
            stmt.executeUpdate();
            
            boolean success = stmt.getBoolean(5);
            if (!success) {
                System.err.println("Error adding to cart: " + stmt.getString(6));
            }
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all cart items for a user with current currency conversion
     */
    public List<CartItem> getCartItems(int userId, String currencyCode) {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "{CALL sp_get_cart_items(?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, currencyCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartId(rs.getInt("cart_id"));
                    item.setUserId(userId);
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setCustomizations(rs.getString("customizations"));
                    item.setCurrencySymbol(rs.getString("currency_symbol"));
                    item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                    
                    cartItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting cart items: " + e.getMessage());
        }
        
        return cartItems;
    }
    
    /**
     * Update cart item quantity - using direct SQL since no specific SP exists
     */
    public boolean updateCartItemQuantity(int cartId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, cartId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating cart item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update cart item customizations - using direct SQL since no specific SP exists
     */
    public boolean updateCartItemCustomization(int cartId, String customizations) {
        String sql = "UPDATE cart_items SET customizations = ? WHERE cart_id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customizations);
            stmt.setInt(2, cartId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating cart item customizations: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove item from cart - using direct SQL since no specific SP exists
     */
    public boolean removeFromCart(int cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, cartId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing from cart: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clear all items from user's cart
     */
    public boolean clearCart(int userId) {
        String sql = "{CALL sp_clear_cart(?, ?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.registerOutParameter(3, Types.VARCHAR);
            
            stmt.executeUpdate();
            
            return stmt.getBoolean(2);
            
        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get cart total for a user in specified currency
     */
    public BigDecimal getCartTotal(int userId, String currencyCode) {
        String sql = """
            SELECT SUM(
                CASE 
                    WHEN c.currency_code = ? THEN (p.base_price * ci.quantity)
                    ELSE ROUND((p.base_price * (target_c.exchange_rate_to_usd / c.exchange_rate_to_usd)) * ci.quantity, 2)
                END
            ) as total_amount
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.product_id
            JOIN currencies c ON p.currency_id = c.currency_id
            JOIN currencies target_c ON target_c.currency_code = ?
            WHERE ci.user_id = ?
            """;
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, currencyCode);
            stmt.setString(2, currencyCode);
            stmt.setInt(3, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting cart total: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Get cart item count for a user
     */
    public int getCartItemCount(int userId) {
        String sql = "SELECT COUNT(*) as item_count FROM cart_items WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("item_count");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting cart item count: " + e.getMessage());
        }
        
        return 0;
    }
}