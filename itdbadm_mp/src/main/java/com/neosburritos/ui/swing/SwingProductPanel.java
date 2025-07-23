package com.neosburritos.ui.swing;

import com.neosburritos.dao.ProductDAO;
import com.neosburritos.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

public class SwingProductPanel extends JPanel {
    private final ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JComboBox<Product.Category> categoryCombo;
    private JCheckBox activeCheck;
    private JTextField stockField;
    private JComboBox<String> currencyCombo;

    private JButton addButton;
    private JButton clearButton;
    private JButton saveButton;
    private JButton backButton;
    private JButton deleteButton;

    private int selectedProductId = -1;

    private final JFrame parent;

    public interface ProductListener {
        void onBackToAdmin();
    }

    public SwingProductPanel(JFrame parent, ProductDAO productDAO, ProductListener listener) {
        this.parent = parent;
        this.productDAO = productDAO;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents(listener);
        loadAllProducts(); 
    }

    private void initComponents(ProductListener listener) {
        JPanel topButtonPanel = new JPanel(new BorderLayout());
        backButton = new JButton("Back to Admin");
        saveButton = new JButton("Update Product");
        backButton.addActionListener(e -> listener.onBackToAdmin());
        saveButton.addActionListener(this::handleSave);
        topButtonPanel.add(backButton, BorderLayout.WEST);
        topButtonPanel.add(saveButton, BorderLayout.EAST);
        add(topButtonPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Category", "Stock", "Active"}, 0);
        productTable = new JTable(tableModel);
        productTable.getSelectionModel().addListSelectionListener(e -> loadSelectedProduct());
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setPreferredSize(new Dimension(800, 200));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField();
        formPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryCombo = new JComboBox<>(Product.Category.values());
        formPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        stockField = new JTextField();
        formPanel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Currency:"), gbc);
        gbc.gridx = 1;
        currencyCombo = new JComboBox<>(new String[]{"USD", "PHP", "KRW"});
        formPanel.add(currencyCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        activeCheck = new JCheckBox("Active");
        formPanel.add(activeCheck, gbc);


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(formPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Product");
        clearButton = new JButton("Clear Form");
        deleteButton = new JButton("Delete Product");

        addButton.addActionListener(this::handleAdd);
        clearButton.addActionListener(e -> clearForm());
        deleteButton.addActionListener(e -> handleDelete());

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadProducts(String currencyCode) {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getProductsByCurrency(currencyCode, null);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getFormattedPrice(), p.getCategory(),
                    p.getStockQuantity(), p.isActive()
            });
        }
    }

    private void loadSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            selectedProductId = (int) tableModel.getValueAt(row, 0);
            nameField.setText((String) tableModel.getValueAt(row, 1));
            priceField.setText(tableModel.getValueAt(row, 2).toString().replaceAll("[^0-9.]", ""));
            categoryCombo.setSelectedItem(Product.Category.valueOf(tableModel.getValueAt(row, 3).toString()));
            stockField.setText(tableModel.getValueAt(row, 4).toString());
            activeCheck.setSelected((Boolean) tableModel.getValueAt(row, 5));
        }
    }

    private void handleAdd(ActionEvent e) {
        try {
            String name = nameField.getText();
            String desc = descriptionArea.getText();
            BigDecimal price = new BigDecimal(priceField.getText());
            String currency = (String) currencyCombo.getSelectedItem();
            Product.Category category = (Product.Category) categoryCombo.getSelectedItem();
            int stock = Integer.parseInt(stockField.getText());

            ProductDAO.AddProductResult result = productDAO.addProduct(name, desc, price, currency, category, false, stock);
            JOptionPane.showMessageDialog(this, result.getMessage());
            if (result.isSuccess()) {
                loadProducts(currency);
                clearForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage());
        }
    }

    private void handleSave(ActionEvent e) {
        if (selectedProductId < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }

        if (!nameField.isEnabled()) {
            nameField.setEnabled(true);
            descriptionArea.setEnabled(true);
            priceField.setEnabled(true);
            categoryCombo.setEnabled(true);
            stockField.setEnabled(true);
            activeCheck.setEnabled(true);
            return;
        }

        try {
            String name = nameField.getText();
            String desc = descriptionArea.getText();
            if (desc == null || desc.trim().isEmpty()) {
                desc = productDAO.getProductDescription(selectedProductId);
            }
            BigDecimal price = new BigDecimal(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            boolean isActive = activeCheck.isSelected();

            ProductDAO.UpdateProductResult result = productDAO.updateProduct(
                    selectedProductId, name, desc, price, stock, isActive);
            JOptionPane.showMessageDialog(this, result.getMessage());
            if (result.isSuccess()) {
                loadAllProducts();
                clearForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving product: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedProductId < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = productDAO.deleteProduct(selectedProductId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Product deleted successfully.");
            loadAllProducts();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete product.");
        }
    }
        private void loadAllProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getFormattedPrice(), p.getCategory(),
                    p.getStockQuantity(), p.isActive()
            });
        }
    }


    private void clearForm() {
        selectedProductId = -1;
        nameField.setText("");
        descriptionArea.setText("");
        priceField.setText("");
        stockField.setText("");
        categoryCombo.setSelectedIndex(0);
        currencyCombo.setSelectedIndex(0);
        activeCheck.setSelected(false);
        nameField.setEnabled(true);
        descriptionArea.setEnabled(true);
        priceField.setEnabled(true);
        categoryCombo.setEnabled(true);
        stockField.setEnabled(true);
        activeCheck.setEnabled(true);
        productTable.clearSelection();
    }
}
