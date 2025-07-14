package com.neosburritos;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;
import com.neosburritos.service.PaymentService;
import com.neosburritos.ui.AdminPanel;
import com.neosburritos.ui.CartPanel;
import com.neosburritos.ui.CheckoutPanel;
import com.neosburritos.ui.LoginPanel;
import com.neosburritos.ui.OrderHistoryPanel;
import com.neosburritos.ui.StorePanel;
import com.neosburritos.ui.UIConstants;
import com.neosburritos.util.DatabaseConnection;

/**
 * Main application entry point for Neo's Burritos Online Store
 * Coordinates between UI components and business logic
 * Follows single responsibility principle - acts as application coordinator
 */
public class NeosApp extends Frame implements 
    LoginPanel.LoginListener,
    StorePanel.StoreListener,
    CartPanel.CartListener,
    CheckoutPanel.CheckoutListener,
    OrderHistoryPanel.OrderHistoryListener,
    AdminPanel.AdminListener {
    
    // DAOs and Services
    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final OrderDAO orderDAO;
    private final PaymentService paymentService;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    
    // UI Components
    private CardLayout cardLayout;
    private Panel mainPanel;
    private LoginPanel loginPanel;
    private StorePanel storePanel;
    private CartPanel cartPanel;
    private CheckoutPanel checkoutPanel;
    private OrderHistoryPanel orderHistoryPanel;
    private AdminPanel adminPanel;
    
    public NeosApp() {
        super("Neo's Burritos - Professional Online Store");
        
        // Initialize DAOs and services
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        this.paymentService = new PaymentService();
        
        // Setup UI
        initializeUI();
        setupEventHandlers();
        
        // Test database connection
        testDatabaseConnection();
        
        System.out.println("Neo's Burritos Professional application started");
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(UIConstants.MAIN_WINDOW_WIDTH, UIConstants.MAIN_WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Create card layout for different views
        cardLayout = new CardLayout();
        mainPanel = new Panel(cardLayout);
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Create panels
        createPanels();
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(storePanel, "STORE");
        mainPanel.add(cartPanel, "CART");
        mainPanel.add(checkoutPanel, "CHECKOUT");
        mainPanel.add(orderHistoryPanel, "ORDERS");
        mainPanel.add(adminPanel, "ADMIN");
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Show login panel initially
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    private void createPanels() {
        // Create all UI panels
        loginPanel = new LoginPanel(this, userDAO, this);
        storePanel = new StorePanel(this, productDAO, cartDAO, this);
        cartPanel = new CartPanel(this, cartDAO, this);
        checkoutPanel = new CheckoutPanel(this, orderDAO, cartDAO, paymentService, this);
        orderHistoryPanel = new OrderHistoryPanel(this, orderDAO, this);
        adminPanel = new AdminPanel(this, orderDAO, productDAO, userDAO, this);
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }
    
    private void testDatabaseConnection() {
        try {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            if (connected) {
                System.out.println("Database connection test successful");
            } else {
                System.err.println("Database connection test failed");
                showError("Database connection failed. Please check your MySQL server.");
            }
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            showError("Database error: " + e.getMessage());
        }
    }
    
    // LoginPanel.LoginListener implementation
    @Override
    public void onLoginSuccess(User user) {
        this.currentUser = user;
        System.out.println("User logged in: " + user.getName() + " (" + user.getRole() + ")");
        
        // Initialize panels with user data
        storePanel.setCurrentUser(user);
        cartPanel.setCurrentUser(user, currentCurrency);
        checkoutPanel.setCurrentUser(user, currentCurrency);
        orderHistoryPanel.setCurrentUser(user);
        
        // Navigate to appropriate panel based on role
        if (user.getRole() == User.Role.ADMIN) {
            adminPanel.setCurrentUser(user);
            cardLayout.show(mainPanel, "ADMIN");
        } else {
            cardLayout.show(mainPanel, "STORE");
        }
    }
    
    @Override
    public void onLoginFailure(String message) {
        System.out.println("Login failed: " + message);
        // LoginPanel handles error display, no additional action needed
    }
    
    // StorePanel.StoreListener implementation
    @Override
    public void onViewCart() {
        cartPanel.refreshCart();
        cardLayout.show(mainPanel, "CART");
    }
    
    @Override
    public void onViewOrderHistory() {
        orderHistoryPanel.refreshOrders();
        cardLayout.show(mainPanel, "ORDERS");
    }
    
    @Override
    public void onLogout() {
        handleLogout();
    }
    
    @Override
    public void onCartUpdated(int itemCount) {
        // Cart count is handled by individual panels
        System.out.println("Cart updated: " + itemCount + " items");
    }
    
    // CartPanel.CartListener implementation
    @Override
    public void onContinueShopping() {
        storePanel.refreshData();
        cardLayout.show(mainPanel, "STORE");
    }
    
    @Override
    public void onProceedToCheckout() {
        checkoutPanel.updateOrderSummary();
        cardLayout.show(mainPanel, "CHECKOUT");
    }
    
    @Override
    public void onCartUpdated() {
        // Refresh store panel cart count
        storePanel.refreshData();
    }
    
    // CheckoutPanel.CheckoutListener implementation
    @Override
    public void onBackToCart() {
        cartPanel.refreshCart();
        cardLayout.show(mainPanel, "CART");
    }
    
    @Override
    public void onOrderPlaced(int orderId) {
        // Refresh all panels and go to store
        storePanel.refreshData();
        cartPanel.refreshCart();
        orderHistoryPanel.refreshOrders();
        cardLayout.show(mainPanel, "STORE");
    }
    
    @Override
    public void onCheckoutCancelled() {
        cardLayout.show(mainPanel, "CART");
    }
    
    // OrderHistoryPanel.OrderHistoryListener implementation
    @Override
    public void onBackToStore() {
        storePanel.refreshData();
        cardLayout.show(mainPanel, "STORE");
    }
    
    // AdminPanel.AdminListener implementation (reusing onBackToStore)

    private void handleLogout() {
        currentUser = null;
        loginPanel.clearForm();
        cardLayout.show(mainPanel, "LOGIN");
        System.out.println("User logged out");
    }
    
    private void handleExit() {
        System.out.println("Application shutting down");
        DatabaseConnection.getInstance().closeConnection();
        System.exit(0);
    }
    
    private void showError(String message) {
        Dialog dialog = new Dialog(this, "Error", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(UIConstants.SURFACE_COLOR);
        
        Label messageLabel = new Label(message, Label.CENTER);
        messageLabel.setFont(UIConstants.BODY_FONT);
        messageLabel.setForeground(UIConstants.TEXT_PRIMARY);
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        Button okButton = UIConstants.createPrimaryButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        Panel buttonPanel = new Panel(new FlowLayout());
        buttonPanel.setBackground(UIConstants.SURFACE_COLOR);
        buttonPanel.add(okButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        
        EventQueue.invokeLater(() -> {
            try {
                NeosApp app = new NeosApp();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to start application: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}