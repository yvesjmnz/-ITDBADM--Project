package com.neosburritos.ui;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Base panel class providing common UI functionality
 * Implements single responsibility for UI component creation
 */
public abstract class BasePanel extends Panel {
    
    protected final Frame parentFrame;
    
    public BasePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setBackground(UIConstants.BACKGROUND_COLOR);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    /**
     * Initialize UI components - to be implemented by subclasses
     */
    protected abstract void initializeComponents();
    
    /**
     * Layout components - to be implemented by subclasses
     */
    protected abstract void layoutComponents();
    
    /**
     * Setup event handlers - to be implemented by subclasses
     */
    protected abstract void setupEventHandlers();
    
    /**
     * Create a header panel with title and optional back button
     */
    protected Panel createHeaderPanel(String title, String backButtonText, ActionListener backAction) {
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        
        // Title
        Label titleLabel = UIConstants.createTitleLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignment(Label.CENTER);
        
        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(UIConstants.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Back button if provided
        if (backButtonText != null && backAction != null) {
            Button backButton = UIConstants.createSecondaryButton(backButtonText);
            backButton.addActionListener(backAction);
            
            Panel backPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
            backPanel.setBackground(UIConstants.PRIMARY_COLOR);
            backPanel.add(backButton);
            headerPanel.add(backPanel, BorderLayout.WEST);
        }
        
        return headerPanel;
    }
    
    /**
     * Create a form panel with GridBagLayout
     */
    protected Panel createFormPanel() {
        Panel formPanel = new Panel(new GridBagLayout());
        formPanel.setBackground(UIConstants.SURFACE_COLOR);
        return formPanel;
    }
    
    /**
     * Create GridBagConstraints with common settings
     */
    protected GridBagConstraints createFormConstraints(int x, int y, int width, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.anchor = anchor;
        gbc.insets = new Insets(UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL);
        return gbc;
    }
    
    /**
     * Create a button panel with consistent spacing
     */
    protected Panel createButtonPanel(Button... buttons) {
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, UIConstants.PADDING_MEDIUM, UIConstants.PADDING_SMALL));
        buttonPanel.setBackground(UIConstants.SURFACE_COLOR);
        
        for (Button button : buttons) {
            buttonPanel.add(button);
        }
        
        return buttonPanel;
    }
    
    /**
     * Show error dialog
     */
    protected void showError(String message) {
        showDialog("Error", message, UIConstants.ERROR_COLOR);
    }
    
    /**
     * Show success dialog
     */
    protected void showSuccess(String message) {
        showDialog("Success", message, UIConstants.SUCCESS_COLOR);
    }
    
    /**
     * Show info dialog
     */
    protected void showInfo(String message) {
        showDialog("Information", message, UIConstants.PRIMARY_COLOR);
    }
    
    /**
     * Generic dialog method
     */
    private void showDialog(String title, String message, Color titleColor) {
        Dialog dialog = new Dialog(parentFrame, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setBackground(UIConstants.SURFACE_COLOR);
        
        // Title panel
        Panel titlePanel = new Panel(new FlowLayout());
        titlePanel.setBackground(titleColor);
        Label titleLabel = new Label(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(UIConstants.HEADER_FONT);
        titlePanel.add(titleLabel);
        dialog.add(titlePanel, BorderLayout.NORTH);
        
        // Message
        Label messageLabel = new Label(message, Label.CENTER);
        messageLabel.setFont(UIConstants.BODY_FONT);
        messageLabel.setForeground(UIConstants.TEXT_PRIMARY);
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        // OK button
        Button okButton = UIConstants.createPrimaryButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        Panel buttonPanel = createButtonPanel(okButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Show confirmation dialog
     */
    protected boolean showConfirmDialog(String message, String title) {
        Dialog dialog = new Dialog(parentFrame, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(UIConstants.DIALOG_WIDTH, UIConstants.DIALOG_HEIGHT);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setBackground(UIConstants.SURFACE_COLOR);
        
        // Message
        Label messageLabel = new Label(message, Label.CENTER);
        messageLabel.setFont(UIConstants.BODY_FONT);
        messageLabel.setForeground(UIConstants.TEXT_PRIMARY);
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        final boolean[] result = {false};
        
        // Buttons
        Button yesButton = UIConstants.createPrimaryButton("Yes");
        yesButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        
        Button noButton = UIConstants.createSecondaryButton("No");
        noButton.addActionListener(e -> dialog.dispose());
        
        Panel buttonPanel = createButtonPanel(yesButton, noButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        return result[0];
    }
    
    /**
     * Show input dialog
     */
    protected String showInputDialog(String message, String title) {
        Dialog dialog = new Dialog(parentFrame, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setBackground(UIConstants.SURFACE_COLOR);
        
        // Message
        Panel messagePanel = new Panel(new FlowLayout());
        messagePanel.setBackground(UIConstants.SURFACE_COLOR);
        Label messageLabel = UIConstants.createBodyLabel(message);
        messagePanel.add(messageLabel);
        dialog.add(messagePanel, BorderLayout.NORTH);
        
        // Input field
        TextField inputField = UIConstants.createStyledTextField(20);
        Panel inputPanel = new Panel(new FlowLayout());
        inputPanel.setBackground(UIConstants.SURFACE_COLOR);
        inputPanel.add(inputField);
        dialog.add(inputPanel, BorderLayout.CENTER);
        
        final String[] result = {null};
        
        // Buttons
        Button okButton = UIConstants.createPrimaryButton("OK");
        okButton.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });
        
        Button cancelButton = UIConstants.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        Panel buttonPanel = createButtonPanel(okButton, cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter key support
        inputField.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });
        
        dialog.setVisible(true);
        return result[0];
    }
}