package com.neosburritos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order model representing customer orders
 */
public class Order {
    public enum Status {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }

    private int orderId;
    private int userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String currencyCode;
    private String currencySymbol;
    private Status status;
    private String deliveryAddress;
    private String notes;
    private int itemCount;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Order() {}

    public Order(int orderId, BigDecimal totalAmount, Status status) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFormattedTotal() {
        return currencySymbol + totalAmount.toString();
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + getFormattedTotal() + " (" + status + ")";
    }
}