package com.neosburritos.ui.swing;

import com.neosburritos.dao.UserDAO;
import com.neosburritos.model.User;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User Management Dialog for Admin Panel
 * Allows viewing and editing user status and roles
 */
public class UserManagementDialog extends JDialog {
    
    private final UserDAO userDAO;
    private final JFrame parentFrame;
    
    // UI Components
    private JTable userTable;
    private UserTableModel tableModel;
    private JButton refreshButton;
    private JButton editStatusButton;
    private JButton editRoleButton;
    private JButton closeButton;
    
    // Data
    private List<User> users;
    private User selectedUser;
    
    public UserManagementDialog(JFrame parent, UserDAO userDAO) {
        super(parent, "User Management", true);
        this.parentFrame = parent;
        this.userDAO = userDAO;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadUsers();
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Table setup
        tableModel = new UserTableModel();
        userTable = new JTable(tableModel);
        userTable.setFont(SwingUIConstants.BODY_FONT);
        userTable.setRowHeight(30);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setBackground(SwingUIConstants.SURFACE_COLOR);
        
        // Custom cell renderer for status column
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        userTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Role
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Phone
        userTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        userTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Created
        
        // Buttons
        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        editStatusButton = SwingUIConstants.createWarningButton("Toggle Status");
        editRoleButton = SwingUIConstants.createSecondaryButton("Change Role");
        closeButton = SwingUIConstants.createPrimaryButton("Close");
        
        // Fix button sizing to prevent text truncation
        editStatusButton.setPreferredSize(new Dimension(140, 36)); // Wider to accommodate "Toggle Status"
        editRoleButton.setPreferredSize(new Dimension(140, 36));   // Match for consistency
        
        // Initially disable edit buttons
        editStatusButton.setEnabled(false);
        editRoleButton.setEnabled(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "System Users",
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
        
        JLabel titleLabel = SwingUIConstants.createTitleLabel("User Management");
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel instructionLabel = SwingUIConstants.createSecondaryLabel(
            "Select a user to edit their status or role"
        );
        panel.add(instructionLabel, BorderLayout.CENTER);
        
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        panel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        
        panel.add(editStatusButton);
        panel.add(editRoleButton);
        panel.add(Box.createHorizontalStrut(SwingUIConstants.PADDING_LARGE));
        panel.add(closeButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Table selection
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleUserSelection();
            }
        });
        
        // Button actions
        refreshButton.addActionListener(this::handleRefresh);
        editStatusButton.addActionListener(this::handleEditStatus);
        editRoleButton.addActionListener(this::handleEditRole);
        closeButton.addActionListener(e -> dispose());
    }
    
    private void handleUserSelection() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < users.size()) {
            selectedUser = users.get(selectedRow);
            editStatusButton.setEnabled(true);
            editRoleButton.setEnabled(true);
        } else {
            selectedUser = null;
            editStatusButton.setEnabled(false);
            editRoleButton.setEnabled(false);
        }
    }
    
    private void handleRefresh(ActionEvent e) {
        loadUsers();
    }
    
    private void handleEditStatus(ActionEvent e) {
        if (selectedUser == null) return;
        
        boolean newStatus = !selectedUser.isActive();
        String statusText = newStatus ? "activate" : "deactivate";
        
        boolean confirmed = SwingUIConstants.showConfirmDialog(this,
            "Are you sure you want to " + statusText + " user '" + selectedUser.getName() + "'?",
            "Confirm Status Change");
        
        if (confirmed) {
            UserDAO.UpdateResult result = userDAO.updateUserStatus(selectedUser.getUserId(), newStatus);
            
            if (result.isSuccess()) {
                SwingUIConstants.showSuccessDialog(this, result.getMessage(), "Status Updated");
                loadUsers(); // Refresh the table
            } else {
                SwingUIConstants.showErrorDialog(this, result.getMessage(), "Update Failed");
            }
        }
    }
    
    private void handleEditRole(ActionEvent e) {
        if (selectedUser == null) return;
        
        // Create role selection dialog
        User.Role[] roles = User.Role.values();
        User.Role selectedRole = (User.Role) JOptionPane.showInputDialog(
            this,
            "Select new role for user '" + selectedUser.getName() + "':",
            "Change User Role",
            JOptionPane.QUESTION_MESSAGE,
            null,
            roles,
            selectedUser.getRole()
        );
        
        if (selectedRole != null && selectedRole != selectedUser.getRole()) {
            boolean confirmed = SwingUIConstants.showConfirmDialog(this,
                "Change role of '" + selectedUser.getName() + "' from " + 
                selectedUser.getRole() + " to " + selectedRole + "?",
                "Confirm Role Change");
            
            if (confirmed) {
                UserDAO.UpdateResult result = userDAO.updateUserRole(selectedUser.getUserId(), selectedRole);
                
                if (result.isSuccess()) {
                    SwingUIConstants.showSuccessDialog(this, result.getMessage(), "Role Updated");
                    loadUsers(); // Refresh the table
                } else {
                    SwingUIConstants.showErrorDialog(this, result.getMessage(), "Update Failed");
                }
            }
        }
    }
    
    private void loadUsers() {
        // Show loading state
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");
        
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userDAO.getAllUsers();
            }
            
            @Override
            protected void done() {
                try {
                    users = get();
                    tableModel.setUsers(users);
                    
                    // Reset selection
                    selectedUser = null;
                    editStatusButton.setEnabled(false);
                    editRoleButton.setEnabled(false);
                    
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(UserManagementDialog.this,
                        "Failed to load users: " + e.getMessage(),
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
     * Table model for user data
     */
    private static class UserTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Name", "Email", "Role", "Phone", "Status", "Created"
        };
        
        private List<User> users = new java.util.ArrayList<>();
        
        public void setUsers(List<User> users) {
            this.users = users;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return users.size();
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
            if (rowIndex >= users.size()) return null;
            
            User user = users.get(rowIndex);
            switch (columnIndex) {
                case 0: return user.getUserId();
                case 1: return user.getName();
                case 2: return user.getEmail();
                case 3: return user.getRole();
                case 4: return user.getPhone() != null ? user.getPhone() : "N/A";
                case 5: return user.isActive() ? "Active" : "Inactive";
                case 6: return user.getCreatedAt() != null ? 
                    user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
                default: return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Integer.class;
                case 3: return User.Role.class;
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