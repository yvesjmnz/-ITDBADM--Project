package com.neosburritos.ui;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.model.Order;
import com.neosburritos.model.OrderItem;
import com.neosburritos.model.User;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * Order history panel for viewing past orders and order details
 * Handles order display and detailed order information
 */
public class OrderHistoryPanel extends BasePanel {
    
    public interface OrderHistoryListener {
        void onBackToStore();
    }
    
    private final OrderDAO orderDAO;
    private final OrderHistoryListener orderHistoryListener;
    
    // Current state
    private User currentUser;
    private List<Order> currentOrders;
    
    // UI Components
    private java.awt.List orderList;
    private TextArea orderDetailsArea;
    private Button refreshButton;
    
    public OrderHistoryPanel(Frame parentFrame, OrderDAO orderDAO, OrderHistoryListener orderHistoryListener) {
        super(parentFrame);
        this.orderDAO = orderDAO;
        this.orderHistoryListener = orderHistoryListener;
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Order list
        orderList = new java.awt.List(8);
        orderList.setFont(UIConstants.BODY_FONT);
        orderList.setBackground(UIConstants.SURFACE_COLOR);
        
        // Order details area
        orderDetailsArea = new TextArea(15, 40);
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setBackground(UIConstants.BACKGROUND_COLOR);
        orderDetailsArea.setFont(UIConstants.BODY_FONT);
        
        // Refresh button
        refreshButton = UIConstants.createSecondaryButton("Refresh");
    }
    
    @Override
    protected void layoutComponents() {
        // Header
        Panel headerPanel = createHeaderPanel("Order History", "Back to Store", 
                                            e -> orderHistoryListener.onBackToStore());
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Left side - Order list
        Panel leftPanel = createOrderListPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Order details
        Panel rightPanel = createOrderDetailsPanel();
        contentPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private Panel createOrderListPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        panel.setPreferredSize(new Dimension(400, 0));
        
        // Title and refresh button
        Panel titlePanel = new Panel(new BorderLayout());
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        
        Label titleLabel = UIConstants.createHeaderLabel("Your Orders");
        Panel titleLeft = new Panel(new FlowLayout(FlowLayout.LEFT));
        titleLeft.setBackground(UIConstants.SURFACE_COLOR);
        titleLeft.add(titleLabel);
        titlePanel.add(titleLeft, BorderLayout.WEST);
        
        Panel titleRight = new Panel(new FlowLayout(FlowLayout.RIGHT));
        titleRight.setBackground(UIConstants.SURFACE_COLOR);
        titleRight.add(refreshButton);
        titlePanel.add(titleRight, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Order list
        panel.add(orderList, BorderLayout.CENTER);
        
        // Instructions
        Label instructionLabel = UIConstants.createBodyLabel("Select an order to view details");
        instructionLabel.setForeground(UIConstants.TEXT_SECONDARY);
        instructionLabel.setAlignment(Label.CENTER);
        Panel instructionPanel = new Panel(new FlowLayout());
        instructionPanel.setBackground(UIConstants.SURFACE_COLOR);
        instructionPanel.add(instructionLabel);
        panel.add(instructionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Panel createOrderDetailsPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title
        Label titleLabel = UIConstants.createHeaderLabel("Order Details");
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Details area
        panel.add(orderDetailsArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    @Override
    protected void setupEventHandlers() {
        orderList.addItemListener(this::handleOrderSelection);
        refreshButton.addActionListener(e -> refreshOrders());
    }
    
    private void handleOrderSelection(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            int selectedIndex = orderList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < currentOrders.size()) {
                Order order = currentOrders.get(selectedIndex);
                displayOrderDetails(order);
            }
        }
    }
    
    private void displayOrderDetails(Order order) {
        // Get full order details
        Order fullOrder = orderDAO.getOrderById(order.getOrderId());
        
        if (fullOrder == null) {
            orderDetailsArea.setText("Error loading order details");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        
        // Order header information
        details.append("ORDER INFORMATION\\n");
        details.append("================\\n");
        details.append("Order ID: ").append(fullOrder.getOrderId()).append("\\n");
        details.append("Date: ").append(formatDateTime(fullOrder.getOrderDate())).append("\\n");
        details.append("Status: ").append(formatStatus(fullOrder.getStatus())).append("\\n");
        details.append("Total: ").append(fullOrder.getFormattedTotal()).append("\\n");
        details.append("\\n");
        
        // Delivery information
        details.append("DELIVERY INFORMATION\\n");
        details.append("===================\\n");
        details.append("Address: ").append(fullOrder.getDeliveryAddress()).append("\\n");
        if (fullOrder.getNotes() != null && !fullOrder.getNotes().trim().isEmpty()) {
            details.append("Notes: ").append(fullOrder.getNotes()).append("\\n");
        }
        details.append("\\n");
        
        // Order items
        details.append("ORDER ITEMS\\n");
        details.append("===========\\n");
        
        if (fullOrder.getItems() != null && !fullOrder.getItems().isEmpty()) {
            for (OrderItem item : fullOrder.getItems()) {
                details.append("• ").append(item.getProductName());
                details.append(" (x").append(item.getQuantity()).append(")");
                details.append(" - ").append(fullOrder.getCurrencySymbol());
                details.append(item.getTotalPrice().toString());
                
                if (item.getCustomizations() != null && !item.getCustomizations().trim().isEmpty()) {
                    details.append("\\n  Customizations: ").append(item.getCustomizations());
                }
                details.append("\\n");
            }
        } else {
            details.append("No items found\\n");
        }
        
        // Order timeline
        details.append("\\nORDER TIMELINE\\n");
        details.append("==============\\n");
        details.append("Created: ").append(formatDateTime(fullOrder.getCreatedAt())).append("\\n");
        if (fullOrder.getUpdatedAt() != null && !fullOrder.getUpdatedAt().equals(fullOrder.getCreatedAt())) {
            details.append("Last Updated: ").append(formatDateTime(fullOrder.getUpdatedAt())).append("\\n");
        }
        
        orderDetailsArea.setText(details.toString());
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
    }
    
    private String formatStatus(Order.Status status) {
        switch (status) {
            case PENDING: return "Pending";
            case CONFIRMED: return "Confirmed";
            case PREPARING: return "Preparing";
            case READY: return "Ready for Pickup/Delivery";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return status.toString();
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        refreshOrders();
    }
    
    public void refreshOrders() {
        if (currentUser == null) return;
        
        currentOrders = orderDAO.getUserOrders(currentUser.getUserId());
        
        orderList.removeAll();
        
        if (currentOrders.isEmpty()) {
            orderList.add("No orders found");
            orderDetailsArea.setText("You haven't placed any orders yet.\\n\\n" +
                                   "Start shopping to see your order history here!");
        } else {
            for (Order order : currentOrders) {
                String displayText = String.format("Order #%d - %s - %s (%s)",
                        order.getOrderId(),
                        order.getFormattedTotal(),
                        formatStatus(order.getStatus()),
                        formatDateTime(order.getOrderDate()));
                orderList.add(displayText);
            }
            
            orderDetailsArea.setText("Select an order from the list to view detailed information.");
        }
        
        System.out.println("Refreshed order history: " + currentOrders.size() + " orders");
    }
}