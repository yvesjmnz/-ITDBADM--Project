package com.neosburritos.ui.swing;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.model.Product;
import com.neosburritos.model.User;
import com.neosburritos.NeosAppSwing;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Enhanced Swing Store Panel with product browsing, currency selection, and cart functionality
 * Single responsibility: Product display and cart interaction
 */
public class SwingStorePanel extends JPanel {
    
    public interface StoreListener {
        void onViewCart();
        void onViewOrderHistory();
        void onLogout();
        void onCartUpdated(int itemCount);
        void onCurrencyChanged(String newCurrency);
    }
    
    private final JFrame parentFrame;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final StoreListener storeListener;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    private List<Product> currentProducts;
    private Product.Category selectedCategory = null;
    
    // UI Components
    private JLabel welcomeLabel;
    private JLabel cartCountLabel;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> currencyComboBox;
    private JPanel productsPanel;
    private JScrollPane productsScrollPane;
    private JButton refreshButton;
    private JButton cartButton;
    private JButton ordersButton;
    private JButton logoutButton;
    
    public SwingStorePanel(JFrame parentFrame, ProductDAO productDAO, CartDAO cartDAO, StoreListener storeListener) {
        this.parentFrame = parentFrame;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
        this.storeListener = storeListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Header components
        welcomeLabel = SwingUIConstants.createTitleLabel("Neo's Burritos Store");
        cartCountLabel = SwingUIConstants.createBodyLabel("Cart: 0 items");
        cartCountLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        
        // Category filter
        String[] categories = {"All Categories", "BURRITO", "BOWL", "DRINK", "SIDE"};
        categoryComboBox = SwingUIConstants.createStyledComboBox(categories);
        
        // Currency selector
        String[] currencies = {"USD", "PHP", "KRW"};
        currencyComboBox = SwingUIConstants.createStyledComboBox(currencies);
        currencyComboBox.setSelectedItem(currentCurrency);
        
        // Products panel with better layout
        productsPanel = new JPanel();
        productsPanel.setLayout(new GridLayout(0, 3, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        productsPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        productsScrollPane = new JScrollPane(productsPanel);
        productsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        productsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        productsScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Our Delicious Menu",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.HEADER_FONT,
            SwingUIConstants.TEXT_PRIMARY
        ));
        
        // Action buttons
        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        cartButton = SwingUIConstants.createPrimaryButton("View Cart");
        ordersButton = SwingUIConstants.createSecondaryButton("Order History");
        logoutButton = SwingUIConstants.createDangerButton("Logout");
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
        
        // Title and cart info
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        welcomeLabel.setForeground(Color.WHITE);
        titlePanel.add(welcomeLabel, BorderLayout.WEST);
        
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setFont(SwingUIConstants.SUBTITLE_FONT);
        titlePanel.add(cartCountLabel, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Filter and currency panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        // Category filter
        JLabel filterLabel = SwingUIConstants.createBodyLabel("Category:");
        filterLabel.setForeground(Color.WHITE);
        filterPanel.add(filterLabel);
        filterPanel.add(categoryComboBox);
        
        // Add spacing
        filterPanel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_LARGE));
        
