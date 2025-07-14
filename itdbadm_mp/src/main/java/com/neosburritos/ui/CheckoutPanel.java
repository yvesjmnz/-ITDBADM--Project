package com.neosburritos.ui;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.CartDAO;
import com.neosburritos.model.User;
import com.neosburritos.service.PaymentService;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;

/**
 * Checkout panel for order placement and payment processing
 * Handles delivery information, payment details, and order creation
 */
public class CheckoutPanel extends Panel {
    
    public interface CheckoutListener {
        void onBackToCart();
        void onOrderPlaced(int orderId);
        void onCheckoutCancelled();
    }
    
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final PaymentService paymentService;
    private final CheckoutListener checkoutListener;
    private final Frame parentFrame;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    
    // UI Components
    private TextField deliveryAddressField;
    private TextArea orderNotesArea;
    private Choice paymentMethodChoice;
    private TextField paymentDetailsField;
    private Label paymentRequirementsLabel;
    private Label orderSummaryLabel;
    private Button placeOrderButton;
    
    public CheckoutPanel(Frame parentFrame, OrderDAO orderDAO, CartDAO cartDAO, 
                        PaymentService paymentService, CheckoutListener checkoutListener) {
        this.parentFrame = parentFrame;
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.paymentService = paymentService;
        this.checkoutListener = checkoutListener;
        
        setBackground(UIConstants.BACKGROUND_COLOR);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Delivery address field
        deliveryAddressField = UIConstants.createStyledTextField(40);
        
        // Order notes area
        orderNotesArea = new TextArea(3, 40);
        orderNotesArea.setBackground(UIConstants.SURFACE_COLOR);
        orderNotesArea.setFont(UIConstants.BODY_FONT);
        
        // Payment method choice
        paymentMethodChoice = new Choice();
        for (PaymentService.PaymentMethod method : PaymentService.PaymentMethod.values()) {
            paymentMethodChoice.add(method.getDisplayName());
        }
        paymentMethodChoice.setFont(UIConstants.BODY_FONT);
        
        // Payment details field
        paymentDetailsField = UIConstants.createStyledTextField(40);
        
        // Payment requirements label
        paymentRequirementsLabel = UIConstants.createBodyLabel("");
        paymentRequirementsLabel.setFont(UIConstants.CAPTION_FONT);
        paymentRequirementsLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        // Order summary label
        orderSummaryLabel = UIConstants.createHeaderLabel("Order Summary: ");
        
        // Place order button
        placeOrderButton = UIConstants.createPrimaryButton("Place Order");
        placeOrderButton.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
    }
    
    protected void layoutComponents() {
        // Header
        Panel headerPanel = createHeaderPanel("Checkout", "Back to Cart", 
                                            e -> checkoutListener.onBackToCart());
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form
        Panel formPanel = createCheckoutForm();
        add(formPanel, BorderLayout.CENTER);
    }
    
