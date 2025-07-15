package com.neosburritos.ui.swing;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.dao.OrderDAO;
import com.neosburritos.model.CartItem;
import com.neosburritos.model.User;
import com.neosburritos.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Simplified Swing Checkout Panel with streamlined order placement
 * Single responsibility: Order placement and payment processing
 */
public class SwingCheckoutPanel extends JPanel {
    
    public interface CheckoutListener {
        void onBackToCart();
        void onOrderPlaced(int orderId);
        void onCheckoutCancelled();
    }
    
    private final JFrame parentFrame;
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final PaymentService paymentService;
    private final CheckoutListener checkoutListener;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    private List<CartItem> cartItems;
    private BigDecimal orderTotal = BigDecimal.ZERO;
    
    // UI Components
    private JLabel titleLabel;
    private JLabel orderSummaryLabel;
    private JTextArea orderItemsArea;
    private JTextField deliveryAddressField;
    private JTextArea orderNotesArea;
    private JButton backButton;
    private JButton placeOrderButton;
    private JButton cancelButton;
    
    public SwingCheckoutPanel(JFrame parentFrame, OrderDAO orderDAO, CartDAO cartDAO, 
                             PaymentService paymentService, CheckoutListener checkoutListener) {
        this.parentFrame = parentFrame;
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.paymentService = paymentService;
        this.checkoutListener = checkoutListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Header components
        titleLabel = SwingUIConstants.createTitleLabel("💳 Checkout");
        orderSummaryLabel = SwingUIConstants.createSubtitleLabel("Order Summary: $0.00");
        orderSummaryLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        
        // Order items display
        orderItemsArea = SwingUIConstants.createStyledTextArea(6, 50);
        orderItemsArea.setEditable(false);
        
        // Delivery information
        deliveryAddressField = SwingUIConstants.createStyledTextField(40);
        orderNotesArea = SwingUIConstants.createStyledTextArea(3, 40);
        
        // Action buttons
        backButton = SwingUIConstants.createSecondaryButton("← Back to Cart");
        placeOrderButton = SwingUIConstants.createSuccessButton("Place Order & Pay");
        placeOrderButton.setPreferredSize(SwingUIConstants.LARGE_BUTTON_SIZE);
        cancelButton = SwingUIConstants.createDangerButton("Cancel");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JScrollPane mainScrollPane = new JScrollPane(createMainPanel());
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(mainScrollPane, BorderLayout.CENTER);
        
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
        
        // Title and summary
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        orderSummaryLabel.setForeground(Color.WHITE);
        titlePanel.add(orderSummaryLabel, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Order Review Section
        mainPanel.add(createOrderReviewSection());
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_LARGE));
        
