package com.neosburritos.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.Order;
import com.neosburritos.model.OrderItem;
import com.neosburritos.model.User;

/**
 * Enhanced Admin panel with logout and product management
 * Single responsibility: Administrative functions only
 */
public class SwingAdminPanel extends JPanel {
    
    public interface AdminListener {
        // Removed onBackToStore - admin stays in admin panel
        void onManageProducts();
        void onLogout();
    }
    
    private final JFrame parentFrame;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final AdminListener adminListener;
    
    // Current state
    private User currentUser;
    private List<Order> allOrders;
    private Order selectedOrder;
    
    // UI Components
    private JLabel welcomeLabel;
    private JLabel statsLabel;
    private JButton logoutButton;
    
    // Order Management Section
    private JList<String> orderList;
    private DefaultListModel<String> orderListModel;
    private JTextArea orderDetailsArea;
    private JComboBox<String> statusComboBox;
    private JButton updateStatusButton;
    private JButton refreshOrdersButton;
    
    // Quick Actions Section
    private JButton viewAllUsersButton;
    private JButton viewProductsButton;
    private JButton systemStatsButton;
    
    public SwingAdminPanel(JFrame parentFrame, OrderDAO orderDAO, ProductDAO productDAO, 
                          UserDAO userDAO, AdminListener adminListener) {
        this.parentFrame = parentFrame;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.adminListener = adminListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Header components
        welcomeLabel = SwingUIConstants.createTitleLabel("Admin Dashboard");
        statsLabel = SwingUIConstants.createBodyLabel("Loading system statistics...");
        logoutButton = SwingUIConstants.createDangerButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 36));
        
        // Order management components
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setFont(SwingUIConstants.BODY_FONT);
        orderList.setBackground(SwingUIConstants.SURFACE_COLOR);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        orderDetailsArea = SwingUIConstants.createStyledTextArea(15, 40);
        orderDetailsArea.setEditable(false);
        
        // Status update components
        String[] statuses = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"};
        statusComboBox = SwingUIConstants.createStyledComboBox(statuses);
        statusComboBox.setEnabled(false);
        
        updateStatusButton = SwingUIConstants.createPrimaryButton("Update Status");
        updateStatusButton.setEnabled(false);
        
        refreshOrdersButton = SwingUIConstants.createSecondaryButton("Refresh Orders");
        
        // Quick action buttons
        viewAllUsersButton = SwingUIConstants.createSecondaryButton("View All Users");
        viewProductsButton = SwingUIConstants.createSecondaryButton("Manage Products");
        systemStatsButton = SwingUIConstants.createSecondaryButton("System Statistics");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = SwingUIConstants.createStyledPanel(SwingUIConstants.PRIMARY_COLOR);
        headerPanel.setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE,
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE
        ));
        
        // Title and admin badge with logout button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        welcomeLabel.setForeground(Color.WHITE);
        titlePanel.add(welcomeLabel, BorderLayout.CENTER);
        
        // Right side with admin badge and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_SMALL, 0));
        rightPanel.setOpaque(false);
        
        JLabel adminBadge = SwingUIConstants.createBodyLabel("ADMIN ACCESS");
        adminBadge.setForeground(Color.WHITE);
        adminBadge.setFont(SwingUIConstants.SMALL_FONT);
        rightPanel.add(adminBadge);
        rightPanel.add(logoutButton);
        
        titlePanel.add(rightPanel, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Stats
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(SwingUIConstants.SUBTITLE_FONT);
        headerPanel.add(statsLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Left side - Quick Actions
        JPanel quickActionsPanel = createQuickActionsPanel();
        mainPanel.add(quickActionsPanel, BorderLayout.WEST);
        
        // Center - Order Management
        JPanel orderManagementPanel = createOrderManagementPanel();
        mainPanel.add(orderManagementPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = SwingUIConstants.createSectionPanel("Quick Actions");
        panel.setPreferredSize(new Dimension(250, 0));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Add buttons with spacing
        contentPanel.add(createActionButton(viewAllUsersButton, "View and manage all users"));
        contentPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        contentPanel.add(createActionButton(viewProductsButton, "Manage product inventory"));
        contentPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        contentPanel.add(createActionButton(systemStatsButton, "View detailed statistics"));
        contentPanel.add(Box.createVerticalGlue());
        
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createActionButton(JButton button, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(button, BorderLayout.NORTH);
        
        JLabel descLabel = SwingUIConstants.createSecondaryLabel(description);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(descLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderManagementPanel() {
        JPanel panel = SwingUIConstants.createSectionPanel("Order Management");
        
        // Split into left (order list) and right (order details)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        
        // Left side - Order list and controls
        JPanel leftPanel = createOrderListPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // Right side - Order details
        JPanel rightPanel = createOrderDetailsPanel();
        splitPane.setRightComponent(rightPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createOrderListPanel() {
        JPanel panel = new JPanel(new BorderLayout(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        panel.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        // Title and refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = SwingUIConstants.createHeaderLabel("All Orders");
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(refreshOrdersButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Order list with scroll
        JScrollPane scrollPane = new JScrollPane(orderList);
        scrollPane.setPreferredSize(new Dimension(380, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Select an order to manage",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.SMALL_FONT,
            SwingUIConstants.TEXT_SECONDARY
        ));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Status update controls
        JPanel statusPanel = createStatusUpdatePanel();
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        panel.setBackground(SwingUIConstants.SURFACE_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Update Order Status",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.SMALL_FONT,
            SwingUIConstants.TEXT_SECONDARY
        ));
        
        // Use BoxLayout for better vertical spacing
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setOpaque(false);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL,
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL
        ));
        
        // Status selection row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, SwingUIConstants.PADDING_SMALL, 0));
        statusRow.setOpaque(false);
        statusRow.add(SwingUIConstants.createBodyLabel("New Status:"));
        statusRow.add(statusComboBox);
        
        // Button row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, SwingUIConstants.PADDING_SMALL, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(updateStatusButton);
        
        controlsPanel.add(statusRow);
        controlsPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_SMALL));
        controlsPanel.add(buttonRow);
        
        panel.add(controlsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createOrderDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        panel.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        JLabel titleLabel = SwingUIConstants.createHeaderLabel("Order Details");
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(orderDetailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Detailed order information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.SMALL_FONT,
            SwingUIConstants.TEXT_SECONDARY
        ));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        JLabel footerLabel = SwingUIConstants.createSecondaryLabel(
            "Neo's Burritos Admin Panel - Manage orders, users, and system settings"
        );
        footerPanel.add(footerLabel);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Order list selection
        orderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleOrderSelection();
            }
        });
        
        // Button actions
        updateStatusButton.addActionListener(this::handleUpdateStatus);
        refreshOrdersButton.addActionListener(e -> refreshOrders());
        logoutButton.addActionListener(this::handleLogout);
        
        // Quick action buttons
        viewAllUsersButton.addActionListener(this::handleViewUsers);
        viewProductsButton.addActionListener(this::handleViewProducts);
        systemStatsButton.addActionListener(this::handleSystemStats);
    }
    
    private void handleOrderSelection() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < allOrders.size()) {
            selectedOrder = allOrders.get(selectedIndex);
            displayOrderDetails(selectedOrder);
            
            // Enable status update controls
            statusComboBox.setEnabled(true);
            statusComboBox.setSelectedItem(selectedOrder.getStatus().toString());
            updateStatusButton.setEnabled(true);
        } else {
            selectedOrder = null;
            statusComboBox.setEnabled(false);
            updateStatusButton.setEnabled(false);
        }
    }
    
    private void displayOrderDetails(Order order) {
        // Get full order details including items
        Order fullOrder = orderDAO.getOrderById(order.getOrderId());
        
        if (fullOrder == null) {
            orderDetailsArea.setText("Error loading order details\n\nPlease try refreshing or check the database connection.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        
        // Order header
        details.append("═══════════════════════════════════════\n");
        details.append("           ORDER #").append(fullOrder.getOrderId()).append("\n");
        details.append("═══════════════════════════════════════\n\n");
        
        // Basic information
        details.append("BASIC INFORMATION\n");
        details.append("─────────────────────\n");
        details.append("Customer ID: ").append(fullOrder.getUserId()).append("\n");
        details.append("Order Date: ").append(formatDateTime(fullOrder.getOrderDate())).append("\n");
        details.append("Status: ").append(formatStatus(fullOrder.getStatus())).append("\n");
        details.append("Total Amount: ").append(fullOrder.getFormattedTotal()).append("\n");
        details.append("Item Count: ").append(fullOrder.getItemCount()).append("\n\n");
        
        // Delivery information
        details.append("DELIVERY INFORMATION\n");
        details.append("─────────────────────\n");
        details.append("Address: ").append(fullOrder.getDeliveryAddress()).append("\n");
        if (fullOrder.getNotes() != null && !fullOrder.getNotes().trim().isEmpty()) {
            details.append("Special Notes: ").append(fullOrder.getNotes()).append("\n");
        }
        details.append("\n");
        
        // Order items
        details.append("ORDER ITEMS\n");
        details.append("─────────────────────\n");
        
        if (fullOrder.getItems() != null && !fullOrder.getItems().isEmpty()) {
            for (OrderItem item : fullOrder.getItems()) {
                details.append("• ").append(item.getProductName());
                details.append(" (Qty: ").append(item.getQuantity()).append(")");
                details.append(" - ").append(fullOrder.getCurrencySymbol());
                details.append(item.getTotalPrice().toString()).append("\n");
                
                if (item.getCustomizations() != null && !item.getCustomizations().trim().isEmpty()) {
                    details.append("  └─ Customizations: ").append(item.getCustomizations()).append("\n");
                }
            }
        } else {
            details.append("No items found\n");
        }
        
        // Timeline
        details.append("\nORDER TIMELINE\n");
        details.append("─────────────────────\n");
        details.append("Created: ").append(formatDateTime(fullOrder.getCreatedAt())).append("\n");
        if (fullOrder.getUpdatedAt() != null && !fullOrder.getUpdatedAt().equals(fullOrder.getCreatedAt())) {
            details.append("Last Updated: ").append(formatDateTime(fullOrder.getUpdatedAt())).append("\n");
        }
        
        orderDetailsArea.setText(details.toString());
        orderDetailsArea.setCaretPosition(0); // Scroll to top
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
    }
    
    private String formatStatus(Order.Status status) {
        switch (status) {
            case PENDING: return "Pending";
            case CONFIRMED: return "Confirmed";
            case COMPLETED: return "Completed";
            case CANCELLED: return "Cancelled";
            default: return status.toString();
        }
    }
    
    private void handleUpdateStatus(ActionEvent e) {
        if (selectedOrder == null) {
            SwingUIConstants.showErrorDialog(this, "Please select an order first", "No Order Selected");
            return;
        }
        
        String newStatusStr = (String) statusComboBox.getSelectedItem();
        Order.Status newStatus = Order.Status.valueOf(newStatusStr);
        
        if (newStatus == selectedOrder.getStatus()) {
            SwingUIConstants.showWarningDialog(this, 
                "Order status is already " + newStatus, "No Change Required");
            return;
        }
        
        boolean confirmed = SwingUIConstants.showConfirmDialog(this,
            "Update order #" + selectedOrder.getOrderId() + " status to " + newStatus + "?",
            "Confirm Status Update");
        
        if (confirmed) {
            boolean success = orderDAO.updateOrderStatus(selectedOrder.getOrderId(), newStatus);
            
            if (success) {
                SwingUIConstants.showSuccessDialog(this, 
                    "Order status updated successfully", "Status Updated");
                refreshOrders();
            } else {
                SwingUIConstants.showErrorDialog(this, 
                    "Failed to update order status", "Update Failed");
            }
        }
    }
    
    private void handleLogout(ActionEvent e) {
        boolean confirmed = SwingUIConstants.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout");
        
        if (confirmed && adminListener != null) {
            adminListener.onLogout();
        }
    }
    
    private void handleViewUsers(ActionEvent e) {
        UserManagementDialog dialog = new UserManagementDialog(parentFrame, userDAO);
        dialog.setVisible(true);
    }
    
    private void handleViewProducts(ActionEvent e) {
        adminListener.onManageProducts();
    }
    
    private void handleSystemStats(ActionEvent e) {
        SystemStatisticsDialog dialog = new SystemStatisticsDialog(parentFrame);
        dialog.setVisible(true);
    }

    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + " (Administrator)");
        refreshOrders();
        updateStats();
    }
    
    public void refreshOrders() {
        allOrders = orderDAO.getAllOrders();
        
        orderListModel.clear();
        
        if (allOrders.isEmpty()) {
            orderListModel.addElement("No orders found");
            orderDetailsArea.setText("No orders in the system.\n\nOrders will appear here as customers place them.");
        } else {
            for (Order order : allOrders) {
                String displayText = String.format("Order #%d - User %d - %s - %s",
                        order.getOrderId(),
                        order.getUserId(),
                        order.getFormattedTotal(),
                        formatStatus(order.getStatus()));
                orderListModel.addElement(displayText);
            }
            
            orderDetailsArea.setText("Select an order from the list to view detailed information and update its status.");
        }
        
        // Reset controls
        selectedOrder = null;
        statusComboBox.setEnabled(false);
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
                .mapToLong(order -> order.getStatus() == Order.Status.COMPLETED ? 1 : 0)
                .sum();
        long activeOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == Order.Status.CONFIRMED ? 1 : 0)
                .sum();
        long cancelledOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == Order.Status.CANCELLED ? 1 : 0)
                .sum();
        
        statsLabel.setText(String.format(
            "System Overview: %d Total Orders | %d Pending | %d Active | %d Completed | %d Cancelled", 
            totalOrders, pendingOrders, activeOrders, completedOrders, cancelledOrders));
    }
}