        // Currency selector
        JLabel currencyLabel = SwingUIConstants.createBodyLabel("Currency:");
        currencyLabel.setForeground(Color.WHITE);
        filterPanel.add(currencyLabel);
        filterPanel.add(currencyComboBox);
        
        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Products scroll pane
        mainPanel.add(productsScrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_MEDIUM));
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        footerPanel.add(refreshButton);
        footerPanel.add(cartButton);
        footerPanel.add(ordersButton);
        footerPanel.add(logoutButton);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Category filter
        categoryComboBox.addActionListener(this::handleCategoryChange);
        
        // Currency selector
        currencyComboBox.addActionListener(this::handleCurrencyChange);
        
        // Action buttons
        refreshButton.addActionListener(e -> refreshData());
        cartButton.addActionListener(e -> storeListener.onViewCart());
        ordersButton.addActionListener(e -> storeListener.onViewOrderHistory());
        logoutButton.addActionListener(e -> storeListener.onLogout());
    }
    
    private void handleCategoryChange(ActionEvent e) {
        String selectedItem = (String) categoryComboBox.getSelectedItem();
        if ("All Categories".equals(selectedItem)) {
            selectedCategory = null;
        } else {
            selectedCategory = Product.Category.valueOf(selectedItem);
        }
        loadProducts();
    }
    
    private void handleCurrencyChange(ActionEvent e) {
        String newCurrency = (String) currencyComboBox.getSelectedItem();
        if (!newCurrency.equals(currentCurrency)) {
            currentCurrency = newCurrency;
            loadProducts();
            updateCartCount();
            // Notify the main application about currency change
            if (storeListener instanceof NeosAppSwing) {
                ((NeosAppSwing) storeListener).onCurrencyChanged(newCurrency);
            }
        }
    }
    
    private void loadProducts() {
        // Load products in background thread
        SwingWorker<List<Product>, Void> worker = new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productDAO.getProductsByCurrency(currentCurrency, selectedCategory);
            }
            
            @Override
            protected void done() {
                try {
                    currentProducts = get();
                    displayProducts();
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(parentFrame, 
                        "Failed to load products: " + e.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }
    
    private void displayProducts() {
        productsPanel.removeAll();
        
        if (currentProducts == null || currentProducts.isEmpty()) {
            JLabel noProductsLabel = SwingUIConstants.createHeaderLabel("No products available");
            noProductsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            productsPanel.add(noProductsLabel);
        } else {
            for (Product product : currentProducts) {
                JPanel productCard = createProductCard(product);
                productsPanel.add(productCard);
            }
        }
        
        productsPanel.revalidate();
        productsPanel.repaint();
    }
    
    private JPanel createProductCard(Product product) {
        JPanel card = SwingUIConstants.createCardPanel();
        card.setLayout(new BorderLayout(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        card.setPreferredSize(new Dimension(280, 220));
        card.setMaximumSize(new Dimension(280, 220));
        
        // Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        // Product name
        JLabel nameLabel = SwingUIConstants.createHeaderLabel(product.getName());
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(nameLabel);
        
        infoPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_SMALL));
        
        // Product description
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            JTextArea descArea = new JTextArea(product.getDescription());
            descArea.setFont(SwingUIConstants.SMALL_FONT);
            descArea.setForeground(SwingUIConstants.TEXT_SECONDARY);
            descArea.setOpaque(false);
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setRows(2);
            descArea.setMaximumSize(new Dimension(260, 40));
            infoPanel.add(descArea);
        }
        
        infoPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_SMALL));
        
        // Price with proper currency formatting
        String currencySymbol = getCurrencySymbol(currentCurrency);
        String formattedPrice = currencySymbol + product.getPriceInBigDecimal().toString();
        JLabel priceLabel = SwingUIConstants.createBodyLabel(formattedPrice);
        priceLabel.setFont(SwingUIConstants.SUBTITLE_FONT);
        priceLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(priceLabel);
        
        JLabel stockLabel = SwingUIConstants.createSecondaryLabel("Stock: " + product.getStockQuantity());
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(stockLabel);
        
        if (product.isCustomizable()) {
            JLabel customLabel = SwingUIConstants.createSecondaryLabel("Customizable");
            customLabel.setForeground(SwingUIConstants.SUCCESS_COLOR);
            customLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(customLabel);
        }
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Add to cart button
        JButton addButton = SwingUIConstants.createPrimaryButton("Add to Cart");
        addButton.setEnabled(product.getStockQuantity() > 0);
        addButton.addActionListener(e -> handleAddToCart(product));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "USD": return "$";
            case "PHP": return "₱";
            case "KRW": return "₩";
            default: return "$";
        }
    }
    
    private void handleAddToCart(Product product) {
        if (product.isCustomizable()) {
            // Show customization dialog
            showCustomizationDialog(product);
        } else {
            // Add directly to cart
            addToCart(product, 1, null);
        }
    }
    
    private void showCustomizationDialog(Product product) {
        ProductCustomizationDialog dialog = new ProductCustomizationDialog(parentFrame, product);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // Include additional price information in customizations
            String customizations = dialog.getCustomizations();
            addToCart(product, dialog.getQuantity(), customizations);
        }
    }
    
    private void addToCart(Product product, int quantity, String customizations) {
        // Add to cart in background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return cartDAO.addToCart(currentUser.getUserId(), product.getProductId(), quantity, customizations);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        SwingUIConstants.showSuccessDialog(parentFrame, 
                            "Added " + product.getName() + " to cart!", "Added to Cart");
                        updateCartCount();
                        storeListener.onCartUpdated(cartDAO.getCartItemCount(currentUser.getUserId()));
                    } else {
                        SwingUIConstants.showErrorDialog(parentFrame, 
                            "Failed to add item to cart", "Error");
                    }
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(parentFrame, 
                        "Error adding to cart: " + e.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }
    
    private void updateCartCount() {
        if (currentUser != null) {
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return cartDAO.getCartItemCount(currentUser.getUserId());
                }
                
                @Override
                protected void done() {
                    try {
                        int count = get();
                        cartCountLabel.setText("🛒 Cart: " + count + " items");
                    } catch (Exception e) {
                        System.err.println("Error updating cart count: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + "!");
        refreshData();
    }
    
    public void setCurrentCurrency(String currency) {
        this.currentCurrency = currency;
        currencyComboBox.setSelectedItem(currency);
        if (currentUser != null) {
            loadProducts();
            updateCartCount();
        }
    }
    
    public String getCurrentCurrency() {
        return currentCurrency;
    }
    
    public void refreshData() {
        if (currentUser != null) {
            loadProducts();
            updateCartCount();
        }
    }
}