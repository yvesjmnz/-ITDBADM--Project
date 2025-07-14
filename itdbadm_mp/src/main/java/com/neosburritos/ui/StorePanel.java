package com.neosburritos.ui;

import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.CartDAO;
import com.neosburritos.model.Product;
import com.neosburritos.model.User;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * Store panel for product browsing and cart management
 * Handles product display, currency conversion, and add to cart functionality
 */
public class StorePanel extends BasePanel {
    
    public interface StoreListener {
        void onViewCart();
        void onViewOrderHistory();
        void onLogout();
        void onCartUpdated(int itemCount);
    }
    
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final StoreListener storeListener;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    private List<Product> currentProducts;
    
    // UI Components
    private Label userInfoLabel;
    private Label cartCountLabel;
    private Choice currencyChoice;
    private Choice categoryChoice;
    private java.awt.List productList;
    private TextArea productDetailsArea;
    private TextField quantityField;
    private TextArea customizationArea;
    private Button addToCartButton;
    
    public StorePanel(Frame parentFrame, ProductDAO productDAO, CartDAO cartDAO, StoreListener storeListener) {
        super(parentFrame);
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
        this.storeListener = storeListener;
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // User info label
        userInfoLabel = UIConstants.createHeaderLabel("Welcome!");
        userInfoLabel.setForeground(Color.WHITE);
        
        // Cart count label
        cartCountLabel = UIConstants.createBodyLabel("Cart (0)");
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setFont(UIConstants.HEADER_FONT);
        
        // Currency choice
        currencyChoice = new Choice();
        currencyChoice.add("USD");
        currencyChoice.add("PHP");
        currencyChoice.add("KRW");
        currencyChoice.setFont(UIConstants.BODY_FONT);
        
        // Category choice
        categoryChoice = new Choice();
        categoryChoice.add("ALL");
        categoryChoice.add("BURRITO");
        categoryChoice.add("BOWL");
        categoryChoice.add("DRINK");
        categoryChoice.add("SIDE");
        categoryChoice.setFont(UIConstants.BODY_FONT);
        
        // Product list
        productList = new java.awt.List(12);
        productList.setFont(UIConstants.BODY_FONT);
        productList.setBackground(UIConstants.SURFACE_COLOR);
        
        // Product details area
        productDetailsArea = new TextArea(8, 30);
        productDetailsArea.setEditable(false);
        productDetailsArea.setBackground(UIConstants.BACKGROUND_COLOR);
        productDetailsArea.setFont(UIConstants.BODY_FONT);
        
        // Quantity field
        quantityField = UIConstants.createStyledTextField(5);
        quantityField.setText("1");
        
        // Customization area
        customizationArea = new TextArea(3, 25);
        customizationArea.setBackground(UIConstants.SURFACE_COLOR);
        customizationArea.setFont(UIConstants.BODY_FONT);
        
        // Add to cart button
        addToCartButton = UIConstants.createPrimaryButton("Add to Cart");
        addToCartButton.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
    }
    
