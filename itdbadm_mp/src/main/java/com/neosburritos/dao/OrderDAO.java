package com.neosburritos.dao;

import com.neosburritos.model.Order;
import com.neosburritos.model.OrderItem;
import com.neosburritos.util.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object for Order operations
 * Handles order creation, retrieval, and status management
 */
public class OrderDAO {
    
    /**
     * Result class for order creation operations
     */
    public static class OrderResult {
        private final boolean success;
        private final String message;
        private final int orderId;
        
        public OrderResult(boolean success, String message, int orderId) {
            this.success = success;
            this.message = message;
            this.orderId = orderId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getOrderId() { return orderId; }
    }
    
    /**
     * Create order from user's cart
     */
    public OrderResult createOrderFromCart(int userId, String currencyCode, String deliveryAddress, String notes) {
        String sql = "{CALL sp_place_order(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, currencyCode);
            stmt.setString(3, deliveryAddress);
            stmt.setString(4, notes);
            stmt.registerOutParameter(5, Types.INTEGER);
            stmt.registerOutParameter(6, Types.DECIMAL);
            stmt.registerOutParameter(7, Types.BOOLEAN);
            stmt.registerOutParameter(8, Types.VARCHAR);
            
            stmt.executeUpdate();
            
            boolean success = stmt.getBoolean(7);
            String message = stmt.getString(8);
            int orderId = success ? stmt.getInt(5) : 0;
            
            return new OrderResult(success, message, orderId);
            
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return new OrderResult(false, "Database error: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Get order by ID with items - using sp_get_order_details
     */
    public Order getOrderById(int orderId) {
        String sql = "{CALL sp_get_order_details(?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, orderId);
            stmt.setInt(2, 0); // We'll need to modify this to accept user_id parameter
            
            boolean hasResults = stmt.execute();
            Order order = null;
            
            if (hasResults) {
                // First result set - order header
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        order = new Order();
                        order.setOrderId(rs.getInt("order_id"));
                        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                        order.setTotalAmount(rs.getBigDecimal("total_amount"));
                        order.setCurrencyCode(rs.getString("currency_code"));
                        order.setCurrencySymbol(rs.getString("currency_symbol"));
                        order.setStatus(Order.Status.valueOf(rs.getString("status")));
                        order.setDeliveryAddress(rs.getString("delivery_address"));
                        order.setNotes(rs.getString("notes"));
                    }
                }
                
                // Second result set - order items
                if (order != null && stmt.getMoreResults()) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        java.util.List<OrderItem> items = new ArrayList<>();
                        while (rs.next()) {
                            OrderItem item = new OrderItem();
                            item.setOrderItemId(rs.getInt("order_item_id"));
                            item.setOrderId(rs.getInt("order_id"));
                            item.setProductId(rs.getInt("product_id"));
                            item.setProductName(rs.getString("product_name"));
                            item.setQuantity(rs.getInt("quantity"));
                            item.setUnitPrice(rs.getBigDecimal("unit_price"));
                            item.setTotalPrice(rs.getBigDecimal("total_price"));
                            item.setCustomizations(rs.getString("customizations"));
                            items.add(item);
                        }
                        order.setItems(items);
                        order.setItemCount(items.size());
                    }
                }
            }
            
            return order;
            
        } catch (SQLException e) {
            System.err.println("Error getting order: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get order items for an order - using direct SQL since sp_get_order_details handles this
     */
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
            SELECT 
                oi.order_item_id,
                oi.order_id,
                oi.product_id,
                p.name as product_name,
                oi.quantity,
                oi.unit_price,
                (oi.quantity * oi.unit_price) as total_price,
                oi.customizations
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
            """;
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setCustomizations(rs.getString("customizations"));
                    
                    items.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting order items: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Get orders for a user
     */
    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "{CALL sp_get_order_history(?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, 50); // Limit to 50 orders
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(userId);
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setCurrencyCode(rs.getString("currency_code"));
                    order.setCurrencySymbol(rs.getString("currency_symbol"));
                    order.setStatus(Order.Status.valueOf(rs.getString("status")));
                    order.setDeliveryAddress(rs.getString("delivery_address"));
                    order.setNotes(rs.getString("notes"));
                    order.setItemCount(rs.getInt("item_count"));
                    
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user orders: " + e.getMessage());
        }
        
        return orders;
    }
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, Order.Status status) {
        String sql = "{CALL sp_update_order_status(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, orderId);
            stmt.setString(2, status.name());
            stmt.registerOutParameter(3, Types.BOOLEAN);
            stmt.registerOutParameter(4, Types.VARCHAR);
            
            stmt.executeUpdate();
            
            return stmt.getBoolean(3);
            
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all orders (admin function) - using direct SQL since no specific SP exists
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.order_id,
                o.user_id,
                o.order_date,
                o.total_amount,
                c.currency_code,
                c.symbol as currency_symbol,
                o.status,
                o.delivery_address,
                o.notes,
                COUNT(oi.order_item_id) as item_count,
                o.created_at,
                o.updated_at
            FROM orders o
            JOIN currencies c ON o.currency_id = c.currency_id
            LEFT JOIN order_items oi ON o.order_id = oi.order_id
            GROUP BY o.order_id
            ORDER BY o.order_date DESC
            """;
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setCurrencyCode(rs.getString("currency_code"));
                    order.setCurrencySymbol(rs.getString("currency_symbol"));
                    order.setStatus(Order.Status.valueOf(rs.getString("status")));
                    order.setDeliveryAddress(rs.getString("delivery_address"));
                    order.setNotes(rs.getString("notes"));
                    order.setItemCount(rs.getInt("item_count"));
                    order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        
        return orders;
    }
}