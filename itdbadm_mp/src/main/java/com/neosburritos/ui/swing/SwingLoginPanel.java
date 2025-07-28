package com.neosburritos.ui.swing;

import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Modern Swing-based Login Panel
 * Clean, simple login interface with improved text field sizing
 */
public class SwingLoginPanel extends JPanel {
    
    public interface LoginListener {
        void onLoginSuccess(User user);
        void onLoginFailure(String message);
        void onRegisterRequest();
    }
    
    private final JFrame parentFrame;
    private final UserDAO userDAO;
    private final LoginListener loginListener;
    
    // UI Components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    
    public SwingLoginPanel(JFrame parentFrame, UserDAO userDAO, LoginListener loginListener) {
        this.parentFrame = parentFrame;
        this.userDAO = userDAO;
        this.loginListener = loginListener;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        // Input fields with improved height to prevent text cutoff
        emailField = SwingUIConstants.createStyledTextField(20);
        Dimension improvedTextFieldSize = new Dimension(300, 50);
        emailField.setPreferredSize(improvedTextFieldSize);
        emailField.setMinimumSize(improvedTextFieldSize);
        emailField.setMaximumSize(improvedTextFieldSize);
        // Enhanced border with better padding for text visibility
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL, 
                                          SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL)
        ));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(SwingUIConstants.BODY_FONT);
        passwordField.setPreferredSize(improvedTextFieldSize);
        passwordField.setMinimumSize(improvedTextFieldSize);
        passwordField.setMaximumSize(improvedTextFieldSize);
        passwordField.setBackground(SwingUIConstants.SURFACE_COLOR);
        passwordField.setForeground(SwingUIConstants.TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_SMALL, 
                                          SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_SMALL)
        ));
        
        // Buttons
        loginButton = SwingUIConstants.createPrimaryButton("Login");
        loginButton.setPreferredSize(SwingUIConstants.LARGE_BUTTON_SIZE);
        
        registerButton = SwingUIConstants.createSecondaryButton("Register");
        registerButton.setPreferredSize(SwingUIConstants.LARGE_BUTTON_SIZE);
        
        // Status label
        statusLabel = SwingUIConstants.createSecondaryLabel("");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main login panel
        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLoginPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
                               SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM);
        
        // Logo/Title
        JLabel titleLabel = SwingUIConstants.createTitleLabel("Neo's Burritos");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = SwingUIConstants.createSubtitleLabel("Welcome Back!");
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);
        
        // Email field
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(SwingUIConstants.createBodyLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emailField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(SwingUIConstants.createBodyLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        // Status label
        gbc.gridy = 5;
        mainPanel.add(statusLabel, gbc);
        
        // Demo credentials info
        JPanel demoPanel = createDemoCredentialsPanel();
        gbc.gridy = 6;
        mainPanel.add(demoPanel, gbc);
        
        return mainPanel;
    }
    
    private JPanel createDemoCredentialsPanel() {
        JPanel panel = SwingUIConstants.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SwingUIConstants.INFO_COLOR.brighter().brighter());
        
        JLabel titleLabel = SwingUIConstants.createHeaderLabel("Demo Credentials");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(SwingUIConstants.PADDING_SMALL));
        
        JLabel adminLabel = SwingUIConstants.createBodyLabel("Admin: admin@neosburritos.com / admin123");
        adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(adminLabel);
        
        JLabel customerLabel = SwingUIConstants.createBodyLabel("Customer: john@email.com / customer123");
        customerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(customerLabel);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Enter key support
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        emailField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "login");
        emailField.getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });
        
        passwordField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "login");
        passwordField.getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        // Button actions
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
    }
    
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both email and password", SwingUIConstants.ERROR_COLOR);
            return;
        }
        
        // Disable button during login
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        showStatus("Authenticating...", SwingUIConstants.TEXT_SECONDARY);
        
        // Perform login in background thread
        SwingWorker<UserDAO.AuthResult, Void> loginWorker = new SwingWorker<UserDAO.AuthResult, Void>() {
            @Override
            protected UserDAO.AuthResult doInBackground() throws Exception {
                return userDAO.authenticate(email, password);
            }
            
            @Override
            protected void done() {
                try {
                    UserDAO.AuthResult authResult = get();
                    if (authResult.isSuccess() && authResult.getUser() != null) {
                        showStatus("Login successful!", SwingUIConstants.SUCCESS_COLOR);
                        loginListener.onLoginSuccess(authResult.getUser());
                    } else {
                        String errorMessage = authResult.getMessage() != null ? 
                            authResult.getMessage() : "Invalid email or password";
                        showStatus(errorMessage, SwingUIConstants.ERROR_COLOR);
                        loginListener.onLoginFailure(errorMessage);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showStatus("Login was interrupted", SwingUIConstants.ERROR_COLOR);
                    loginListener.onLoginFailure("Login interrupted");
                } catch (java.util.concurrent.ExecutionException e) {
                    String errorMessage = "Login failed: " + e.getCause().getMessage();
                    showStatus(errorMessage, SwingUIConstants.ERROR_COLOR);
                    loginListener.onLoginFailure(errorMessage);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        
        loginWorker.execute();
    }
    
    private void handleRegister() {
        loginListener.onRegisterRequest();
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    public void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        emailField.requestFocus();
    }
}