package com.neosburritos.ui;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.Order;
import com.neosburritos.model.User;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * Admin panel for administrative functions
 * Handles order management, user management, and system administration
 */
public class AdminPanel extends BasePanel {
    
    public interface AdminListener {
        void onBackToStore();
    }
    
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final AdminListener adminListener;
    
    // Current state
    private User currentUser;
    private List<Order> allOrders;
    
    // UI Components
    private java.awt.List orderList;
    private TextArea orderDetailsArea;
    private Choice statusChoice;
    private Button updateStatusButton;
    private Button refreshOrdersButton;
    private Label statsLabel;
    
    public AdminPanel(Frame parentFrame, OrderDAO orderDAO, ProductDAO productDAO, 
                     UserDAO userDAO, AdminListener adminListener) {
        super(parentFrame);
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.adminListener = adminListener;
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Order list
        orderList = new java.awt.List(10);
        orderList.setFont(UIConstants.BODY_FONT);
        orderList.setBackground(UIConstants.SURFACE_COLOR);
        
        // Order details area
        orderDetailsArea = new TextArea(12, 40);
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setBackground(UIConstants.BACKGROUND_COLOR);
        orderDetailsArea.setFont(UIConstants.BODY_FONT);
        
        // Status choice
        statusChoice = new Choice();
        for (Order.Status status : Order.Status.values()) {
            statusChoice.add(status.toString());
        }
        statusChoice.setFont(UIConstants.BODY_FONT);
        statusChoice.setEnabled(false);
        
        // Action buttons
        updateStatusButton = UIConstants.createPrimaryButton("Update Status");
        updateStatusButton.setEnabled(false);
        
        refreshOrdersButton = UIConstants.createSecondaryButton("Refresh Orders");
        
        // Stats label
        statsLabel = UIConstants.createBodyLabel("System Statistics");
    }
    
    @Override
    protected void layoutComponents() {
        // Header
        Panel headerPanel = createHeaderPanel("Admin Panel", "Back to Store", 
                                            e -> adminListener.onBackToStore());
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Top section - Statistics
        Panel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Middle section - Order management
        Panel orderManagementPanel = createOrderManagementPanel();
        contentPanel.add(orderManagementPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private Panel createStatsPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(0, 60));
        
        Label titleLabel = UIConstants.createHeaderLabel("System Overview");
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        Panel statsContentPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        statsContentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        statsContentPanel.add(statsLabel);
        panel.add(statsContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Panel createOrderManagementPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title and refresh button
        Panel titlePanel = new Panel(new BorderLayout());
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        
        Label titleLabel = UIConstants.createHeaderLabel("Order Management");
        Panel titleLeft = new Panel(new FlowLayout(FlowLayout.LEFT));
        titleLeft.setBackground(UIConstants.SURFACE_COLOR);
        titleLeft.add(titleLabel);
        titlePanel.add(titleLeft, BorderLayout.WEST);
        
        Panel titleRight = new Panel(new FlowLayout(FlowLayout.RIGHT));
        titleRight.setBackground(UIConstants.SURFACE_COLOR);
        titleRight.add(refreshOrdersButton);
        titlePanel.add(titleRight, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Main order management content
        Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Left side - Order list
        Panel leftPanel = new Panel(new BorderLayout());
        leftPanel.setBackground(UIConstants.SURFACE_COLOR);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        Label orderListLabel = UIConstants.createBodyLabel("All Orders:");
        Panel orderListTitlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        orderListTitlePanel.setBackground(UIConstants.SURFACE_COLOR);
        orderListTitlePanel.add(orderListLabel);
        leftPanel.add(orderListTitlePanel, BorderLayout.NORTH);
        
        leftPanel.add(orderList, BorderLayout.CENTER);
        
        // Status update controls
        Panel statusPanel = createStatusUpdatePanel();
        leftPanel.add(statusPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Order details
        Panel rightPanel = new Panel(new BorderLayout());
        rightPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        Label detailsLabel = UIConstants.createBodyLabel("Order Details:");
        Panel detailsTitlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        detailsTitlePanel.setBackground(UIConstants.SURFACE_COLOR);
        detailsTitlePanel.add(detailsLabel);
        rightPanel.add(detailsTitlePanel, BorderLayout.NORTH);
        
        rightPanel.add(orderDetailsArea, BorderLayout.CENTER);
        
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        panel.add(mainPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Panel createStatusUpdatePanel() {
        Panel panel = createFormPanel();
        panel.setPreferredSize(new Dimension(0, 80));
        
        GridBagConstraints gbc = createFormConstraints(0, 0, 1, GridBagConstraints.WEST);
        
        panel.add(UIConstants.createBodyLabel("Update Status:"), gbc);
        gbc = createFormConstraints(1, 0, 1, GridBagConstraints.WEST);
        panel.add(statusChoice, gbc);
        gbc = createFormConstraints(2, 0, 1, GridBagConstraints.WEST);
        panel.add(updateStatusButton, gbc);
        
        return panel;
    }
    
    @Override
    protected void setupEventHandlers() {
        orderList.addItemListener(this::handleOrderSelection);
        updateStatusButton.addActionListener(e -> handleUpdateStatus());
        refreshOrdersButton.addActionListener(e -> refreshOrders());
    }
    
    private void handleOrderSelection(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            int selectedIndex = orderList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < allOrders.size()) {
                Order order = allOrders.get(selectedIndex);
                displayOrderDetails(order);
                
                // Enable status update controls
                statusChoice.setEnabled(true);
                statusChoice.select(order.getStatus().toString());
                updateStatusButton.setEnabled(true);
            }
        }
    }
    
    private void displayOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        
        details.append("ORDER #").append(order.getOrderId()).append("\\n");
        details.append("===================\\n");
        details.append("Customer ID: ").append(order.getUserId()).append("\\n");
        details.append("Date: ").append(order.getOrderDate()).append("\\n");
        details.append("Status: ").append(order.getStatus()).append("\\n");
        details.append("Total: ").append(order.getFormattedTotal()).append("\\n");
        details.append("Items: ").append(order.getItemCount()).append("\\n");
        details.append("\\nDelivery Address:\\n").append(order.getDeliveryAddress()).append("\\n");
        
        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            details.append("\\nNotes:\\n").append(order.getNotes()).append("\\n");
        }
        
        details.append("\\nCreated: ").append(order.getCreatedAt()).append("\\n");
        if (order.getUpdatedAt() != null) {
            details.append("Updated: ").append(order.getUpdatedAt()).append("\\n");
        }
        
        orderDetailsArea.setText(details.toString());
    }
    
    private void handleUpdateStatus() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= allOrders.size()) {
            showError("Please select an order first");
            return;
        }
        
