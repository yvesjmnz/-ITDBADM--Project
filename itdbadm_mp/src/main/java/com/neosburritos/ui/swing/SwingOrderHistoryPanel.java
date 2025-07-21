package com.neosburritos.ui.swing;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.model.Order;
import com.neosburritos.model.OrderItem;
import com.neosburritos.model.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern Swing-based Order History Panel for customers
 * Enhanced UX with better order details display
 */
public class SwingOrderHistoryPanel extends JPanel {
    
    public interface OrderHistoryListener {
        void onBackToStore();
    }
    
    private final JFrame parentFrame;
    private final OrderDAO orderDAO;
    private final OrderHistoryListener orderHistoryListener;
    
    // Current state
    private User currentUser;
    private List<Order> currentOrders;
    
    // UI Components
    private JLabel welcomeLabel;
    private JLabel summaryLabel;
    private JList<String> orderList;
    private DefaultListModel<String> orderListModel;
    private JTextArea orderDetailsArea;
    private JButton refreshButton;
    private JButton backButton;
    
    public SwingOrderHistoryPanel(JFrame parentFrame, OrderDAO orderDAO, OrderHistoryListener orderHistoryListener) {
        this.parentFrame = parentFrame;
        this.orderDAO = orderDAO;
        this.orderHistoryListener = orderHistoryListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Header components
        welcomeLabel = SwingUIConstants.createTitleLabel("My Order History");
        summaryLabel = SwingUIConstants.createBodyLabel("Loading your orders...");
        
        // Order list
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setFont(SwingUIConstants.BODY_FONT);
        orderList.setBackground(SwingUIConstants.SURFACE_COLOR);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Order details area
        orderDetailsArea = SwingUIConstants.createStyledTextArea(20, 50);
        orderDetailsArea.setEditable(false);
        
        // Action buttons
        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        backButton = SwingUIConstants.createPrimaryButton("Back to Store");
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
        
        // Title and back button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        welcomeLabel.setForeground(Color.WHITE);
        titlePanel.add(welcomeLabel, BorderLayout.WEST);
        titlePanel.add(backButton, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Summary
        summaryLabel.setForeground(Color.WHITE);
        summaryLabel.setFont(SwingUIConstants.SUBTITLE_FONT);
        headerPanel.add(summaryLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Split pane for order list and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.4);
        
        // Left side - Order list
        JPanel leftPanel = createOrderListPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // Right side - Order details
        JPanel rightPanel = createOrderDetailsPanel();
        splitPane.setRightComponent(rightPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createOrderListPanel() {
        JPanel panel = SwingUIConstants.createSectionPanel("Your Orders");
        panel.setPreferredSize(new Dimension(430, 0));
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.setOpaque(false);
        controlsPanel.add(refreshButton);
        
        panel.add(controlsPanel, BorderLayout.NORTH);
        
        // Order list with scroll
        JScrollPane scrollPane = new JScrollPane(orderList);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Click on an order to view details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.SMALL_FONT,
            SwingUIConstants.TEXT_SECONDARY
        ));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Instructions
        JLabel instructionLabel = SwingUIConstants.createSecondaryLabel(
            "Tip: Your most recent orders appear at the top"
        );
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(instructionLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrderDetailsPanel() {
        JPanel panel = SwingUIConstants.createSectionPanel("Order Details");
        
        JScrollPane scrollPane = new JScrollPane(orderDetailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Detailed order information and tracking",
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
            "Need help with an order? Contact our support team for assistance."
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
        refreshButton.addActionListener(e -> refreshOrders());
        backButton.addActionListener(e -> orderHistoryListener.onBackToStore());
    }
    
    private void handleOrderSelection() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentOrders.size()) {
            Order order = currentOrders.get(selectedIndex);
            displayOrderDetails(order);
        }
    }
    
    private void displayOrderDetails(Order order) {
        // Get full order details including items
        Order fullOrder = orderDAO.getOrderById(order.getOrderId());
        
        if (fullOrder == null) {
            orderDetailsArea.setText("Error loading order details\\n\\nPlease try refreshing or contact support if the problem persists.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        
        // Order header with status indicator
        details.append("═══════════════════════════════════════\\n");
        details.append("           ORDER #").append(fullOrder.getOrderId()).append("\\n");
        details.append("══════════════════════════════════════\\n\\n");
        
        // Status with visual indicator
        details.append("ORDER STATUS\\n");
        details.append("─────────────────────\\n");
        details.append("Current Status: ").append(formatStatusWithIcon(fullOrder.getStatus())).append("\\n");
        details.append("Order Date: ").append(formatDateTime(fullOrder.getOrderDate())).append("\\n");
        details.append("Total Amount: ").append(fullOrder.getFormattedTotal()).append("\\n\\n");
        
        // Delivery information
        details.append("DELIVERY INFORMATION\\n");
        details.append("─────────────────────\\n");
        details.append("Delivery Address:\\n").append(fullOrder.getDeliveryAddress()).append("\\n");
        if (fullOrder.getNotes() != null && !fullOrder.getNotes().trim().isEmpty()) {
            details.append("\\nSpecial Instructions:\\n").append(fullOrder.getNotes()).append("\\n");
        }
        details.append("\\n");
        
        // Order items with better formatting
        details.append("YOUR ORDER\\n");
        details.append("─────────────────────\\n");
        
        if (fullOrder.getItems() != null && !fullOrder.getItems().isEmpty()) {
            for (int i = 0; i < fullOrder.getItems().size(); i++) {
                OrderItem item = fullOrder.getItems().get(i);
                details.append(String.format("%d. %s\\n", i + 1, item.getProductName()));
                details.append(String.format("   Quantity: %d\\n", item.getQuantity()));
                details.append(String.format("   Price: %s%s\\n", 
                    fullOrder.getCurrencySymbol(), item.getTotalPrice().toString()));
                
                if (item.getCustomizations() != null && !item.getCustomizations().trim().isEmpty()) {
                    details.append("   Customizations: ").append(item.getCustomizations()).append("\\n");
                }
                details.append("\\n");
            }
        } else {
            details.append("No items found for this order.\\n\\n");
        }
        
        // Order timeline
        details.append("ORDER TIMELINE\\n");
        details.append("─────────────────────\\n");
        details.append("Order Placed: ").append(formatDateTime(fullOrder.getCreatedAt())).append("\\n");
        if (fullOrder.getUpdatedAt() != null && !fullOrder.getUpdatedAt().equals(fullOrder.getCreatedAt())) {
            details.append("Last Updated: ").append(formatDateTime(fullOrder.getUpdatedAt())).append("\\n");
        }
        
        // Status-specific information
        details.append("\\nWHAT'S NEXT?\\n");
        details.append("─────────────────────\\n");
        switch (fullOrder.getStatus()) {
            case PENDING:
                details.append("Your order is being reviewed. You'll receive confirmation shortly.\\n");
                break;
            case CONFIRMED:
                details.append("Your order has been confirmed and will begin preparation soon.\\n");
                break;
            case PREPARING:
                details.append("Our kitchen is preparing your delicious order right now!\\n");
                break;
            case READY:
                details.append("Your order is ready! It will be delivered to you soon.\\n");
                break;
            case DELIVERED:
                details.append("Your order has been delivered. We hope you enjoyed it!\\n");
                break;
            case CANCELLED:
                details.append("This order was cancelled. If you have questions, please contact support.\\n");
                break;
        }
        
        orderDetailsArea.setText(details.toString());
        orderDetailsArea.setCaretPosition(0); // Scroll to top
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' h:mm a"));
    }
    
    private String formatStatusWithIcon(Order.Status status) {
        switch (status) {
            case PENDING: return "Pending Review";
            case CONFIRMED: return "Confirmed";
            case PREPARING: return "Being Prepared";
            case READY: return "Ready for Delivery";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return status.toString();
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Order History for " + user.getName());
        refreshOrders();
    }
    
    public void refreshOrders() {
        if (currentUser == null) return;
        
        currentOrders = orderDAO.getUserOrders(currentUser.getUserId());
        
        orderListModel.clear();
        
        if (currentOrders.isEmpty()) {
            orderListModel.addElement("No orders found");
            orderDetailsArea.setText("Welcome to Neo's Burritos!\\n\\n" +
                                   "You haven't placed any orders yet.\\n\\n" +
                                   "Ready to try our delicious burritos?\\n" +
                                   "Click 'Back to Store' to start shopping!\\n\\n" +
                                   "Your order history will appear here once you\\n" +
                                   "place your first order.");
            summaryLabel.setText("No orders yet - Start shopping to see your history here!");
        } else {
            for (Order order : currentOrders) {
                String statusIcon = getStatusIcon(order.getStatus());
                String displayText = String.format("%s Order #%d - %s - %s (%s)",
                        statusIcon,
                        order.getOrderId(),
                        order.getFormattedTotal(),
                        formatStatusWithIcon(order.getStatus()),
                        formatDateTime(order.getOrderDate()));
                orderListModel.addElement(displayText);
            }
            
            orderDetailsArea.setText("Select an order from the list to view detailed information\\n\\n" +
                                   "You can see order status, delivery details, items ordered,\\n" +
                                   "and track your order progress here.");
            
            // Update summary
            long completedOrders = currentOrders.stream()
                    .mapToLong(order -> order.getStatus() == Order.Status.DELIVERED ? 1 : 0)
                    .sum();
            long activeOrders = currentOrders.stream()
                    .mapToLong(order -> order.getStatus() != Order.Status.DELIVERED && 
                                       order.getStatus() != Order.Status.CANCELLED ? 1 : 0)
                    .sum();
            
            summaryLabel.setText(String.format(
                "%d Total Orders | %d Active | %d Completed", 
                currentOrders.size(), activeOrders, completedOrders));
        }
        
        System.out.println("Refreshed order history: " + currentOrders.size() + " orders for user " + currentUser.getUserId());
    }
    
    private String getStatusIcon(Order.Status status) {
        switch (status) {
            case PENDING: return "⏳";
            case CONFIRMED: return "✅";
            case PREPARING: return "👨‍🍳";
            case READY: return "📦";
            case DELIVERED: return "🎉";
            case CANCELLED: return "❌";
            default: return "📋";
        }
    }
}