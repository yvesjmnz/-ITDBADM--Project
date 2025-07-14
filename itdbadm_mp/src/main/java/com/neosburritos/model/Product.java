package com.neosburritos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product model representing menu items
 */
public class Product {
    public enum Category {
        BURRITO, BOWL, DRINK, SIDE
    }

    private int productId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String currencyCode;
    private String currencySymbol;
    private Category category;
    private boolean isCustomizable;
    private int stockQuantity;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Product() {}

    public Product(int productId, String name, BigDecimal basePrice, Category category) {
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.category = category;
        this.isActive = true;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public boolean isCustomizable() { return isCustomizable; }
    public void setCustomizable(boolean customizable) { isCustomizable = customizable; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFormattedPrice() {
        return currencySymbol + basePrice.toString();
    }

    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }
}