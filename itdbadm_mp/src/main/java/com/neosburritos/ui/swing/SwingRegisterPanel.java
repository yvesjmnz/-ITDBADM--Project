package com.neosburritos.ui.swing;

import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SwingRegisterPanel extends JPanel {

    private final JFrame parentFrame;
    private final UserDAO userDAO;
    private final RegisterListener registerListener;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField addressField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel;

    public SwingRegisterPanel(JFrame parentFrame, UserDAO userDAO, RegisterListener registerListener) { // ✅ corrected param name
        this.parentFrame = parentFrame;
        this.userDAO = userDAO;
        this.registerListener = registerListener;

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setBackground(SwingUIConstants.BACKGROUND_COLOR);

        nameField = SwingUIConstants.createStyledTextField(20);
        emailField = SwingUIConstants.createStyledTextField(20);
        passwordField = new JPasswordField(20);
        phoneField = SwingUIConstants.createStyledTextField(20);
        addressField = SwingUIConstants.createStyledTextField(20);

        passwordField.setFont(SwingUIConstants.BODY_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL,
                        SwingUIConstants.PADDING_SMALL, SwingUIConstants.PADDING_SMALL)
        ));

        registerButton = SwingUIConstants.createPrimaryButton("Register");
        registerButton.setPreferredSize(SwingUIConstants.LARGE_BUTTON_SIZE);

        backButton = SwingUIConstants.createSecondaryButton("Back to Login");
        backButton.setPreferredSize(SwingUIConstants.LARGE_BUTTON_SIZE);

        statusLabel = SwingUIConstants.createSecondaryLabel("");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
                SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM);

        JLabel titleLabel = SwingUIConstants.createTitleLabel("Neo's Burritos");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = SwingUIConstants.createSubtitleLabel("Create Your Account");
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);

        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(SwingUIConstants.createBodyLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(SwingUIConstants.createBodyLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(SwingUIConstants.createBodyLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(SwingUIConstants.createBodyLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(SwingUIConstants.createBodyLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(addressField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        gbc.gridy = 8;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        nameField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "register");
        nameField.getActionMap().put("register", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                emailField.requestFocus();
            }
        });

        emailField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "register");
        emailField.getActionMap().put("register", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });

        passwordField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "register");
        passwordField.getActionMap().put("register", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                phoneField.requestFocus();
            }
        });

        phoneField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "register");
        phoneField.getActionMap().put("register", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addressField.requestFocus();
            }
        });

        addressField.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "register");
        addressField.getActionMap().put("register", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        registerButton.addActionListener(e -> handleRegister());
        backButton.addActionListener(e -> registerListener.onBackToLogin());
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Name, Email, and Password are required.", SwingUIConstants.ERROR_COLOR);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");
        showStatus("Processing registration...", SwingUIConstants.TEXT_SECONDARY);

        SwingWorker<UserDAO.RegisterResult, Void> worker = new SwingWorker<>() {
            protected UserDAO.RegisterResult doInBackground() {
                return userDAO.register(name, email, password, User.Role.CUSTOMER, phone, address);
            }

            protected void done() {
                try {
                    UserDAO.RegisterResult result = get();
                    if (result.isSuccess()) {
                        showStatus("Registration successful! Your User ID: " + result.getUserId(), SwingUIConstants.SUCCESS_COLOR);
                        clearForm();
                        registerListener.onRegisterSuccess(); // ✅ Trigger success callback
                    } else {
                        showStatus("Registration failed: " + result.getMessage(), SwingUIConstants.ERROR_COLOR);
                    }
                } catch (Exception e) {
                    showStatus("Unexpected error: " + e.getMessage(), SwingUIConstants.ERROR_COLOR);
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                }
            }
        };

        worker.execute();
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        phoneField.setText("");
        addressField.setText("");
        nameField.requestFocus();
    }

    public interface RegisterListener {
        void onRegisterSuccess();
        void onBackToLogin();
    }
}