        Order order = allOrders.get(selectedIndex);
        String newStatusStr = statusChoice.getSelectedItem();
        Order.Status newStatus = Order.Status.valueOf(newStatusStr);
        
        if (newStatus == order.getStatus()) {
            showInfo("Order status is already " + newStatus);
            return;
        }
        
        if (showConfirmDialog("Update order #" + order.getOrderId() + " status to " + newStatus + "?", 
                            "Update Order Status")) {
            
            boolean success = orderDAO.updateOrderStatus(order.getOrderId(), newStatus);
            
            if (success) {
                showSuccess("Order status updated successfully");
                refreshOrders();
            } else {
                showError("Failed to update order status");
            }
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        refreshOrders();
        updateStats();
    }
    
    public void refreshOrders() {
        allOrders = orderDAO.getAllOrders();
        
        orderList.removeAll();
        
        if (allOrders.isEmpty()) {
            orderList.add("No orders found");
            orderDetailsArea.setText("No orders in the system.");
        } else {
            for (Order order : allOrders) {
                String displayText = String.format("Order #%d - User %d - %s - %s",
                        order.getOrderId(),
                        order.getUserId(),
                        order.getFormattedTotal(),
                        order.getStatus());
                orderList.add(displayText);
            }
            
            orderDetailsArea.setText("Select an order to view details and update status.");
        }
        
        // Reset status controls
        statusChoice.setEnabled(false);
        updateStatusButton.setEnabled(false);
        
        System.out.println("Admin: Refreshed orders - " + allOrders.size() + " total orders");
    }
    
    private void updateStats() {
        if (allOrders == null) {
            statsLabel.setText("Loading statistics...");
            return;
        }
        
        int totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == Order.Status.PENDING ? 1 : 0)
                .sum();
        long completedOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == Order.Status.DELIVERED ? 1 : 0)
                .sum();
        
        statsLabel.setText(String.format("Total Orders: %d | Pending: %d | Completed: %d", 
                                        totalOrders, pendingOrders, completedOrders));
    }
}