    private Panel createCheckoutForm() {
        Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Form panel
        Panel formPanel = createFormPanel();
        
        GridBagConstraints gbc = createFormConstraints(0, 0, 1, GridBagConstraints.WEST);
        
        // Delivery Address Section
        Label deliveryLabel = UIConstants.createHeaderLabel("Delivery Information");
        gbc.gridwidth = 3;
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        formPanel.add(deliveryLabel, gbc);
        
        // Delivery address
        gbc = createFormConstraints(0, 1, 1, GridBagConstraints.WEST);
        formPanel.add(UIConstants.createBodyLabel("Delivery Address:"), gbc);
        gbc = createFormConstraints(1, 1, 2, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(deliveryAddressField, gbc);
        
        // Order notes
        gbc = createFormConstraints(0, 2, 1, GridBagConstraints.NORTHWEST);
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(UIConstants.createBodyLabel("Order Notes:"), gbc);
        gbc = createFormConstraints(1, 2, 2, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(orderNotesArea, gbc);
        
        // Payment Section
        gbc = createFormConstraints(0, 3, 3, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        Label paymentLabel = UIConstants.createHeaderLabel("Payment Information");
        formPanel.add(paymentLabel, gbc);
        
        // Payment method
        gbc = createFormConstraints(0, 4, 1, GridBagConstraints.WEST);
        formPanel.add(UIConstants.createBodyLabel("Payment Method:"), gbc);
        gbc = createFormConstraints(1, 4, 1, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(paymentMethodChoice, gbc);
        
        // Payment details
        gbc = createFormConstraints(0, 5, 1, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(UIConstants.createBodyLabel("Payment Details:"), gbc);
        gbc = createFormConstraints(1, 5, 2, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(paymentDetailsField, gbc);
        
        // Payment requirements
        gbc = createFormConstraints(1, 6, 2, GridBagConstraints.WEST);
        formPanel.add(paymentRequirementsLabel, gbc);
        
        // Order Summary Section
        gbc = createFormConstraints(0, 7, 3, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        formPanel.add(orderSummaryLabel, gbc);
        
        // Place order button
        gbc = createFormConstraints(0, 8, 3, GridBagConstraints.CENTER);
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM);
        formPanel.add(placeOrderButton, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        return mainPanel;
    }
    
    protected void setupEventHandlers() {
        paymentMethodChoice.addItemListener(this::handlePaymentMethodChange);
        placeOrderButton.addActionListener(e -> handlePlaceOrder());
        
        // Update payment requirements initially - now safe since paymentService is set
        updatePaymentRequirements();
    }
    
    private void handlePaymentMethodChange(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updatePaymentRequirements();
        }
    }
    
    private void updatePaymentRequirements() {
        String selectedMethod = paymentMethodChoice.getSelectedItem();
        PaymentService.PaymentMethod method = PaymentService.PaymentMethod.valueOf(
            selectedMethod.toUpperCase().replace(" ", "_"));
        
        paymentRequirementsLabel.setText(paymentService.getPaymentMethodRequirements(method));
    }
    
    private void handlePlaceOrder() {
        // Validate delivery address
        String deliveryAddress = deliveryAddressField.getText().trim();
        if (deliveryAddress.isEmpty()) {
            showError("Please enter a delivery address");
            deliveryAddressField.requestFocus();
            return;
        }
        
        // Validate payment details
        String selectedMethod = paymentMethodChoice.getSelectedItem();
        PaymentService.PaymentMethod paymentMethod = PaymentService.PaymentMethod.valueOf(
            selectedMethod.toUpperCase().replace(" ", "_"));
        
        String paymentDetails = paymentDetailsField.getText().trim();
        if (!paymentService.validatePaymentDetails(paymentMethod, paymentDetails)) {
            showError("Please enter valid payment details");
            paymentDetailsField.requestFocus();
            return;
        }
        
        // Disable button to prevent double submission
        placeOrderButton.setEnabled(false);
        placeOrderButton.setLabel("Processing...");
        
        // Process order in background thread
        new Thread(() -> {
            try {
                // Create order
                String orderNotes = orderNotesArea.getText().trim();
                OrderDAO.OrderResult orderResult = orderDAO.createOrderFromCart(
                    currentUser.getUserId(), 
                    currentCurrency, 
                    deliveryAddress, 
                    orderNotes.isEmpty() ? null : orderNotes
                );
                
                if (!orderResult.isSuccess()) {
                    EventQueue.invokeLater(() -> {
                        placeOrderButton.setEnabled(true);
                        placeOrderButton.setLabel("Place Order");
                        showError("Failed to create order: " + orderResult.getMessage());
                    });
                    return;
                }
                
                // Process payment
                BigDecimal total = cartDAO.getCartTotal(currentUser.getUserId(), currentCurrency);
                PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                    orderResult.getOrderId(), total, currentCurrency, paymentMethod, paymentDetails);
                
                EventQueue.invokeLater(() -> {
                    placeOrderButton.setEnabled(true);
                    placeOrderButton.setLabel("Place Order");
                    
                    if (paymentResult.isSuccess()) {
                        // Clear form
                        clearForm();
                        
                        showSuccess("Order placed successfully!\n" +
                                  "Order ID: " + orderResult.getOrderId() + "\n" +
                                  "Transaction ID: " + paymentResult.getTransactionId());
                        
                        checkoutListener.onOrderPlaced(orderResult.getOrderId());
                    } else {
                        showError("Payment failed: " + paymentResult.getMessage());
                    }
                });
                
            } catch (Exception e) {
                EventQueue.invokeLater(() -> {
                    placeOrderButton.setEnabled(true);
                    placeOrderButton.setLabel("Place Order");
                    showError("Order processing failed: " + e.getMessage());
                });
            }
        }).start();
    }
    
    public void setCurrentUser(User user, String currency) {
        this.currentUser = user;
        this.currentCurrency = currency;
        updateOrderSummary();
    }
    
    public void updateOrderSummary() {
        if (currentUser == null) return;
        
        int itemCount = cartDAO.getCartItemCount(currentUser.getUserId());
        BigDecimal total = cartDAO.getCartTotal(currentUser.getUserId(), currentCurrency);
        String currencySymbol = getCurrencySymbol(currentCurrency);
        
        orderSummaryLabel.setText("Order Summary: " + itemCount + " items - " + 
                                currencySymbol + total.toString());
    }
    
    private String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "USD": return "$";
            case "PHP": return "₱";
            case "KRW": return "₩";
            default: return "$";
        }
    }
    
    public void clearForm() {
        deliveryAddressField.setText("");
        orderNotesArea.setText("");
        paymentDetailsField.setText("");
        paymentMethodChoice.select(0);
        updatePaymentRequirements();
    }
    
    // BasePanel utility methods
    protected Panel createHeaderPanel(String title, String backButtonText, java.awt.event.ActionListener backAction) {
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        
        // Title
        Label titleLabel = UIConstants.createTitleLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignment(Label.CENTER);
        
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(UIConstants.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Back button if provided
        if (backButtonText != null && backAction != null) {
            Button backButton = UIConstants.createSecondaryButton(backButtonText);
            backButton.addActionListener(backAction);
            
            Panel backPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
            backPanel.setBackground(UIConstants.PRIMARY_COLOR);
            backPanel.add(backButton);
            headerPanel.add(backPanel, BorderLayout.WEST);
        }
        
        return headerPanel;
    }
    
    protected Panel createFormPanel() {
        Panel formPanel = new Panel(new GridBagLayout());
        formPanel.setBackground(UIConstants.SURFACE_COLOR);
        return formPanel;
    }
    
    protected GridBagConstraints createFormConstraints(int x, int y, int width, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.anchor = anchor;
        gbc.insets = new Insets(UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL);
        return gbc;
    }
    
    protected void showError(String message) {
        showDialog("Error", message, UIConstants.ERROR_COLOR);
    }
    
    protected void showSuccess(String message) {
        showDialog("Success", message, UIConstants.SUCCESS_COLOR);
    }
    
    private void showDialog(String title, String message, Color titleColor) {
        Dialog dialog = new Dialog(parentFrame, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title panel
        Panel titlePanel = new Panel(new FlowLayout());
        titlePanel.setBackground(titleColor);
        Label titleLabel = new Label(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(UIConstants.HEADER_FONT);
        titlePanel.add(titleLabel);
        dialog.add(titlePanel, BorderLayout.NORTH);
        
        // Message
        Label messageLabel = new Label(message, Label.CENTER);
        messageLabel.setFont(UIConstants.BODY_FONT);
        messageLabel.setForeground(UIConstants.TEXT_PRIMARY);
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        // OK button
        Button okButton = UIConstants.createPrimaryButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        Panel buttonPanel = new Panel(new FlowLayout());
        buttonPanel.setBackground(UIConstants.SURFACE_COLOR);
        buttonPanel.add(okButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}