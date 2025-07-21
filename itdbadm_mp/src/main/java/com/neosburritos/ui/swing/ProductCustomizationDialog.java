package com.neosburritos.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.neosburritos.model.Product;

/**
 * Enhanced Product Customization Dialog with proper ingredient selection
 * Single responsibility: Handle product customization with pricing
 */
public class ProductCustomizationDialog extends JDialog {
    
    private final Product product;
    private boolean confirmed = false;
    private int quantity = 1;
    private String customizations = "";
    private BigDecimal additionalPrice = BigDecimal.ZERO;
    
    // UI Components
    private JSpinner quantitySpinner;
    private ButtonGroup proteinGroup;
    private JRadioButton[] proteinRadioButtons;
    private JCheckBox[] riceCheckboxes;
    private JCheckBox[] beanCheckboxes;
    private JCheckBox[] vegetableCheckboxes;
    private JCheckBox[] sauceCheckboxes;
    private JCheckBox[] extraCheckboxes;
    private JTextArea notesArea;
    private JLabel totalPriceLabel;
    private JButton confirmButton;
    private JButton cancelButton;
    
    // Ingredient options with prices
    private static final IngredientOption[] PROTEINS = {
        new IngredientOption("Grilled Chicken", new BigDecimal("0.00")),
        new IngredientOption("Carnitas", new BigDecimal("0.50")),
        new IngredientOption("Barbacoa", new BigDecimal("1.00")),
        new IngredientOption("Sofritas", new BigDecimal("0.00")),
        new IngredientOption("Steak", new BigDecimal("1.50"))
    };
    
    private static final IngredientOption[] RICE = {
        new IngredientOption("Cilantro Lime Rice", BigDecimal.ZERO),
        new IngredientOption("Brown Rice", BigDecimal.ZERO)
    };
    
    private static final IngredientOption[] BEANS = {
        new IngredientOption("Black Beans", BigDecimal.ZERO),
        new IngredientOption("Pinto Beans", BigDecimal.ZERO)
    };
    
    private static final IngredientOption[] VEGETABLES = {
        new IngredientOption("Lettuce", BigDecimal.ZERO),
        new IngredientOption("Tomatoes", BigDecimal.ZERO),
        new IngredientOption("Onions", BigDecimal.ZERO),
        new IngredientOption("Peppers", BigDecimal.ZERO),
        new IngredientOption("Corn", new BigDecimal("0.25"))
    };
    
    private static final IngredientOption[] SAUCES = {
        new IngredientOption("Mild Salsa", BigDecimal.ZERO),
        new IngredientOption("Medium Salsa", BigDecimal.ZERO),
        new IngredientOption("Hot Salsa", BigDecimal.ZERO),
        new IngredientOption("Guacamole", new BigDecimal("1.00")),
        new IngredientOption("Sour Cream", new BigDecimal("0.50"))
    };
    
    private static final IngredientOption[] EXTRAS = {
        new IngredientOption("Extra Cheese", new BigDecimal("0.75")),
        new IngredientOption("Extra Meat", new BigDecimal("2.00")),
        new IngredientOption("Jalapeños", BigDecimal.ZERO),
        new IngredientOption("Pickled Onions", BigDecimal.ZERO)
    };
    
    public ProductCustomizationDialog(JFrame parent, Product product) {
        super(parent, "Customize " + product.getName(), true);
        this.product = product;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updateTotalPrice();
        
        setSize(650, 750);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Quantity spinner
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        quantitySpinner.setFont(SwingUIConstants.BODY_FONT);
        
        // Protein radio buttons (single selection)
        proteinGroup = new ButtonGroup();
        proteinRadioButtons = createRadioButtonArray(PROTEINS);
        
        // Other ingredient checkboxes (multi-selection)
        riceCheckboxes = createCheckboxArray(RICE);
        beanCheckboxes = createCheckboxArray(BEANS);
        vegetableCheckboxes = createCheckboxArray(VEGETABLES);
        sauceCheckboxes = createCheckboxArray(SAUCES);
        extraCheckboxes = createCheckboxArray(EXTRAS);
        
        // Notes area
        notesArea = SwingUIConstants.createStyledTextArea(3, 30);
        notesArea.setBorder(BorderFactory.createTitledBorder("Special Instructions"));
        
        // Total price label
        totalPriceLabel = SwingUIConstants.createHeaderLabel("Total: " + product.getFormattedPrice());
        totalPriceLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        
        // Buttons
        confirmButton = SwingUIConstants.createPrimaryButton("Add to Cart");
        cancelButton = SwingUIConstants.createSecondaryButton("Cancel");
    }
    
