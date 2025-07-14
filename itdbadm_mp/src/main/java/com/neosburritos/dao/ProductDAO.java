package com.neosburritos.dao;

import com.neosburritos.model.Product;
import com.neosburritos.util.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations
 * Demonstrates stored procedure calls for product management and currency conversion
 */
public class ProductDAO {

    public ProductDAO() {
        // No instance connection - each method manages its own connection lifecycle
    }

    /**
     * Get products with currency conversion using sp_get_products_by_currency
     */
    public List<Product> getProductsByCurrency(String currencyCode, Product.Category category) {
        String sql = "{CALL sp_get_products_by_currency(?, ?)}";
        List<Product> products = new ArrayList<>();
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, currencyCode);
            if (category != null) {
                stmt.setString(2, category.name());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setBasePrice(rs.getBigDecimal("converted_price"));
                product.setCurrencyCode(rs.getString("display_currency"));
                product.setCurrencySymbol(rs.getString("currency_symbol"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setCategory(Product.Category.valueOf(rs.getString("category")));
                product.setCustomizable(rs.getBoolean("is_customizable"));
                product.setActive(rs.getBoolean("is_active"));
                
                products.add(product);
            }
            
            System.out.println("Retrieved " + products.size() + " products for currency: " + currencyCode);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving products for currency: " + currencyCode + " - " + e.getMessage());
        }
        
        return products;
    }

    /**
     * Add new product using sp_add_product stored procedure (Admin only)
     */
    public AddProductResult addProduct(String name, String description, BigDecimal basePrice,
                                     String currencyCode, Product.Category category,
                                     boolean isCustomizable, int stockQuantity) {
        String sql = "{CALL sp_add_product(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setBigDecimal(3, basePrice);
            stmt.setString(4, currencyCode);
            stmt.setString(5, category.name());
            stmt.setBoolean(6, isCustomizable);
            stmt.setInt(7, stockQuantity);
            
            // Register output parameters
            stmt.registerOutParameter(8, Types.INTEGER); // product_id
            stmt.registerOutParameter(9, Types.BOOLEAN); // success
            stmt.registerOutParameter(10, Types.VARCHAR); // message
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(9);
            String message = stmt.getString(10);
            Integer productId = success ? stmt.getInt(8) : null;
            
            if (success) {
                System.out.println("Product added successfully: " + name);
            } else {
                System.out.println("Failed to add product: " + message);
            }
            
            return new AddProductResult(success, message, productId);
            
        } catch (SQLException e) {
            System.err.println("Error adding product: " + name + " - " + e.getMessage());
            return new AddProductResult(false, "Database error while adding product", null);
        }
    }

    /**
     * Update product using sp_update_product stored procedure (Admin only)
     */
    public UpdateProductResult updateProduct(int productId, String name, String description,
                                           BigDecimal basePrice, int stockQuantity, boolean isActive) {
        String sql = "{CALL sp_update_product(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setInt(1, productId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setBigDecimal(4, basePrice);
            stmt.setInt(5, stockQuantity);
            stmt.setBoolean(6, isActive);
            
            // Register output parameters
            stmt.registerOutParameter(7, Types.BOOLEAN); // success
            stmt.registerOutParameter(8, Types.VARCHAR); // message
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(7);
            String message = stmt.getString(8);
            
            if (success) {
                System.out.println("Product updated successfully: ID " + productId);
            } else {
                System.out.println("Failed to update product ID " + productId + ": " + message);
            }
            
            return new UpdateProductResult(success, message);
            
        } catch (SQLException e) {
            System.err.println("Error updating product ID: " + productId + " - " + e.getMessage());
            return new UpdateProductResult(false, "Database error while updating product");
        }
    }

    /**
     * Convert price between currencies using sp_convert_price
     */
    public ConversionResult convertPrice(BigDecimal amount, String fromCurrency, String toCurrency) {
        String sql = "{CALL sp_convert_price(?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setBigDecimal(1, amount);
            stmt.setString(2, fromCurrency);
            stmt.setString(3, toCurrency);
            
            // Register output parameters
            stmt.registerOutParameter(4, Types.DECIMAL); // converted_amount
            stmt.registerOutParameter(5, Types.BOOLEAN); // success
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(5);
            BigDecimal convertedAmount = success ? stmt.getBigDecimal(4) : null;
            
            return new ConversionResult(success, convertedAmount);
            
        } catch (SQLException e) {
            System.err.println("Error converting currency: " + amount + " " + fromCurrency + " to " + toCurrency + " - " + e.getMessage());
            return new ConversionResult(false, null);
        }
    }

    // Result classes
    public static class AddProductResult {
        private final boolean success;
        private final String message;
        private final Integer productId;

        public AddProductResult(boolean success, String message, Integer productId) {
            this.success = success;
            this.message = message;
            this.productId = productId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getProductId() { return productId; }
    }

    public static class UpdateProductResult {
        private final boolean success;
        private final String message;

        public UpdateProductResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class ConversionResult {
        private final boolean success;
        private final BigDecimal convertedAmount;

        public ConversionResult(boolean success, BigDecimal convertedAmount) {
            this.success = success;
            this.convertedAmount = convertedAmount;
        }

        public boolean isSuccess() { return success; }
        public BigDecimal getConvertedAmount() { return convertedAmount; }
    }
}