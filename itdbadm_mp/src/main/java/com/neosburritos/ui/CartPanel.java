package com.neosburritos.ui;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.model.CartItem;
import com.neosburritos.model.User;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Cart panel for managing shopping cart items
 * Handles cart display, item updates, and checkout navigation
 */
public class CartPanel extends BasePanel {
    
    public interface CartListener {
        void onContinueShopping();
        void onProceedToCheckout();
        void onCartUpdated();
    }
    
    private final CartDAO cartDAO;
    private final CartListener cartListener;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    private List<CartItem> currentCartItems;
    
    // UI Components
    private java.awt.List cartList;
    private Label cartTotalLabel;
    private Button updateQuantityButton;
    private Button removeItemButton;
    private Button clearCartButton;
    private Button checkoutButton;
    
    public CartPanel(Frame parentFrame, CartDAO cartDAO, CartListener cartListener) {
        super(parentFrame);
        this.cartDAO = cartDAO;
        this.cartListener = cartListener;
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Cart list
        cartList = new java.awt.List(10);
        cartList.setFont(UIConstants.BODY_FONT);
        cartList.setBackground(UIConstants.SURFACE_COLOR);
        
        // Cart total label
        cartTotalLabel = UIConstants.createHeaderLabel("Total: $0.00");
        cartTotalLabel.setFont(UIConstants.SUBTITLE_FONT);
        
        // Action buttons
        updateQuantityButton = UIConstants.createSecondaryButton("Update Quantity");
        updateQuantityButton.setEnabled(false);
        
        removeItemButton = UIConstants.createStyledButton("Remove Item", UIConstants.ERROR_COLOR);
        removeItemButton.setEnabled(false);
        
        clearCartButton = UIConstants.createStyledButton("Clear Cart", UIConstants.WARNING_COLOR);
        
        checkoutButton = UIConstants.createPrimaryButton("Proceed to Checkout");
        checkoutButton.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
    }
    
    @Override
    protected void layoutComponents() {
        // Header
        Panel headerPanel = createHeaderPanel("Shopping Cart", "Continue Shopping", 
                                            e -> cartListener.onContinueShopping());
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Cart items section
        Panel cartItemsPanel = createCartItemsPanel();
        contentPanel.add(cartItemsPanel, BorderLayout.CENTER);
        
        // Bottom section with total and checkout
        Panel bottomPanel = createBottomPanel();
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private Panel createCartItemsPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title
        Label titleLabel = UIConstants.createHeaderLabel("Cart Items");
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.SURFACE_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Cart list
        panel.add(cartList, BorderLayout.CENTER);
        
        // Action buttons
        Panel buttonPanel = createButtonPanel(updateQuantityButton, removeItemButton, clearCartButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Panel createBottomPanel() {
        Panel panel = new Panel(new BorderLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(0, 80));
        
        // Total on the left
        Panel totalPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        totalPanel.add(cartTotalLabel);
        panel.add(totalPanel, BorderLayout.WEST);
        
        // Checkout button on the right
        Panel checkoutPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        checkoutPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        checkoutPanel.add(checkoutButton);
        panel.add(checkoutPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    @Override
    protected void setupEventHandlers() {
        cartList.addItemListener(this::handleCartItemSelection);
        updateQuantityButton.addActionListener(e -> handleUpdateQuantity());
        removeItemButton.addActionListener(e -> handleRemoveItem());
        clearCartButton.addActionListener(e -> handleClearCart());
        checkoutButton.addActionListener(e -> handleCheckout());
    }
    
    private void handleCartItemSelection(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            boolean hasSelection = cartList.getSelectedIndex() >= 0;
            updateQuantityButton.setEnabled(hasSelection);
            removeItemButton.setEnabled(hasSelection);
        }
    }
    
    private void handleUpdateQuantity() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
            showError("Please select a cart item first");
            return;
        }
        
        String quantityStr = showInputDialog("Enter new quantity:", "Update Quantity");
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr.trim());
                if (quantity <= 0) {
                    showError("Please enter a valid quantity");
                    return;
                }
                
                CartItem item = currentCartItems.get(selectedIndex);
                boolean success = cartDAO.updateCartItemQuantity(item.getCartId(), quantity);
                
                if (success) {
                    refreshCart();
                    cartListener.onCartUpdated();
                    showSuccess("Cart updated!");
                } else {
                    showError("Failed to update cart");
                }
                
            } catch (NumberFormatException e) {
                showError("Please enter a valid quantity");
            }
        }
    }
    
    private void handleRemoveItem() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
            showError("Please select a cart item first");
            return;
        }
        
        CartItem item = currentCartItems.get(selectedIndex);
        if (showConfirmDialog("Remove " + item.getProductName() + " from cart?", "Remove Item")) {
            boolean success = cartDAO.removeFromCart(item.getCartId());
            
            if (success) {
                refreshCart();
                cartListener.onCartUpdated();
                showSuccess("Item removed from cart!");
            } else {
                showError("Failed to remove item from cart");
            }
        }
    }
    
    private void handleClearCart() {
        if (currentCartItems.isEmpty()) {
            showInfo("Cart is already empty");
            return;
        }
        
        if (showConfirmDialog("Are you sure you want to clear your entire cart?", "Clear Cart")) {
            boolean success = cartDAO.clearCart(currentUser.getUserId());
            
            if (success) {
                refreshCart();
                cartListener.onCartUpdated();
                showSuccess("Cart cleared!");
            } else {
                showError("Failed to clear cart");
            }
        }
    }
    
    private void handleCheckout() {
        if (currentCartItems.isEmpty()) {
            showError("Your cart is empty");
            return;
        }
        
        cartListener.onProceedToCheckout();
    }
    
    public void setCurrentUser(User user, String currency) {
        this.currentUser = user;
        this.currentCurrency = currency;
        refreshCart();
    }
    
    public void refreshCart() {
        if (currentUser == null) return;
        
        currentCartItems = cartDAO.getCartItems(currentUser.getUserId(), currentCurrency);
        
        cartList.removeAll();
        for (CartItem item : currentCartItems) {
            String displayText = String.format("%s (x%d) - %s",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getFormattedTotalPrice());
            
            if (item.getCustomizations() != null && !item.getCustomizations().trim().isEmpty()) {
                displayText += " [" + item.getCustomizations() + "]";
            }
            
            cartList.add(displayText);
        }
        
        // Update total
        BigDecimal total = cartDAO.getCartTotal(currentUser.getUserId(), currentCurrency);
        String currencySymbol = getCurrencySymbol(currentCurrency);
        cartTotalLabel.setText("Total: " + currencySymbol + total.toString());
        
        // Update button states
        boolean hasItems = !currentCartItems.isEmpty();
        clearCartButton.setEnabled(hasItems);
        checkoutButton.setEnabled(hasItems);
        
        // Reset selection-dependent buttons
        updateQuantityButton.setEnabled(false);
        removeItemButton.setEnabled(false);
        
        System.out.println("Refreshed cart: " + currentCartItems.size() + " items, total: " + 
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
    
    public boolean isEmpty() {
        return currentCartItems == null || currentCartItems.isEmpty();
    }
    
    public BigDecimal getTotal() {
        if (currentUser == null) return BigDecimal.ZERO;
        return cartDAO.getCartTotal(currentUser.getUserId(), currentCurrency);
    }
}