    private JRadioButton[] createRadioButtonArray(IngredientOption[] items) {
        JRadioButton[] radioButtons = new JRadioButton[items.length];
        for (int i = 0; i < items.length; i++) {
            String text = items[i].name;
            if (items[i].price.compareTo(BigDecimal.ZERO) > 0) {
                text += " (+$" + items[i].price + ")";
            }
            radioButtons[i] = new JRadioButton(text);
            radioButtons[i].setFont(SwingUIConstants.BODY_FONT);
            radioButtons[i].setOpaque(false);
            proteinGroup.add(radioButtons[i]);
            
            // Add action listener to update price
            radioButtons[i].addActionListener(e -> updateTotalPrice());
        }
        return radioButtons;
    }
    
    private JCheckBox[] createCheckboxArray(IngredientOption[] items) {
        JCheckBox[] checkboxes = new JCheckBox[items.length];
        for (int i = 0; i < items.length; i++) {
            String text = items[i].name;
            if (items[i].price.compareTo(BigDecimal.ZERO) > 0) {
                text += " (+$" + items[i].price + ")";
            }
            checkboxes[i] = new JCheckBox(text);
            checkboxes[i].setFont(SwingUIConstants.BODY_FONT);
            checkboxes[i].setOpaque(false);
            
            // Add action listener to update price
            checkboxes[i].addActionListener(e -> updateTotalPrice());
        }
        return checkboxes;
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with scroll
        JPanel mainPanel = createMainPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = SwingUIConstants.createStyledPanel(SwingUIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE,
            SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_LARGE
        ));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = SwingUIConstants.createTitleLabel("Customize Your " + product.getName());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Quantity and price panel
        JPanel quantityPricePanel = new JPanel(new BorderLayout());
        quantityPricePanel.setOpaque(false);
        
        JPanel quantityPanel = new JPanel(new FlowLayout());
        quantityPanel.setOpaque(false);
        JLabel quantityLabel = SwingUIConstants.createBodyLabel("Quantity:");
        quantityLabel.setForeground(Color.WHITE);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(quantitySpinner);
        quantityPricePanel.add(quantityPanel, BorderLayout.WEST);
        
        totalPriceLabel.setForeground(Color.WHITE);
        quantityPricePanel.add(totalPriceLabel, BorderLayout.EAST);
        