    @Override
    protected void layoutComponents() {
        // Header panel
        Panel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Left side - Product list and filters
        Panel leftPanel = createProductListPanel();
        contentPanel.add(leftPanel, BorderLayout.CENTER);
        
        // Right side - Product details and add to cart
        Panel rightPanel = createProductDetailsPanel();
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private Panel createHeaderPanel() {
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        
        // Left side - User info and filters
        Panel leftPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(UIConstants.PRIMARY_COLOR);
        
        leftPanel.add(userInfoLabel);
        
        Label separatorLabel = new Label("  |  ");
        separatorLabel.setForeground(Color.WHITE);
        leftPanel.add(separatorLabel);
        
        Label currencyLabel = UIConstants.createBodyLabel("Currency:");
        currencyLabel.setForeground(Color.WHITE);
        leftPanel.add(currencyLabel);
        leftPanel.add(currencyChoice);
        
        Label categoryLabel = UIConstants.createBodyLabel("  Category:");
        categoryLabel.setForeground(Color.WHITE);
        leftPanel.add(categoryLabel);
        leftPanel.add(categoryChoice);
        
        // Right side - Cart and navigation
        Panel rightPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(UIConstants.PRIMARY_COLOR);
        
        rightPanel.add(cartCountLabel);
        
        Button viewCartButton = UIConstants.createStyledButton("View Cart", UIConstants.PRIMARY_DARK);
        viewCartButton.addActionListener(e -> storeListener.onViewCart());
        rightPanel.add(viewCartButton);
        
        Button orderHistoryButton = UIConstants.createStyledButton("Order History", UIConstants.PRIMARY_DARK);
        orderHistoryButton.addActionListener(e -> storeListener.onViewOrderHistory());
        rightPanel.add(orderHistoryButton);
        
        Button logoutButton = UIConstants.createSecondaryButton("Logout");
        logoutButton.addActionListener(e -> storeListener.onLogout());
        rightPanel.add(logoutButton);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private Panel createProductListPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title
        Label titleLabel = UIConstants.createHeaderLabel("Products");
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Product list
        panel.add(productList, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Panel createProductDetailsPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        panel.setPreferredSize(new Dimension(400, 0));
        
        // Title
        Label titleLabel = UIConstants.createHeaderLabel("Product Details");
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Details area
        panel.add(productDetailsArea, BorderLayout.CENTER);
        
        // Add to cart form
        Panel formPanel = createAddToCartForm();
        panel.add(formPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Panel createAddToCartForm() {
        Panel formPanel = createFormPanel();
        
        GridBagConstraints gbc = createFormConstraints(0, 0, 1, GridBagConstraints.WEST);
        
        // Quantity
        formPanel.add(UIConstants.createBodyLabel("Quantity:"), gbc);
        gbc = createFormConstraints(1, 0, 1, GridBagConstraints.WEST);
        formPanel.add(quantityField, gbc);
        
        // Customizations
        gbc = createFormConstraints(0, 1, 2, GridBagConstraints.WEST);
        formPanel.add(UIConstants.createBodyLabel("Customizations:"), gbc);
        gbc = createFormConstraints(0, 2, 2, GridBagConstraints.WEST);
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(customizationArea, gbc);
        
        // Add to cart button
        gbc = createFormConstraints(0, 3, 2, GridBagConstraints.CENTER);
        gbc.insets = new Insets(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_SMALL, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL);
        formPanel.add(addToCartButton, gbc);
        
        return formPanel;
    }
    
    @Override
    protected void setupEventHandlers() {
        currencyChoice.addItemListener(this::handleCurrencyChange);
        categoryChoice.addItemListener(this::handleCategoryChange);
        productList.addItemListener(this::handleProductSelection);
        addToCartButton.addActionListener(e -> handleAddToCart());
    }
    
    private void handleCurrencyChange(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            currentCurrency = currencyChoice.getSelectedItem();
            refreshProducts();
            refreshCartCount();
        }
    }
    
    private void handleCategoryChange(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            refreshProducts();
        }
    }
    
    private void handleProductSelection(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            int selectedIndex = productList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < currentProducts.size()) {
                Product product = currentProducts.get(selectedIndex);
                displayProductDetails(product);
            }
        }
    }
    
    private void displayProductDetails(Product product) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(product.getName()).append("\n");
        details.append("Price: ").append(product.getFormattedPrice()).append("\n");
        details.append("Category: ").append(product.getCategory()).append("\n");
        details.append("Stock: ").append(product.getStockQuantity()).append("\n");
        details.append("Customizable: ").append(product.isCustomizable() ? "Yes" : "No").append("\n");
        
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            details.append("\nDescription:\n").append(product.getDescription());
        }
        
        productDetailsArea.setText(details.toString());
    }
    
    private void handleAddToCart() {
        int selectedIndex = productList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentProducts.size()) {
            showError("Please select a product first");
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showError("Please enter a valid quantity");
                return;
            }
            
            Product product = currentProducts.get(selectedIndex);
            String customizations = customizationArea.getText().trim();
            
            boolean success = cartDAO.addToCart(
                currentUser.getUserId(), 
                product.getProductId(), 
                quantity, 
                customizations.isEmpty() ? null : customizations
            );
            
            if (success) {
                quantityField.setText("1");
                customizationArea.setText("");
                refreshCartCount();
                showSuccess("Item added to cart!");
            } else {
                showError("Failed to add item to cart");
            }
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid quantity");
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        refreshProducts();
        refreshCartCount();
    }
    
    private void refreshProducts() {
        if (currentUser == null) return;
        
        String selectedCategory = categoryChoice.getSelectedItem();
        Product.Category category = selectedCategory.equals("ALL") ? null : 
                                  Product.Category.valueOf(selectedCategory);
        
        currentProducts = productDAO.getProductsByCurrency(currentCurrency, category);
        
        productList.removeAll();
        for (Product product : currentProducts) {
            String displayText = String.format("%s - %s (Stock: %d)%s",
                    product.getName(),
                    product.getFormattedPrice(),
                    product.getStockQuantity(),
                    product.isCustomizable() ? " [Customizable]" : "");
            productList.add(displayText);
        }
        
        // Clear product details when list changes
        productDetailsArea.setText("Select a product to view details");
        
        System.out.println("Refreshed product list: " + currentProducts.size() + " products in " + currentCurrency);
    }
    
    private void refreshCartCount() {
        if (currentUser == null) return;
        
        int itemCount = cartDAO.getCartItemCount(currentUser.getUserId());
        cartCountLabel.setText("Cart (" + itemCount + ")");
        storeListener.onCartUpdated(itemCount);
    }
    
    public void refreshData() {
        refreshProducts();
        refreshCartCount();
    }
}