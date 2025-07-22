package com.neosburritos.ui.swing;

import com.neosburritos.dao.ProductDAO;
import com.neosburritos.model.Product;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Product View Dialog for Staff Panel
 * Read-only view of products for staff members
 */
public class ProductViewDialog extends JDialog {
    
    private final ProductDAO productDAO;
    private final JFrame parentFrame;
    
    // UI Components
    private JTable productTable;
    private ProductTableModel tableModel;
    private JButton refreshButton;
    private JButton closeButton;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    
    // Data
    private List<Product> products;
    private List<Product> filteredProducts;
    
    public ProductViewDialog(JFrame parent, ProductDAO productDAO) {
        super(parent, "Product Inventory - View Only", true);
        this.parentFrame = parent;
        this.productDAO = productDAO;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadProducts();
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Search and filter components
        searchField = SwingUIConstants.createStyledTextField(15);
        searchField.setPreferredSize(new Dimension(200, 32));
        
        String[] categories = {"All Categories", "BURRITO", "BOWL", "DRINK", "SIDE"};
        categoryFilter = SwingUIConstants.createStyledComboBox(categories);
        categoryFilter.setPreferredSize(new Dimension(150, 32));
        
        // Table setup
        tableModel = new ProductTableModel();
        productTable = new JTable(tableModel);
        productTable.setFont(SwingUIConstants.BODY_FONT);
        productTable.setRowHeight(30);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        // Custom cell renderer for status column
        productTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Category
        productTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Stock
        productTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        productTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Customizable
        
        // Buttons
        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        closeButton = SwingUIConstants.createPrimaryButton("Close");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Header with search and filter
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Product Inventory (Read-Only)",
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
        
        // Title
        JLabel titleLabel = SwingUIConstants.createTitleLabel("Product Inventory");
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        searchPanel.add(SwingUIConstants.createBodyLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_MEDIUM));
        searchPanel.add(SwingUIConstants.createBodyLabel("Category:"));
        searchPanel.add(categoryFilter);
        searchPanel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_MEDIUM));
        searchPanel.add(refreshButton);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        JLabel infoLabel = SwingUIConstants.createSecondaryLabel("Staff can view product information but cannot make changes");
        panel.add(infoLabel);
        panel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_LARGE));
        panel.add(closeButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Button actions
        refreshButton.addActionListener(this::handleRefresh);
        closeButton.addActionListener(e -> dispose());
        
        // Search functionality
        searchField.addActionListener(e -> applyFilters());
        categoryFilter.addActionListener(e -> applyFilters());
        
        // Real-time search as user types
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
    }
    
    private void handleRefresh(ActionEvent e) {
        loadProducts();
    }
    
    private void applyFilters() {
        if (products == null) return;
        
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        filteredProducts = products.stream()
            .filter(product -> {
                // Category filter
                if (!"All Categories".equals(selectedCategory)) {
                    if (!product.getCategory().name().equals(selectedCategory)) {
                        return false;
                    }
                }
                
                // Search filter
                if (!searchText.isEmpty()) {
                    return product.getName().toLowerCase().contains(searchText) ||
                           product.getDescription().toLowerCase().contains(searchText);
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        tableModel.setProducts(filteredProducts);
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
                    filteredProducts = new java.util.ArrayList<>(products);
                    tableModel.setProducts(filteredProducts);
                    
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(ProductViewDialog.this,
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
     * Table model for product data (read-only)
     */
    private static class ProductTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Name", "Description", "Price", "Category", "Stock", "Status", "Customizable"
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
                case 6: return product.isActive() ? "Active" : "Inactive";
                case 7: return product.isCustomizable() ? "Yes" : "No";
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
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // Read-only for staff
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