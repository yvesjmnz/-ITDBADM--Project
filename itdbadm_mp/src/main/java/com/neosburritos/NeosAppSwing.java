package com.neosburritos;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;
import com.neosburritos.service.PaymentService;
import com.neosburritos.ui.swing.SwingAdminPanel;
import com.neosburritos.ui.swing.SwingCartPanel;
import com.neosburritos.ui.swing.SwingCheckoutPanel;
import com.neosburritos.ui.swing.SwingLoginPanel;
import com.neosburritos.ui.swing.SwingRegisterPanel;
import com.neosburritos.ui.swing.SwingOrderHistoryPanel;
import com.neosburritos.ui.swing.SwingStorePanel;
import com.neosburritos.ui.swing.SwingUIConstants;
import com.neosburritos.util.DatabaseConnection;


/**
 * Modern Swing-based main application for Neo's Burritos Online Store
 * Replaces AWT with Swing for better UI experience
 */
public class NeosAppSwing extends JFrame implements 
    SwingLoginPanel.LoginListener,
    SwingRegisterPanel.RegisterListener,
    SwingStorePanel.StoreListener,
    SwingCartPanel.CartListener,
    SwingCheckoutPanel.CheckoutListener,
    SwingOrderHistoryPanel.OrderHistoryListener,
    SwingAdminPanel.AdminListener {
    
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
    private JPanel mainPanel;
    private SwingLoginPanel loginPanel;
    private SwingRegisterPanel registerPanel;
    private SwingStorePanel storePanel;
    private SwingCartPanel cartPanel;
    private SwingCheckoutPanel checkoutPanel;
    private SwingOrderHistoryPanel orderHistoryPanel;
    private SwingAdminPanel adminPanel;
    
    public NeosAppSwing() {
        super("Neo's Burritos - Modern Online Store");
        
        // Initialize DAOs and services
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        this.paymentService = new PaymentService();
        
        // Setup modern look and feel
        setupLookAndFeel();
        
        // Setup UI
        initializeUI();
        setupEventHandlers();
        
        // Test database connection
        testDatabaseConnection();
        
        System.out.println("Neo's Burritos Modern Swing application started");
    }
    
    private void setupLookAndFeel() {
        try {
            // Use system look and feel for native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set custom UI defaults for better appearance
            UIManager.put("Button.font", SwingUIConstants.BODY_FONT);
            UIManager.put("Label.font", SwingUIConstants.BODY_FONT);
            UIManager.put("TextField.font", SwingUIConstants.BODY_FONT);
            UIManager.put("TextArea.font", SwingUIConstants.BODY_FONT);
            UIManager.put("List.font", SwingUIConstants.BODY_FONT);
            UIManager.put("ComboBox.font", SwingUIConstants.BODY_FONT);
            
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(SwingUIConstants.MAIN_WINDOW_WIDTH, SwingUIConstants.MAIN_WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Create card layout for different views
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Create panels
        createPanels();
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registerPanel, "REGISTER");
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
        // Create all UI panels with Swing components
        loginPanel = new SwingLoginPanel(this, userDAO, this);
        registerPanel = new SwingRegisterPanel(this, userDAO, this);
        storePanel = new SwingStorePanel(this, productDAO, cartDAO, this);
        cartPanel = new SwingCartPanel(this, cartDAO, this);
        checkoutPanel = new SwingCheckoutPanel(this, orderDAO, cartDAO, paymentService, this);
        orderHistoryPanel = new SwingOrderHistoryPanel(this, orderDAO, this);
        adminPanel = new SwingAdminPanel(this, orderDAO, productDAO, userDAO, this);
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
    @Override
    public void onRegisterRequest() {
        cardLayout.show(mainPanel, "REGISTER");
    }

    

    // RegisterPanel.RegisterListener implementation
    @Override
    public void onRegisterSuccess() {
        loginPanel.clearForm();
        cardLayout.show(mainPanel, "LOGIN");
        SwingUIConstants.showInfoDialog(this, "Registration successful!", "Welcome!");
    }

    @Override
    public void onBackToLogin() {
        loginPanel.clearForm();
        cardLayout.show(mainPanel, "LOGIN");
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
        System.out.println("Cart updated: " + itemCount + " items");
    }
    
    @Override
    public void onCurrencyChanged(String newCurrency) {
        this.currentCurrency = newCurrency;
        System.out.println("Currency changed to: " + newCurrency);
        
        // Update all panels with new currency
        if (currentUser != null) {
            cartPanel.setCurrentUser(currentUser, newCurrency);
            checkoutPanel.setCurrentUser(currentUser, newCurrency);
        }
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
    
    // // AdminPanel.AdminListener implementation
    // @Override
    // public void onBackToStore() {
    //     storePanel.refreshData();
    //     cardLayout.show(mainPanel, "STORE");
    // }

    private void handleLogout() {
        currentUser = null;
        loginPanel.clearForm();
        cardLayout.show(mainPanel, "LOGIN");
        System.out.println("User logged out");
    }
    
    private void handleExit() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Application",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            System.out.println("Application shutting down");
            DatabaseConnection.getInstance().closeConnection();
            System.exit(0);
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public static void main(String[] args) {
        // Enable anti-aliasing for better text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                NeosAppSwing app = new NeosAppSwing();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to start application: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}