package com.neosburritos.ui;

import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login panel with professional styling
 * Handles user authentication with clean UI design
 */
public class LoginPanel extends BasePanel {
    
    public interface LoginListener {
        void onLoginSuccess(User user);
        void onLoginFailure(String message);
    }
    
    private final UserDAO userDAO;
    private final LoginListener loginListener;
    
    // UI Components
    private TextField emailField;
    private TextField passwordField;
    private Label statusLabel;
    private Button loginButton;
    
    public LoginPanel(Frame parentFrame, UserDAO userDAO, LoginListener loginListener) {
        super(parentFrame);
        this.userDAO = userDAO;
        this.loginListener = loginListener;
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Email field
        emailField = UIConstants.createStyledTextField(25);
        emailField.setText("alice@email.com"); // Demo default
        
        // Password field
        passwordField = UIConstants.createStyledTextField(25);
        passwordField.setEchoChar('*');
        passwordField.setText("customer123"); // Demo default
        
        // Status label for error messages
        statusLabel = new Label("");
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        statusLabel.setFont(UIConstants.SMALL_FONT);
        statusLabel.setAlignment(Label.CENTER);
        
        // Login button
        loginButton = UIConstants.createPrimaryButton("Login");
        loginButton.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
    }
    
    @Override
    protected void layoutComponents() {
        // Main container
        Panel mainContainer = new Panel(new GridBagLayout());
        mainContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_LARGE, 
                               UIConstants.PADDING_LARGE, UIConstants.PADDING_LARGE);
        
        // Login card panel
        Panel loginCard = createLoginCard();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(loginCard, gbc);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private Panel createLoginCard() {
        Panel card = new Panel(new GridBagLayout());
        card.setBackground(UIConstants.SURFACE_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM);
        
        // Title
        Label titleLabel = UIConstants.createTitleLabel("Neo's Burritos");
        titleLabel.setAlignment(Label.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(titleLabel, gbc);
        
        // Subtitle
        Label subtitleLabel = new Label("Enhanced Online Store");
        subtitleLabel.setFont(UIConstants.SUBTITLE_FONT);
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subtitleLabel.setAlignment(Label.CENTER);
        gbc.gridy = 1;
        card.add(subtitleLabel, gbc);
        
        // Spacing
        gbc.gridy = 2;
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, 0, UIConstants.PADDING_MEDIUM, 0);
        card.add(new Label(""), gbc);
        
        // Email field
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL);
        Label emailLabel = UIConstants.createBodyLabel("Email:");
        emailLabel.setFont(UIConstants.HEADER_FONT);
        card.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(emailField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        Label passwordLabel = UIConstants.createBodyLabel("Password:");
        passwordLabel.setFont(UIConstants.HEADER_FONT);
        card.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(UIConstants.PADDING_LARGE, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        card.add(loginButton, gbc);
        
        // Status label
        gbc.gridy = 6;
        gbc.insets = new Insets(UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        card.add(statusLabel, gbc);
        
        // Demo info
        gbc.gridy = 7;
        gbc.insets = new Insets(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                               UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM);
        Label demoLabel = new Label("Demo: admin@neosburritos.com/admin123 | alice@email.com/customer123");
        demoLabel.setFont(UIConstants.CAPTION_FONT);
        demoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        demoLabel.setAlignment(Label.CENTER);
        card.add(demoLabel, gbc);
        
        return card;
    }
    
    @Override
    protected void setupEventHandlers() {
        loginButton.addActionListener(this::handleLogin);
        emailField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(this::handleLogin);
    }
    
    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter email and password");
            return;
        }
        
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        // Disable login button during authentication
        loginButton.setEnabled(false);
        
        // Perform authentication in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                UserDAO.AuthResult result = userDAO.authenticate(email, password);
                
                // Update UI on EDT
                EventQueue.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    
                    if (result.isSuccess()) {
                        statusLabel.setText("");
                        loginListener.onLoginSuccess(result.getUser());
                    } else {
                        statusLabel.setText(result.getMessage());
                        statusLabel.setForeground(UIConstants.ERROR_COLOR);
                        loginListener.onLoginFailure(result.getMessage());
                    }
                });
                
            } catch (Exception ex) {
                EventQueue.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    statusLabel.setText("Authentication failed");
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                    loginListener.onLoginFailure("Authentication failed: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Clear the form
     */
    public void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        loginButton.setEnabled(true);
    }
    
    /**
     * Set demo credentials
     */
    public void setDemoCredentials(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
    }
}