        // Delivery Information Section
        mainPanel.add(createDeliverySection());
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_LARGE));
        
        // Payment Information Section
        mainPanel.add(createPaymentInfoSection());
        
        return mainPanel;
    }
    
    private JPanel createOrderReviewSection() {
        JPanel section = SwingUIConstants.createSectionPanel("📋 Order Review");
        
        JScrollPane scrollPane = new JScrollPane(orderItemsArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Items in your order"));
        
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createDeliverySection() {
        JPanel section = SwingUIConstants.createSectionPanel("🚚 Delivery Information");
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL,
                               SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL);
        
        // Delivery address
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(SwingUIConstants.createBodyLabel("Delivery Address: *"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(deliveryAddressField, gbc);
        
        // Order notes
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(SwingUIConstants.createBodyLabel("Special Instructions:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        JScrollPane notesScrollPane = new JScrollPane(orderNotesArea);
        notesScrollPane.setPreferredSize(new Dimension(0, 80));
        formPanel.add(notesScrollPane, gbc);
        
        section.add(formPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createPaymentInfoSection() {
        JPanel section = SwingUIConstants.createSectionPanel("��� Payment Information");
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        JLabel paymentInfo = SwingUIConstants.createBodyLabel(
            "<html><div style='text-align: center;'>" +
            "🔒 Secure Payment Processing<br/>" +
            "Your payment will be processed securely when you place your order.<br/>" +
            "Multiple payment methods accepted at checkout." +
            "</div></html>"
        );
        paymentInfo.setHorizontalAlignment(SwingConstants.CENTER);
        paymentInfo.setForeground(SwingUIConstants.TEXT_SECONDARY);
        
        infoPanel.add(paymentInfo, BorderLayout.CENTER);
        section.add(infoPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, SwingUIConstants.PADDING_LARGE, 0));
        navPanel.setOpaque(false);
        
        navPanel.add(backButton);
        navPanel.add(placeOrderButton);
        navPanel.add(cancelButton);
        
        footerPanel.add(navPanel, BorderLayout.CENTER);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Navigation buttons
        backButton.addActionListener(e -> checkoutListener.onBackToCart());
        cancelButton.addActionListener(e -> checkoutListener.onCheckoutCancelled());
        placeOrderButton.addActionListener(this::handlePlaceOrder);
    }
    
    private void handlePlaceOrder(ActionEvent e) {
        // Validate inputs
        String deliveryAddress = deliveryAddressField.getText().trim();
        if (deliveryAddress.isEmpty()) {
            SwingUIConstants.showWarningDialog(parentFrame, 
                "Please enter a delivery address.", "Missing Information");
            deliveryAddressField.requestFocus();
            return;
        }
        
        // Confirm order
        boolean confirmed = SwingUIConstants.showConfirmDialog(parentFrame,
            "Place order for " + orderSummaryLabel.getText().split(": ")[1] + "?",
            "Confirm Order");
        
        if (!confirmed) return;
        
        // Disable button and show processing
        placeOrderButton.setEnabled(false);
        placeOrderButton.setText("Processing Payment...");
        
        // Process order in background
        SwingWorker<OrderDAO.OrderResult, Void> worker = new SwingWorker<OrderDAO.OrderResult, Void>() {
            @Override
            protected OrderDAO.OrderResult doInBackground() throws Exception {
                String orderNotes = orderNotesArea.getText().trim();
                return orderDAO.createOrderFromCart(
                    currentUser.getUserId(),
                    currentCurrency,
                    deliveryAddress,
                    orderNotes.isEmpty() ? null : orderNotes
                );
            }
            
            @Override
            protected void done() {
                try {
                    OrderDAO.OrderResult orderResult = get();
                    
                    if (orderResult.isSuccess()) {
                        // Process payment (simplified)
                        PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                            orderResult.getOrderId(), orderTotal, currentCurrency);
                        
                        if (paymentResult.isSuccess()) {
                            SwingUIConstants.showSuccessDialog(parentFrame,
                                "Order placed and payment processed successfully!\n" +
                                "Order ID: " + orderResult.getOrderId() + "\n" +
                                "Transaction ID: " + paymentResult.getTransactionId(),
                                "Order Confirmed");
                            
                            clearForm();
                            checkoutListener.onOrderPlaced(orderResult.getOrderId());
                        } else {
                            SwingUIConstants.showErrorDialog(parentFrame,
                                "Payment failed: " + paymentResult.getMessage(),
                                "Payment Failed");
                        }
                    } else {
                        SwingUIConstants.showErrorDialog(parentFrame,
                            "Failed to create order: " + orderResult.getMessage(),
                            "Order Failed");
                    }
                } catch (Exception ex) {
                    SwingUIConstants.showErrorDialog(parentFrame,
                        "Error processing order: " + ex.getMessage(),
                        "Error");
                } finally {
                    placeOrderButton.setEnabled(true);
                    placeOrderButton.setText("Place Order & Pay");
                }
            }
        };
        worker.execute();
    }
    
    public void setCurrentUser(User user, String currency) {
        this.currentUser = user;
        this.currentCurrency = currency;
        
        // Pre-fill delivery address if available
        if (user.getAddress() != null && !user.getAddress().trim().isEmpty()) {
            deliveryAddressField.setText(user.getAddress());
        }
    }
    
    public void updateOrderSummary() {
        if (currentUser == null) return;
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                cartItems = cartDAO.getCartItems(currentUser.getUserId(), currentCurrency);
                orderTotal = cartDAO.getCartTotal(currentUser.getUserId(), currentCurrency);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    displayOrderSummary();
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(parentFrame,
                        "Failed to load order summary: " + e.getMessage(),
                        "Error");
                }
            }
        };
        worker.execute();
    }
    
    private void displayOrderSummary() {
        // Update summary label
        String currencySymbol = cartItems.isEmpty() ? "$" : cartItems.get(0).getCurrencySymbol();
        orderSummaryLabel.setText("Order Summary: " + currencySymbol + orderTotal.toString());
        
        // Update items display
        StringBuilder itemsText = new StringBuilder();
        for (CartItem item : cartItems) {
            itemsText.append("• ").append(item.getProductName())
                    .append(" (x").append(item.getQuantity()).append(")")
                    .append(" - ").append(item.getFormattedTotalPrice())
                    .append("\n");
            
            if (item.getCustomizations() != null && !item.getCustomizations().trim().isEmpty()) {
                itemsText.append("  Customizations: ").append(item.getCustomizations()).append("\n");
            }
            itemsText.append("\n");
        }
        
        if (itemsText.length() == 0) {
            itemsText.append("No items in cart");
        }
        
        orderItemsArea.setText(itemsText.toString());
        orderItemsArea.setCaretPosition(0);
        
        // Enable/disable place order button
        placeOrderButton.setEnabled(!cartItems.isEmpty());
    }
    
    private void clearForm() {
        deliveryAddressField.setText("");
        orderNotesArea.setText("");
    }
}