        headerPanel.add(quantityPricePanel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));
        
        // Create ingredient sections
        mainPanel.add(createProteinSection("Choose Your Protein (Required)", proteinRadioButtons));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        mainPanel.add(createIngredientSection("Choose Your Rice", riceCheckboxes, false));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        mainPanel.add(createIngredientSection("Add Beans", beanCheckboxes, false));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        mainPanel.add(createIngredientSection("Add Vegetables", vegetableCheckboxes, false));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        mainPanel.add(createIngredientSection("Choose Your Sauces", sauceCheckboxes, false));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        mainPanel.add(createIngredientSection("Add Extras", extraCheckboxes, false));
        mainPanel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_MEDIUM));
        
        // Notes section
        JPanel notesPanel = SwingUIConstants.createCardPanel();
        notesPanel.setLayout(new BorderLayout());
        notesPanel.add(notesArea, BorderLayout.CENTER);
        mainPanel.add(notesPanel);
        
        return mainPanel;
    }
    
    private JPanel createProteinSection(String title, JRadioButton[] radioButtons) {
        JPanel sectionPanel = SwingUIConstants.createCardPanel();
        sectionPanel.setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = SwingUIConstants.createHeaderLabel(title);
        titleLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Radio buttons in grid
        JPanel radioPanel = new JPanel(new GridLayout(0, 1, SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        radioPanel.setOpaque(false);
        radioPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_MEDIUM
        ));
        
        for (JRadioButton radioButton : radioButtons) {
            radioPanel.add(radioButton);
        }
        
        sectionPanel.add(radioPanel, BorderLayout.CENTER);
        
        return sectionPanel;
    }
    
    private JPanel createIngredientSection(String title, JCheckBox[] checkboxes, boolean required) {
        JPanel sectionPanel = SwingUIConstants.createCardPanel();
        sectionPanel.setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = SwingUIConstants.createHeaderLabel(title);
        if (required) {
            titleLabel.setText(title + " *");
            titleLabel.setForeground(SwingUIConstants.PRIMARY_COLOR);
        }
        sectionPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Checkboxes in grid
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL));
        checkboxPanel.setOpaque(false);
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_MEDIUM
        ));
        
        for (JCheckBox checkbox : checkboxes) {
            checkboxPanel.add(checkbox);
        }
        
        sectionPanel.add(checkboxPanel, BorderLayout.CENTER);
        
        return sectionPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, SwingUIConstants.PADDING_LARGE, SwingUIConstants.PADDING_MEDIUM));
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        footerPanel.add(cancelButton);
        footerPanel.add(confirmButton);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        confirmButton.addActionListener(this::handleConfirm);
        cancelButton.addActionListener(e -> dispose());
        
        // Quantity spinner listener
        quantitySpinner.addChangeListener(e -> updateTotalPrice());
        
        // Close dialog on Escape key
        KeyStroke escapeKey = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void updateTotalPrice() {
        additionalPrice = BigDecimal.ZERO;
        
        // Add protein price
        for (int i = 0; i < proteinRadioButtons.length; i++) {
            if (proteinRadioButtons[i].isSelected()) {
                additionalPrice = additionalPrice.add(PROTEINS[i].price);
                break;
            }
        }
        
        // Add other ingredient prices
        additionalPrice = additionalPrice.add(calculateIngredientPrice(riceCheckboxes, RICE));
        additionalPrice = additionalPrice.add(calculateIngredientPrice(beanCheckboxes, BEANS));
        additionalPrice = additionalPrice.add(calculateIngredientPrice(vegetableCheckboxes, VEGETABLES));
        additionalPrice = additionalPrice.add(calculateIngredientPrice(sauceCheckboxes, SAUCES));
        additionalPrice = additionalPrice.add(calculateIngredientPrice(extraCheckboxes, EXTRAS));
        
        // Calculate total
        BigDecimal basePrice = product.getPriceInBigDecimal();
        BigDecimal totalPerItem = basePrice.add(additionalPrice);
        BigDecimal totalPrice = totalPerItem.multiply(new BigDecimal(quantity));
        
        // Update display
        String currencySymbol = getCurrencySymbol();
        totalPriceLabel.setText("Total: " + currencySymbol + totalPrice.toString());
    }
    
    private BigDecimal calculateIngredientPrice(JCheckBox[] checkboxes, IngredientOption[] options) {
        BigDecimal price = BigDecimal.ZERO;
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].isSelected()) {
                price = price.add(options[i].price);
            }
        }
        return price;
    }
    
    private String getCurrencySymbol() {
        // This should ideally come from the product or be passed in
        return "$"; // Default to USD symbol
    }
    
    private void handleConfirm(ActionEvent e) {
        // Validate required selections
        if (!hasProteinSelection()) {
            SwingUIConstants.showWarningDialog(this, "Please select a protein.", "Selection Required");
            return;
        }
        
        // Build customizations string
        quantity = (Integer) quantitySpinner.getValue();
        customizations = buildCustomizationsString();
        confirmed = true;
        dispose();
    }
    
    private boolean hasProteinSelection() {
        for (JRadioButton radioButton : proteinRadioButtons) {
            if (radioButton.isSelected()) {
                return true;
            }
        }
        return false;
    }
    
    private String buildCustomizationsString() {
        List<String> selections = new ArrayList<>();
        
        // Add protein selection
        for (int i = 0; i < proteinRadioButtons.length; i++) {
            if (proteinRadioButtons[i].isSelected()) {
                selections.add("Protein: " + PROTEINS[i].name);
                break;
            }
        }
        
        addSelections(selections, "Rice", riceCheckboxes, RICE);
        addSelections(selections, "Beans", beanCheckboxes, BEANS);
        addSelections(selections, "Vegetables", vegetableCheckboxes, VEGETABLES);
        addSelections(selections, "Sauces", sauceCheckboxes, SAUCES);
        addSelections(selections, "Extras", extraCheckboxes, EXTRAS);
        
        String notes = notesArea.getText().trim();
        if (!notes.isEmpty()) {
            selections.add("Notes: " + notes);
        }
        
        // Add pricing information
        if (additionalPrice.compareTo(BigDecimal.ZERO) > 0) {
            selections.add("Additional Price: $" + additionalPrice.toString());
        }
        
        return String.join("; ", selections);
    }
    
    private void addSelections(List<String> selections, String category, JCheckBox[] checkboxes, IngredientOption[] options) {
        List<String> categorySelections = new ArrayList<>();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].isSelected()) {
                categorySelections.add(options[i].name);
            }
        }
        
        if (!categorySelections.isEmpty()) {
            selections.add(category + ": " + String.join(", ", categorySelections));
        }
    }
    
    // Getters
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getCustomizations() {
        return customizations;
    }
    
    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }
    
    // Helper class for ingredient options with pricing
    private static class IngredientOption {
        final String name;
        final BigDecimal price;
        
        IngredientOption(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }
    }
}