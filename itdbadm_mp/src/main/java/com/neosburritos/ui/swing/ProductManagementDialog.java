package com.neosburritos.ui.swing;

import com.neosburritos.dao.ProductDAO;
import com.neosburritos.model.Product;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Product Management Dialog for Admin Panel
 * Allows viewing, adding, and editing products
 */
public class ProductManagementDialog extends JDialog {
    
    private final ProductDAO productDAO;
    private final JFrame parentFrame;
    
    // UI Components
    private JTable productTable;
    private ProductTableModel tableModel;
    private JButton refreshButton;
    private JButton addProductButton;
    private JButton editProductButton;
    private JButton toggleStatusButton;
    private JButton closeButton;
    
    // Data
    private List<Product> products;
    private Product selectedProduct;
    
    public ProductManagementDialog(JFrame parent, ProductDAO productDAO) {
        super(parent, "Product Management", true);
        this.parentFrame = parent;
        this.productDAO = productDAO;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadProducts();
        
        setSize(1000, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Table setup
        tableModel = new ProductTableModel();
        productTable = new JTable(tableModel);
        productTable.setFont(SwingUIConstants.BODY_FONT);
        productTable.setRowHeight(30);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        // Custom cell renderer for status column
        productTable.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Category
        productTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Stock
        productTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Customizable
        productTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Status
        productTable.getColumnModel().getColumn(8).setPreferredWidth(120); // Created
        
        // Buttons
        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        addProductButton = SwingUIConstants.createSuccessButton("Add Product");
        editProductButton = SwingUIConstants.createSecondaryButton("Edit Product");
        toggleStatusButton = SwingUIConstants.createWarningButton("Toggle Status");
        closeButton = SwingUIConstants.createPrimaryButton("Close");
        
        // Fix button sizing
        addProductButton.setPreferredSize(new Dimension(140, 36));
        editProductButton.setPreferredSize(new Dimension(140, 36));
        toggleStatusButton.setPreferredSize(new Dimension(140, 36));
        
        // Initially disable edit buttons
        editProductButton.setEnabled(false);
        toggleStatusButton.setEnabled(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Product Inventory",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            SwingUIConstants.HEADER_FONT,
            SwingUIConstants.TEXT_PRIMARY
        ));
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer - action buttons
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        JLabel titleLabel = SwingUIConstants.createTitleLabel("Product Management");
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel instructionLabel = SwingUIConstants.createSecondaryLabel(
            "Manage your restaurant's menu items and inventory"
        );
        panel.add(instructionLabel, BorderLayout.CENTER);
        
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        panel.add(addProductButton);
        panel.add(editProductButton);
        panel.add(toggleStatusButton);
        panel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_LARGE));
        panel.add(closeButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Table selection
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleProductSelection();
            }
        });
        
        // Button actions
        refreshButton.addActionListener(this::handleRefresh);
        addProductButton.addActionListener(this::handleAddProduct);
        editProductButton.addActionListener(this::handleEditProduct);
        toggleStatusButton.addActionListener(this::handleToggleStatus);
        closeButton.addActionListener(e -> dispose());
    }
    
    private void handleProductSelection() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < products.size()) {
            selectedProduct = products.get(selectedRow);
            editProductButton.setEnabled(true);
            toggleStatusButton.setEnabled(true);
        } else {
            selectedProduct = null;
            editProductButton.setEnabled(false);
            toggleStatusButton.setEnabled(false);
        }
    }
    
    private void handleRefresh(ActionEvent e) {
        loadProducts();
    }
    
    private void handleAddProduct(ActionEvent e) {
        ProductEditDialog dialog = new ProductEditDialog(this, productDAO, null);
        dialog.setVisible(true);
        if (dialog.isProductSaved()) {
            loadProducts(); // Refresh the table
        }
    }
    
    private void handleEditProduct(ActionEvent e) {
        if (selectedProduct == null) return;
        
        ProductEditDialog dialog = new ProductEditDialog(this, productDAO, selectedProduct);
        dialog.setVisible(true);
        if (dialog.isProductSaved()) {
            loadProducts(); // Refresh the table
        }
    }
    
    private void handleToggleStatus(ActionEvent e) {
        if (selectedProduct == null) return;
        
        boolean newStatus = !selectedProduct.isActive();
        String statusText = newStatus ? "activate" : "deactivate";
        
        boolean confirmed = SwingUIConstants.showConfirmDialog(this,
            "Are you sure you want to " + statusText + " product '" + selectedProduct.getName() + "'?",
            "Confirm Status Change");
        
        if (confirmed) {
            ProductDAO.UpdateProductResult result = productDAO.updateProduct(
                selectedProduct.getProductId(),
                selectedProduct.getName(),
                selectedProduct.getDescription(),
                selectedProduct.getBasePrice(),
                selectedProduct.getStockQuantity(),
                newStatus
            );
            
            if (result.isSuccess()) {
                SwingUIConstants.showSuccessDialog(this, result.getMessage(), "Status Updated");
                loadProducts(); // Refresh the table
            } else {
                SwingUIConstants.showErrorDialog(this, result.getMessage(), "Update Failed");
            }
        }
    }
    
    private void loadProducts() {
        // Show loading state
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");
        
        SwingWorker<List<Product>, Void> worker = new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productDAO.getAllProducts();
            }
            
            @Override
            protected void done() {
                try {
                    products = get();
                    tableModel.setProducts(products);
                    
                    // Reset selection
                    selectedProduct = null;
                    editProductButton.setEnabled(false);
                    toggleStatusButton.setEnabled(false);
                    
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(ProductManagementDialog.this,
                        "Failed to load products: " + e.getMessage(),
                        "Load Error");
                } finally {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Refresh");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Table model for product data
     */
    private static class ProductTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Name", "Description", "Price", "Category", "Stock", "Customizable", "Status", "Created"
        };
        
        private List<Product> products = new java.util.ArrayList<>();
        
        public void setProducts(List<Product> products) {
            this.products = products;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return products.size();
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
            if (rowIndex >= products.size()) return null;
            
            Product product = products.get(rowIndex);
            switch (columnIndex) {
                case 0: return product.getProductId();
                case 1: return product.getName();
                case 2: return product.getDescription();
                case 3: return product.getFormattedPrice();
                case 4: return product.getCategory();
                case 5: return product.getStockQuantity();
                case 6: return product.isCustomizable() ? "Yes" : "No";
                case 7: return product.isActive() ? "Active" : "Inactive";
                case 8: return product.getCreatedAt() != null ? 
                    product.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
                default: return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Integer.class;
                case 4: return Product.Category.class;
                case 5: return Integer.class;
                default: return String.class;
            }
        }
    }
    
    /**
     * Custom cell renderer for status column
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if ("Active".equals(value)) {
                    c.setForeground(SwingUIConstants.SUCCESS_COLOR);
                } else if ("Inactive".equals(value)) {
                    c.setForeground(SwingUIConstants.ERROR_COLOR);
                }
            }
            
            return c;
        }
    }
}