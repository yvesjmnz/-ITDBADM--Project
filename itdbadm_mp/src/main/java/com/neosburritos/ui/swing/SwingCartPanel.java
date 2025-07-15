package com.neosburritos.ui.swing;

import com.neosburritos.dao.CartDAO;
import com.neosburritos.model.CartItem;
import com.neosburritos.model.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Swing Cart Panel with full cart management
 * Single responsibility: Cart item display and management
 */
public class SwingCartPanel extends JPanel {
    
    public interface CartListener {
        void onContinueShopping();
        void onProceedToCheckout();
        void onCartUpdated();
    }
    
    private final JFrame parentFrame;
    private final CartDAO cartDAO;
    private final CartListener cartListener;
    
    // Current state
    private User currentUser;
    private String currentCurrency = "USD";
    private List<CartItem> cartItems = new ArrayList<>();
    
    // UI Components
    private JLabel titleLabel;
    private JLabel totalLabel;
    private JTable cartTable;
    private CartTableModel tableModel;
    private JButton continueShoppingButton;
    private JButton checkoutButton;
    private JButton clearCartButton;
    private JButton refreshButton;
    
    public SwingCartPanel(JFrame parentFrame, CartDAO cartDAO, CartListener cartListener) {
        this.parentFrame = parentFrame;
        this.cartDAO = cartDAO;
        this.cartListener = cartListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Header components
        titleLabel = SwingUIConstants.createTitleLabel("🛒 Your Shopping Cart");
        totalLabel = SwingUIConstants.createSubtitleLabel("Total: $0.00");
        totalLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        
        // Cart table
        tableModel = new CartTableModel();
        cartTable = new JTable(tableModel);
        cartTable.setFont(SwingUIConstants.BODY_FONT);
        cartTable.setRowHeight(60);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        // Configure table columns
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Product
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Customizations
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Quantity
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Price
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Actions
        
        // Custom cell renderer for wrapping text
        DefaultTableCellRenderer wrapRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setVerticalAlignment(SwingConstants.TOP);
                    if (column == 1) { // Customizations column
                        String text = value != null ? value.toString() : "";
                        if (text.length() > 50) {
                            text = "<html>" + text.replaceAll(";", "<br>") + "</html>";
                        }
                        label.setText(text);
                    }
                }
                return c;
            }
        };
        cartTable.getColumnModel().getColumn(1).setCellRenderer(wrapRenderer);
        
        // Action buttons
        continueShoppingButton = SwingUIConstants.createSecondaryButton("← Continue Shopping");
        checkoutButton = SwingUIConstants.createPrimaryButton("Proceed to Checkout →");
        clearCartButton = SwingUIConstants.createDangerButton("Clear Cart");
        refreshButton = SwingUIConstants.createSecondaryButton("🔄 Refresh");
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
        
        // Title and total
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        totalLabel.setForeground(Color.WHITE);
        titlePanel.add(totalLabel, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Cart table with scroll pane
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Cart Items",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            SwingUIConstants.HEADER_FONT,
            SwingUIConstants.TEXT_PRIMARY
        ));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.add(refreshButton);
        actionPanel.add(clearCartButton);
        
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        return mainPanel;
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
        
        navPanel.add(continueShoppingButton);
        navPanel.add(checkoutButton);
        
        footerPanel.add(navPanel, BorderLayout.CENTER);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Navigation buttons
        continueShoppingButton.addActionListener(e -> cartListener.onContinueShopping());
        checkoutButton.addActionListener(e -> {
            if (cartItems.isEmpty()) {
                SwingUIConstants.showWarningDialog(parentFrame, 
                    "Your cart is empty. Add some items before checkout.", "Empty Cart");
            } else {
                cartListener.onProceedToCheckout();
            }
        });
        
        // Action buttons
        clearCartButton.addActionListener(this::handleClearCart);
        refreshButton.addActionListener(e -> refreshCart());
    }
    
    private void handleClearCart(ActionEvent e) {
        if (cartItems.isEmpty()) {
            SwingUIConstants.showWarningDialog(parentFrame, "Your cart is already empty.", "Empty Cart");
            return;
        }
        
        boolean confirmed = SwingUIConstants.showConfirmDialog(parentFrame,
            "Are you sure you want to clear your cart? This action cannot be undone.",
            "Clear Cart");
        
        if (confirmed) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return cartDAO.clearCart(currentUser.getUserId());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            SwingUIConstants.showSuccessDialog(parentFrame, 
                                "Cart cleared successfully!", "Cart Cleared");
                            refreshCart();
                            cartListener.onCartUpdated();
                        } else {
                            SwingUIConstants.showErrorDialog(parentFrame, 
                                "Failed to clear cart", "Error");
                        }
                    } catch (Exception ex) {
                        SwingUIConstants.showErrorDialog(parentFrame, 
                            "Error clearing cart: " + ex.getMessage(), "Error");
                    }
                }
            };
            worker.execute();
        }
    }
    
    private void handleUpdateQuantity(int cartId, int newQuantity) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return cartDAO.updateCartItemQuantity(cartId, newQuantity);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        refreshCart();
                        cartListener.onCartUpdated();
                    } else {
                        SwingUIConstants.showErrorDialog(parentFrame, 
                            "Failed to update quantity", "Error");
                    }
                } catch (Exception ex) {
                    SwingUIConstants.showErrorDialog(parentFrame, 
                        "Error updating quantity: " + ex.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }
    
    private void handleRemoveItem(int cartId, String productName) {
        boolean confirmed = SwingUIConstants.showConfirmDialog(parentFrame,
            "Remove " + productName + " from cart?", "Remove Item");
        
        if (confirmed) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return cartDAO.removeFromCart(cartId);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            refreshCart();
                            cartListener.onCartUpdated();
                        } else {
                            SwingUIConstants.showErrorDialog(parentFrame, 
                                "Failed to remove item", "Error");
                        }
                    } catch (Exception ex) {
                        SwingUIConstants.showErrorDialog(parentFrame, 
                            "Error removing item: " + ex.getMessage(), "Error");
                    }
                }
            };
            worker.execute();
        }
    }
    
    public void setCurrentUser(User user, String currency) {
        this.currentUser = user;
        this.currentCurrency = currency;
        refreshCart();
    }
    
    public void refreshCart() {
        if (currentUser == null) return;
        
        SwingWorker<List<CartItem>, Void> worker = new SwingWorker<List<CartItem>, Void>() {
            @Override
            protected List<CartItem> doInBackground() throws Exception {
                return cartDAO.getCartItems(currentUser.getUserId(), currentCurrency);
            }
            
            @Override
            protected void done() {
                try {
                    cartItems = get();
                    tableModel.fireTableDataChanged();
                    updateTotal();
                    updateButtonStates();
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(parentFrame, 
                        "Failed to load cart: " + e.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }
    
    private void updateTotal() {
        BigDecimal total = cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String currencySymbol = cartItems.isEmpty() ? "$" : cartItems.get(0).getCurrencySymbol();
        totalLabel.setText("Total: " + currencySymbol + total.toString());
    }
    
    private void updateButtonStates() {
        boolean hasItems = !cartItems.isEmpty();
        checkoutButton.setEnabled(hasItems);
        clearCartButton.setEnabled(hasItems);
    }
    
    // Table Model for Cart Items
    private class CartTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Product", "Customizations", "Quantity", "Price", "Actions"};
        
        @Override
        public int getRowCount() {
            return cartItems.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= cartItems.size()) return null;
            
            CartItem item = cartItems.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.getProductName();
                case 1: return item.getCustomizations() != null ? item.getCustomizations() : "No customizations";
                case 2: return item.getQuantity();
                case 3: return item.getFormattedTotalPrice();
                case 4: return "Actions";
                default: return null;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 || columnIndex == 4; // Quantity and Actions columns
        }
        
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (rowIndex >= cartItems.size()) return;
            
            CartItem item = cartItems.get(rowIndex);
            if (columnIndex == 2) { // Quantity column
                try {
                    int newQuantity = Integer.parseInt(value.toString());
                    if (newQuantity > 0 && newQuantity <= 10) {
                        handleUpdateQuantity(item.getCartId(), newQuantity);
                    } else {
                        SwingUIConstants.showWarningDialog(parentFrame, 
                            "Quantity must be between 1 and 10", "Invalid Quantity");
                    }
                } catch (NumberFormatException e) {
                    SwingUIConstants.showWarningDialog(parentFrame, 
                        "Please enter a valid number", "Invalid Quantity");
                }
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 2: return Integer.class;
                default: return String.class;
            }
        }
    }
}