package com.neosburritos.ui.swing;

import com.neosburritos.dao.ProductDAO;
import com.neosburritos.model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;

/**
 * Dialog for adding or editing products with improved text field sizing
 */
public class ProductEditDialog extends JDialog {
    
    private final ProductDAO productDAO;
    private final Product existingProduct; // null for new product
    private boolean productSaved = false;
    
    // UI Components
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JComboBox<Product.Category> categoryComboBox;
    private JTextField stockField;
    private JCheckBox customizableCheckBox;
    private JCheckBox activeCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    public ProductEditDialog(Dialog parent, ProductDAO productDAO, Product existingProduct) {
        super(parent, existingProduct == null ? "Add New Product" : "Edit Product", true);
        this.productDAO = productDAO;
        this.existingProduct = existingProduct;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        if (existingProduct != null) {
            populateFields();
        }
        
        setSize(550, 650);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Input fields with improved sizing for better interaction
        nameField = SwingUIConstants.createStyledTextField(20);
        nameField.setPreferredSize(new Dimension(350, 38));
        nameField.setMinimumSize(new Dimension(350, 38));
        
        descriptionArea = SwingUIConstants.createStyledTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        priceField = SwingUIConstants.createStyledTextField(10);
        priceField.setPreferredSize(new Dimension(200, 38));
        priceField.setMinimumSize(new Dimension(200, 38));
        
        categoryComboBox = new JComboBox<>(Product.Category.values());
        categoryComboBox.setFont(SwingUIConstants.BODY_FONT);
        categoryComboBox.setPreferredSize(new Dimension(250, 38));
        
        stockField = SwingUIConstants.createStyledTextField(10);
        stockField.setPreferredSize(new Dimension(200, 38));
        stockField.setMinimumSize(new Dimension(200, 38));
        
        customizableCheckBox = new JCheckBox("Customizable");
        customizableCheckBox.setFont(SwingUIConstants.BODY_FONT);
        customizableCheckBox.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setFont(SwingUIConstants.BODY_FONT);
        activeCheckBox.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        activeCheckBox.setSelected(true); // Default to active for new products
        
        // Buttons
        saveButton = SwingUIConstants.createSuccessButton(existingProduct == null ? "Add Product" : "Update Product");
        cancelButton = SwingUIConstants.createSecondaryButton("Cancel");
        
        saveButton.setPreferredSize(new Dimension(140, 36));
        cancelButton.setPreferredSize(new Dimension(140, 36));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Main form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE,
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
                               SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM);
        
        // Title
        JLabel titleLabel = SwingUIConstants.createTitleLabel(
            existingProduct == null ? "Add New Product" : "Edit Product"
        );
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // Name field
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(SwingUIConstants.createBodyLabel("Name:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(nameField, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(SwingUIConstants.createBodyLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(350, 120));
        panel.add(descScrollPane, gbc);
        
        // Price field
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(SwingUIConstants.createBodyLabel("Price (USD):"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(priceField, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(SwingUIConstants.createBodyLabel("Category:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(categoryComboBox, gbc);
        
        // Stock field
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        panel.add(SwingUIConstants.createBodyLabel("Stock Quantity:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(stockField, gbc);
        
        // Checkboxes
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        panel.add(customizableCheckBox, gbc);
        
        gbc.gridy = 7;
        panel.add(activeCheckBox, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        panel.add(cancelButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(this::handleSave);
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key support for save
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void populateFields() {
        if (existingProduct != null) {
            nameField.setText(existingProduct.getName());
            descriptionArea.setText(existingProduct.getDescription());
            priceField.setText(existingProduct.getBasePrice().toString());
            categoryComboBox.setSelectedItem(existingProduct.getCategory());
            stockField.setText(String.valueOf(existingProduct.getStockQuantity()));
            customizableCheckBox.setSelected(existingProduct.isCustomizable());
            activeCheckBox.setSelected(existingProduct.isActive());
        }
    }
    
    private void handleSave(ActionEvent e) {
        if (!validateInput()) {
            return;
        }
        
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            Product.Category category = (Product.Category) categoryComboBox.getSelectedItem();
            int stock = Integer.parseInt(stockField.getText().trim());
            boolean customizable = customizableCheckBox.isSelected();
            boolean active = activeCheckBox.isSelected();
            
            // Disable save button during operation
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (existingProduct == null) {
                        // Add new product
                        ProductDAO.AddProductResult result = productDAO.addProduct(
                            name, description, price, "USD", category, customizable, stock
                        );
                        return result.isSuccess();
                    } else {
                        // Update existing product
                        ProductDAO.UpdateProductResult result = productDAO.updateProduct(
                            existingProduct.getProductId(), name, description, price, stock, active
                        );
                        return result.isSuccess();
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            productSaved = true;
                            SwingUIConstants.showSuccessDialog(ProductEditDialog.this,
                                existingProduct == null ? "Product added successfully!" : "Product updated successfully!",
                                "Success");
                            dispose();
                        } else {
                            SwingUIConstants.showErrorDialog(ProductEditDialog.this,
                                "Failed to save product. Please try again.",
                                "Save Failed");
                        }
                    } catch (Exception ex) {
                        SwingUIConstants.showErrorDialog(ProductEditDialog.this,
                            "Error saving product: " + ex.getMessage(),
                            "Error");
                    } finally {
                        saveButton.setEnabled(true);
                        saveButton.setText(existingProduct == null ? "Add Product" : "Update Product");
                    }
                }
            };
            
            worker.execute();
            
        } catch (NumberFormatException ex) {
            SwingUIConstants.showErrorDialog(this,
                "Please enter valid numbers for price and stock quantity.",
                "Invalid Input");
        }
    }
    
    private boolean validateInput() {
        // Check required fields
        if (nameField.getText().trim().isEmpty()) {
            SwingUIConstants.showErrorDialog(this, "Product name is required.", "Validation Error");
            nameField.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            SwingUIConstants.showErrorDialog(this, "Product description is required.", "Validation Error");
            descriptionArea.requestFocus();
            return false;
        }
        
        if (priceField.getText().trim().isEmpty()) {
            SwingUIConstants.showErrorDialog(this, "Product price is required.", "Validation Error");
            priceField.requestFocus();
            return false;
        }
        
        if (stockField.getText().trim().isEmpty()) {
            SwingUIConstants.showErrorDialog(this, "Stock quantity is required.", "Validation Error");
            stockField.requestFocus();
            return false;
        }
        
        // Validate price format
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                SwingUIConstants.showErrorDialog(this, "Price must be greater than 0.", "Validation Error");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            SwingUIConstants.showErrorDialog(this, "Please enter a valid price.", "Validation Error");
            priceField.requestFocus();
            return false;
        }
        
        // Validate stock format
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                SwingUIConstants.showErrorDialog(this, "Stock quantity cannot be negative.", "Validation Error");
                stockField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            SwingUIConstants.showErrorDialog(this, "Please enter a valid stock quantity.", "Validation Error");
            stockField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public boolean isProductSaved() {
        return productSaved;
    }
}